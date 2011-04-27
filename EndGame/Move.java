package CSE4705_final.EndGame;

public class Move {
  int _from, _to, _shot;
  int _value;
  public Move(int f,int t,int s, int v)
  {
    _from = f;
    _to = t;
    _shot = s;
    _value= v;
  }

  public int getFrom(){return _from;}
  public int getTo(){return _to;}
  public int getShot(){return _shot;}
  public int getValue(){return _value;}
}
