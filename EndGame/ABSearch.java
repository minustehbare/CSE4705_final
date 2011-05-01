package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ABSearch{

  //entry function.  Start an ABSearch here
  public ClientMove ABStart(Partition _state, boolean _isBlack){
    //is there a java thing for infinity?
    if(_isBlack)
      return (MaxValue(_state, new ClientMove(0,0,0,-100), new ClientMove(0,0,0,100)));
    return   (MinValue(_state, new ClientMove(0,0,0,-100), new ClientMove(0,0,0,100)));
  }

  private ClientMove MaxValue(Partition _state, ClientMove a, ClientMove b){
    _state.print();
    //Get the index of the black queen
    List<Integer> _blackQueens = _state.getBlackQueens();
    Iterator<Integer> BQItr = _blackQueens.iterator();
    Integer BQindex = BQItr.next();

    //List of black's possible moves.
    List<ClientMove> _moves = _state.getPossibleMoves(BQindex);
    //We can't move!
    if (_moves.isEmpty()){
      System.out.println("We're stuck!");
      System.out.println(BQindex);
      System.out.flush();
      return null;
    }
    Partition _pw=null;
    Partition _pb=null;

    //This is where the magic happens

      for (ClientMove _move : _moves){
        System.out.println("Running max");
        //build a new state, pump it through MinValue, and grab the max of the returned move
        List<Partition> _lp = _state.forkMove(_move, true);
        //nasty bit of logic to check where the black and white queens ended up
        for (Partition _p3 : _lp){
          if (!_p3.getWhiteQueens().isEmpty())
            _pw=_p3;
          if (!_p3.getBlackQueens().isEmpty())
            _pb=_p3;
        }

        //Black and White queens are in the same partitions
        if(_pb==_pw){

          List<Integer> _whiteQueens = _pw.getWhiteQueens();
          Integer WQindex = _whiteQueens.get(0);

          //if the white queen can't move
          if(_pw.getReachableIndicies(WQindex).isEmpty())
            return (new ClientMove(_move.getFromIndex(),_move.getToIndex(),_move.getShootIndex(),99));

          ClientMove _newMove = MinValue(_pw, a, b);
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
          if(_pw.getReachableIndicies(WQindex).isEmpty()){
            return (new ClientMove(_move.getFromIndex(),_move.getToIndex(),_move.getShootIndex(),99));
          }
          ClientMove _newMove = MinValue(_pw, a, b);
          if(_newMove.getValue() > a.getValue())
            a = _newMove;
          if (b.getValue() <= a.getValue()) return a;
        }
      }
    return a;
  }

  //TODO: when MaxValue works, paste it here and fix it up real nice for MinValue
  private ClientMove MinValue(Partition _state, ClientMove a, ClientMove b){
    //Get the index of the white queen
    List<Integer> _whiteQueens = _state.getWhiteQueens();
    Iterator<Integer> WQItr = _whiteQueens.iterator();
    Integer WQindex = WQItr.next();

    //List of white's possible moves.
    List<ClientMove> _moves = _state.getPossibleMoves(WQindex);

    //We can't move!
    if (_moves.isEmpty())
      return null;

    Partition _pw=null;
    Partition _pb=null;
    Partition _temP1=null;
    Partition _temP2=null;

    //This is where the magic happens
    for (ClientMove _move : _moves){
        System.out.println("Running min");
        //build a new state, pump it through MaxValue, and grab the max of the returned move
        List<Partition> _lp = _state.forkMove(_move, false);

        for (Partition _p3 : _lp){
          if (!_p3.getWhiteQueens().isEmpty())
            _pw=_p3;
          if (!_p3.getBlackQueens().isEmpty())
            _pb=_p3;
        }

        //Black and White queens are in the same partitions
        if(_pb==_pw){
          //Get the index of the black queen
          List<Integer> _blackQueens = _pb.getBlackQueens();
          Integer BQindex = _blackQueens.get(0);

          //if the Black queen can't move
          if(_pw.getReachableIndicies(BQindex).isEmpty())
            return (new ClientMove(_move.getFromIndex(),_move.getToIndex(),_move.getShootIndex(),-99));

          ClientMove _newMove = MaxValue(_pb, a, b);
          if(_newMove.getValue() > a.getValue())
            b = _newMove;
          if (b.getValue() <= a.getValue()) return b;
        }
        else{
          //Get the index of the black queen
          List<Integer> _blackQueens = _pb.getBlackQueens();
          Integer BQindex = _blackQueens.get(0);

          //if the Black queen can't move
          if(_pw.getReachableIndicies(BQindex).isEmpty())
          //if the black queen can't move
          if(_pw.getReachableNodes(BQindex).size()==1)
            return (new ClientMove(_move.getFromIndex(),_move.getToIndex(),_move.getShootIndex(),-99));
          ClientMove _newMove = MaxValue(_pb, a, b);
          if(_newMove.getValue() > a.getValue())
            b = _newMove;
          if (b.getValue() <= a.getValue()) return b;
        }
      }
      return b;
    }

  public static void main(String[] args){
    //create a partition
    NodeSet _refSet = new NodeSet();
    Partition _p = _refSet.getRootPartition();

    List<Partition> _lp1 = _p.forkNode(5, NodeState.BLOCKED);
    Partition _p1 = _lp1.get(0);
    List<Partition> _lp2 = _p1.forkNode(4, NodeState.BLOCKED);
    Partition _p2 = _lp2.get(0);
    List<Partition> _lp3 = _p2.forkNode(24, NodeState.BLOCKED);
    Partition _p3 = _lp3.get(0);
    List<Partition> _lp4 = _p3.forkMove(new ClientMove(3,0,2,1,0,1),true);
    Partition _p4 = _lp4.get(0);
    List<Partition> _lp5 = _p4.forkMove(new ClientMove(6,0,0,0,2,0),false);
    Partition _p5 = _lp5.get(0);
    List<Partition> _lp6 = _p5.forkNode(30, NodeState.BLOCKED);
    Partition _p6 = _lp6.get(0);
    List<Partition> _lp7 = _p6.forkNode(31, NodeState.BLOCKED);
    Partition _p7 = _lp7.get(0);
    List<Partition> _lp8 = _p7.forkNode(22, NodeState.BLOCKED);
    Partition _p8 = _lp8.get(0);
    List<Partition> _lp9 = _p8.forkNode(32, NodeState.BLOCKED);
    Partition _p9 = _lp9.get(0);
    List<Partition> _lp10 = _p9.forkNode(3, NodeState.BLOCKED);
    Partition _p10 = _lp10.get(0);
    List<Partition> _lp11 = _p10.forkNode(13, NodeState.BLOCKED);
    Partition _p11 = _lp11.get(0);
    List<Partition> _lp12 = _p11.forkNode(23, NodeState.BLOCKED);
    Partition _p12 = _lp12.get(0);
    System.out.println(_p12.getReachableIndicies(21));
    ABSearch _a = new ABSearch();
    ClientMove _move = _a.ABStart(_p12,true);
    System.out.println(""+_move.getFromIndex()+"|"+_move.getToIndex()+"|"+_move.getShootIndex());

    _p12.print();
    //EndPartition _i = new EndPartition(_p12,true);
    //List<ClientMove> i = _i.getMove();
    //for (ClientMove j : i){
    //  System.out.println(""+j.getFromIndex()+j.getToIndex()+j.getShootIndex());
    //}

    //Set<Integer> _blackQueens = _p6.getWhiteQueens();
    //for(Integer B : _blackQueens){
    //  System.out.println("Queen is on space "+B);
    //}

  }
}
