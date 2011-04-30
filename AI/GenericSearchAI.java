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
        // We need to launch a thread for each of the top initialSearchWidth moves.

        final InitialMoveStore initialStore = new InitialMoveStore(scoredMoves);
        List<AggregateMove> threadMoves1 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves2 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves3 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves4 = new LinkedList<AggregateMove>();
        {
            int assignment = 0;
            for (AggregateMove m : scoredMoves.keySet()) {
                switch (assignment) {
                    case 0:
                        threadMoves1.add(m);
                    case 1:
                        threadMoves2.add(m);
                    case 2:
                        threadMoves3.add(m);
                    case 3:
                        threadMoves4.add(m);
                }
                assignment = (assignment + 1) % 4;
            }
        }
        
        
        // TODO: Finish implementation.
        throw new RuntimeException("search is not yet implemented.");
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

    private class InitialMoveStore {
        private final Map<AggregateMove, Integer> _currentValues;
        private final Map<AggregateMove, Integer> _newValues;
        private int _currentDepth;

        private int _totalThreads;
        private int _finishedThreads;

        private final Object _threadCountMutex = new Object();

        public InitialMoveStore(Map<AggregateMove, Integer> seedMoves) {
            _currentValues = new ConcurrentHashMap<AggregateMove, Integer>();
            _newValues = new ConcurrentHashMap<AggregateMove, Integer>();
            _currentDepth = 1;
            _totalThreads = 0;
            _finishedThreads = 0;
            for (AggregateMove m : seedMoves.keySet()) {
                _currentValues.put(m, seedMoves.get(m));
            }
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
}
