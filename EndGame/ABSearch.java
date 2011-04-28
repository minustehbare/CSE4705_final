package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ABSearch{
  
  //entry function.  Start an ABSearch here
  public Move ABStart(Partition _state, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, new Move(0,0,0,0), new Move(0,0,0,0)));
    return   (MinValue(_state, new Move(0,0,0,0), new Move(0,0,0,0)));
  }
  
  private Move MaxValue(Partition _state, Move a, Move b){
    //Get the index of the black queen
    Set<Integer> _blackQueens = _state.getBlackQueens();
    Integer BQindex = null;
    for(Integer B : _blackQueens){
      BQindex = B;
      System.out.println("Queen is on space "+B);
    }

    //List of black's possible moves.
    List<Node> _moves = _state.getReachableNodes(BQindex);

    //We can't move!
    if (_moves.isEmpty())
      return null;

    List<Node> _shots;    //Store a list of shots from a given move
    Partition _p1,_p2;    //Some temporary partitions to play with
    Partition _pw=null;
    Partition _pb=null;

    //This is where the magic happens
    for (Node _move : _moves){
      //Get shots you can make from the first possible move
      _shots = _state.getReachableNodes(_move.getIndex());

      //Run through the shots, build new states, and run them recursively
      for (Node _shot : _shots){
        //build a new state, pump it through MinValue, and grab the max of the returned move
        List<Partition> _lp1 = _state.forkNode(BQindex, NodeState.EMPTY);
        _p1 = _lp1.get(0);
        List<Partition> _lp2 = _state.forkNode(_move.getIndex(), NodeState.BLACK);
        _p2 = _lp2.get(0);
        List<Partition> _lp3 = _state.forkNode(_shot.getIndex(), NodeState.BLOCKED);

        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
          if (_p3.getWhiteQueens()!=null)
            _pw=_p3;
          if (_p3.getBlackQueens()!=null)
            _pb=_p3;
        }
        if(_pb==null||_pw==null)
          System.out.println("WWWTTTTFFFFFFFFFFFF");

        //Black and White queens are in the same partitions
        if(_pb==_pw){
          //Get the index of the white queen
          Set<Integer> _whiteQueens = _pw.getWhiteQueens();
          Iterator<Integer> WQItr = _whiteQueens.iterator();
          Integer WQindex = WQItr.next();

          //if the white queen can't move
          if(_pw.getReachableNodes(WQindex).isEmpty())
            return (new Move(BQindex.intValue(),_move.getIndex(),_shot.getIndex(),99));

          Move _newMove = MinValue(_pw, a, b);
          if(_newMove.getValue() > a.getValue())
            a = _newMove;
          if (b.getValue() <= a.getValue()) return a;
        }

        //Get the index of the white queen
        Set<Integer> _whiteQueens = _pw.getWhiteQueens();
        Iterator<Integer> WQItr = _whiteQueens.iterator();
        Integer WQindex = WQItr.next();

        //if the white queen can't move
        if(_pw.getReachableNodes(WQindex).isEmpty())
          return (new Move(BQindex.intValue(),_move.getIndex(),_shot.getIndex(),99));
        return (new Move(BQindex.intValue(),_move.getIndex(),_shot.getIndex(),_pb.enclosedCount()-_pw.enclosedCount()));
      }
    }
    return a;
  }

  //TODO: when MaxValue works, paste it here and fix it up real nice for MinValue
  private Move MinValue(Partition _state, Move a, Move b){
    //Get the index of the black queen
    Set<Integer> _whiteQueens = _state.getWhiteQueens();
    Iterator<Integer> WQItr = _whiteQueens.iterator();
    Integer WQindex = WQItr.next();

    //List of black's possible moves.
    List<Node> _moves = _state.getReachableNodes(WQindex);

    //We can't move!
    if (_moves.isEmpty())
      return null;

    List<Node> _shots;    //Store a list of shots from a given move
    Partition _p1,_p2;    //Some temporary partitions to play with
    Partition _pw=null;
    Partition _pb=null;

    //This is where the magic happens
    for (Node _move : _moves){
      //Get shots you can make from the first possible move
      _shots = _state.getReachableNodes(_move.getIndex());

      //Run through the shots, build new states, and run them recursively
      for (Node _shot : _shots){
        //build a new state, pump it through MaxValue, and grab the max of the returned move
        List<Partition> _lp1 = _state.forkNode(WQindex, NodeState.EMPTY);
        _p1 = _lp1.get(0);
        List<Partition> _lp2 = _state.forkNode(_move.getIndex(), NodeState.WHITE);
        _p2 = _lp2.get(0);
        List<Partition> _lp3 = _state.forkNode(_shot.getIndex(), NodeState.BLOCKED);

        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
          if (_p3.getWhiteQueens()!=null)
            _pw=_p3;
          if (_p3.getBlackQueens()!=null)
            _pb=_p3;
        }

        if(_pb==null||_pw==null)
          System.out.println("WWWTTTTFFFFFFFFFFFF");

        //Black and White queens are in the same partitions
        if(_pb==_pw){
          //Get the index of the black queen
          Set<Integer> _blackQueens = _pb.getBlackQueens();
          Iterator<Integer> BQItr = _blackQueens.iterator();
          Integer BQindex = BQItr.next();

          //if the white queen can't move
          if(_pw.getReachableNodes(BQindex).isEmpty())
            return (new Move(WQindex.intValue(),_move.getIndex(),_shot.getIndex(),99));

          Move _newMove = MaxValue(_pw, a, b);
          if(_newMove.getValue() > a.getValue())
            b = _newMove;
          if (b.getValue() <= a.getValue()) return b;
        }

        //Get the index of the black queen
        Set<Integer> _blackQueens = _pw.getWhiteQueens();
        Iterator<Integer> BQItr = _blackQueens.iterator();
        Integer BQindex = BQItr.next();

        //if the black queen can't move
        if(_pw.getReachableNodes(BQindex).isEmpty())
          return (new Move(WQindex.intValue(),_move.getIndex(),_shot.getIndex(),99));
        return (new Move(WQindex.intValue(),_move.getIndex(),_shot.getIndex(),_pb.enclosedCount()-_pw.enclosedCount()));
      }
    }
    return b;
  }

  public static void main(String[] args){
    //create a partition
    NodeSet _refSet = new NodeSet();
    Partition _p = _refSet.getRootPartition();

    List<Partition> _lp1 = _p.forkNode(11, NodeState.BLOCKED);
    Partition _p1 = _lp1.get(0);
    List<Partition> _lp2 = _p1.forkNode(21, NodeState.BLOCKED);
    Partition _p2 = _lp2.get(0);
    List<Partition> _lp3 = _p2.forkNode(41, NodeState.BLOCKED);
    Partition _p3 = _lp3.get(0);
    List<Partition> _lp4 = _p3.forkMove(new ClientMove(3,0,0,0,0,1),true);
    Partition _p4 = _lp4.get(0);
    List<Partition> _lp5 = _p4.forkMove(new ClientMove(6,0,3,0,3,1),false);
    Partition _p5 = _lp5.get(0);
    List<Partition> _lp6 = _p5.forkNode(40, NodeState.BLOCKED);
    Partition _p6 = _lp6.get(0);

    List<Node> _moves = _p6.getReachableNodes(0);

    //ABSearch _a = new ABSearch();
    //Move _move = _a.ABStart(_p6,true);
    //System.out.println(""+_move._from+_move._to+_move._shot);
    //Set<Integer> _blackQueens = _p6.getWhiteQueens();
    //for(Integer B : _blackQueens){
    //  System.out.println("Queen is on space "+B);
    //}

  }
}
