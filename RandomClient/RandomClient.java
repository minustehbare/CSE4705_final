package CSE4705_final.RandomClient;

import java.io.*;
import java.net.*;

public class RandomClient {
    private final static String _user = "5";
    private final static String _password = "733167";
    private final static String _opponent = "0";
    private final String _machine  = "icarus.engr.uconn.edu";
    private int port = 3499;
    private Socket _socket = null;
    private PrintWriter _out = null;
    private BufferedReader _in = null;

    private String _gameID;
    private String _myColor;
  
    private void white(RandomClient myClient, String readMessage, Board _board, Pair[] _WQueen){
      _WQueen[0] = new Pair(0,6);
      _WQueen[1] = new Pair(3,9);
      _WQueen[2] = new Pair(6,9);
      _WQueen[3] = new Pair(9,6);
      while(readMessage != null)
        {
         //Pick randomly one of the four queens to move
          int q;
          java.util.Random rand = new java.util.Random();
          q =  java.lang.Math.abs(rand.nextInt())%4;
          java.util.List<Pair> _moves;
          _moves = _board.legalMoves(_WQueen[q].x,_WQueen[q].y);
          for(int i=0;i<4;i++)
          {
            //Pick randomly a move to select
            _moves = _board.legalMoves(_WQueen[q].x,_WQueen[q].y);
            if(_moves.size()!=0)break;
            q =(q+1)%4;
          }
          int _moveNumber =  java.lang.Math.abs(rand.nextInt())%_moves.size();
          Pair _move = _moves.get(_moveNumber);
          _board.move(_WQueen[q].x,_WQueen[q].y,_move.x,_move.y);
          //Pick randomly an arrow shot
          java.util.List<Pair> _shots = _board.legalMoves(_move.x,_move.y);
          int _shotNumber =  java.lang.Math.abs(rand.nextInt())%_shots.size();
          Pair _shot = _shots.get(_shotNumber);
          _board.markShot(_shot.x,_shot.y);
          _board.printBoard();
          try{
	          myClient.writeMessageAndEcho("("+_WQueen[q].y+":"+_WQueen[q].x+"):("+_move.y+":"+_move.x+"):("+_shot.y+":"+_shot.x+")");
	          _WQueen[q].x=_move.x;
           _WQueen[q].y=_move.y;  
	          readMessage = myClient.readAndEcho();
	          readMessage = myClient.readAndEcho();
            //mark his move
	          char from_y=readMessage.charAt(12);
	          char from_x=readMessage.charAt(14);
	          char to_y=readMessage.charAt(18);
	          char to_x=readMessage.charAt(20);
	          char sht_y=readMessage.charAt(24);
	          char sht_x=readMessage.charAt(26);
	          _board.move((int) from_x - 48,(int) from_y - 48 ,(int) to_x - 48,(int) to_y- 48);
	          _board.markShot((int) sht_x - 48,(int) sht_y - 48);
	          readMessage = myClient.readAndEcho();  // move query
          }catch  (IOException e) {
	          System.out.println("Failed in read/close");
	          System.exit(1);
          }
        }
      }
    
    
    private void black(RandomClient myClient, String readMessage,Board _board, Pair[] _BQueen){
      _BQueen[0] = new Pair(0,3);
      _BQueen[1] = new Pair(3,0);
      _BQueen[2] = new Pair(6,0);
      _BQueen[3] = new Pair(9,3);

	    //mark his move
	          char from_y=readMessage.charAt(12);
	          char from_x=readMessage.charAt(14);
	          char to_y=readMessage.charAt(18);
	          char to_x=readMessage.charAt(20);
	          char sht_y=readMessage.charAt(24);
	          char sht_x=readMessage.charAt(26);
	          _board.move((int) from_x - 48,(int) from_y - 48 ,(int) to_x - 48,(int) to_y- 48);
	          _board.markShot((int) sht_x - 48,(int) sht_y - 48);
      try{
      readMessage = myClient.readAndEcho();
      }catch  (IOException e) {
	          System.out.println("Failed in read/close");
	          System.exit(1);
          }
      while(readMessage != null)
        {
         //Pick randomly one of the four queens to move
          int q;
          java.util.Random rand = new java.util.Random();
          q =  java.lang.Math.abs(rand.nextInt())%4;
          java.util.List<Pair> _moves;
          _moves = _board.legalMoves(_BQueen[q].x,_BQueen[q].y);	
          for(int i=0;i<4;i++)
          {
            //Pick randomly a move to select
            _moves = _board.legalMoves(_BQueen[q].x,_BQueen[q].y);
            if(_moves.size()!=0)break;
            q =(q+1)%4;
          }
          int _moveNumber =  java.lang.Math.abs(rand.nextInt())%_moves.size();
          Pair _move = _moves.get(_moveNumber);
          _board.move(_BQueen[q].x,_BQueen[q].y,_move.x,_move.y);
          //Pick randomly an arrow shot
          java.util.List<Pair> _shots = _board.legalMoves(_move.x,_move.y);
          int _shotNumber =  java.lang.Math.abs(rand.nextInt())%_shots.size();
          Pair _shot = _shots.get(_shotNumber);
          _board.markShot(_shot.x,_shot.y);
          _board.printBoard();
          try{
	          myClient.writeMessageAndEcho("("+_BQueen[q].y+":"+_BQueen[q].x+"):("+_move.y+":"+_move.x+"):("+_shot.y+":"+_shot.x	+")");
	         _BQueen[q].x=_move.x;
           _BQueen[q].y=_move.y;
	          readMessage = myClient.readAndEcho();
	          readMessage = myClient.readAndEcho();
	          //mark his move
	          from_y=readMessage.charAt(12);
	          from_x=readMessage.charAt(14);
	          to_y=readMessage.charAt(18);
	          to_x=readMessage.charAt(20);
	          sht_y=readMessage.charAt(24);
	          sht_x=readMessage.charAt(26);
	          _board.move((int) from_x - 48,(int) from_y - 48 ,(int) to_x - 48,(int) to_y- 48);
	          _board.markShot((int) sht_x - 48,(int) sht_y - 48);
	          readMessage = myClient.readAndEcho();  // move query
          }catch  (IOException e) {
	          System.out.println("Failed in read/close");
	          System.exit(1);
          }
        }
      }
    
    
  
