package CSE4705_final.EndGame;

import CSE4705_final.State.Node;

public class Move {
  Node _move, _shot;
  int _value;
  public Move(Node m, Node s, int v)
  {
    _move = m;
    _shot = s;
    _value= v;
  }

  public Node getMove(){return _move;}
  public Node getShot(){return _shot;}
  public int getValue(){return _value;}
}
