
import java.io.*;
import java.util.*;


/**
 * Class 'ArticPointDFS' finds articulation points and biconnected components
 * in a graph. This class has three inner classes - Vertex, Edge, BCC - which
 * are used as data structures for a graph.
 * @author Hyung-Joon Kim
 */
public class ArticPointDFS {
   
   private static Vertex[] vArray; // an array which represents a graph
   private static int numVertices; // total number of vertices in a graph
   private static int numEdges;  // total number of edges in a graph
   private static int numBCC;  // number of biconnected components in a graph
   private static int dfsCounter = 1;  // will be used to assign dfsNum to each vertex
   private ArrayList articPointList;
   private ArrayList bccList;   
   private Stack stackVisited;  // a stack in which all visited edges are stored during DFS
   private Edge _e;

         
   /**
    * Constructor: create a data structure of a graph for articulation point
    * search.
    * @param numV total number of vertices in a graph.
    */   
   public ArticPointDFS(int numV) {
      Integer n = new Integer(numV);
      numVertices = n.intValue();      
      // Create an array to set up all vertices in a graph
      vArray = new Vertex[numVertices];
      for(int i=0; i < numVertices; i++) {
         vArray[i] = new Vertex(i);
      } 
      articPointList = new ArrayList();
      bccList = new ArrayList();
      stackVisited = new Stack();
      }
   
    public void runArticPoint(java.util.LinkedList<Edge> _edges){
      //add edges
      while(_edges.size()!=0){
        _e = _edges.removeFirst();
        this.addEdge((_e.v).id ,(_e.x).id);
      }
      // Determine the starting vertex for search
      Vertex startVertex = vArray[0];  
      
      //run it
      for (int j=0; j <100; j++) {
         ArticPointDFS.dfsCounter = 1;
         ArticPointDFS.numBCC = 0;
         this.articPointList = new ArrayList();
         this.bccList = new ArrayList();
         this.stackVisited = new Stack();
         for (int i=0; i<numVertices; i++) {
            Vertex v = vArray[i];
            v.dfslevel = 0;
            v.dfsnum = -1;
            v.low = 0;
            v.numChildren = 0;
         }
         this.doArticPointDFS(startVertex);
      }
      
      //print results
      this.showResult();
    }
   
   /**
    * Add an edge to the adjacent edge list of vertices while reading pairs
    * from the input. 
    * @param v one vertex incident to the edge
    * @param x the other vertex incident to the edge
    */
   public void addEdge(int v, int x) {      
      Integer idxV1 = new Integer(v);
      Integer idxV2 = new Integer(x);      
      Vertex v1 = vArray[idxV1.intValue()];
      Vertex v2 = vArray[idxV2.intValue()];      
      // Add the edge to the adjacent edge list of both vertices
      v1.eList.add(idxV2);
      v2.eList.add(idxV1);
      numEdges++;       
   }
   
   /**
    * Using recursive DFS, find articulation points and biconnected components
    * in a graph. While doing DFS, all information needed to find artic. point
    * and BCC. will be stored and updated. And also, as soon as an artic. point
    * is found, it will build a list of all edges in each BCC rooted at the
    * artic. point by using a stack of global scope.
    * @param v the vertex at which DFS will be performed.
    */
   public void doArticPointDFS(Vertex v) {
      
      // Set DFS info of vertex 'v'      
      v.dfsnum = dfsCounter++;
      v.low = v.dfsnum;
      
      ///////////////////////////////////////////////////////////////////////
      // The followings are only for eye-debugging purpose
      //
      // Create a sorted set of neighbors 'x' of vertex 'v' so that DFS can
      // visit edge (v,x) in order. 
      //SortedSet neighbors = new TreeSet();
      //Iterator eIter = v.eList.iterator();
      //while(eIter.hasNext()) {
      //   neighbors.add(eIter.next());  // guaranteed log(n) time by Java Lib. 
      //}
      //Iterator xIter = neighbors.iterator();      
      ///////////////////////////////////////////////////////////////////////
      
      Iterator xIter = v.eList.iterator();
      
      while(xIter.hasNext()) {
         Integer idxX = (Integer)xIter.next();
         Vertex x = vArray[idxX.intValue()];         
         if (x.dfsnum == -1) {  // x is undiscovered
            x.dfslevel = v.dfslevel + 1;
            v.numChildren = v.numChildren + 1;            
            stackVisited.push(new Edge(v, x));  // add this edge to the stack        
            
            doArticPointDFS(x);  // recursively perform DFS at children nodes
            
            v.low = Math.min(v.low, x.low);
            
            if (v.dfsnum == 1) {
               // Special Case for root
               // Root is an artic. point iff there are two or more children   
               if (v.numChildren >=2) {
                  if (!articPointList.contains(v)) {
                     articPointList.add(v);                      
                  }                                   
               }
               retrieveBCCEdges(v, x);
            }
            else{
               if (x.low >= v.dfsnum) {
                  // v is artic. point seperating x. That is, children of v
                  // cannot climb higher than v without passing through v.
                  if (!articPointList.contains(v)) {
                     articPointList.add(v);                      
                  } 
                  retrieveBCCEdges(v, x);
               }
            }
         }
         else if (x.dfslevel < v.dfslevel - 1) {   
            // x is at a lower level than the level of v's parent.
            // equiv. (v,x) is a back edge
            v.low = Math.min(v.low, x.dfsnum);           
            stackVisited.push(new Edge(v, x)); // add the back edge to the stack        
         }         
      }     
   }
   