    public RandomClient(){	
	_socket = openSocket();
    }

    public Socket getSocket(){
	return _socket;
    }

    public PrintWriter getOut(){
	return _out;
    }

    public BufferedReader getIn(){
	return _in;
    }
     
    public void setGameID(String id){
	_gameID = id;
    }
    
    public String getGameID() {
	return _gameID;
    }

    public void setColor(String color){
	_myColor = color;
    }
    
    public String getColor() {
	return _myColor;
    }

    public static void main(String[] argv){
	String readMessage;
	RandomClient myClient = new RandomClient();

	try{
	    myClient.readAndEcho(); // start message
	    myClient.readAndEcho(); // ID query
	    myClient.writeMessageAndEcho(_user); // user ID
	    
	    myClient.readAndEcho(); // password query 
	    myClient.writeMessage(_password);  // password

	    myClient.readAndEcho(); // opponent query
	    myClient.writeMessageAndEcho(_opponent);  // opponent

	    myClient.setGameID(myClient.readAndEcho()); // game 
	    myClient.setColor(myClient.readAndEcho().substring(6,11));  // color
	    System.out.println("I am playing as "+myClient.getColor()+ " in game number "+ myClient.getGameID());
	    readMessage = myClient.readAndEcho();  
	    // depends on color--a black move if i am white, Move:Black:i:j
	    // otherwise a query to move, ?Move(time):
	    
	    Board _board = new Board();
      Pair[] _Queen = new Pair[4]; 
	    
	    if (myClient.getColor().equals("White")) {
        myClient.white(myClient, readMessage,_board,_Queen);
	    }
	    else {
        myClient.black(myClient, readMessage,_board,_Queen);
	    }
	   
	    myClient.getSocket().close();
	} catch  (IOException e) {
	    System.out.println("Failed in read/close");
	    System.exit(1);
	}
    }

    public String readAndEcho() throws IOException
    {
	String readMessage = _in.readLine();
	System.out.println("read: "+readMessage);
	return readMessage;
    }

    public void writeMessage(String message) throws IOException
    {
	_out.print(message+"\r\n");  
	_out.flush();
    }
 
    public void writeMessageAndEcho(String message) throws IOException
    {
	_out.print(message+"\r\n");  
	_out.flush();
	System.out.println("sent: "+ message);
    }
			       
    public  Socket openSocket(){
	//Create socket connection, adapted from Sun example
	try{
       _socket = new Socket(_machine, port);
       _out = new PrintWriter(_socket.getOutputStream(), true);
       _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
     } catch (UnknownHostException e) {
       System.out.println("Unknown host: " + _machine);
       System.exit(1);
     } catch  (IOException e) {
       System.out.println("No I/O");
       System.exit(1);
     }
     return _socket;
  }
}
