/*
 * This class represents a partition with only our queens left in it.
 * Asking it for a move should return one move towards a most efficient
 * filling up of the board.
 */

package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.NodeSet;
import CSE4705_final.State.NodeState;
import CSE4705_final.State.Partition;
import CSE4705_final.State.PartitionSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author steve
 */
public class EndPartition {
  Partition _partition;
  int _final;
  List<Integer> _queens;
  List _returnList;


  //Constructor
  public EndPartition(Partition _p)
  {
    _partition = _p;
    _final = _p.enclosedCount() - _p.getBlackQueens().size() - _p.getWhiteQueens().size();
  }

  public List<ClientMove> getMoves(){
    List<ClientMove> _tempList = new LinkedList<ClientMove>();
    _returnList = new LinkedList<ClientMove>();
    return dfs(_tempList, _partition);
  }

  private List<ClientMove> dfs(List<ClientMove> _tempList, Partition _p){
    List<ClientMove> _tList = new LinkedList<ClientMove>();
    _queens = _p.getWhiteQueens();

    _tList = new LinkedList(_tempList);
    //for each queen for each move...
    for(Integer _from : _queens)
    {
      for(ClientMove _ret : _p.getPossibleMoves(_from)){
        _tempList = new LinkedList<ClientMove>(_tList);
        _tempList.add(_ret);

        if(_tempList.size() > _returnList.size())
          _returnList = new LinkedList(_tempList);
        if (_tempList.size() == _final){
          System.out.println("FINAL!"+_final);
          _returnList = new LinkedList(_tempList);
          return _tempList;
        }

        //Make the move and get all sorts of recursive
        List<Partition> _forkMove = _p.forkMove(_ret, false);
        for(Partition _pr : _forkMove)
        {
          if (!_pr.getWhiteQueens().isEmpty()){
            //if the queen is trapped and the depth is deep, set the return list
            if (_pr.enclosedCount()==1){
              if (_tempList.size() == _final){
                System.out.println("FINAL!"+_final);
                _returnList = new LinkedList(_tempList);
                return _tempList;
              }
              if (_tempList.size() > _returnList.size()){
                _returnList = new LinkedList(_tempList);
                continue;
              }
              continue;
            }

            //queen is free to continue
            System.out.println("Recursion!");
            _tempList = new LinkedList<ClientMove>(_tList);
            _tempList.add(_ret);
            _tempList = dfs(_tempList,_pr);
            if(_tempList.size() > _returnList.size())
              _returnList = new LinkedList(_tempList);
          }
        }
      }
    }
    _tempList = new LinkedList(_returnList);
    return _tempList;
  }
}