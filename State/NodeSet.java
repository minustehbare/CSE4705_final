/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.State;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 *
 * @author Ethan
 */
public class NodeSet {
    private List<Map<Integer, NodeState>> _nodeMaps;
    private Map<Integer, GenTreeNode> _genMap;

    private ReentrantReadWriteLock[] _nodeLocks;
    private ReentrantReadWriteLock _genLock;

    private AtomicInteger _nextGen;

    public NodeSet() {
        _nodeMaps = new ArrayList<Map<Integer, NodeState>>(100);
        _genMap = new TreeMap<Integer, GenTreeNode>();
        _nodeLocks = new ReentrantReadWriteLock[100];
        _genLock = new ReentrantReadWriteLock();

        // instantiate all node locks.
        for (int i = 0; i <= 99; i++) {
            _nodeLocks[i] = new ReentrantReadWriteLock();
        }

        _nextGen = new AtomicInteger(1);

        initializeBoard();
    }

    private NodeSet(List<NodeState> init) {
        _nodeMaps = new ArrayList<Map<Integer, NodeState>>(100);
        _genMap = new TreeMap<Integer, GenTreeNode>();
        _nodeLocks = new ReentrantReadWriteLock[100];
        _genLock = new ReentrantReadWriteLock();

        // instantiate all node locks.
        for (int i = 0; i <= 99; i++) {
            _nodeLocks[i] = new ReentrantReadWriteLock();
        }

        _nextGen = new AtomicInteger(1);

        initializeBoard(init);
    }

    // Helper function.  The optimizer should inline this.
    private int getIndex(int row, int col) {
        return col + row*10;
    }

    private void genReadLock()    { _genLock.readLock().lock();    }
    private void genReadUnlock()  { _genLock.readLock().unlock();  }
    private void genWriteLock()   { _genLock.writeLock().lock();   }
    private void genWriteUnlock() { _genLock.writeLock().unlock(); }

    private void nodeReadLock(int index)    { _nodeLocks[index].readLock().lock();   }
    private void nodeReadUnlock(int index)  { _nodeLocks[index].readLock().unlock(); }
    private void nodeWriteLock(int index)   { _nodeLocks[index].writeLock().lock();  }
    private void nodeWriteUnlock(int index) { _nodeLocks[index].writeLock().lock();  }

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
            throw new StateException("Node at (" + row + ":" + col +")[" + generation + "] was not found.", e);
        } finally {
            genReadUnlock();
            nodeReadUnlock(index);
        }
    }

    public Node getNode(int row, int col, int generation) {
        return new Node(row, col, getNodeState(row, col, generation), generation);
    }

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
            throw new StateException("Could not get neighbors of (" + row + ":" + col + ")[" + generation + "].", e);
        }
    }

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

    public int forkNode(Node newNode) {
        return forkNode(newNode.getRow(), newNode.getCol(), newNode.getGen(), newNode.getState());
    }

    public NodeSet isolateGen(int generation) {
        // used to free memory.
        List<NodeState> nodes = new ArrayList<NodeState>();
        for (int i = 0; i <= 99; i++) {
            nodes.add(i, getNodeState(i % 10, i / 10, generation));
        }
        return new NodeSet(nodes);
    }

    private void initializeBoard() {
        _genMap.put(0, new GenTreeNode(0));
        for (int i = 0; i <= 99; i++) {
            Map<Integer, NodeState> nodeMap = new HashMap<Integer, NodeState>();
            if (i == getIndex(3,0) || i == getIndex(0,3) || i == getIndex(3,9) || i == getIndex(0,6)) {
                nodeMap.put(0, NodeState.BLACK);
            } else if (i == getIndex(6,0) || i == getIndex(9,3) || i == getIndex(9,6) || i == getIndex(6,9)) {
                nodeMap.put(0, NodeState.WHITE);
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

    private class GenTreeNode {
        private int _gen;
        private GenTreeNode _parent;

        public GenTreeNode(int generation) {
            // makes a root node
            _gen = generation;
            _parent = null;
        }

        public GenTreeNode(int generation, GenTreeNode parent) {
            _gen = generation;
            _parent = parent;
        }

        public boolean isRoot() {
            return _parent == null;
        }

        public int getGeneration() {
            return _gen;
        }

        public GenTreeNode getParent() {
            return _parent;
        }

        public Deque<Integer> getAllGenerations() {
            // IN FUTURE: Optimize by caching this.
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
