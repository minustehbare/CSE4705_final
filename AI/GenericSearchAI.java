package CSE4705_final.AI;

import java.util.*;
import java.util.concurrent.*;

import CSE4705_final.AI.Eval.*;
import CSE4705_final.Client.*;
import CSE4705_final.State.*;

/**
 *
 * @author Ethan
 */
public class GenericSearchAI extends PartitionBasedAI {
    
    protected Evaluator _scorer;
    protected int _initialSearchWidth;
    protected int _searchWidth;
    
    private final Object _stateMutex = new Object();
    private final Object _timerMutex = new Object();
    private final Object _notifyMutex = new Object();
    
    private boolean _timeExpired;
    
    public GenericSearchAI(boolean isPlayerBlack, Evaluator scorer, int searchWidth) {
        super(isPlayerBlack);
        _scorer = scorer;
        _searchWidth = searchWidth;
        _initialSearchWidth = searchWidth;
    }
    
    public GenericSearchAI(boolean isPlayerBlack, Evaluator scorer, int searchWidth, int initialSearchWidth) {
        super(isPlayerBlack);
        _scorer = scorer;
        _searchWidth = searchWidth;
        _initialSearchWidth = initialSearchWidth;
    }
    
    public void switchEvaluator(Evaluator newEval) {
        synchronized (_stateMutex) {
            _scorer = newEval;
        }
    }
    
    public void switchSearchWidth(int newWidth) {
        synchronized (_stateMutex) {
            _searchWidth = newWidth;
        }
    }
    
    public void switchInitialSearchWidth(int newWidth) {
        synchronized (_stateMutex) {
            _initialSearchWidth = newWidth;
        }
    }
    
    @Override
    protected ClientMove getPlayerMove(int timeRemaining) {
        synchronized (_stateMutex) {
            // TODO: Add code to prime the evaluator.
            // Set the timer.
            final int timeGiven = timeRemaining * 1000;
            new Thread(new Runnable() {
                public void run() {
                    synchronized (_timerMutex) {
                        _timeExpired = false;
                        try {
                            _timerMutex.wait(timeGiven);
                        } catch (InterruptedException e) {

                        }
                        _timeExpired = true;
                        synchronized (_notifyMutex) {
                            _notifyMutex.notifyAll();
                        }
                    }
                }
            }).start();
            // Start with the contested blocks.
            if (_currentSet.areAnyContestedParts()) {
                // There are contested parts - get a move.
                return getPlayerMoveCore(timeRemaining);
            } else {
                // There are no contested parts - we are filling.
                return getPlayerMoveFilling(timeRemaining);
            }
        }
    }
    
