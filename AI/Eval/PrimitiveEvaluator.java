package CSE4705_final.AI.Eval;

import CSE4705_final.State.*;

/**
 *
 * @author Ethan
 */
public class PrimitiveEvaluator implements Evaluator {
    
    @Override
    public int score(PartitionSet set) {
        int s = 0;
        for (Partition p : set.getContestedParts()) {
            for (int qi : p.getBlackQueens()) {
                s -= p.getReachableIndicies(qi).size();
            }
            for (int qi : p.getWhiteQueens()) {
                s += p.getReachableIndicies(qi).size();
            }
        }
        for (Partition p : set.getWhiteOwnedParts()) {
            s += p.getFreeStates();
        }
        for (Partition p : set.getBlackOwnedParts()) {
            s -= p.getFreeStates();
        }
        return s;
    }
    
}
