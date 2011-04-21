package CSE4705_final.EndGame;

import CSE4705_final.State.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ABSearch{
  
  //entry function.  Start an ABSearch here
  public Move ABStart(Partition _state, int gen, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, gen, new Move(null, null,-9999), new Move(null, null, 9999)));
    return   (MinValue(_state, gen, new Move(null, null,-9999), new Move(null, null, 9999)));
  }
  
  private Move MaxValue(Partition _state, int gen, Move a, Move b){
    //Get the index of the white queen
    Set<Integer> _whiteQueens = _state.getWhiteQueens(gen, true);
    Iterator<Integer> WQItr = _whiteQueens.iterator();
    Integer WQindex = WQItr.next();

    //if the white queen can't move
    if(_state.getReachableNodes(WQindex, true).isEmpty())
      return null; //TODO: This will be the best move for black to take with a value that is the number of spaces blackQ can get to.

    //Get the index of the black queen
    Set<Integer> _blackQueens = _state.getWhiteQueens(gen, true);
    Iterator<Integer> BQItr = _whiteQueens.iterator();
    Integer BQindex = BQItr.next();

    //List of black's possible moves.
    List<Node> _moves = _state.getReachableNodes(BQindex, true);

    //We can't move!
    if (_moves.isEmpty())
      return null;

    Iterator<Node> _itrM = _moves.iterator();
    Iterator<Node> _itrS; //Use this to scroll through shots for each move
    List<Node> _shots;    //Store a list of shots from a given move
    Move _move;
    Node _tempVertex;     //Hold the current move in question

    //This is where the magic happens
    while(_itrM.hasNext()){
      //Get an iterator oh shots you can make from the first possible move
      _tempVertex = _itrM.next();
      _shots = _state.getReachableNodes(_tempVertex.getIndex(), true);
      _itrS = _shots.listIterator();

      //Run through the shots, build new states, and run them recursively
      while(_itrS.hasNext()){
        //TODO: build a new state, pump it through MinValue, and grab the max of the returned move
        Move _newMove = MinValue(_state, gen, a, b);
        if(_newMove.getValue() > a.getValue())
          a = _newMove;
        if (b.getValue() <= a.getValue()) return a;
      }
    }
    return a;
  }

  //TODO: when MaxValue works, paste it here and fix it up real nice for MinValue
  private Move MinValue(Partition _state, int gen, Move a, Move b){
    return null;
  }
}
