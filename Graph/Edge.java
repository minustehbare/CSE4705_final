public class Edge {      
  Vertex v, x;   // two vertices incident to this edge       
  
  // Create an edge with given vertices
  Edge(Vertex a, Vertex b) {
     v = a;  x = b;
  }
  // Return the string repesent'n of the edge in form of a pair of vertices
  String getEdge() {
     return "("+v.id+","+x.id+")";
  }
  // Check if two edges are the same by comparing two vertices in edges
  boolean equal(Vertex a, Vertex b) {
     return ((v.id == a.id) && (x.id == b.id));
  }      
}
