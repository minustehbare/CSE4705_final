package CSE4705_final.EndGame;

import CSE4705_final.Graph.Vertex;

public class Move {
  Vertex _move, _shot;
  int _value;
  public Move(Vertex m, Vertex s, int v)
  {
    _move = m;
    _shot = s;
    _value= v;
  }

  public Vertex getMove(){return _move;}
  public Vertex getShot(){return _shot;}
  public int getValue(){return _value;}
}
