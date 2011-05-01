package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.*;
import java.util.List;

public class ABSearch{

  //entry function.  Start an ABSearch here
  public ClientMove ABStart(PartitionSet _state, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, new ClientMove(0,0,0,-100), new ClientMove(0,0,0,100)));
    return   (MinValue(_state, new ClientMove(0,0,0,-100), new ClientMove(0,0,0,100)));
  }

  private ClientMove MaxValue(PartitionSet _set, ClientMove a, ClientMove b){
    System.out.println("Running max");
    List<Partition> _states = _set.getContestedParts();
    for (Partition _state : _states)
    {
      //Get the index of the black queen
      List<Integer> _blackQueens = _state.getBlackQueens();
      for(Integer BQindex : _blackQueens)
      {
        //List of black's possible moves.
        List<ClientMove> _moves = _state.getPossibleMoves(BQindex);
        //We can't move!
        if (_moves.isEmpty()){
          System.out.println("We're stuck!");
          System.out.println(BQindex);
          System.out.flush();
          continue;
        }
        Partition _pw=null;
        Partition _pb=null;

        //This is where the magic happens
          for (ClientMove _move : _moves){
          //build a new state
          PartitionSet _ps = _set.forkPartitionSet(_move, true);

          //if there are no contested regions, do we have more space then them? Return the difference
          if (!_ps.areAnyContestedParts()){
            int v=0;
            for(Partition p : _ps.getBlackOwnedParts()){
              v = v+p.enclosedCount();
            }
            for(Partition p : _ps.getWhiteOwnedParts()){
              v = v-p.enclosedCount();
            }
            _move.setValue(v);
            if(_move.getValue() > a.getValue())
              a = _move;
            if (b.getValue() <= a.getValue())
              return a;
          }

          //if there are contested regions, do a recursive call

          ClientMove _newMove = MinValue(_ps, a, b);
          if(_newMove.getValue() > a.getValue())
            a = _newMove;
          if (b.getValue() <= a.getValue()) return a;
        }
      }
    }
    return a;
  }

  private ClientMove MinValue(PartitionSet _set, ClientMove a, ClientMove b){
    System.out.println("Running min");
    List<Partition> _states = _set.getContestedParts();
    for (Partition _state : _states)
    {
      //Get the index of the white queens
      List<Integer> _whiteQueens = _state.getWhiteQueens();
      for(Integer WQindex : _whiteQueens)
      {
        //List of white's possible moves.
        List<ClientMove> _moves = _state.getPossibleMoves(WQindex);
        //We can't move!
        if (_moves.isEmpty()){
          System.out.println("We're stuck!");
          System.out.println(WQindex);
          System.out.flush();
          continue;
        }
        //This is where the magic happens
          for (ClientMove _move : _moves){
          //build a new state
          PartitionSet _ps = _set.forkPartitionSet(_move, true);

          //if there are no contested regions, do we have more space then them? Return the difference
          if (!_ps.areAnyContestedParts()){
            int v=0;
            for(Partition p : _ps.getBlackOwnedParts()){
              v = v+p.enclosedCount();
            }
            for(Partition p : _ps.getWhiteOwnedParts()){
              v = v-p.enclosedCount();
            }
            _move.setValue(v);
            if(_move.getValue() < b.getValue())
              b = _move;
            if (b.getValue() <= a.getValue())
              return b;
          }

          //if there are contested regions, do a recursive call
          ClientMove _newMove = MaxValue(_ps, a, b);
          if(_newMove.getValue() < b.getValue())
            b = _newMove;
          if (b.getValue() <= a.getValue()) return b;
        }
      }
    }
    return b;
  }


  public static void main(String[] args){
    NodeSet init = NodeSet.BLOCKED_NODE_SET;
    int ng = init.forkNode(0, 0, NodeState.EMPTY);
    ng = init.forkNode(1, ng, NodeState.EMPTY);
    ng = init.forkNode(2, ng, NodeState.WHITE);
    ng = init.forkNode(11, ng, NodeState.BLACK);
    NodeSet isolated = init.isolateGen(ng);
    Partition rootPart = isolated.getRootPartition();
    PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
    List<Partition> blackOwnedParts = testPartSet.getBlackOwnedParts();
    System.out.println(testPartSet.getPrintout());
    ABSearch _ab = new ABSearch();
    ClientMove _move = _ab.ABStart(testPartSet, true);
    System.out.println(_move.getFromIndex()+","+_move.getToIndex()+","+_move.getShootIndex());
  }
}
