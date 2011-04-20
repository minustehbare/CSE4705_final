/*
 * This will return the next move that most effectively
 * fills up a partition which is left with only one
 * queen.
 */

package CSE4705_final.EndGame;

import CSE4705_final.State.*;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author steve
 */
public class IdleMove {

  public Move getIdleMove(Partition _partition){
    //compare legal moves to find one that does not create
    //an articulation point
    List<Node> _moves = _partition.getReachableNodes(_queen.row, _queen.col, _partition.getPartID(), true);

    ListIterator<Node> _itr = _moves.listIterator();

    Node _tempMove;
    while(_itr.hasNext())
    {
      _tempMove = _itr.next();
      List<Node> _shots = _partition.getReachableNodes(_tempMove.getRow(),_tempMove.getCol(), _partition.getPartID(), true);

      ListIterator<Node> _itrS = _shots.listIterator();
      Node _tempShot;
      while(_itr.hasNext())
        {
          if (_tempShot /*is not articulating*/)
            return (new Move(_tempMove, _tempShot));
        }
    }
    //No optimal move.  Come up with a heuristic to choose a less optimal one
    return null;

  }


}
