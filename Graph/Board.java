public class Board{
  private Node[][] _board;
  private Node[] _Bqueens;
  private Node[] _Wqueens;

  //constructor
  public Board(){
    //initialize the board
    _board = new Node[10][10];
    for(int i=0;i<10;i++)
    {
      for(int j=0;j<10;j++)
      {
        _board[i][j] = new Node(i,j);          
      }
    }
    //initialize the queens
    _Bqueens = new Node[4];
    _Bqueens[0] = new Node(3,0);
    _Bqueens[0] = new Node(0,3);
    _Bqueens[0] = new Node(0,6);
    _Bqueens[0] = new Node(3,9);

    _Wqueens = new Node[4];
    _Wqueens[0] = new Node(9,0);
    _Wqueens[0] = new Node(9,3);
    _Wqueens[0] = new Node(9,6);
    _Wqueens[0] = new Node(6,9);
  }
  
  //return a list of all neighboring nodes
  public java.util.List<Node> getNeighbors(int _row, int _col){
    java.util.List<Node> _list = new java.util.LinkedList<Node>();
    int _colT;
    int _rowT;
    
    _rowT=_row-1;
    _colT=_col-1;
    if(!(_colT<0||_rowT<0))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    _rowT=_row-1;
    _colT=_col;
    if(!(_rowT<0))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    _rowT=_row-1;
    _colT=_col+1;
    if(!(_colT>9||_rowT<0))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    _rowT=_row;
    _colT=_col+1;
    if(!(_colT>9))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    _rowT=_row+1;
    _colT=_col+1;
    if(!(_colT>9||_rowT>9))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    _rowT=_row+1;
    _colT=_col;
    if(!(_rowT>9))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    _rowT=_row+1;
    _colT=_col-1;
    if(!(_colT<0||_rowT>9))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);

    _rowT=_row;
    _colT=_col-1;
    if(!(_colT<0))
      if(!_board[_rowT][_colT].isMarked) _list.add(_board[_rowT][_colT]);
    
    return _list;   
  }
 
  //get a list of legal moves
  public java.util.List<Node> legalMoves(int _row, int _col)
  {
    java.util.List<Node> _return = new java.util.LinkedList<Node>();
    int _colT, _rowT;
    _colT=_col;
    _rowT=_row;
    
    while(true){
      if (--_rowT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (++_rowT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (--_colT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (++_colT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (++_colT<10 && ++_rowT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
        
    _colT=_col;
    _rowT=_row;
    while(true){
      if (--_colT>=0 && --_rowT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}

    _colT=_col;
    _rowT=_row;
    
    while(true){
      if (++_colT<10 && --_rowT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (--_colT>=0 && ++_rowT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(new Node(_rowT,_colT));
          continue;}}
      break;}
    
    return _return;
  }
  
    //mark a space as 'shot'
    public boolean markShot(int _row,int _col){
    if (_board[_row][_col].isMarked){
      System.out.println("ATTEMPTED TO SHOOT A MARKED SPACE: "+_row+" "+_col);
      return false;
    }
    _board[_row][_col].isMarked = true;
    return true;
  }
    
    //unmark a shot.  May be usefull for marking where the queens
    //are then removing them when they move.
    public boolean unmarkShot(int _row,int _col){
    if (!_board[_row][_col].isMarked){
      System.out.println("ATTEMPTED TO UNMARK A BLANK SPACE: "+_row+" "+_col);
      return false;
    }
    _board[_row][_col].isMarked = false;
    return true;
  }
  
}
