package CSE4705_final.AI;

import CSE4705_final.Client.*;
import CSE4705_final.State.*;

/**
 *
 * @author Ethan
 */
public abstract class PartitionBasedAI extends BareAI {
    
    protected PartitionSet _currentSet;
    
    protected PartitionBasedAI(boolean isPlayerBlack) {
        // note: AI is always WHITE.
        _currentSet = new PartitionSet(new NodeSet(isPlayerBlack).getRootPartition());
    }
    
    @Override
    protected final void registerOpponentMoveAbstract(ClientMove move) {
        notifyOpponentMove(move);
        _currentSet = _currentSet.forkPartitionSet(move, true).isolate();
    }
    
    @Override
    protected final ClientMove getPlayerMoveAbstract(int timeRemaining) {
        // Stop idling.
        stopIdling();
        ClientMove move = getPlayerMove(timeRemaining);
        _currentSet = _currentSet.forkPartitionSet(move, false).isolate();
        System.out.println(_currentSet.getSummaryInfo());
        System.out.println(_currentSet.getPrintout());
        // Start idling.
        startIdling();
        return move;
    }
    
    protected void startIdling() {
        // By default, do nothing.
    }
    
    protected void stopIdling() {
        // By default, do nothing.
    }
    
    protected void notifyOpponentMove(ClientMove move) {
        // By default, do nothing.
    }
    
    abstract protected ClientMove getPlayerMove(int timeRemaining);
}
