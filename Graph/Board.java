import java.util.*;

public class Board{
  private Vertex[][] _board;
  private Vertex[] _Bqueens;
  private Vertex[] _Wqueens;
  private java.util.LinkedList<Edge> _edges;

  //constructor
  public Board(){

    //initialize the board
    _board = new Vertex[10][10];
    _edges = new java.util.LinkedList<Edge>();
    Edge _tempEdge;
    int _rowT, _colT, counter;
    counter = 0;
    for(int i=0;i<10;i++)
    {
      for(int j=0;j<10;j++)
      {
        //add vertex
        _board[i][j] = new Vertex(counter++,i,j);
        //add edges to the edge list
        _rowT=i-1;
        _colT=j-1;
        if(!(_rowT<0||_colT<0)){
          _tempEdge = new Edge(_board[i][j],_board[_rowT][_colT]);
          _edges.add(_tempEdge);
          _board[i][j].eList.add(_tempEdge);
        }

        _rowT=i-1;
        _colT=j;
        if(!(_rowT<0)){
          _tempEdge = new Edge(_board[i][j],_board[_rowT][_colT]);
          _edges.add(_tempEdge);
          _board[i][j].eList.add(_tempEdge);
        }

        _rowT=i-1;
        _colT=j+1;
        if(!(_rowT<0||_colT>9)){
          _tempEdge = new Edge(_board[i][j],_board[_rowT][_colT]);
          _edges.add(_tempEdge);
          _board[i][j].eList.add(_tempEdge);
        }

        _rowT=i;
        _colT=j-1;
        if(!(_colT<0)){
          _tempEdge = new Edge(_board[i][j],_board[_rowT][_colT]);
          _edges.add(_tempEdge);
          _board[i][j].eList.add(_tempEdge);
        }
      }
    }
  }
  
  //return a list of all neighboring verticies
  public java.util.List<Vertex> getNeighbors(int _row, int _col)
  {
    java.util.List<Vertex> _list = new java.util.LinkedList<Vertex>();
    int _colT;
    int _rowT;
    
    _rowT=_row-1;
    _colT=_col-1;
    if(!(_colT<0||_rowT<0))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    _rowT=_row-1;
    _colT=_col;
    if(!(_rowT<0))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    _rowT=_row-1;
    _colT=_col+1;
    if(!(_colT>9||_rowT<0))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    _rowT=_row;
    _colT=_col+1;
    if(!(_colT>9))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    _rowT=_row+1;
    _colT=_col+1;
    if(!(_colT>9||_rowT>9))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    _rowT=_row+1;
    _colT=_col;
    if(!(_rowT>9))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    _rowT=_row+1;
    _colT=_col-1;
    if(!(_colT<0||_rowT>9))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));

    _rowT=_row;
    _colT=_col-1;
    if(!(_colT<0))
      if(!_board[_rowT][_colT].isMarked) _edges.add(new Edge(_board[_row][_col],_board[_rowT][_colT]));
    
    return _list;   
  }
 
  //get a list of legal moves
  public java.util.List<Vertex> legalMoves(int _row, int _col)
  {
    java.util.List<Vertex> _return = new java.util.LinkedList<Vertex>();
    int _colT, _rowT;
    _colT=_col;
    _rowT=_row;
    
    while(true){
      if (--_rowT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (++_rowT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (--_colT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (++_colT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (++_colT<10 && ++_rowT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
        
    _colT=_col;
    _rowT=_row;
    while(true){
      if (--_colT>=0 && --_rowT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}

    _colT=_col;
    _rowT=_row;
    
    while(true){
      if (++_colT<10 && --_rowT>=0){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
    
    _colT=_col;
    _rowT=_row;
    while(true){
      if (--_colT>=0 && ++_rowT<10){
        if (!_board[_rowT][_colT].isMarked){
          _return.add(_board[_rowT][_colT]);
          continue;}}
      break;}
    
    return _return;
  }
  
    //mark a space as 'shot'
   public boolean markShot(int _row,int _col)
   {
    if (_board[_row][_col].isMarked){
      System.out.println("ATTEMPTED TO SHOOT A MARKED SPACE: "+_row+" "+_col);
      return false;
    }
    _board[_row][_col].isMarked = true;
    return true;
  }
    
    //unmark a shot.  May be usefull for marking where the queens
    //are then removing them when they move.
   public boolean unmarkShot(int _row,int _col)
   {
    if (!_board[_row][_col].isMarked){
      System.out.println("ATTEMPTED TO UNMARK A BLANK SPACE: "+_row+" "+_col);
      return false;
    }
    _board[_row][_col].isMarked = false;
    return true;
  }
 
  //Grab the magnitudinal change in available moves between one spot and another
  public int deltaMoves(int _fromRow, int _fromCol, int _toRow, int _toCol)
  {
    java.util.List<Vertex> _fromList = legalMoves(_fromRow, _fromCol);
    java.util.List<Vertex> _toList = legalMoves(_toRow, _toCol);
    return(_toList.size()-_fromList.size());
  }
  
  //Can you guess what this method does?
  public void printBoard()
  {
    System.out.print("\n\n -------------------- \n");
    for(int i=0;i<10;i++)
    {
      System.out.print("|");
      for(int j=0;j<10;j++)
      {
        if (_board[i][j].isMarked)System.out.print(" #");
        else System.out.print(" -");
      }
    {System.out.print("|\n");}
    }
    System.out.print(" -------------------- \n\n");
  }
  
  public java.util.LinkedList<Edge> getEdgeParam(){
    java.util.LinkedList<Edge> _edgeParam;
    java.util.ListIterator<Edge> itr = _edges.listIterator();;   
    _edgeParam = new java.util.LinkedList<Edge>();
    Edge _tempEdge;
    while(itr.hasNext())
    {
      _tempEdge = itr.next();
      if (!_tempEdge.v.isMarked && !_tempEdge.x.isMarked){
        _edgeParam.add(_tempEdge); 
      }
    }
    return _edgeParam;
  }
  
  public static void main(String[] args){
    Board _b = new Board();
    _b.markShot(1,1);
    _b.markShot(1,2);
    _b.markShot(1,3);
    _b.markShot(0,3);
    System.out.println(_b.deltaMoves(0,0,0,2));
    _b.printBoard();
 
    ArticPointDFS _DFS = new ArticPointDFS(100);
    _DFS.runArticPoint(_b.getEdgeParam());

  }
}
