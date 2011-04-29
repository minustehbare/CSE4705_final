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
        NodeSet ns = new NodeSet(isPlayerBlack);
        _currentSet = new PartitionSet(ns.getRootPartition());
    }
    
    @Override
    protected final void registerOpponentMoveAbstract(ClientMove move) {
        _currentSet = _currentSet.forkPartitionSet(move, true);
        notifyOpponentMove(move);
    }
    
    @Override
    protected final ClientMove getPlayerMoveAbstract(int timeRemaining) {
        ClientMove move = getPlayerMove(timeRemaining);
        _currentSet = _currentSet.forkPartitionSet(move, false);
        return move;
    }
    
    protected void notifyOpponentMove(ClientMove move) {
        // By default, do nothing.
    }
    
    abstract protected ClientMove getPlayerMove(int timeRemaining);
}
