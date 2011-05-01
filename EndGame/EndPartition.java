/*
 * This class represents a partition with only our queens left in it.
 * Asking it for a move should return one move towards a most efficient
 * filling up of the board.
 */

package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.Partition;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author steve
 */
public class EndPartition {
  Partition _partition;
  boolean _isBlack;
  int _final;
  List<Integer> _queens;
  List _returnList;


  //Constructor
  public EndPartition(Partition _p, boolean _Black)
  {
    _partition = _p;
    _isBlack = _Black;
    _final = _p.enclosedCount() - _p.getBlackQueens().size() - _p.getWhiteQueens().size();
  }

  public List<ClientMove> getMove(){
    List<ClientMove> _tempList = new LinkedList<ClientMove>();
    _returnList = new LinkedList<ClientMove>();
    return dfs(_tempList, _partition);
  }

  private List<ClientMove> dfs(List<ClientMove> _tempList, Partition _p){
    if(_isBlack)
      _queens = _p.getBlackQueens();
    else
      _queens = _p.getWhiteQueens();

    //for each queen for each move...
    for(Integer _from : _queens)
    {
      System.out.println(_p.getPossibleMoves(_from));
      for(ClientMove _ret : _p.getPossibleMoves(_from)){
        _tempList.add(_ret);

        if(_tempList.size() > _returnList.size())
          _returnList = new LinkedList(_tempList);
        if (_tempList.size() == _final){
          System.out.println("FINAL!"+_final);
          _returnList = new LinkedList(_tempList);
          return _tempList;
        }

        //Make the move and get all sorts of recursive
        List<Partition> _forkMove = _p.forkMove(_ret, _isBlack);
        for(Partition _pr : _forkMove)
        {
                      System.out.println("TEST");
          if (true){ //
            System.out.println("TEST");
            //if the queen is trapped and the depth deep, set the return list
            if (_pr.enclosedCount()==1){
              if (_tempList.size() > _returnList.size()){
                _returnList = new LinkedList(_tempList);
                continue;
              }
              continue;
            }

            //queen is free to continue
            System.out.println("Recursion!");
            _tempList = dfs(_tempList,_pr);
            if(_tempList.size() > _returnList.size())
              _returnList = new LinkedList(_tempList);
          }
        }
        if(!_tempList.isEmpty())
          ((LinkedList) _tempList).removeLast();
      }
    }
    _tempList = new LinkedList(_returnList);
    return _tempList;
  }
}