   /**
    * Retrieve all edges in a biconnected component. Since DFS is a recursive
    * algorithm, using a global stack, all edges in a bicconected component
    * can be traced back whenever each articulation point is found. If v is
    * an artic. point, all edges of the subtree rooted at v can be retrieved
    * from the top of the stack down to (v,x) where (v,x) is the last edge
    * incident to the artic. point.
    * @param v articulation point
    * @param x child node of the articulation point
    */
   public void retrieveBCCEdges(Vertex v, Vertex x) {      
      // Whenever this method is called, a new biconected component must be
      // found. So, number of BCC. increment by 1 and create a new instance of
      // BCC.
      numBCC++;
      BCC bcc = new BCC(numBCC);     
      
      Edge top = (Edge)stackVisited.peek();      
      // Until the top of the stack is (v,x), trace back and build a list of
      // all edges in each BCC. 
      while (!top.equal(v, x)) {
         Edge e = (Edge)stackVisited.pop();
         bcc.edgeList.add(e);
         top = (Edge)stackVisited.peek();         
      }      
      Edge e = (Edge)stackVisited.pop();      
      bcc.edgeList.add(e);      
      bccList.add(bcc);  // add the BCC to a list
   }
   
   /**
    * Show all the results of DFS.
    */
   public void showResult() {
      System.out.println("       Total number of vertices : "+numVertices);
      System.out.println("          Total number of edges : "+numEdges);      
      System.out.println("  Number of articulation points : "+articPointList.size());
      System.out.print("    List of articulation points : ");  
      if (articPointList.isEmpty()) {
         System.out.print("No articulation point in the graph.");
      }
      else {
         Iterator iter1 = articPointList.iterator();
         while (iter1.hasNext()) {
            Vertex v = (Vertex)iter1.next();
            System.out.print(v.id + " ");
         }
      }           
      System.out.println("\nNumber of biconnected component : "+numBCC);
      System.out.print("Edges in each biconnected component : ");
      if (articPointList.isEmpty()) {
         System.out.print("The graph is biconnected since no articulation point exists.");                  
      }
      if (numEdges > 100) {
         System.out.print("\n\n   - Display disabled due to its large number.");
      }
      else {
         for (int i=0; i<bccList.size(); i++) {
            BCC bcc = (BCC)bccList.get(i);
            System.out.print("\n  Component "+bcc.id+" : ");
            Iterator eIter = bcc.edgeList.iterator();
            while (eIter.hasNext()) {
               Edge e = (Edge) eIter.next();
               System.out.print(e.getEdge()+" ");
            }  
         }
      }      
      System.out.println();
      System.out.println("Statistics :  ");
      for (int i=0; i<vArray.length; i++) {
         Vertex v = vArray[i];
         System.out.print("vertex: "+v.id+ ", dfs#: "+v.dfsnum+", Low: "+
               v.low+", dfsLevel: "+v.dfslevel+", #children: "+v.numChildren);
         System.out.println();
      }      
   }
      
