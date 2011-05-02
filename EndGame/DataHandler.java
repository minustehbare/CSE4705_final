/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.NodeSet;
import CSE4705_final.State.NodeState;
import CSE4705_final.State.Partition;
import CSE4705_final.State.PartitionSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

/**
 *
 * @author steve
 */
public class DataHandler {
  Writer _out;
  BufferedReader _in;
  String _input;
  String _path = "/home/steve/Projects/AI/CSE4705_final/EndGame/Repo/";
  FileWriter _out1;

  public void printMove(PartitionSet _p, ClientMove _m) throws FileNotFoundException, IOException{
    System.out.println(_path + _p.getNamePrefix());
    _out1 = new FileWriter(_path + _p.getNamePrefix(),true);
    _out = new BufferedWriter(_out1);
    System.out.println(""+ _p.getNameSuffix() + "," + _m.getFromIndex() + "," + _m.getToIndex() + "," + _m.getShootIndex());
    _out.write(""+ _p.getNameSuffix() + "," + _m.getFromIndex() + "," + _m.getToIndex() + "," + _m.getShootIndex()+ "\n");
    _out.close();
  }

  public ClientMove getMove(PartitionSet _p) throws IOException{
    _in = new BufferedReader(new FileReader(_path + _p.getNamePrefix()));
    while((_input = _in.readLine()) != null){
      if (_input.startsWith(_p.getNameSuffix())){
        StringTokenizer _t = new StringTokenizer(_input,",");
        _t.nextToken();
        return new ClientMove(Integer.parseInt(_t.nextToken()),Integer.parseInt(_t.nextToken()),Integer.parseInt(_t.nextToken()),0);
      }
    }
    return null;
  }
  public static void main(String[] args) throws FileNotFoundException, IOException {
    //run some tests!
 /*   NodeSet init = NodeSet.BLOCKED_NODE_SET;
    int ng = init.forkNode(0, 0, NodeState.WHITE);
    ng = init.forkNode(1, ng, NodeState.BLACK);
    ng = init.forkNode(10, ng, NodeState.EMPTY);
    //ng = init.forkNode(13, ng, NodeState.EMPTY);
    NodeSet isolated = init.isolateGen(ng);
    Partition rootPart = isolated.getRootPartition();
    PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
    DataHandler _file = new DataHandler();
    ClientMove _move = _file.getMove(testPartSet);
    System.out.println(""+_move.getFromIndex()+","+_move.getToIndex()+","+_move.getShootIndex());
 */
    ABSearch _ab = new ABSearch();
    DataHandler _data = new DataHandler();

    //3
    for(int i=0; i<=8; i++){
      for(int j=i+1; j<7; j++){
        for(int k=j+1; k<6; k++){
          for(int q1=0; q1<=8; q1++){
            for(int q2=0; q2<=8; q2++){
              if(q1!=q2){
                NodeSet init = NodeSet.BLOCKED_NODE_SET;
                int ng = init.forkNode((i+7*(i/3)), 0, NodeState.EMPTY);
                ng = init.forkNode(j+7*(j/3), ng, NodeState.EMPTY);
                ng = init.forkNode(k+7*(k/3), ng, NodeState.EMPTY);
                //ng = init.forkNode(l+7*(l/3), ng, NodeState.EMPTY);
                ng = init.forkNode(q1+7*(q1/3), ng, NodeState.WHITE);
                ng = init.forkNode(q2+7*(q1/3), ng, NodeState.BLACK);
                NodeSet isolated = init.isolateGen(ng);
                Partition rootPart = isolated.getRootPartition();
                PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
                ClientMove _move = _ab.ABStart(testPartSet, true);
                if(_move.getFromIndex()!=_move.getToIndex()){
                  _data.printMove(testPartSet, _move);
                }
              }
            }
          }
        }
      }
    }

    //4
    for(int i=0; i<=15; i++){
      for(int j=i+1; j<14; j++){
        for(int k=j+1; k<13; k++){
          for(int l=k+1; l<12; l++){
            for(int q1=0; q1<=15; q1++){
              for(int q2=0; q2<=15; q2++){
                if(q1!=q2){
                  NodeSet init = NodeSet.BLOCKED_NODE_SET;
                  int ng = init.forkNode((i+6*(i/4)), 0, NodeState.EMPTY);
                  ng = init.forkNode(j+6*(j/4), ng, NodeState.EMPTY);
                  ng = init.forkNode(k+6*(k/4), ng, NodeState.EMPTY);
                  ng = init.forkNode(l+6*(l/4), ng, NodeState.EMPTY);
                  ng = init.forkNode(q1+6*(q1/4), ng, NodeState.WHITE);
                  ng = init.forkNode(q2+6*(q1/4), ng, NodeState.BLACK);
                  NodeSet isolated = init.isolateGen(ng);
                  Partition rootPart = isolated.getRootPartition();
                  PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
                  ClientMove _move = _ab.ABStart(testPartSet, true);
                  if(_move.getFromIndex()!=_move.getToIndex()){
                    _data.printMove(testPartSet, _move);
                  }
                }
              }
            }
          }
        }
      }
    }
    //5
    for(int i=0; i<=24; i++){
      for(int j=i+1; j<23; j++){
        for(int k=j+1; k<22; k++){
          for(int l=k+1; l<21; l++){
            for(int m=l+1; m<20; m++){
              for(int q1=0; q1<=24; q1++){
                for(int q2=0; q2<=24; q2++){
                  if(q1!=q2){
                    NodeSet init = NodeSet.BLOCKED_NODE_SET;
                    int ng = init.forkNode((i+5*(i/5)), 0, NodeState.EMPTY);
                    ng = init.forkNode(j+5*(j/5), ng, NodeState.EMPTY);
                    ng = init.forkNode(k+5*(k/5), ng, NodeState.EMPTY);
                    ng = init.forkNode(l+5*(l/5), ng, NodeState.EMPTY);
                    ng = init.forkNode(m+5*(l/5), ng, NodeState.EMPTY);
                    ng = init.forkNode(q1+5*(q1/5), ng, NodeState.WHITE);
                    ng = init.forkNode(q2+5*(q1/5), ng, NodeState.BLACK);
                    NodeSet isolated = init.isolateGen(ng);
                    Partition rootPart = isolated.getRootPartition();
                    PartitionSet testPartSet = new PartitionSet(rootPart.forceSplitCheck());
                    ClientMove _move = _ab.ABStart(testPartSet, true);
                    if(_move.getFromIndex()!=_move.getToIndex()){
                      _data.printMove(testPartSet, _move);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}