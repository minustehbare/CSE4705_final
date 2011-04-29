package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.Graph.ArticPointDFS;
import CSE4705_final.State.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ABSearch{

  //entry function.  Start an ABSearch here
  public Move ABStart(Partition _state, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, new Move(0,0,0,-100), new Move(0,0,0,100)));
    return   (MinValue(_state, new Move(0,0,0,-100), new Move(0,0,0,100)));
  }

  private Move MaxValue(Partition _state, Move a, Move b){
    //Get the index of the black queen
    List<Integer> _blackQueens = _state.getBlackQueens();
    System.out.println("TEST"+_blackQueens);
    Integer BQindex = null;
    for(Integer B : _blackQueens){
      BQindex = B;
//      System.out.println("Queen is on space "+B);
    }

    //List of black's possible moves.
    List<Integer> _moves = _state.getReachableIndicies(BQindex);
    //We can't move!
    if (_moves.size()==1)
      return null;

    List<Integer> _shots;    //Store a list of shots from a given move

    Partition _pw=null;
    Partition _pb=null;
    Partition _temP1=null;
    Partition _temP2=null;

    //This is where the magic happens
    for (Integer _move : _moves){
      if (_move==BQindex)
        continue;
      List<Partition> _TP1 = _state.forkNode(BQindex, NodeState.EMPTY);
      _temP1 = _TP1.get(0);
      List<Partition> _TP2 = _temP1.forkNode(_move, NodeState.BLACK);
      _temP2 = _TP2.get(0);
      //Get shots you can make from the first possible move
      _shots = _temP2.getReachableIndicies(_move);

      //Run through the shots, build new states, and run them recursively
      for (Integer _shot : _shots){
//        System.out.println(""+_move+_shots);
        if (_shot == _move)
          continue;

        System.out.println("Running max, _moves: "+_move+_moves+" _shots: "+_shot+_shots);
        //build a new state, pump it through MinValue, and grab the max of the returned move
        List<Partition> _lp3 = _temP2.forkNode(_shot, NodeState.BLOCKED);
        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
          if (!_p3.getWhiteQueens().isEmpty()){
            _pw=_p3;
//            System.out.println("trueW");
          }
          if (!_p3.getBlackQueens().isEmpty()){
            _pb=_p3;
//            System.out.println("trueB");
          }
        }
        if(_pb==null||_pw==null)
//          System.out.println("WWWTTTTFFFFFFFFFFFF");

        //Black and White queens are in the same partitions
        if(_pb==_pw){
//          System.out.println("the queens are in the boat");
          //Get the index of the white queen
          List<Integer> _whiteQueens = _pw.getWhiteQueens();
          Integer WQindex = _whiteQueens.get(0);

          //if the white queen can't move
          if(_pw.getReachableIndicies(WQindex).size()==1)
          {
            System.out.println("!!"+_pw.getReachableIndicies(WQindex));
            return (new Move(BQindex.intValue(),_move,_shot,99));
          }
          System.out.println("sdgasdehadzrhzhtd");
          Move _newMove = MinValue(_pw, a, b);
          if(_newMove.getValue() > a.getValue())
            a = _newMove;
          if (b.getValue() <= a.getValue()) return a;
        }
        else{
//          System.out.println("Queens are all (forever) alone");
//          System.out.println(_pw.enclosedCount());
          //Get the index of the white queen
          List<Integer> _whiteQueens = _pw.getWhiteQueens();
          Iterator<Integer> WQItr = _whiteQueens.iterator();
          Integer WQindex = WQItr.next();

          //if the white queen can't move
          if(_pw.getReachableIndicies(WQindex).size()==1){
//            System.out.println("White queen can't move");
            return (new Move(BQindex.intValue(),_move,_shot,99));
          }
          a = new Move(BQindex.intValue(),_move,_shot,_pb.enclosedCount()-_pw.enclosedCount());
          if (b.getValue() <= a.getValue()){
//            System.out.println("alpha got pruned");
            return a;
          }
        }
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
    if (_moves.size()==1)
      return null;

    List<Integer> _shots;    //Store a list of shots from a given move
    Partition _pw=null;
    Partition _pb=null;
    Partition _temP1=null;
    Partition _temP2=null;

    //This is where the magic happens
    for (Integer _move : _moves){
      if (_move == WQindex)
        continue;
      List<Partition> _TP1 = _state.forkNode(WQindex, NodeState.EMPTY);
      _temP1 = _TP1.get(0);
      List<Partition> _TP2 = _temP1.forkNode(_move, NodeState.WHITE);
      _temP2 = _TP2.get(0);
      //Get shots you can make from the first possible move
      _shots = _temP2.getReachableIndicies(_move);

      //Run through the shots, build new states, and run them recursively
      for (Integer _shot : _shots){
        if (_shot == _move)
          continue;
        System.out.println("Running min, _moves: "+_move+_moves+" _shots: "+_shot+_shots);
        //build a new state, pump it through MaxValue, and grab the max of the returned move
        List<Partition> _lp3 = _temP2.forkNode(_shot, NodeState.BLOCKED);

        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
          if (!_p3.getWhiteQueens().isEmpty())
            _pw=_p3;
          if (!_p3.getBlackQueens().isEmpty()){
            _pb=_p3;
//            System.out.println("TTTTTTT");
          }
        }

        if(_pb==null||_pw==null)
//          System.out.println("WWWTTTTFFFFFFFFFFFF");
        //Black and White queens are in the same partitions
        if(_pb==_pw){
          //Get the index of the black queen
          List<Integer> _blackQueens = _pb.getBlackQueens();
          Integer BQindex = _blackQueens.get(0);

          //if the Black queen can't move
          if(_pw.getReachableIndicies(BQindex).size()==1)
            return (new Move(WQindex.intValue(),_move,_shot,-99));

          Move _newMove = MaxValue(_pb, a, b);
          if(_newMove.getValue() > a.getValue())
            b = _newMove;
          if (b.getValue() <= a.getValue()) return b;
        }
        else{
          //Get the index of the black queen
          List<Integer> _blackQueens = _pw.getWhiteQueens();
          Iterator<Integer> BQItr = _blackQueens.iterator();
          Integer BQindex = BQItr.next();

          //if the black queen can't move
          if(_pw.getReachableNodes(BQindex).size()==1)
            return (new Move(WQindex.intValue(),_move,_shot,-99));
          return (new Move(WQindex.intValue(),_move,_shot,_pw.enclosedCount()-_pb.enclosedCount()));
        }
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
    List<Partition> _lp3 = _p2.forkNode(22, NodeState.BLOCKED);
    Partition _p3 = _lp3.get(0);
    List<Partition> _lp4 = _p3.forkMove(new ClientMove(3,0,1,0,0,1),true);
    Partition _p4 = _lp4.get(0);
    List<Partition> _lp5 = _p4.forkMove(new ClientMove(6,0,4,0,4,1),false);
    Partition _p5 = _lp5.get(0);
    List<Partition> _lp6 = _p5.forkNode(50, NodeState.BLOCKED);
    Partition _p6 = _lp6.get(0);
    List<Partition> _lp7 = _p6.forkNode(51, NodeState.BLOCKED);
    Partition _p7 = _lp7.get(0);
    List<Partition> _lp8 = _p7.forkNode(32, NodeState.BLOCKED);
    Partition _p8 = _lp8.get(0);
    List<Partition> _lp9 = _p8.forkNode(42, NodeState.BLOCKED);
    Partition _p9 = _lp9.get(0);
    //System.out.println(_p9.getReachableIndicies(40));
    //ABSearch _a = new ABSearch();
    //Move _move = _a.ABStart(_p9,true);
    //System.out.println(""+_move._from+"|"+_move._to+"|"+_move._shot);

    //ArticPoint test
    ArticPointDFS _a = new ArticPointDFS();
    Set<Integer> _AP = _a.runArticPoint(_p9);
    System.out.println("Artic Points: "+_AP);


    //Set<Integer> _blackQueens = _p6.getWhiteQueens();
    //for(Integer B : _blackQueens){
    //  System.out.println("Queen is on space "+B);
    //}

  }
}
