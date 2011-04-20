package CSE4705_final.RandomClient;

//Class representing the state of the board
public class Board 
{  
  public final static int _boardSize = 10;

  /************************
  /the game board         /
  /Blank spaces ====> -   /
  /Queens       ====> B W /
  /Arrowed      ====> #   /
  ************************/
  private char[][] _board;

  public Board()
  {
    _board = new char[_boardSize][_boardSize];

    //populate the board with blank spaces
    for(int i=0;i<_boardSize;i++)
    {
      for(int j=0;j<_boardSize;j++)
      {
        _board[j][i] = '-';
      }
    }
    //put queens in their starting positions
    _board[0][3] = 'B';
    _board[3][0] = 'B';
    _board[6][0] = 'B';
    _board[9][3] = 'B';
    _board[0][6] = 'W';
    _board[3][9] = 'W';
    _board[6][9] = 'W';
    _board[9][6] = 'W';
  }

  public void printBoard()
  {
    System.out.print("\n\n -------------------- \n");
    for(int i=0;i<_boardSize;i++)
    {
      System.out.print("|");
      for(int j=0;j<_boardSize;j++)
      {
        System.out.print(" " + _board[j][i]);
      }
    {System.out.print("|\n");}
    }
    System.out.print(" -------------------- \n\n");
  }
  
  //move a queen.  Returns false on an invalid move
  public boolean move(int from_x, int from_y, int to_x, int to_y)
  {
    if (isLegal(from_x,from_y,to_x,to_y))
    {
      _board[to_x][to_y] = _board[from_x][from_y];
      _board[from_x][from_y] = '-';
      return(true);
    }
    System.out.println("ATTEMPTED INVALID MOVE: from " + from_x + " " + from_y +
      " to " + to_x + " " + to_y);
    return(false);
  }
  
  //check if a move is legal
  public boolean isLegal(int from_x, int from_y, int to_x, int to_y)
  {
    if (_board[from_x][from_y]=='B' || _board[from_x][from_y]=='W')
    {
      if (_board[to_x][to_y]=='-')
      {
        return(true);
      }
    }
    return(false);
  }
  
  //mark a spot as arrowed
  public boolean markShot(int x, int y)
  {
    if (_board[x][y]=='-'){
      _board[x][y] = '#';
      return(true); 
    }
    System.out.println("Gun up to your waist Please don't shoot up the place "
      + x + " " + y);
    return(false);
  }
  
  //Get a list of legal moves
  public java.util.List<Pair> legalMoves(int x, int y)
  {
    java.util.List<Pair> _return = new java.util.LinkedList<Pair>();
    int _tx, _ty;
    _tx=x;
    _ty=y;
    
    while(true){
      if (--_ty>=0){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
    
    _tx=x;
    _ty=y;
    while(true){
      if (++_ty<_boardSize){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
    
    _tx=x;
    _ty=y;
    while(true){
      if (--_tx>=0){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
    
    _tx=x;
    _ty=y;
    while(true){
      if (++_tx<_boardSize){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
    
    _tx=x;
    _ty=y;
    while(true){
      if (++_tx<_boardSize && ++_ty<_boardSize){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
        
    _tx=x;
    _ty=y;
    while(true){
      if (--_tx>=0 && --_ty>=0){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}

    _tx=x;
    _ty=y;
    
    while(true){
      if (++_tx<_boardSize && --_ty>=0){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
    
    _tx=x;
    _ty=y;
    while(true){
      if (--_tx>=0 && ++_ty<_boardSize){
        if (_board[_tx][_ty] == '-'){
          _return.add(new Pair(_tx,_ty));
          continue;}}
      break;}
    
    return _return;
  }
  
  public static void main(String[] argv)
  {
    Board b = new Board();
    b.printBoard();
    b.markShot(0,3);
    b.printBoard();
    
    
    //Demo for getting legal moves
    java.util.List<Pair> l = b.legalMoves(0,3);

    java.util.Iterator<Pair> itr = l.iterator();
    while(itr.hasNext())
    {
      Pair p = itr.next();
      System.out.print(p.x + " " + p.y + "\n");
    }
    
  }
}
