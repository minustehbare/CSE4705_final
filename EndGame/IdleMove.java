/*
 * This will return the next move that most effectively
 * fills up a partition which is left with only one
 * queen.
 */

package CSE4705_final.EndGame;

import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author steve
 */
public class IdleMove {
  List<EndPartition> _endPartitions;
  ListIterator<EndPartition> _partItr;

  public IdleMove(){
  }

  public void addPartition(EndPartition _p){
    _endPartitions.add(_p);
    _partItr = _endPartitions.listIterator();
  }

  //returns a move from one of the partitions with only our queens
  public Move getIdleMove(){
    EndPartition _p;
    while(_partItr.hasNext())
    {
      _p=_partItr.next();
      Move _move = _p.getMove();
      if(_move==null){
        _partItr.remove();
        continue;
      }
      return _move;
    }
    return null;
  }
}
