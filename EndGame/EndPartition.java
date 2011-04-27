/*
 * This class represents a partition with only our queens left in it.
 * Asking it for a move should return one move towards a most efficient
 * filling up of the board.
 */

package CSE4705_final.EndGame;

import CSE4705_final.State.Node;
import CSE4705_final.State.Partition;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author steve
 */
public class EndPartition {
  Partition _partition;
  boolean _isBlack;
  int _gen;
  Set<Integer> _queens;

  //Constructor
  public EndPartition(Partition _p, boolean _Black, int _generation)
  {
    _partition = _p;
    _isBlack = _Black;
    _gen = _generation;
    if (_isBlack)
      _queens = _partition.getBlackQueens();
    else
      _queens = _partition.getWhiteQueens();

  }

  //grab the next move
  //returns null if there are no more moves
  public Move getMove(){
    Iterator<Integer> _itr = _queens.iterator();
    Integer _from = _itr.next();
    List<Node> _moves = _partition.getReachableNodes(_from);

    //This currently checks for first possible non-articulating move+shot that can be made
    Node _tempMove=null;
    Node _tempShot=null;
    ListIterator<Node> _moveItr = _moves.listIterator();
    while(_itr.hasNext())
    {
      _tempMove = _moveItr.next();
      _moveItr.remove();
      List<Node> _shots = _partition.getReachableNodes(_tempMove.getRow(),_tempMove.getCol());

      ListIterator<Node> _shotItr = _shots.listIterator();
      while(_shotItr.hasNext())
        {
          _tempShot = _shotItr.next();
          if (_tempShot /*is not articulating*/)
            return (new Move(_from, _tempMove.getIndex(), _tempShot.getIndex(), 0));
        }
    }
    //No optimal move.  Come up with a heuristic to choose a less optimal one
    return (new Move(_from, _tempMove.getIndex(), _tempShot.getIndex(), 0));
  }
}