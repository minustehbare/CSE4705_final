/*
 * This will return the next move that most effectively
 * fills up a partition which is left with only one
 * queen.
 */

package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.NodeSet;
import CSE4705_final.State.NodeState;
import CSE4705_final.State.Partition;
import CSE4705_final.State.PartitionSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author steve
 */
public class IdleMove {
  List<ClientMove> _endMoves;
  ListIterator<ClientMove> _moveItr;

  public IdleMove(){
    _endMoves = new LinkedList<ClientMove>();
  }

  public void addPartition(Partition _p){
    //create a new endpartition
    EndPartition _ep = new EndPartition(_p);

    //calculate the end partition's moves.  This is where the DFS is called.  Thread this up real good-like
    List<ClientMove> _moves = _ep.getMoves();
    
    for(ClientMove _move : _moves){
      _endMoves.add(_move);
    }
    _moveItr = _endMoves.listIterator();
  }

  //returns a move from one of the partitions with only our queens
  public ClientMove getIdleMove(){
    while(_moveItr.hasNext())
    {
      ClientMove _next = _moveItr.next();
      _moveItr.remove();
      return(_next);
    }
    return null;
  }
      public static void main(String[] args){
    NodeSet init = NodeSet.BLOCKED_NODE_SET;
    int ng = init.forkNode(0, 0, NodeState.EMPTY);
    ng = init.forkNode(10, ng, NodeState.WHITE);
    ng = init.forkNode(11, ng, NodeState.EMPTY);
    ng = init.forkNode(1, ng, NodeState.EMPTY);
    ng = init.forkNode(3, ng, NodeState.EMPTY);
    ng = init.forkNode(13, ng, NodeState.EMPTY);
    ng = init.forkNode(23, ng, NodeState.EMPTY);
    ng = init.forkNode(34, ng, NodeState.WHITE);
    ng = init.forkNode(25, ng, NodeState.EMPTY);
    ng = init.forkNode(15, ng, NodeState.WHITE);
    ng = init.forkNode(5, ng, NodeState.EMPTY);
    NodeSet isolated = init.isolateGen(ng);
    Partition rootPart = isolated.getRootPartition();
    PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
    List<Partition> whiteOwnedParts = testPartSet.getWhiteOwnedParts();
    //print that board
    System.out.println(testPartSet.getPrintout());

    //Grab two seporate partitions that have only white queens
    Partition _ep1 = whiteOwnedParts.get(0);
    Partition _ep2 = whiteOwnedParts.get(1);

    //Create an IdleMove instance to handle the idle partitions
    IdleMove _endHolder = new IdleMove();

    //add the partitions to the idle move holder
    _endHolder.addPartition(_ep2);
    _endHolder.addPartition(_ep1);

    //print out all the idle moves.  Note that you can grab some moves, then add another idle partition and it will still work fine.
    ClientMove _move = _endHolder.getIdleMove();
    while(_move!=null){
      System.out.println(_move.getFromIndex()+","+_move.getToIndex()+","+_move.getShootIndex());
      _move = _endHolder.getIdleMove();
    }
  }
}