    protected ClientMove getPlayerMoveCore(int timeRemaining) {
        List<Partition> contestedParts = _currentSet.getContestedParts();
        // Get a list of all our moves.
        Map<AggregateMove, Integer> scoredMoves = new ConcurrentHashMap<AggregateMove, Integer>();
        // Launch a new thread for each partition/queen.
        // There is a maximum of 4 moving queens.
        List<Thread> evalThreadList = new LinkedList<Thread>();
        {
            for (Partition part : contestedParts) {
                for (Integer queenIndex : part.getWhiteQueens()) {
                    Thread evalThread = new EvalQueenThread(part, queenIndex, scoredMoves,
                            _currentSet, false);
                    evalThreadList.add(evalThread);
                    evalThread.start();
                }
            }
            // Join with all threads.
            try {
                for (Thread t : evalThreadList) {
                    t.join();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupt while evaluation initial moves!");
            }
        }
        
        // We need to launch a thread for each of the top initialSearchWidth moves.
        // Create the global store.
        final InitialMoveStore initialStore = new InitialMoveStore();
        // Create a sorted list of all moves.
        List<AggregateMoveValue> allMoves = new ArrayList<AggregateMoveValue>(scoredMoves.size());
        for (AggregateMove m : scoredMoves.keySet()) {
            allMoves.add(new AggregateMoveValue(m, scoredMoves.get(m)));
        }
        Collections.sort(allMoves);
        
        // Sort the moves into 4 lists.
        List<AggregateMove> threadMoves1 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves2 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves3 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves4 = new LinkedList<AggregateMove>();
        {
            int assignment = 0;
            for (int i = 1; i <= _initialSearchWidth; i++) {
                AggregateMove placementMove = allMoves.get(allMoves.size() - i).getMove();
                initialStore.addInitialMove(allMoves.get(allMoves.size() - i));
                switch (assignment) {
                    case 0:
                        threadMoves1.add(placementMove);
                    case 1:
                        threadMoves2.add(placementMove);
                    case 2:
                        threadMoves3.add(placementMove);
                    case 3:
                        threadMoves4.add(placementMove);
                }
                assignment = (assignment + 1) % 4;
            }
            initialStore.doneInitializing();
        }
        
        // Create threads.
        Thread searchThread1 = new SearchThread(threadMoves1, initialStore);
        Thread searchThread2 = new SearchThread(threadMoves2, initialStore);
        Thread searchThread3 = new SearchThread(threadMoves3, initialStore);
        Thread searchThread4 = new SearchThread(threadMoves4, initialStore);
        
        // Start threads.
        searchThread1.start();
        searchThread2.start();
        searchThread3.start();
        searchThread4.start();
        
        // Wait for the time to run out.
        if (!_timeExpired) {
            synchronized (_notifyMutex) {
                try {
                    _notifyMutex.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        
        // Stop the threads.
        searchThread1.interrupt();
        searchThread2.interrupt();
        searchThread3.interrupt();
        searchThread4.interrupt();
        
        // Let them clean up.
        try {
            searchThread1.join();
            searchThread2.join();
            searchThread3.join();
            searchThread4.join();
        } catch (InterruptedException e) {
            // should not occur
        }
        
        // Return the best move we have.
        return initialStore.getBestSoFar().getMove();
    }
    
    protected ClientMove getPlayerMoveFilling(int timeRemaining) {
        throw new RuntimeException("Filling is not yet implemented.");
    }
    
    protected class AggregateMove {
        private final ClientMove _move;
        private final Partition _part;
        private final PartitionSet _partSet;
        public AggregateMove(ClientMove move, Partition part, PartitionSet partSet) {
            _move = move;
            _part = part;
            _partSet = partSet;
        }
        public ClientMove getMove() { return _move; }
        public Partition getPart() { return _part; }
        public PartitionSet getPartSet() { return _partSet; }
    }
    
    protected class AggregateMoveValue implements Comparable<AggregateMoveValue> {
        private final AggregateMove _move;
        private final int _value;
        public AggregateMoveValue(AggregateMove move, int value) {
            _move = move;
            _value = value;
        }
        
        public AggregateMove getMove() { return _move; }
        public int getValue() { return _value; }
        
        @Override
        public boolean equals(Object other) {
            if (other.getClass().equals(AggregateMoveValue.class)) {
                return _move.equals(((AggregateMoveValue) other).getMove());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this._move != null ? this._move.hashCode() : 0);
            return hash;
        }
        
        @Override
        public int compareTo(AggregateMoveValue other) {
            if (_value == other.getValue()) {
                return 0;
            } else if (_value < other.getValue()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
    
    protected class EvalQueenThread extends Thread {
        private final Partition _part;
        private final int _queenIndex;
        private final Map<AggregateMove, Integer> _moveMap;
        private final PartitionSet _partSet;
        private final boolean _isMovingPlayerBlack;
        public EvalQueenThread(Partition part, int queenIndex,
                Map<AggregateMove, Integer> moveMap, PartitionSet partSet,
                boolean isMovingPlayerBlack) {
            _part = part;
            _queenIndex = queenIndex;
            _moveMap = moveMap;
            _partSet = partSet;
            _isMovingPlayerBlack = isMovingPlayerBlack;
        }
        
        @Override
        public void run() {
            for (ClientMove possibleMove : _part.getPossibleMoves(_queenIndex)) {
                PartitionSet newPartSet = _partSet.forkPartitionSet(possibleMove, _isMovingPlayerBlack);
                _moveMap.put(new AggregateMove(possibleMove, _part, newPartSet), _scorer.score(newPartSet));
            }
        }
    }
    
    protected class SearchThread extends Thread {
        private final InitialMoveStore _globalStore;
        private final List<AggregateMove> _globalMoves;
        public SearchThread(List<AggregateMove> globalMoves, InitialMoveStore globalStore) {
            _globalStore = globalStore;
            _globalMoves = globalMoves;
        }
        
        @Override
        public void run() {
            // TODO - implement search threads.
            _globalStore.registerThread();
            // For now, here's some "filler"
            while (Thread.currentThread().isInterrupted()) {
                
            }
            _globalStore.unregisterThread();
        }
    }

    protected class InitialMoveStore {
        private final Map<AggregateMove, Integer> _currentValues;
        private final Map<AggregateMove, Integer> _newValues;
        private int _currentDepth;

        private int _totalThreads;
        private int _finishedThreads;

        private final Object _threadCountMutex = new Object();
        
        private boolean _isInitialized;

        public InitialMoveStore() {
            _currentValues = new ConcurrentHashMap<AggregateMove, Integer>();
            _newValues = new ConcurrentHashMap<AggregateMove, Integer>();
            _currentDepth = 1;
            _totalThreads = 0;
            _finishedThreads = 0;
            _isInitialized = false;
        }

        public void registerThread() {
            synchronized (_threadCountMutex) {
                _totalThreads ++;
            }
        }

        public void unregisterThread() {
            synchronized (_threadCountMutex) {
                _totalThreads --;
            }
        }

        public void finish() {
            synchronized (_threadCountMutex) {
                _finishedThreads ++;
                if (_finishedThreads == _totalThreads) {
                    _currentValues.clear();
                    for (AggregateMove m : _newValues.keySet()) {
                        _currentValues.put(m, _newValues.get(m));
                    }
                    _newValues.clear();
                    _currentDepth ++;
                    _threadCountMutex.notifyAll();
                } else {
                    try {
                        _threadCountMutex.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
        
        public void addInitialMove(AggregateMove move, int value) {
            if (!_isInitialized) {
                _currentValues.put(move, value);
            }
        }
        
        public void addInitialMove(AggregateMoveValue moveValue) {
            addInitialMove(moveValue.getMove(), moveValue.getValue());
        }
        
        public void doneInitializing() {
            _isInitialized = true;
        }
        
        public void addMove(AggregateMove move, int value) {
            _newValues.put(move, value);
        }
        
        public void addMove(AggregateMoveValue moveValue) {
            addMove(moveValue.getMove(), moveValue.getValue());
        }

        public AggregateMove getBestSoFar() {
            if (_newValues.size() > 0) {
                int completedTotal = 0;
                AggregateMove bestMove = null;
                int bestScore = Integer.MIN_VALUE;
                for (AggregateMove m : _newValues.keySet()) {
                    completedTotal += _newValues.get(m) - _currentValues.get(m);
                }
                int meanDelta = completedTotal / _newValues.size();
                for (AggregateMove m : _currentValues.keySet()) {
                    int tempScore;
                    if (_newValues.containsKey(m)) {
                        tempScore = _newValues.get(m);
                    } else {
                        tempScore = _currentValues.get(m) + meanDelta;
                    }
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                        bestMove = m;
                    }
                }
                return bestMove;
            } else {
                AggregateMove bestMove = null;
                int bestScore = Integer.MIN_VALUE;
                for (AggregateMove m : _currentValues.keySet()) {
                    int tempScore = _currentValues.get(m);
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                        bestMove = m;
                    }
                }
                return bestMove;
            }
        }
    }
    
    protected class MoveStore {
        
    }
}
