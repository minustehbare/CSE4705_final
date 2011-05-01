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
    protected boolean _syncThreads;
    
    private final Object _stateMutex = new Object();
    private final Object _timerMutex = new Object();
    private final Object _notifyMutex = new Object();
    
    private boolean _timeExpired;
    
    public GenericSearchAI(boolean isPlayerBlack, Evaluator scorer,
            int searchWidth, boolean syncThreads) {
        super(isPlayerBlack);
        _scorer = scorer;
        _searchWidth = searchWidth;
        _initialSearchWidth = searchWidth;
        _syncThreads = syncThreads;
    }
    
    public GenericSearchAI(boolean isPlayerBlack, Evaluator scorer,
            int searchWidth, int initialSearchWidth, boolean syncThreads) {
        super(isPlayerBlack);
        _scorer = scorer;
        _searchWidth = searchWidth;
        _initialSearchWidth = initialSearchWidth;
        _syncThreads = syncThreads;
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
            Thread timerThread = new Thread(new Runnable() {
                public void run() {
                    synchronized (_timerMutex) {
                        _timeExpired = false;
                        try {
                            _timerMutex.wait(timeGiven);
                        } catch (InterruptedException e) {

                        }
                        synchronized (_notifyMutex) {
                            _timeExpired = true;
                            _notifyMutex.notifyAll();
                        }
                    }
                }
            });
            timerThread.setPriority(Thread.MAX_PRIORITY);
            timerThread.start();
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
        SortedSet<AggregateMove> scoredMoves =
                Collections.synchronizedSortedSet(new TreeSet<AggregateMove>()); 
        // Launch a new thread for each partition/queen.
        // There is a maximum of 4 moving queens.
        List<Thread> evalThreadList = new LinkedList<Thread>();
        {
            for (Partition part : contestedParts) {
                for (Integer queenIndex : part.getWhiteQueens()) {
                    Thread evalThread = new EvalQueenThread(part, queenIndex,
                            scoredMoves, _currentSet, false);
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
                throw new RuntimeException(
                        "Thread interrupt while evaluation initial moves!");
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
                initialStore.addMove(allMoves.get(allMoves.size() - i));
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
        synchronized (_notifyMutex) {
            if (!_timeExpired) {
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
        AggregateMove bestMove = initialStore.getBestSoFar();
        System.out.println("Winning move: " +
                bestMove.getMove().getSendingPrintout() + " [" +
                bestMove.getValue() + "]");
        return bestMove.getMove();
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
        
        // NOTE: I am trading some proper behavior for speed here.  This will
        // only work if only moves at the same depth in the game tree are
        // compared.  This should be the case.
        @Override
        public boolean equals(Object other) {
            if (other.getClass().equals(AggregateMove.class)) {
                return _move.equals(((AggregateMove) other).getMove());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return _move.hashCode();
        }
        
        @Override
        public int compareTo(AggregateMove other) {
            if (_value == other.getValue()) {
                // Sort based on moves, to prevent duplicate munching.
                return hashCode() - other.hashCode();
            } else {
                return _value - other.getValue();
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
                PartitionSet newPartSet = _partSet.forkPartitionSet(
                        possibleMove, _isMovingPlayerBlack);
                _moveSet.add(new AggregateMove(possibleMove, _part, newPartSet,
                        _scorer.score(newPartSet)));
            }
        }
    }
    
    protected class SearchThread extends Thread {
        private final InitialMoveStore _globalStore;
        private final List<AggregateMove> _threadMoves;
        private final List<MoveStore> _threadStore;
        
        int levelsTraversed = 0;
        
        public SearchThread(List<AggregateMove> threadMoves,
                InitialMoveStore globalStore) {
            _globalStore = globalStore;
            _threadMoves = threadMoves;
            _threadStore = new LinkedList<MoveStore>();
        }
        
        @Override
        public void run() {
            boolean isBlackMoving = true;
            // For each part set, create a new move store for it.
            for (AggregateMove m : _threadMoves) {
                // The first level of moves stores are MINIMIZING.
                MoveStore newStore = new MoveStore(m, isBlackMoving);
                _threadStore.add(newStore);
                _globalStore.addChild(newStore);
            }
            // For each part set, launch a new call to evaluate it.
            List<MoveStore> currentStores = _threadStore;
            List<MoveStore> nextStores = new LinkedList<MoveStore>();
            while (!Thread.currentThread().isInterrupted()) {
                isBlackMoving = !isBlackMoving;
                for (MoveStore currentStore : currentStores) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    } else {
                        SortedSet<AggregateMove> nextMoves =
                                new TreeSet<AggregateMove>();
                        for (Partition part : currentStore.getRootMove().getPartSet().getContestedParts()) {
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            } else {
                                for (int queenIndex : (isBlackMoving ? part.getBlackQueens() : part.getWhiteQueens())) {
                                    if (Thread.currentThread().isInterrupted()) {
                                        break;
                                    } else {
                                        for (ClientMove move : part.getPossibleMoves(queenIndex)) {
                                            if (Thread.currentThread().isInterrupted()) {
                                                System.out.println("Pending moves (current level): " + nextMoves.size());
                                                break;
                                            } else {
                                                PartitionSet newPartSet =
                                                        currentStore.getRootMove().getPartSet().forkPartitionSet(part, move, isBlackMoving);
                                                nextMoves.add(new AggregateMove(move, part, newPartSet, _scorer.score(newPartSet)));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // only take the best _searchWidth of them.
                        List<AggregateMove> moveList =
                                new ArrayList<AggregateMove>(nextMoves);
                        if (isBlackMoving) {
                            // we're trying to minimize - take the FIRST moves.
                            for (int i = 0; (i < _searchWidth) && (i < moveList.size()); i++) {
                                MoveStore nextStore =
                                        new MoveStore(moveList.get(i), isBlackMoving);
                                nextStores.add(nextStore);
                                currentStore.addChild(nextStore);
                                currentStore.addMove(moveList.get(i));
                            }
                        } else {
                            // we're trying to minimize - take the LAST moves.
                            for (int i = 1; (i <= _searchWidth) && (i <= moveList.size()); i++) {
                                MoveStore nextStore =
                                        new MoveStore(moveList.get(moveList.size() - i), isBlackMoving);
                                nextStores.add(nextStore);
                                currentStore.addChild(nextStore);
                                currentStore.addMove(moveList.get(moveList.size() - i));
                            }
                        }
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Confirmed moves (current level): "
                            + nextStores.size());
                    break;
                } else {
                    currentStores = nextStores;
                    nextStores = new LinkedList<MoveStore>();
                    levelsTraversed++;
                    _globalStore.waitForOtherThreads();
                }
            }
            System.out.println("Levels traversed: " + levelsTraversed);
        }
    }

    protected class InitialMoveStore {
        private SortedSet<AggregateMove> _currentValues;
        private int _currentDepth;
        
        private final List<MoveStore> _children;
        
        private final CyclicBarrier _barrier;

        public InitialMoveStore() {
            _currentValues =
                    Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
            _currentDepth = 1;
            _children = new LinkedList<MoveStore>();
            _barrier = new CyclicBarrier(4);
        }
        
        public void addChild(MoveStore store) {
            _children.add(store);
        }
        
        public void waitForOtherThreads() {
            try {
                _barrier.await();
            } catch (InterruptedException e) {
                // reset the interrupted flag.
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                
            }
        }
        
        public void addMove(AggregateMove move) {
            _currentValues.add(move);
        }

        public AggregateMove getBestSoFar() {
            Set<AggregateMove> newValues = new HashSet<AggregateMove>();
            for (MoveStore store : _children) {
                newValues.add(new AggregateMove(store.getRootMove().getMove(),
                        store.getRootMove().getPart(),
                        store.getRootMove().getPartSet(),
                        store.getBestSoFar().getValue()));
            }
            if (newValues.size() > 0) {
                int completedTotal = 0;
                AggregateMove bestMove = null;
                int bestScore = Integer.MIN_VALUE;
                Map<AggregateMove, Integer> newScoreMap =
                        new HashMap<AggregateMove, Integer>();
                for (AggregateMove m : newValues) {
                    newScoreMap.put(m, m.getValue());
                }
                for (AggregateMove m : _currentValues) {
                    if (newValues.contains(m)) {
                        completedTotal += newScoreMap.get(m) - m.getValue();
                    }
                }
                int meanDelta = completedTotal / newValues.size();
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
        private int _currentDepth;
        
        private final List<MoveStore> _children;
        
        private final AggregateMove _rootMove;
        private final boolean _isMinimizing;

        public MoveStore(AggregateMove rootMove, boolean isMinimizing) {
            _currentValues =
                    Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
            _currentDepth = 1;
            _children = new LinkedList<MoveStore>();
            _rootMove = rootMove;
            _isMinimizing = isMinimizing;
        }
        
        public AggregateMove getRootMove() {
            return _rootMove;
        }
        
        public void addChild(MoveStore child) {
            _children.add(child);
        }
        
        public List<MoveStore> getChildren() {
            return Collections.unmodifiableList(_children);
        }
        
        public void addMove(AggregateMove move) {
            _currentValues.add(move);
        }

        public AggregateMove getBestSoFar() {
            if (_currentValues.size() == 0) {
                return _rootMove;
            } else {
                Set<AggregateMove> newValues = new HashSet<AggregateMove>();
                for (MoveStore store : _children) {
                    newValues.add(store.getBestSoFar());
                }

                if (newValues.size() > 0) {
                    int completedTotal = 0;
                    AggregateMove bestMove = null;
                    int bestScore;
                    if (_isMinimizing) {
                        bestScore = Integer.MAX_VALUE;
                    } else {
                        bestScore = Integer.MIN_VALUE;
                    }
                    Map<AggregateMove, Integer> newScoreMap =
                            new HashMap<AggregateMove, Integer>();
                    for (AggregateMove m : newValues) {
                        newScoreMap.put(m, m.getValue());
                    }
                    for (AggregateMove m : _currentValues) {
                        if (newValues.contains(m)) {
                            completedTotal += newScoreMap.get(m) - m.getValue();
                        }
                    }
                    int meanDelta = completedTotal / newValues.size();
                    for (AggregateMove m : _currentValues) {
                        int tempScore;
                        if (newScoreMap.containsKey(m)) {
                            tempScore = newScoreMap.get(m);
                        } else {
                            tempScore = m.getValue() + meanDelta;
                        }
                        // That ^ is the XOR operator.
                        // _isMinimizing    (tempScore > bestScore)     result
                        //         false                      false      false
                        //         false                       true       true
                        //          true                      false       true
                        //          true                       true      false
                        if (_isMinimizing ^ (tempScore > bestScore)) {
                            bestScore = tempScore;
                            bestMove = m;
                        }
                    }
                    return bestMove;
                } else {
                    if (_isMinimizing) {
                        return _currentValues.first();
                    } else {
                        return _currentValues.last();
                    }
                }
            }
        }
    }
}
