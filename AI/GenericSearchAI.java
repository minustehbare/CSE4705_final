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
        Map<Integer, AggregateMove> scoredMoves = new ConcurrentSkipListMap<Integer, AggregateMove>();
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
        // We need to launch a thread for each of the 
        
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
        private final Map<Integer, AggregateMove> _moveMap;
        private final PartitionSet _partSet;
        private final boolean _isMovingPlayerBlack;
        public EvalQueenThread(Partition part, int queenIndex,
                Map<Integer, AggregateMove> moveMap, PartitionSet partSet,
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
                _moveMap.put(_scorer.score(newPartSet), new AggregateMove(possibleMove, _part, newPartSet));
            }
        }
    }
}
