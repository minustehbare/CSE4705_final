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
        SortedSet<AggregateMove> scoredMoves = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>()); 
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
        List<AggregateMove> allMoves = new ArrayList<AggregateMove>(scoredMoves);
        
        // Sort the moves into 4 lists.
        List<AggregateMove> threadMoves1 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves2 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves3 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves4 = new LinkedList<AggregateMove>();
        {
            int assignment = 0;
            for (int i = 1; i <= _initialSearchWidth; i++) {
                AggregateMove placementMove = allMoves.get(allMoves.size() - i);
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
    
    protected class AggregateMove implements Comparable<AggregateMove> {
        private final ClientMove _move;
        private final Partition _part;
        private final PartitionSet _partSet;
        private final int _value;
        public AggregateMove(ClientMove move, Partition part,
                PartitionSet partSet, int value) {
            _move = move;
            _part = part;
            _partSet = partSet;
            _value = value;
        }
        public ClientMove getMove() { return _move; }
        public Partition getPart() { return _part; }
        public PartitionSet getPartSet() { return _partSet; }
        public int getValue() { return _value; }
        
//        @Override
//        public boolean equals(Object other) {
//            if (other.getClass().equals(AggregateMove.class)) {
//                return _move.equals(((AggregateMove) other).getMove());
//            } else {
//                return false;
//            }
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 5;
//            hash = 47 * hash + (this._move != null ? this._move.hashCode() : 0);
//            return hash;
//        }
        
        @Override
        public int compareTo(AggregateMove other) {
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
        private final SortedSet<AggregateMove> _moveSet;
        private final PartitionSet _partSet;
        private final boolean _isMovingPlayerBlack;
        public EvalQueenThread(Partition part, int queenIndex,
                SortedSet<AggregateMove> moveSet, PartitionSet partSet,
                boolean isMovingPlayerBlack) {
            _part = part;
            _queenIndex = queenIndex;
            _moveSet = moveSet;
            _partSet = partSet;
            _isMovingPlayerBlack = isMovingPlayerBlack;
        }
        
        @Override
        public void run() {
            for (ClientMove possibleMove : _part.getPossibleMoves(_queenIndex)) {
                PartitionSet newPartSet = _partSet.forkPartitionSet(possibleMove, _isMovingPlayerBlack);
                _moveSet.add(new AggregateMove(possibleMove, _part, newPartSet, _scorer.score(newPartSet)));
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
            // For each part set, launch a new call to evaluate it.
            
            // For now, here's some "filler"
            while (Thread.currentThread().isInterrupted()) {
                
            }
            _globalStore.unregisterThread();
        }
    }

    protected class InitialMoveStore {
        private SortedSet<AggregateMove> _currentValues;
        private SortedSet<AggregateMove> _newValues;
        private int _currentDepth;

        private int _totalThreads;
        private int _finishedThreads;

        private final Object _threadCountMutex = new Object();
        
        private boolean _isInitialized;

        public InitialMoveStore() {
            _currentValues = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
            _newValues = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
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
                    _currentValues = _newValues;
                    _newValues = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
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
        
        public void addInitialMove(AggregateMove move) {
            if (!_isInitialized) {
                _currentValues.add(move);
            }
        }
        
        public void doneInitializing() {
            _isInitialized = true;
        }
        
        public void addMove(AggregateMove move) {
            _newValues.add(move);
        }

        public AggregateMove getBestSoFar() {
            if (_newValues.size() > 0) {
                int completedTotal = 0;
                AggregateMove bestMove = null;
                int bestScore = Integer.MIN_VALUE;
                Map<AggregateMove, Integer> newScoreMap = new HashMap<AggregateMove, Integer>();
                for (AggregateMove m : _newValues) {
                    newScoreMap.put(m, m.getValue());
                }
                for (AggregateMove m : _currentValues) {
                    completedTotal += newScoreMap.get(m) - m.getValue();
                }
                int meanDelta = completedTotal / _newValues.size();
                for (AggregateMove m : _currentValues) {
                    int tempScore;
                    if (newScoreMap.containsKey(m)) {
                        tempScore = newScoreMap.get(m);
                    } else {
                        tempScore = m.getValue() + meanDelta;
                    }
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                        bestMove = m;
                    }
                }
                return bestMove;
            } else {
                return _currentValues.last();
            }
        }
    }
    
    protected class MoveStore {
        private SortedSet<AggregateMove> _currentValues;
        private SortedSet<AggregateMove> _newValues;
        private int _currentDepth;
        
        private boolean _isInitialized;

        public MoveStore() {
            _currentValues = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
            _newValues = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
            _currentDepth = 1;
            _isInitialized = false;
        }

        public void finish() {
            _currentValues = _newValues;
            _newValues = Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
            _currentDepth ++;
        }
        
        public void addInitialMove(AggregateMove move) {
            if (!_isInitialized) {
                _currentValues.add(move);
            }
        }
        
        public void doneInitializing() {
            _isInitialized = true;
        }
        
        public void addMove(AggregateMove move) {
            _newValues.add(move);
        }

        public AggregateMove getBestSoFar() {
            if (_newValues.size() > 0) {
                int completedTotal = 0;
                AggregateMove bestMove = null;
                int bestScore = Integer.MIN_VALUE;
                Map<AggregateMove, Integer> newScoreMap = new HashMap<AggregateMove, Integer>();
                for (AggregateMove m : _newValues) {
                    newScoreMap.put(m, m.getValue());
                }
                for (AggregateMove m : _currentValues) {
                    completedTotal += newScoreMap.get(m) - m.getValue();
                }
                int meanDelta = completedTotal / _newValues.size();
                for (AggregateMove m : _currentValues) {
                    int tempScore;
                    if (newScoreMap.containsKey(m)) {
                        tempScore = newScoreMap.get(m);
                    } else {
                        tempScore = m.getValue() + meanDelta;
                    }
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                        bestMove = m;
                    }
                }
                return bestMove;
            } else {
                return _currentValues.last();
            }
        }
    }
}
