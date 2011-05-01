/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.AI;

import CSE4705_final.State.Partition;
import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.PartitionSet;
import CSE4705_final.AI.Eval.Evaluator;
import CSE4705_final.State.NodeState;
import java.util.List;
/**
 *
 * @author tom
 */
public class Heuristic implements Evaluator {
  public final int _friendlyQueenMoveCoeff, _friendlyKingMoveCoeff, _enemyQueenMoveCoeff, _enemyKingMoveCoeff;

  public Heuristic(int friendlyQueenMoveCoeff, int friendlyKingMoveCoeff, int enemyQueenMoveCoeff, int enemyKingMoveCoeff) {
    _friendlyQueenMoveCoeff = friendlyQueenMoveCoeff;
    _friendlyKingMoveCoeff = friendlyKingMoveCoeff;
    _enemyQueenMoveCoeff = enemyQueenMoveCoeff;
    _enemyKingMoveCoeff = enemyKingMoveCoeff;
  }
  public Heuristic() {
    _friendlyQueenMoveCoeff = 1;
    _friendlyKingMoveCoeff = 1;
    _enemyQueenMoveCoeff = 1;
    _enemyKingMoveCoeff = 1;
  }


  public int score(PartitionSet ps) {
    int whiteOwnedPartitionsScore = rateAllOwnedPartitions(ps.getWhiteOwnedParts());
    int blackOwnedPartitionsScore = rateAllOwnedPartitions(ps.getBlackOwnedParts());
    int contestedPartitionsScore = rateAllContestedPartitions(ps.getContestedParts());
    return (whiteOwnedPartitionsScore + blackOwnedPartitionsScore + contestedPartitionsScore);
  }
  public int rateAllOwnedPartitions(List<Partition> pl) {
    int totalReachableSpaces = 0;
    for (Partition p : pl)
      totalReachableSpaces += rateOwnedPartition(p);
    return totalReachableSpaces;
  }
  public int rateAllContestedPartitions(List<Partition> pl) {
    int contestedPartitionScores = 0;
    for (Partition p : pl)
      contestedPartitionScores += rateContestedPartition(p);
    return contestedPartitionScores;
  }
  public int rateOwnedPartition(Partition p) {
    int reachableSpaces = p.enclosedCount();
    reachableSpaces -= p.getBlackQueens().size();
    reachableSpaces -= p.getWhiteQueens().size();
    return reachableSpaces;
  }
  public int rateContestedPartition(Partition p) {
    int enemyScore = rateQueens(p, false);
    int friendlyScore = rateQueens(p, true);
    return (friendlyScore - enemyScore);
  }





  public int rateQueensSet(List<Partition> pl, boolean white) {
    int queenScore = 0;
    for (Partition p : pl)
      queenScore += rateQueens(p, white);
    return queenScore;
  }
  public int rateQueens(Partition p, boolean white) {
    List<Integer> queens = null;
    if (white)
      queens = p.getWhiteQueens();
    else
      queens = p.getBlackQueens();
    int queenScore = 0;
    for (Integer q : queens)
      queenScore += rateNode(p, q, white);
    return queenScore;
  }
  public int rateMove(Partition pre, ClientMove m) {
    List<Partition> postMovePartitions = pre.forkMove(m, true);
    Partition post = pre;
    for (Partition p : postMovePartitions)
      if (p.containsNode(m.getToIndex()))
        post = p;
    return (rateTo(post, m.getToIndex()) - rateFrom(pre, m.getFromIndex()) + rateShot(pre, postMovePartitions));
  }
  public int rateFrom(Partition p, int i) {
    return rateNode(p, i, true);
  }
  public int rateTo(Partition p, int i) {
    return rateNode(p, i, true);
  }
  public int rateShot(Partition pre, List<Partition> postPartitions) {
    int preShot = ratePreShot(pre);
    int postShot = ratePostShot(postPartitions);
    return (preShot - postShot);
  }
  public int ratePreShot(Partition p) {
    int enemyQueenValues = rateQueens(p, false);
    int friendlyQueenValues = rateQueens(p, true);
    return (enemyQueenValues - friendlyQueenValues);
  }
  public int ratePostShot(List<Partition> partitions) {
    int enemyQueenValues = rateQueensSet(partitions, false);
    int friendlyQueenValues = rateQueensSet(partitions, true);
    return (enemyQueenValues - friendlyQueenValues);
  }
  public int rateNode(Partition p, int i, boolean white) {
    int kingMoves = p.getNeighboringIndicies(i).size();
    int queenMoves = p.getReachableIndicies(i).size();
    if (white) {
      kingMoves *= _friendlyKingMoveCoeff;
      queenMoves *= _friendlyQueenMoveCoeff;
    }
    else {
      kingMoves *= _enemyKingMoveCoeff;
      queenMoves *= _enemyQueenMoveCoeff;
    }
    return (kingMoves + queenMoves);
  }
}
