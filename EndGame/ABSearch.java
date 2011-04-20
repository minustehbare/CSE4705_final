public class ABSearch{
  
  //entry function.  Start an ABSearch here
  public ABStart(State _state, boolean _isBlack){
    if(_isBlack)
      return (MaxValue(_state, new Move(null, null,-9999), new Move(null, null, 9999));
    return   (MinValue(_state, new Move(null, null,-9999), new Move(null, null, 9999));
  }
  
  private MaxValue(State _state, Move a, Move b){
    //if the white queen can't move
    if(_state._whiteQ.getMoves().size()==0)
      return null; //TODO: This will be the best move for black to take with a value that is the number of spaces blackQ can get to.
    java.util.LinkedList<Vertex> _moves = _state._blackQ.getMoves();
    if (_moves.size()==0)
      return null;
    java.util.ListIterator<Vertex> _itrM = _moves.listIterator();
    java.util.ListIterator<Vertex> _itrS;
    java.util.LinkedList<Vertex> _shots;
    move _move;
    Vertex _tempVertex;
    while(_itrM.hasNext()){
      _tempVertex = _itrM.next();
      _shots = _tempVertex.getMoves();
      _itrS = _shots.listIterator();
      while(_itrS.hasNext()){
        //TODO: build a new state, pump it through MinValue, and grab the returned move
        a = max(a, MinValue(!!NEW STATE!!, a, b);
        if (b <= a) return a;
      }
    }
    return a;
  }
}
