/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AI;

import CSE4705_final.State.Partition;
import CSE4705_final.Client.ClientMove;
import java.util.List;
/**
 *
 * @author tom
 */
public class Hueristic {
  public int _queenMoveCoeff = 1, _kingMoveCoeff = 1;

  public Hueristic(int queenMoveCoeff, int kingMoveCoeff) {
    _queenMoveCoeff = queenMoveCoeff;
    _kingMoveCoeff = kingMoveCoeff;
  }
  public int rateMove(Partition pre, ClientMove m) {
    List<Partition> postMovePartitions = pre.forkMove(m, true);
    Partition post = pre;
    for (Partition p : postMovePartitions)
      if (p.containsNode(m.getTo()))
        post = p;

    return (rateTo(post, m) - rateFrom(pre, m) + rateShot(pre, postMovePartitions, m));
  }
  public int rateFrom(Partition p, ClientMove m) {
    return rateNode(p, m.getFrom());
  }
  public int rateTo(Partition p, ClientMove m) {
    return rateNode(p, m.getTo());
  }
  public int rateShot(Partition pre, List<Partition> postPartitions, ClientMove m) {
    int preShot = ratePreShot(pre, m);
    int postShot = ratePostShot(postPartitions, m);
    return (preShot - postShot);
  }
  public int ratePreShot(Partition p, ClientMove m) {
    List<Integer> enemyQueens = p.getBlackQueens();
    int enemyQueenValues = 0;
    for (Integer q : enemyQueens)
      enemyQueenValues += rateNode(p, q);
    List<Integer> friendlyQueens = p.getWhiteQueens();
    int friendlyQueenValues = 0;
    for (Integer q : friendlyQueens)
      friendlyQueenValues += rateNode(p, q);
    return (enemyQueenValues - friendlyQueenValues);
  }
  public int ratePostShot(List<Partition> partitions, ClientMove m) {
    int enemyQueenValues = 0;
    int friendlyQueenValues = 0;
    for (Partition p : partitions) {
      List<Integer> enemyQueens = p.getBlackQueens();
      List<Integer> friendlyQueens = p.getWhiteQueens();
      for (Integer q : enemyQueens)
        enemyQueenValues += rateNode(p, q);
      for (Integer q : friendlyQueens)
        friendlyQueenValues += rateNode(p, q);
    }
    return (enemyQueenValues - friendlyQueenValues);
  }
  public int rateNode(Partition p, int i) {
    int kingMoves = p.getNeighboringIndicies(i).size()*_kingMoveCoeff;
    int queenMoves = p.getReachableIndicies(i).size()*_queenMoveCoeff;
    return (kingMoves + queenMoves);
  }
}
