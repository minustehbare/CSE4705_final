/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.EndGame;

import CSE4705_final.State.Partition;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

  public void printMove(Partition _p, Move _m) throws FileNotFoundException, IOException{
    _out = new OutputStreamWriter(new FileOutputStream(_p.getNamePrefix()));
    _out.write(""+ _p.getNameSuffix() + "," + _m.getFrom() + "," + _m.getTo() + "," + _m.getShot());
  }

  public Move getMove(Partition _p) throws IOException{
    _in = new BufferedReader(new FileReader(_p.getNamePrefix()));
    while((_input = _in.readLine()) != null){
      if (_input.startsWith(_p.getNameSuffix())){
        StringTokenizer _t = new StringTokenizer(_input,",");
        return new Move(Integer.parseInt(_t.nextToken()),Integer.parseInt(_t.nextToken()),Integer.parseInt(_t.nextToken()),0);
      }
    }
    return null;
  }
  public static void main(String[] args) {
    //run some tests!
  }
}
