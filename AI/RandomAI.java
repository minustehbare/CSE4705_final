package CSE4705_final.AI;

import CSE4705_final.Client.*;
import CSE4705_final.State.*;

import java.util.*;

/**
 *
 * @author Ethan
 */
public class RandomAI extends PartitionBasedAI {
    
    public RandomAI(boolean isPlayerBlack) {
        super(isPlayerBlack);
    }
    
    @Override
    protected ClientMove getPlayerMove(int timeRemaining) {
        // move in contested parts first.
        List<ClientMove> availableMoves = new LinkedList<ClientMove>();
        for (Partition part : _currentSet.getContestedParts()) {
            for (int queenIndex : part.getWhiteQueens()) {
                availableMoves.addAll(part.getPossibleMoves(queenIndex));
            }
        }
        if (availableMoves.isEmpty()) {
            for (Partition part : _currentSet.getWhiteOwnedParts()) {
                for (int queenIndex : part.getWhiteQueens()) {
                    availableMoves.addAll(part.getPossibleMoves(queenIndex));
                }
            }
        }
        if (availableMoves.isEmpty()) {
            // No more moves!
            // Play a bogus move?
            for (int index : _currentSet.getDeadParts().get(0).getEnclosedSet() )
                return new ClientMove(index,index,index,0);
        }
        Random rand = new Random();
        return availableMoves.get(rand.nextInt(availableMoves.size()));
    }
}
