package CSE4705_final.AI.Eval;

import CSE4705_final.State.*;

/**
 *
 * @author Ethan
 */
public class BasicEvaluator implements Evaluator {
    
    private final int _whiteSpaceCoeff;
    private final int _whiteReachableCoeff;
    private final int _whiteNeighborCoeff;
    private final int _blackSpaceCoeff;
    private final int _blackReachableCoeff;
    private final int _blackNeighborCoeff;
    
    public BasicEvaluator() {
        _whiteSpaceCoeff = 1000;
        _whiteReachableCoeff = 1000;
        _whiteNeighborCoeff = 1000;
        _blackSpaceCoeff = -1000;
        _blackReachableCoeff = -1000;
        _blackNeighborCoeff = -1000;
    }
    
    public BasicEvaluator(int whiteSpaceCoeff, int whiteReachableCoeff, int whiteNeighborCoeff,
            int blackSpaceCoeff, int blackReachableCoeff, int blackNeighborCoeff) {
        _whiteSpaceCoeff = whiteSpaceCoeff;
        _whiteReachableCoeff = whiteReachableCoeff;
        _whiteNeighborCoeff = whiteNeighborCoeff;
        _blackSpaceCoeff = blackSpaceCoeff;
        _blackReachableCoeff = blackReachableCoeff;
        _blackNeighborCoeff = blackNeighborCoeff;
    }
    
    public int score(PartitionSet state) {
        int score = 0;
        // score owned spaces.
        for (Partition part : state.getWhiteOwnedParts()) {
            score += _whiteSpaceCoeff * part.getFreeStates();
        }
        for (Partition part : state.getBlackOwnedParts()) {
            score += _blackSpaceCoeff * part.getFreeStates();
        }
        for (Partition part : state.getContestedParts()) {
            for (int queenIndex : part.getBlackQueens()) {
                score += _blackNeighborCoeff * part.getNeighboringIndicies(queenIndex).size();
                score += _blackReachableCoeff * part.getReachableIndicies(queenIndex).size();
            }
            for (int queenIndex : part.getWhiteQueens()) {
                score += _whiteNeighborCoeff * part.getNeighboringIndicies(queenIndex).size();
                score += _whiteReachableCoeff * part.getReachableIndicies(queenIndex).size();
            }
        }
        return score;
    }
}
