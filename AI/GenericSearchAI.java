package CSE4705_final.AI;

import java.util.*;
import java.util.concurrent.*;

import CSE4705_final.AI.Eval.*;
import CSE4705_final.AI.Timers.*;
import CSE4705_final.Client.*;
import CSE4705_final.State.*;

/**
 *
 * @author Ethan
 */
public class GenericSearchAI extends PartitionBasedAI {
    
    protected Evaluator _scorer;
    protected AITimer _timer;
    protected final FillingMoveSet _fillingSet;
    protected int _initialSearchWidth;
    protected int _searchWidth;
    protected boolean _syncThreads;
    
    private final Object _stateMutex = new Object();
    private final Object _timerMutex = new Object();
    private final Object _notifyMutex = new Object();
    
    private boolean _timeExpired;
    
    public GenericSearchAI(boolean isPlayerBlack, Evaluator scorer, AITimer timer,
            int searchWidth, boolean syncThreads) {
        super(isPlayerBlack);
        _scorer = scorer;
        _searchWidth = searchWidth;
        _initialSearchWidth = searchWidth;
        _syncThreads = syncThreads;
        _timer = timer;
        _fillingSet = new FillingMoveSet();
    }
    
    public GenericSearchAI(boolean isPlayerBlack, Evaluator scorer, AITimer timer,
            int searchWidth, int initialSearchWidth, boolean syncThreads) {
        super(isPlayerBlack);
        _scorer = scorer;
        _searchWidth = searchWidth;
        _initialSearchWidth = initialSearchWidth;
        _syncThreads = syncThreads;
        _timer = timer;
        _fillingSet = new FillingMoveSet();
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
    protected void startIdling() {
        _fillingSet.startCalculating();
    }
    
    @Override
    protected void stopIdling() {
        _fillingSet.pauseCalculating();
    }
    
    @Override
    protected ClientMove getPlayerMove(int timeRemaining) {
        synchronized (_stateMutex) {
            // TODO: Add code to prime the evaluator.
            // Set the timer.
            final int timeGiven = _timer.getMillisecondsAvailable(timeRemaining, _moveCount);
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
        // If there are no moves, delegate to the filler.
        if (allMoves.isEmpty()) {
            return getPlayerMoveFilling(timeRemaining);
        }
        
        // Sort the moves into 4 lists.
        List<AggregateMove> threadMoves1 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves2 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves3 = new LinkedList<AggregateMove>();
        List<AggregateMove> threadMoves4 = new LinkedList<AggregateMove>();
        {
            int assignment = 0;
            for (int i = 1; (i <= _initialSearchWidth) && (i < allMoves.size()); i++) {
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
        
        final CyclicBarrier finishGameTreeBarrier = new CyclicBarrier(5);
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    finishGameTreeBarrier.await();
                } catch (InterruptedException e) {
                    
                } catch (BrokenBarrierException e) {
                    
                }
                synchronized(_timerMutex) {
                    _timerMutex.notifyAll();
                }
            }
        }).start();
        
        // Create threads.
        Thread searchThread1 = new SearchThread(threadMoves1, initialStore, finishGameTreeBarrier);
        Thread searchThread2 = new SearchThread(threadMoves2, initialStore, finishGameTreeBarrier);
        Thread searchThread3 = new SearchThread(threadMoves3, initialStore, finishGameTreeBarrier);
        Thread searchThread4 = new SearchThread(threadMoves4, initialStore, finishGameTreeBarrier);
        
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
        finishGameTreeBarrier.reset();
        
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
        
        ClientMove m = bestMove.getMove();
        _fillingSet.registerOurMove(m, _currentSet);
        return m;
    }
    
    protected ClientMove getPlayerMoveFilling(int timeRemaining) {
        return _fillingSet.getMove();
    }
    
