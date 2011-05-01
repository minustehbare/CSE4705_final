/*
 * This will return the next move that most effectively
 * fills up a partition which is left with only one
 * queen.
 */

package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.Partition;
import java.util.LinkedList;
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
    _endPartitions = new LinkedList<EndPartition>();
  }

  public void addPartition(Partition _p, boolean _isBlack){
    EndPartition _ep = new EndPartition(_p,_isBlack);
    _endPartitions.add(_ep);
    _partItr = _endPartitions.listIterator();
  }

  //returns a move from one of the partitions with only our queens
  public ClientMove getIdleMove(){
//    EndPartition _p;
//    while(_partItr.hasNext())
//    {
//      _p=_partItr.next();
//      ClientMove _move = _p.getMove();
//      if(_move==null){
//        _partItr.remove();
//        continue;
//      }
//      return _move;
//    }
    return null;
  }
}
