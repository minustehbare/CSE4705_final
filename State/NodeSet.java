package CSE4705_final.State;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 * <p>An efficient collection of board nodes.  This collection is efficient and
 * completely thread-safe.  Furthermore, it follows copy-on-write semantics,
 * so any changes that are made will not have any effect on existing objects.</p>
 *
 * <p>This collection is generation-based.  That is, this collection keeps track
 * of several versions at once.  These versions are referenced by a generation,
 * which is an integer.  When a change is made, the new generation number is
 * returned.  If you use an old generation number to query this collection, you
 * will receive the data associated with that generation.</p>
 *
 * <p>In a nutshell, you can imagine this as being a collection of boards.  For
 * each generation, there is a different set of nodes, which can be completely
 * different.  You can understand the forkNode method as creating an entirely
 * new board with the change you gave it.  Underneath, of course, this
 * collection is much more efficient, and only stores the change you made,
 * without duplicating any data.</p>
 *
 * @author Ethan Levine
 */
public class NodeSet {

    /**
     * A list of "node maps".  Each node map matches a generation to the value
     * of a node.  Note that each node map does not contain an entry for every
     * generation.
     */
    private List<Map<Integer, NodeState>> _nodeMaps;

    /**
     * A map that links a generation to that generation's node.  This node is
     * used to link a generation with parent generations.
     */
    private Map<Integer, GenTreeNode> _genMap;

    // Some read-write locks to make this thread-safe.
    private ReentrantReadWriteLock[] _nodeLocks;
    private ReentrantReadWriteLock _genLock;

    // A thread-safe counter to increment the generation number.
    private AtomicInteger _nextGen;

    /**
     * Initializes this NodeSet.  This involves creating new lists and maps,
     * creating locks, and creating the counter.
     */
    private void commonInitialization() {
        _nodeMaps = new ArrayList<Map<Integer, NodeState>>(100);
        _genMap = new TreeMap<Integer, GenTreeNode>();
        _nodeLocks = new ReentrantReadWriteLock[100];
        _genLock = new ReentrantReadWriteLock();

        // instantiate all node locks.
        for (int i = 0; i <= 99; i++) {
            _nodeLocks[i] = new ReentrantReadWriteLock();
        }

        _nextGen = new AtomicInteger(1);
    }

    /**
     * Creates a fresh NodeSet corresponding to the initial board.  The new
     * NodeSet will be set up with the initial configuration corresponding to
     * a new game.  You may wish to use NodeSet(boolean), which allows you to
     * switch colors.  Note that new NodeSets always initialize with generation
     * 0.
     */
    public NodeSet() {
        commonInitialization();
        initializeBoard(false);
    }

    /**
     * Creates a fresh NodeSet roughly corresponding to the initial board.  The
     * new NodeSet will be set up with the initial configuration corresponding to
     * a new game with one difference.  If the argument is "true", then the
     * BLACK and WHITE colors are reversed.  This is useful because the AI can
     * assume that it is always the same color.  Note that new NodeSets always
     * initialize with generation 0.
     *
     * @param switchColors whether or not to swap the BLACK and WHITE queens
     */
    public NodeSet(boolean switchColors) {
        commonInitialization();
        initializeBoard(true);
    }

    /**
     * Creates a new NodeSet from an existing generation.  This is used
     * internally to spawn new NodeSets.  Spawning a new NodeSet will discard
     * all other generations, and lets the garbage collector clean up the mess.
     * Note that new NodeSets always initialize with generation 0.
     *
     * @param init the initial configuration
     */
    private NodeSet(List<NodeState> init) {
        commonInitialization();
        initializeBoard(init);
    }

    // Helper function to convert row/column to an index.
    // The optimizer should inline this.
    private int getIndex(int row, int col) {
        return col + row*10;
    }

    // Helper functions to interact with the _genMap lock.
    private void genReadLock()    { _genLock.readLock().lock();    }
    private void genReadUnlock()  { _genLock.readLock().unlock();  }
    private void genWriteLock()   { _genLock.writeLock().lock();   }
    private void genWriteUnlock() { _genLock.writeLock().unlock(); }

    // Helper functions to interact with the _nodeMaps locks.
    private void nodeReadLock(int index)    { _nodeLocks[index].readLock().lock();    }
    private void nodeReadUnlock(int index)  { _nodeLocks[index].readLock().unlock();  }
    private void nodeWriteLock(int index)   { _nodeLocks[index].writeLock().lock();   }
    private void nodeWriteUnlock(int index) { _nodeLocks[index].writeLock().unlock(); }

