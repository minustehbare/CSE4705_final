/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AI;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.Partition;
import java.util.List;

/**
 *
 * @author tom
 */
public class FirstMove {
  FirstMove() {

  }
  public ClientMove findMove(Partition p) {
    List<Integer> queens = p.getWhiteQueens();
    Integer queenOne = null, queenTwo = null;
    for (Integer q : queens) {
      if (q % 10 == 3)
        queenOne = q;
      if (q % 10 == 6)
        queenTwo = q;
    }
    int from, to, shoot;
    if (p.getNeighboringIndicies(queenOne).size() == 5) {
      from = queenOne;
      shoot = 4;
    }
    else {
      from = queenTwo;
      shoot = 5;
    }
    if (from / 10 == 9)
      to = from - 80;
    else
      to = from + 80;
    shoot += to;
    ClientMove move = new ClientMove(from, to, shoot);
    return move;
  }
}
