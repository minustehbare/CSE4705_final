package CSE4705_final.EndGame;

import CSE4705_final.State.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ABSearch{
  
  //entry function.  Start an ABSearch here
  public Move ABStart(Partition _state, int gen, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, gen, null, null));
    return   (MinValue(_state, gen, null, null));
  }
  
  private Move MaxValue(Partition _state, int gen, Move a, Move b){
    //Get the index of the white queen
    Set<Integer> _whiteQueens = _state.getWhiteQueens();
    Iterator<Integer> WQItr = _whiteQueens.iterator();
    Integer WQindex = WQItr.next();

    //if the white queen can't move
    if(_state.getReachableNodes(WQindex).isEmpty())
      return null; //TODO: This will be the best move for black to take with a value that is the number of spaces blackQ can get to.

    //Get the index of the black queen
    Set<Integer> _blackQueens = _state.getWhiteQueens();
    Iterator<Integer> BQItr = _whiteQueens.iterator();
    Integer BQindex = BQItr.next();

    //List of black's possible moves.
<<<<<<< HEAD
    List<Integer> _moves = _state.getReachableIndicies(BQindex);
=======
    List<Node> _moves = _state.getReachableNodes(BQindex);

>>>>>>> 1e57b68bd874236e2b58098eed5bca01d0e46bdd
    //We can't move!
    if (_moves.isEmpty())
      return null;

<<<<<<< HEAD
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
      List<Partition> _TP2 = _temP1.forkNode(_move, NodeState.WHITE);
      _temP2 = _TP2.get(0);
      //Get shots you can make from the first possible move
      _shots = _temP2.getReachableIndicies(_move);

      //Run through the shots, build new states, and run them recursively
      for (Integer _shot : _shots){
        System.out.println(""+_move+_shots);
        if (_shot == _move)
          continue;

        System.out.println("Running max, _moves: "+_moves+" _shots: "+_shots);
        //build a new state, pump it through MinValue, and grab the max of the returned move
        List<Partition> _lp3 = _temP2.forkNode(_shot, NodeState.BLOCKED);
        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp3){
          if (!_p3.getWhiteQueens().isEmpty()){
            _pw=_p3;
            System.out.println("trueW");
          }
          if (!_p3.getBlackQueens().isEmpty()){
            _pb=_p3;
            System.out.println("trueB");
          }
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
        else{
          //Get the index of the white queen
          List<Integer> _whiteQueens = _pw.getWhiteQueens();
          Iterator<Integer> WQItr = _whiteQueens.iterator();
          Integer WQindex = WQItr.next();

          //if the white queen can't move
          if(_pw.getReachableIndicies(WQindex).isEmpty())
            return (new Move(BQindex.intValue(),_move,_shot,99));
          return (new Move(BQindex.intValue(),_move,_shot,_pb.enclosedCount()-_pw.enclosedCount()));
        }
=======
    Iterator<Node> _itrM = _moves.iterator();
    Iterator<Node> _itrS; //Use this to scroll through shots for each move
    List<Node> _shots;    //Store a list of shots from a given move
    Move _move;
    Node _tempVertex;     //Hold the current move in question

    //This is where the magic happens
    while(_itrM.hasNext()){
      //Get an iterator of shots you can make from the first possible move
      _tempVertex = _itrM.next();
      _shots = _state.getReachableNodes(_tempVertex.getIndex());
      _itrS = _shots.listIterator();

      //Run through the shots, build new states, and run them recursively
      while(_itrS.hasNext()){
        //TODO: build a new state, pump it through MinValue, and grab the max of the returned move
        Move _newMove = MinValue(_state, gen, a, b);
        if(_newMove.getValue() > a.getValue())
          a = _newMove;
        if (b.getValue() <= a.getValue()) return a;
>>>>>>> 1e57b68bd874236e2b58098eed5bca01d0e46bdd
      }
    }
    return a;
  }

  //TODO: when MaxValue works, paste it here and fix it up real nice for MinValue
<<<<<<< HEAD
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
        System.out.println("Running min, _moves: "+_moves+" _shots: "+_shots);
        //build a new state, pump it through MaxValue, and grab the max of the returned move
        List<Partition> _lp3 = _temP2.forkNode(_shot, NodeState.BLOCKED);

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

          //if the Black queen can't move
          if(_pw.getReachableIndicies(BQindex).isEmpty())
            return (new Move(WQindex.intValue(),_move,_shot,-99));

          Move _newMove = MaxValue(_pw, a, b);
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
          if(_pw.getReachableNodes(BQindex).isEmpty())
            return (new Move(WQindex.intValue(),_move,_shot,-99));
          return (new Move(WQindex.intValue(),_move,_shot,_pw.enclosedCount()-_pb.enclosedCount()));
        }
      }
    }
    return b;
=======
  private Move MinValue(Partition _state, int gen, Move a, Move b){
    return null;
>>>>>>> 1e57b68bd874236e2b58098eed5bca01d0e46bdd
  }

  public static void main(String[] args){
    //create a partition
    NodeSet _refSet = new NodeSet();
    SortedSet<Integer> _enclosedNodes = new TreeSet<Integer>();
    int gen = 0;
    Partition _partition;

<<<<<<< HEAD
    List<Partition> _lp1 = _p.forkNode(11, NodeState.BLOCKED);
    Partition _p1 = _lp1.get(0);
    List<Partition> _lp2 = _p1.forkNode(21, NodeState.BLOCKED);
    Partition _p2 = _lp2.get(0);
    List<Partition> _lp3 = _p2.forkNode(41, NodeState.BLOCKED);
    Partition _p3 = _lp3.get(0);
    List<Partition> _lp4 = _p3.forkMove(new ClientMove(3,0,1,0,0,1),true);
    Partition _p4 = _lp4.get(0);
    List<Partition> _lp5 = _p4.forkMove(new ClientMove(6,0,3,0,3,1),false);
    Partition _p5 = _lp5.get(0);
    List<Partition> _lp6 = _p5.forkNode(40, NodeState.BLOCKED);
    Partition _p6 = _lp6.get(0);

    ABSearch _a = new ABSearch();
    Move _move = _a.ABStart(_p6,true);
    System.out.println(""+_move._from+"|"+_move._to+"|"+_move._shot);
    //Set<Integer> _blackQueens = _p6.getWhiteQueens();
    //for(Integer B : _blackQueens){
    //  System.out.println("Queen is on space "+B);
    //}
=======

    _partition = new Partition(_refSet, _enclosedNodes, gen);
>>>>>>> 1e57b68bd874236e2b58098eed5bca01d0e46bdd

  }
}