    /**
     * Gets the state of a node.  The generation must be provided.  This method
     * must potentially search through the generation hierarchy, for a worst-
     * case runtime of O(d), where d is the depth of the generation tree.
     *
     * @param row the row of the node to query
     * @param col the column of the node to query
     * @param generation the generation of the node to query
     * @return the state of the queried node
     */
    public NodeState getNodeState(int row, int col, int generation) {
        // First, get the index.
        int index = getIndex(row, col);
        // Get the node map.
        Map<Integer, NodeState> nodeMap = _nodeMaps.get(index);
        // Get the most relevant node.
        try {
            genReadLock();
            GenTreeNode genNode = _genMap.get(generation);
            genReadUnlock();
            nodeReadLock(index);
            NodeState ret = genNode.fetchNode(nodeMap);
            nodeReadUnlock(index);
            return ret;
        } catch (StateException e) {
            throw new StateException("Node at (" +
                    row + ":" + col +")[" + generation + "] was not found.", e);
        } finally {
            genReadUnlock();
            nodeReadUnlock(index);
        }
    }

    /**
     * Gets a Node object.  This is similar to getting the state of a node,
     * except this state is wrapped inside a Node object with position
     * information.
     *
     * @param row the row of the node to query
     * @param col the column of the node to query
     * @param generation the generation of the node to query
     * @return the queried node
     */
    public Node getNode(int row, int col, int generation) {
        return new Node(row, col, getNodeState(row, col, generation), generation);
    }

    /**
     * Gets a list of neighbors to a node.  This is equivalent to making the
     * neighboring calls individually.
     *
     * @param row the row of the center to query
     * @param col the column of the center to query
     * @param generation the generation of the center to query
     * @return the nodes around the queried center
     */
    public List<Node> getNeighbors(int row, int col, int generation) {
        // Basically, try to get the neighbor at each row +- 1, col +-1.
        try {
            List<Node> retList = new LinkedList<Node>();
            if (row > 0) {
                retList.add(getNode(row - 1, col, generation));
            }
            if (row < 9) {
                retList.add(getNode(row + 1, col, generation));
            }
            if (col > 0) {
                retList.add(getNode(row, col - 1, generation));
            }
            if (col < 9) {
                retList.add(getNode(row, col + 1, generation));
            }
            return retList;
        } catch (StateException e) {
            throw new StateException("Could not get neighbors of (" +
                    row + ":" + col + ")[" + generation + "].", e);
        }
    }

    /**
     * Gets a list of neighbors to a node.  This is functionally equivalent to
     * getNeighbors(int,int,int), except it accepts a Node object as an argument.
     * 
     * @param node the node to query neighbors around
     * @return the neighbors of node
     */
    public List<Node> getNeighbors(Node node) {
        return getNeighbors(node.getRow(), node.getCol(), node.getGen());
    }

    /**
     * Forks a node.  This is used to update the NodeSet with new data.  This
     * creates a new generation with the change requested in the arguments.
     * Note that existing generations will not be changed.
     *
     * @param row the row of the node to change
     * @param col the column of the node to change
     * @param parentGen the old generation of the node to change
     * @param newState the new state of the node
     * @return a generation in which the change has been made
     */
    public int forkNode(int row, int col, int parentGen, NodeState newState) {
        int index = getIndex(row, col);
        try {
            int newGen = _nextGen.getAndIncrement();
            // make a new entry in the generation tree.
            genWriteLock();
            _genMap.put(newGen, new GenTreeNode(newGen, _genMap.get(parentGen)));
            genWriteUnlock();
            // make a new entry in the node map
            Map<Integer, NodeState> nodeMap = _nodeMaps.get(index);
            nodeWriteLock(index);
            nodeMap.put(newGen, newState);
            nodeWriteUnlock(index);
            return newGen;
        } finally {
            genWriteUnlock();
            nodeWriteUnlock(index);
        }
    }

    /**
     * Forks a node.  This is used to update the NodeSet with new data.  This
     * creates a new generation in which the requested change has been affected.
     * Note that existing generations will not be changed.
     *
     * @param newNode the new node data, including the old generation
     * @return a generation in which the change has been made
     */
    public int forkNode(Node newNode) {
        return forkNode(newNode.getRow(), newNode.getCol(), newNode.getGen(),
                newNode.getState());
    }