   //////////////////////////////////////////////////////////////////////////
   // Inner classes - Vertex, Edge, BCC - are data structures for a graph. 
   //////////////////////////////////////////////////////////////////////////   
   
   class BCC {      
      int id;
      LinkedList edgeList;  // list of edges in a biconnected component
      
      // Create a bicconected component with given ID number
      BCC(int n) {
         id = n;
         edgeList = new LinkedList();
      }
   }
   //////////////////////////////////////////////////////////////////////////
   //                      End of Inner class definitions                  
   //////////////////////////////////////////////////////////////////////////
 

   /**
    * Top-level function which creates an instance of 'ArtcPointDFS' class and
    * invokes its methods to find articulation points and biconnected component
    * in graphs.
    * @param args strings of graph representation - total number of vertices
    *             followed by pairs of vertices which indicate edges.
    * @throws IOException
    */    
   public static void main(String[] args) throws IOException {      
      System.out.println(" Enter a positive integer 'n' followed by " +
      		"some number of pairs of integers\n in the range 0 to n-1." +
      		" 'N' represents the number of vertices in the graph,\n and each " +
      		"pair u v represents an edge between vertices u and v."+
      		"\n\n Input Note : The first line MUST contain ONLY the number of vertices."+
      		" Then,\n\t      a pair of vertices seperated by space can follow from "+
      		"the\n\t      second line. And, ONLY ONE of any duplicate edges such"+
      		" as\n\t      (u,v) and (v,u) should be entered.");
      System.out.println(" Input Example : A graph of 5 vertices and e(0,1), "+
            "e(0,2), e(0,4),...,e(3,4)\n\t\t should have an input as shown below.");
      System.out.println("\t\t 5");     System.out.println("\t\t 0 1");
      System.out.println("\t\t 0 2");   System.out.println("\t\t 0 4");
      System.out.println("\t\t 2 3");   System.out.println("\t\t 2 4");
      System.out.println("\t\t 3 4"); 
      System.out.print("\nEnter a string representaion of a graph.\n" + "=> ");      
      
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String firstLine = br.readLine();  // 1st line contains only # of vertices
      StringTokenizer token = new StringTokenizer(firstLine);      
      String numNodes = token.nextToken();
      
      // Create an instance with given number of nodes in a graph
      //ArticPointDFS dfs = new ArticPointDFS(numNodes);
      
      // Read all edges in the form of pairs of vertices
      String pair = br.readLine();
      token = new StringTokenizer(pair);
      while (token.hasMoreTokens()) {         
         if (token.countTokens() == 0) {
            break;  // no more input pair
         }
         else {
            String v = token.nextToken();
            String x = token.nextToken();            
            //dfs.addEdge(v, x);  // add edge to a graph
            
            if (!token.hasMoreTokens()) {  // no more pair in this line
               pair = br.readLine();  // read a next line
               token = new StringTokenizer(pair);
            }
         }
      }
      // Determine the starting vertex for search
      Vertex startVertex = vArray[0];  
      
      // Stamp the starting time of the algorithm.
      long time1 = System.currentTimeMillis();
      
      //dfs.doArticPointDFS(startVertex);  // Perform aritc. point search 
      // For timing the algorithm, perform DFS 100 times
      /*for (int j=0; j <100; j++) {
         ArticPointDFS.dfsCounter = 1;
         ArticPointDFS.numBCC = 0;
         dfs.articPointList = new ArrayList();
         dfs.bccList = new ArrayList();
         dfs.stackVisited = new Stack();
         for (int i=0; i<numVertices; i++) {
            Vertex v = vArray[i];
            v.dfslevel = 0;
            v.dfsnum = -1;
            v.low = 0;
            v.numChildren = 0;
         }
         dfs.doArticPointDFS(startVertex);
      }*/
      
      // Stamp the ending time of the algorithm.
      long time2 = System.currentTimeMillis();
      // Determine running time of DFS
      long elapse = time2 - time1;
      
      System.out.println("\n  Running Time of the algorithm : "+(long)elapse+" ms.");
      //dfs.showResult();  // show results    
   }
}
   
   
