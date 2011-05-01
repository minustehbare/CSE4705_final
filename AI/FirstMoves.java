/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AI;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.Partition;
import java.util.List;
import java.util.LinkedList;

/**
 *
 * @author tom
 */
public class FirstMoves {
  List<ClientMove> _firstTopMoves, _firstBottomMoves;
  FirstMoves() {
    _firstTopMoves = new LinkedList<ClientMove>();
    _firstTopMoves.add(new ClientMove(3, 83, 84));
    _firstTopMoves.add(new ClientMove(6, 86, 85));
    _firstTopMoves.add(new ClientMove(30, 85, 86));
    _firstTopMoves.add(new ClientMove(39, 84, 83));

    _firstBottomMoves = new LinkedList<ClientMove>();
    _firstBottomMoves.add(new ClientMove(93, 13, 14));
    _firstBottomMoves.add(new ClientMove(96, 16, 15));
    _firstBottomMoves.add(new ClientMove(60, 15, 16));
    _firstBottomMoves.add(new ClientMove(69, 14, 13));
  }
  public ClientMove firstMove(Partition p) {
    List<Integer> queens = p.getWhiteQueens();
    List<ClientMove> moves;
    if (queens.contains(_firstTopMoves.get(0).getFromIndex()))
      moves = _firstTopMoves;
    else
      moves = _firstBottomMoves;
    ClientMove idealMove = null;
    
    //check each move:
    //if the TO index is reachable from the FROM index:
    //check that there is an enemy queen neighboring that space:
    //if so, select that move, otherwise continue checking other moves
    for (ClientMove m : moves) {
      List<Integer> enemyQueens = p.getBlackQueens();
      if (p.getReachableIndicies(m.getFromIndex()).contains(m.getToIndex())) {
        for (Integer q : enemyQueens)
          if (p.getNeighboringIndicies(q).contains(m.getToIndex()))
            idealMove = m;
        if (idealMove != null)
          break;
      }
    }
    return idealMove;
  }
}