    /**
     * Isolates a generation in a new NodeSet.  This should be called after a
     * move has been made, to conserve memory.  It will get information from an
     * existing generation and create a new NodeSet in which that information
     * is generation 0.  The garbage collector will clear the old NodeSet object
     * when it needs to, and when all handles to it are dead.
     *
     * @param generation the generation to isolate
     * @return a new NodeSet with only the given generation
     */
    public NodeSet isolateGen(int generation) {
        // used to free memory.
        List<NodeState> nodes = new ArrayList<NodeState>();
        for (int i = 0; i <= 99; i++) {
            nodes.add(i, getNodeState(i % 10, i / 10, generation));
        }
        return new NodeSet(nodes);
    }

    /**
     * Initializes the board to the beginning of a game.  The BLACK and WHITE
     * positions are switched if the parameter is true.
     *
     * @param switchColors switches BLACK and WHITE queens if true, does nothing
     *                     otherwise
     */
    private void initializeBoard(boolean switchColors) {
        _genMap.put(0, new GenTreeNode(0));
        for (int i = 0; i <= 99; i++) {
            Map<Integer, NodeState> nodeMap = new HashMap<Integer, NodeState>();
            if (i == getIndex(3,0) ||
                i == getIndex(0,3) ||
                i == getIndex(3,9) ||
                i == getIndex(0,6)) {
                nodeMap.put(0, (switchColors ? NodeState.WHITE : NodeState.BLACK));
            } else if (i == getIndex(6,0) ||
                       i == getIndex(9,3) ||
                       i == getIndex(9,6) ||
                       i == getIndex(6,9)) {
                nodeMap.put(0, (switchColors ? NodeState.BLACK : NodeState.WHITE));
            } else {
                nodeMap.put(0, NodeState.EMPTY);
            }
            _nodeMaps.add(i, nodeMap);
        }
    }

    private void initializeBoard(List<NodeState> init) {
        _genMap.put(0, new GenTreeNode(0));
        for (int i = 0; i <= 99; i++) {
            Map<Integer, NodeState> nodeMap = new HashMap<Integer, NodeState>();
            nodeMap.put(0, init.get(i));
            _nodeMaps.add(i, nodeMap);
        }
    }

    /**
     * A class to implement the generation tree.  This tree is a lightweight
     * structure that keeps track of different generations.  By moving up the
     * tree, we can see all the relevant generations to a specific query.
     */
    private class GenTreeNode {
        // The payload of the node.
        private int _gen;

        // The parent of this node, or NULL if it is a root node.
        private GenTreeNode _parent;

        /**
         * Creates a new root tree node.  This node has no parent, and should
         * only be created when this NodeSet is instantiated.
         *
         * @param generation the initial generation, typically 0
         */
        public GenTreeNode(int generation) {
            // makes a root node
            _gen = generation;
            _parent = null;
        }

        /**
         * Creates a new tree node.  This node is linked with its parent.
         *
         * @param generation the generation of the new node
         * @param parent the parent of the new node
         */
        public GenTreeNode(int generation, GenTreeNode parent) {
            _gen = generation;
            _parent = parent;
        }

        /**
         * Determines if this node is a root node.
         *
         * @return true if this node is a root, false otherwise
         */
        public boolean isRoot() {
            return _parent == null;
        }

        /**
         * Gets the generation of this node.
         *
         * @return this node's generation
         */
        public int getGeneration() {
            return _gen;
        }

        /**
         * Gets the parent of this node.  If this is a root node, this will
         * return NULL.
         *
         * @return the parent of this node, or null
         */
        public GenTreeNode getParent() {
            return _parent;
        }

        /*
         * This method is no longer used.
        public Deque<Integer> getAllGenerations() {
            // IN FUTURE: Optimize by caching this (if it even gets used).
            if (isRoot()) {
                Deque<Integer> ret = new LinkedList<Integer>();
                ret.addFirst(_gen);
                return ret;
            } else {
                Deque<Integer> ret = _parent.getAllGenerations();
                ret.addFirst(_gen);
                return ret;
            }
        }
        */

        /**
         * Finds the most "recent" state of a node.  This recurses up the
         * generation tree until it finds a generation in which this node has
         * a definition.  Note that "recent" means the most recent generation
         * in this chain, not the absolute most recent generation.
         *
         * @param nodeMap the node map of the desired node
         * @return the most "recent" state of the desired node
         */
        public NodeState fetchNode(Map<Integer, NodeState> nodeMap) {
            // ASSUMES A READ LOCK IS TAKEN OUT ON THE ARGUMENT.
            if (nodeMap.containsKey(_gen)) {
                return nodeMap.get(_gen);
            } else {
                if (isRoot()) {
                    // Not found.
                    throw new StateException ("Requested node not found.");
                } else {
                    return _parent.fetchNode(nodeMap);
                }
            }
        }
    }
}