    protected void notifyOpponentMove(ClientMove move) {
        _fillingSet.registerOpponentMove(move, _currentSet);
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
        private final CyclicBarrier _barrier;
        
        int levelsTraversed = 0;
        
        public SearchThread(List<AggregateMove> threadMoves,
                InitialMoveStore globalStore, CyclicBarrier barrier) {
            _globalStore = globalStore;
            _threadMoves = threadMoves;
            _threadStore = new LinkedList<MoveStore>();
            _barrier = barrier;
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
            
            int confirmedMoves = 0;
            int pendingMoves = 0;
            while (!Thread.currentThread().isInterrupted()) {
                if (currentStores.isEmpty()) {
                    try {
                        _barrier.await();
                    } catch (InterruptedException e) {
                        
                    } catch (BrokenBarrierException e) {
                        
                    }
                    break;
                }
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
                                                pendingMoves = nextMoves.size();
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
                    confirmedMoves = nextStores.size();
                    break;
                } else {
                    currentStores = nextStores;
                    nextStores = new LinkedList<MoveStore>();
                    levelsTraversed++;
                    _globalStore.waitForOtherThreads();
                }
            }
            System.out.println("Thread Report: " + levelsTraversed + "." + confirmedMoves + "." + pendingMoves);
        }
    }

    protected class InitialMoveStore {
        private SortedSet<AggregateMove> _currentValues;
        
        private final List<MoveStore> _children;
        
        private final CyclicBarrier _barrier;

        public InitialMoveStore() {
            _currentValues =
                    Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
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
                AggregateMove childMove = store.getRootMove();
                newValues.add(new AggregateMove(childMove.getMove(),
                        childMove.getPart(),
                        childMove.getPartSet(),
                        childMove.getValue()));
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
        
        private final List<MoveStore> _children;
        
        private final AggregateMove _rootMove;
        private final boolean _isMinimizing;

        public MoveStore(AggregateMove rootMove, boolean isMinimizing) {
            _currentValues =
                    Collections.synchronizedSortedSet(new TreeSet<AggregateMove>());
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
    
    public class FillingMoveSet {
        private final Object _threadControlMutex = new Object();
        
        private final List<FillingMoveStore> _stores;
        
        private List<ClientMove> _currentList;
        
        public FillingMoveSet() {
            _stores = new LinkedList<FillingMoveStore>();
            _currentList = new LinkedList<ClientMove>();
        }
        
//        public void addPartition(Partition part) {
//            _stores.add(new FillingMoveStore(part));
//        }
        
        public void startCalculating() {
            synchronized (_threadControlMutex) {
                System.out.println("Starting " + _stores.size() + " fillers.");
                for (FillingMoveStore store : _stores) {
                    store.startCalculating();
                }
            }
        }
        
        public void pauseCalculating() {
            synchronized (_threadControlMutex) {
                System.out.println("Pausing " + _stores.size() + " fillers.");
                for (FillingMoveStore store : _stores) {
                    store.pauseCalculating();
                }
            }
        }
        
        public ClientMove getMove() {
            if (_stores.isEmpty()) {
                throw new RuntimeException("No more stores to get moves from!");
            } else if (_currentList.isEmpty()) {
                FillingMoveStore selectedStore = null;
                // try to see if any are complete.
                for (FillingMoveStore store : _stores) {
                    if (store.isComplete()) {
                        selectedStore = store;
                        _currentList = selectedStore.getBestMoves();
                        break;
                    }
                }
                if (selectedStore == null) {
                    selectedStore = _stores.get(0);
                    _currentList = selectedStore.getBestMovesSoFar();
                }
                _stores.remove(selectedStore);
                if (_currentList.isEmpty()) {
                    throw new RuntimeException("This store has an empty list of moves...?");
                } else {
                    return getMove();
                }
            } else {
                ClientMove nextMove = _currentList.remove(0);
                return nextMove;
            }
        }
    
        public void registerOurMove(ClientMove move, PartitionSet currentSet) {
            Partition relevantPart = currentSet.getContainingPartition(move.getFromIndex());
            List<Partition> newParts = relevantPart.forkMove(move, false);
            for (Partition part : newParts) {
                if (part.getPartitionState() == PartitionState.WHITE_OWNED) {
                    _stores.add(new FillingMoveStore(part));
                }
            }
        }
    
        public void registerOpponentMove(ClientMove move, PartitionSet currentSet) {
            Partition relevantPart = currentSet.getContainingPartition(move.getFromIndex());
            List<Partition> newParts = relevantPart.forkMove(move, true);
            for (Partition part : newParts) {
                if (part.getPartitionState() == PartitionState.WHITE_OWNED) {
                    _stores.add(new FillingMoveStore(part));
                }
            }
        }
    }
    
    private class FillingMoveStore {
        private final PartitionSet _baseSet;
        private boolean _isComplete;
        private Thread _fillingMoveRunner;
        private final int _optimalDepth;
        
        private List<ClientMove> _bestMoves;
        private Stack<ClientMove> _currentMoves;
        private Stack<PartitionSet> _currentStates;
        private Stack<Stack<ClientMove>> _potentialMoves;
        
        public FillingMoveStore(Partition basePart) {
            _baseSet = new PartitionSet(basePart);
            _isComplete = false;
            _optimalDepth = basePart.getFreeStates();
            _bestMoves = new LinkedList<ClientMove>();
            _currentMoves = new Stack<ClientMove>();
            _currentStates = new Stack<PartitionSet>();
            _potentialMoves = new Stack<Stack<ClientMove>>();
            _currentStates.push(_baseSet);
            _potentialMoves.push(getAllPossibleMoves(_baseSet));
        }
        
        public List<ClientMove> getBestMoves() {
            if (_isComplete) {
                return _bestMoves;
            } else {
                calculateFillingMoves();
                return _bestMoves;
            }
        }
        
        public List<ClientMove> getBestMovesSoFar() {
            if (_isComplete) {
                return _bestMoves;
            } else {
                // allow 10000 ms to compute.
                _fillingMoveRunner = new Thread(new Runnable() {
                    public void run() {
                        if (!_isComplete) {
                            calculateFillingMoves();
                        }
                    }
                });
                _fillingMoveRunner.start();
                
                final Object tempMutex = new Object();
                synchronized (tempMutex) {
                    try {
                        tempMutex.wait(10000);
                    } catch (InterruptedException e) {
                        
                    }
                }
                _fillingMoveRunner.interrupt();
                try {
                    _fillingMoveRunner.join();
                } catch (InterruptedException e) {
                    
                }
                
                _isComplete = true;
                return _bestMoves;
            }
        }
        
        public boolean isComplete() {
            return _isComplete;
        }
        
        public void startCalculating() {
            _fillingMoveRunner = new Thread(new Runnable() {
                public void run() {
                    if (!_isComplete) {
                        calculateFillingMoves();
                    }
                }
            });
            _fillingMoveRunner.start();
        }
        
        public void pauseCalculating() {
            if (_fillingMoveRunner != null) {
                _fillingMoveRunner.interrupt();
            }
        }
        
        private Stack<ClientMove> getAllPossibleMoves(PartitionSet partSet) {
            // Assume only white owned partitions are available.
            Stack<ClientMove> possibleMoves = new Stack<ClientMove>();
            for (Partition part : partSet.getWhiteOwnedParts()) {
                for (int queenIndex : part.getWhiteQueens()) {
                    possibleMoves.addAll(part.getPossibleMoves(queenIndex));
                }
            }
            return possibleMoves;
        }
        
        private void calculateFillingMoves() {
            while (true) {
                // if interrupted, exit.
                if (Thread.currentThread().isInterrupted()) {
                    break;
                } else if (_currentStates.empty()) {
                    // no more moves to check - we're done.
                    _isComplete = true;
                    break;
                } else if (_potentialMoves.peek().empty()){
                    // if there are no possible moves in this path...
                    // then discard it.
                    _potentialMoves.pop();
                    _currentStates.pop();
                    _currentMoves.pop();
                } else {
                    // get a move.
                    ClientMove potentialMove = _potentialMoves.peek().pop();
                    // fork the partition.
                    PartitionSet newSet = _currentStates.peek().forkPartitionSet(potentialMove, false);
                    // get a list of moves in this new partition.
                    Stack<ClientMove> newMoves = getAllPossibleMoves(newSet);
                    // Push everything.
                    _currentStates.push(newSet);
                    _potentialMoves.push(newMoves);
                    _currentMoves.add(potentialMove);
                    // Check to see if this is the best.
                    if (_currentMoves.size() > _bestMoves.size()) {
                        _bestMoves = new LinkedList(_currentMoves);
                        if (_bestMoves.size() == _optimalDepth) {
                            _isComplete = true;
                            break;
                        }
                    }
                }
            }
        }
    }
}
