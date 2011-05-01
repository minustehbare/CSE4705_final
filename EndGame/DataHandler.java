/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.EndGame;

import CSE4705_final.Client.ClientMove;
import CSE4705_final.State.NodeSet;
import CSE4705_final.State.Partition;
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

  public void printMove(Partition _p, ClientMove _m) throws FileNotFoundException, IOException{
    System.out.println(_path + _p.getNamePrefix());
    _out1 = new FileWriter(_path + _p.getNamePrefix(),true);
    _out = new BufferedWriter(_out1);
    System.out.println(""+ _p.getNameSuffix() + "," + _m.getFromIndex() + "," + _m.getToIndex() + "," + _m.getShootIndex());
    _out.write(""+ _p.getNameSuffix() + "," + _m.getFromIndex() + "," + _m.getToIndex() + "," + _m.getShootIndex()+ "\n");
    _out.close();
  }

  public ClientMove getMove(Partition _p) throws IOException{
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
    NodeSet _nodeSet = new NodeSet();
    Partition _p = _nodeSet.getRootPartition();
    DataHandler _file = new DataHandler();
    _file.printMove(_p,new ClientMove(3,4,5,0));

    ClientMove _move = _file.getMove(_p);
    System.out.println(""+_move.getFromIndex()+_move.getToIndex()+_move.getShootIndex());
  }
}
