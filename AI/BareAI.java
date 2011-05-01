package CSE4705_final.AI;

import CSE4705_final.Client.*;

/**
 *
 * @author Ethan
 */
public abstract class BareAI {
    
    protected int _moveCount = -1;
    
    protected abstract void registerOpponentMoveAbstract(ClientMove move);
    
    protected abstract ClientMove getPlayerMoveAbstract(int timeRemaining);
    
    public ClientInterface getInterface() {
        return new ClientInterface() {
            @Override
            public void opponentMove(ClientMove move) {
                _moveCount++;
                registerOpponentMoveAbstract(move);
            }
            
            @Override
            public ClientMove getMove(int timer) {
                _moveCount++;
                return getPlayerMoveAbstract(timer);
            }
        };
    }
}
