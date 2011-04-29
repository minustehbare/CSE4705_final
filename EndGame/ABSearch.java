package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.*;
import java.util.Iterator;
import java.util.List;

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
    List<Integer> _blackQueens = _state.getBlackQueens();
    Integer BQindex = null;
    for(Integer B : _blackQueens){
      BQindex = B;
      System.out.println("Queen is on space "+B);
    }

    //List of black's possible moves.
    List<Integer> _moves = _state.getReachableIndicies(BQindex);
//System.out.println("_move1 is "+_moves.get(1));
    //We can't move!
    if (_moves.isEmpty())
      return null;

    List<Integer> _shots;    //Store a list of shots from a given move
    Partition _p1,_p2;    //Some temporary partitions to play with
    Partition _pw=null;
    Partition _pb=null;

    //This is where the magic happens
    for (Integer _move : _moves){
      if (_move==BQindex)
        continue;
      System.out.println("_moves is "+_move);
      //Get shots you can make from the first possible move
      _shots = _state.getReachableIndicies(_move);

      //Run through the shots, build new states, and run them recursively
      for (Integer _shot : _shots){
        //build a new state, pump it through MinValue, and grab the max of the returned move
        List<Partition> _lp3 = _state.forkMove(new ClientMove(BQindex/10,BQindex%10,_move/10,_move%10,_shot/10,_shot%10),true);
        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
        System.out.println(""+_move);
          if (!_p3.getWhiteQueens().isEmpty())
            _pw=_p3;
          if (!_p3.getBlackQueens().isEmpty())
            _pb=_p3;
        }
        if(_pb==null||_pw==null)
          System.out.println("WWWTTTTFFFFFFFFFFFF");

        //Black and White queens are in the same partitions
        if(_pb==_pw){
          //Get the index of the white queen
          List<Integer> _whiteQueens = _pw.getWhiteQueens();
          Iterator<Integer> WQItr = _whiteQueens.iterator();
          Integer WQindex = WQItr.next();

          //if the white queen can't move
          if(_pw.getReachableIndicies(WQindex).isEmpty())
            return (new Move(BQindex.intValue(),_move,_shot,99));

          Move _newMove = MinValue(_pw, a, b);
          if(_newMove.getValue() > a.getValue())
            a = _newMove;
          if (b.getValue() <= a.getValue()) return a;
        }

        //Get the index of the white queen
        List<Integer> _whiteQueens = _pw.getWhiteQueens();
        Iterator<Integer> WQItr = _whiteQueens.iterator();
        Integer WQindex = WQItr.next();

        //if the white queen can't move
        if(_pw.getReachableIndicies(WQindex).isEmpty())
          return (new Move(BQindex.intValue(),_move,_shot,99));
        return (new Move(BQindex.intValue(),_move,_shot,_pb.enclosedCount()-_pw.enclosedCount()));
      }
    }
    return a;
  }

  //TODO: when MaxValue works, paste it here and fix it up real nice for MinValue
  private Move MinValue(Partition _state, Move a, Move b){
    //Get the index of the black queen
    List<Integer> _whiteQueens = _state.getWhiteQueens();
    Iterator<Integer> WQItr = _whiteQueens.iterator();
    Integer WQindex = WQItr.next();

    //List of black's possible moves.
    List<Integer> _moves = _state.getReachableIndicies(WQindex);

    //We can't move!
    if (_moves.isEmpty())
      return null;

    List<Integer> _shots;    //Store a list of shots from a given move
    Partition _p1,_p2;    //Some temporary partitions to play with
    Partition _pw=null;
    Partition _pb=null;

    //This is where the magic happens
    for (Integer _move : _moves){
      //Get shots you can make from the first possible move
      _shots = _state.getReachableIndicies(_move);

      //Run through the shots, build new states, and run them recursively
      for (Integer _shot : _shots){
        //build a new state, pump it through MaxValue, and grab the max of the returned move
        List<Partition> _lp3 = _state.forkMove(new ClientMove(WQindex/10,WQindex%10,_move/10,_move%10,_shot/10,_shot%10),false);

        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
          if (!_p3.getWhiteQueens().isEmpty())
            _pw=_p3;
          if (!_p3.getBlackQueens().isEmpty())
            _pb=_p3;
        }

        if(_pb==null||_pw==null)
          System.out.println("WWWTTTTFFFFFFFFFFFF");
        //Black and White queens are in the same partitions
        if(_pb==_pw){
          //Get the index of the black queen
          List<Integer> _blackQueens = _pb.getBlackQueens();
          Integer BQindex = _blackQueens.remove(0);

          //if the white queen can't move
          if(_pw.getReachableIndicies(BQindex).isEmpty())
            return (new Move(WQindex.intValue(),_move,_shot,99));

          Move _newMove = MaxValue(_pw, a, b);
          if(_newMove.getValue() > a.getValue())
            b = _newMove;
          if (b.getValue() <= a.getValue()) return b;
        }

        //Get the index of the black queen
        List<Integer> _blackQueens = _pw.getWhiteQueens();
        Iterator<Integer> BQItr = _blackQueens.iterator();
        Integer BQindex = BQItr.next();

        //if the black queen can't move
        if(_pw.getReachableNodes(BQindex).isEmpty())
          return (new Move(WQindex.intValue(),_move,_shot,99));
        return (new Move(WQindex.intValue(),_move,_shot,_pb.enclosedCount()-_pw.enclosedCount()));
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

    ABSearch _a = new ABSearch();
    Move _move = _a.ABStart(_p6,true);
    System.out.println(""+_move._from+_move._to+_move._shot);
    //Set<Integer> _blackQueens = _p6.getWhiteQueens();
    //for(Integer B : _blackQueens){
    //  System.out.println("Queen is on space "+B);
    //}

  }
}
