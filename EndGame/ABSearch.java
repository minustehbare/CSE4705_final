package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ABSearch{

  //entry function.  Start an ABSearch here
  public ClientMove ABStart(PartitionSet _state, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, new ClientMove(0,0,0,-100), new ClientMove(0,0,0,100)));
    return   (MinValue(_state, new ClientMove(0,0,0,-100), new ClientMove(0,0,0,100)));
  }

  private ClientMove MaxValue(PartitionSet _set, ClientMove a, ClientMove b){
    List<Partition> _states = _set.getContestedParts();
    boolean stuck = true;
    for (Partition _state : _states)
    {
      //Get the index of the black queen
      List<Integer> _blackQueens = _state.getBlackQueens();
      for(Integer BQindex : _blackQueens)
      {
        //List of black's possible moves.
        List<ClientMove> _moves = _state.getPossibleMoves(BQindex);
        //We can't move!
        if (_moves.isEmpty())
          continue;
        stuck = false;
        
        //This is where the magic happens
        for (ClientMove _move : _moves)
        {
          //build a new state
          PartitionSet _ps = _set.forkPartitionSet(_move, true);

          //if there are no contested regions, do we have more space than them? Return the difference
          if (!_ps.areAnyContestedParts()){
            int v=0;
            for(Partition pb : _ps.getBlackOwnedParts()){
              v += 100*(pb.enclosedCount()-1);
            }
            for(Partition pw  : _ps.getWhiteOwnedParts()){
              v -= 100*(pw.enclosedCount()-1);
            }
            _move.setValue(v);
            if(_move.getValue() > a.getValue())
              a = _move;
          }
          else
          {
            //if there are contested regions, do a recursive call
            ClientMove _newMove = MinValue(_ps, a, b);
            if(_newMove.getValue() >= a.getValue()){
              _move.setValue(_newMove.getValue());
              a = _move;
            }
          }
        }
      }
    }
    if (stuck){
      return b;
    }
    return a;
  }

  private ClientMove MinValue(PartitionSet _set, ClientMove a, ClientMove b){
    List<Partition> _states = _set.getContestedParts();
    boolean stuck = true;
    for (Partition _state : _states)
    {
      //Get the index of the white queens
      List<Integer> _whiteQueens = _state.getWhiteQueens();
      for(Integer WQindex : _whiteQueens)
      {
        //List of white's possible moves.
        List<ClientMove> _moves = _state.getPossibleMoves(WQindex);
        //We can't move!
        if (_moves.isEmpty())
          continue;
        stuck = false;
        //This is where the magic happens
        for (ClientMove _move : _moves){
          //build a new state
          PartitionSet _ps = _set.forkPartitionSet(_move, true);

          //if there are no contested regions, do we have more space then them? Return the difference
          if (!_ps.areAnyContestedParts()){
            int v=0;
            for(Partition p : _ps.getBlackOwnedParts()){
              v += 100*(p.enclosedCount()-1);
            }
            for(Partition p : _ps.getWhiteOwnedParts()){
              v -= 100*(v-p.enclosedCount()-1);
            }
            _move.setValue(v);
            if(_move.getValue() < b.getValue())
              b = _move;
          }

          else
          {
            //if there are contested regions, do a recursive call
            ClientMove _newMove = MaxValue(_ps, a, b);
            if(_newMove.getValue() <= b.getValue()){
              _move.setValue(_newMove.getValue());
              b = _move;
            }
          }
        }
      }
    }
    if (stuck){
      return a;
    }
    return b;
  }


  public static void main(String[] args) throws FileNotFoundException, IOException{

/*

    NodeSet init = NodeSet.BLOCKED_NODE_SET;
    int ng = init.forkNode(0, 0, NodeState.WHITE);
    ng = init.forkNode(2, ng, NodeState.BLACK);
    ng = init.forkNode(11, ng, NodeState.BLACK);
    ng = init.forkNode(21, ng, NodeState.EMPTY);
    ng = init.forkNode(31, ng, NodeState.EMPTY);
    ng = init.forkNode(27, ng, NodeState.EMPTY);
    ng = init.forkNode(28, ng, NodeState.EMPTY);
    ng = init.forkNode(29, ng, NodeState.EMPTY);
    ng = init.forkNode(19, ng, NodeState.EMPTY);
    ng = init.forkNode(3, ng, NodeState.WHITE);
    ng = init.forkNode(9, ng, NodeState.BLACK);
    ng = init.forkNode(36, ng, NodeState.WHITE);
    ng = init.forkNode(37, ng, NodeState.EMPTY);
    NodeSet isolated = init.isolateGen(ng);
    Partition rootPart = isolated.getRootPartition();
    PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
    List<Partition> blackOwnedParts = testPartSet.getBlackOwnedParts();
    System.out.println(testPartSet.getPrintout());
    ABSearch _ab = new ABSearch();
    ClientMove _move = _ab.ABStart(testPartSet, true);
    System.out.println(_move.getFromIndex()+","+_move.getToIndex()+","+_move.getShootIndex()+","+_move.getValue());

    if(!testPartSet.areAnyContestedParts())
      System.out.println("WTF?");
 */
  }
}
