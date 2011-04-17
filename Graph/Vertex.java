public class Vertex {     
  int id;
  int low;  // lowest tree level reachable from this vertex
  int dfsnum;
  int dfslevel;   // tree level of this vertex in DFS
  int numChildren;
  java.util.LinkedList eList;  // list of edges incident to this vertex

  public int x, y;  //x and y location on the board
  public boolean isMarked;
  
  // Create a vertex with given ID number
  Vertex(int x1, int _x, int _y) {         
    id = x1;
    dfsnum = -1;  // initially undiscovered
    eList = new java.util.LinkedList();  // create empty list for adjacency edges
    x=_x;
    y=_y;
    isMarked=false;
  }
  Vertex(int x1) {         
    id = x1;
    dfsnum = -1;  // initially undiscovered
    eList = new java.util.LinkedList();  // create empty list for adjacency edges
    x=0;
    y=0;
    isMarked=false;
  }     
}
