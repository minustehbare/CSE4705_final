package CSE4705_final.State;

import java.util.concurrent.atomic.*;
import java.util.*;

/**
 *
 * @author Ethan Levine
 */
public class Partition {

    private final NodeSet _refSet;
    private final SortedSet<Integer> _enclosedSet;
    private final int _rowOffset;
    private final int _colOffset;
    private final boolean _flipVertical;
    private final boolean _flipHorizontal;
    private final boolean _swapAxis;
    private final int _gen;
    private final boolean _direct;

    public Partition(NodeSet refSet, SortedSet<Integer> enclosedNodes, int gen) {
        _refSet = refSet;
//        _partID = _NextID.getAndIncrement();
        _enclosedSet = enclosedNodes;
        _gen = gen;
        _rowOffset = 0;
        _colOffset = 0;
        _flipVertical = false;
        _flipHorizontal = false;
        _swapAxis = false;
        _direct = true;
    }

    private Partition(NodeSet refSet, SortedSet<Integer> enclosedNodes, int gen,
            int ro, int co, boolean fv, boolean fh, boolean sa) {
        _refSet = refSet;
//        _partID = _NextID.getAndIncrement();
        _enclosedSet = enclosedNodes;
        _gen = gen;
        _rowOffset = ro;
        _colOffset = co;
        _flipVertical = fv;
        _flipHorizontal = fh;
        _swapAxis = sa;
        _direct = false;
    }

    private SortedSet<Integer> modifyEnclosed(int ro, int co, boolean fv, boolean fh, boolean sa) {
        SortedSet<Integer> newSet = new TreeSet<Integer>();
        for (int i : _enclosedSet) {
            int row = i / 10;
            int col = i % 10;
            row += ro;
            col += co;
            if (sa) { int t = row; row = col; col = t; }
            if (fh) { col = -col; }
            if (fv) { row = -row; }
            newSet.add(Node.getIndex(row,col));
        }
        return newSet;
    }

    public int enclosedCount() {
        return _enclosedSet.size();
    }

    public NodeState getNodeState(int row, int col, boolean cache) {
        return _refSet.getNodeState(getModifiedIndex(row,col), _gen, cache);
    }

    public NodeState getNodeState(int index, boolean cache) {
        return _refSet.getNodeState(getModifiedIndex(index), _gen, cache);
    }

    private NodeState getFutureNodeState(int index, int gen, boolean cache) {
        return _refSet.getNodeState(getModifiedIndex(index), gen, cache);
    }

    public Node getNode(int row, int col, boolean cache) {
        return _refSet.getNode(getModifiedIndex(row,col), _gen, cache);
    }

    public Node getNode(int index, boolean cache) {
        return _refSet.getNode(getModifiedIndex(index), _gen, cache);
    }

    private int getModifiedIndex(int index) {
        return getModifiedIndex(index / 10, index % 10);
    }

    private int getModifiedIndex(int row, int col) {
        // We need to retranslate everything.
        int crow = row;
        int ccol = col;
        // flip vertical
        if (_flipVertical) { crow = -crow; }
        // flip horizontal
        if (_flipHorizontal) { ccol = -ccol; }
        // swap axis
        if (_swapAxis) { int t = crow; crow = ccol; ccol = t; }
        // apply offsets
        crow -= _rowOffset;
        ccol -= _colOffset;
        return Node.getIndex(crow, ccol);
    }

    public boolean containsNode(int row, int col) {
        return _enclosedSet.contains(Node.getIndex(row, col));
    }

    public boolean containsNode(int index) {
        return _enclosedSet.contains(index);
    }

    // TODO - resolve issue with normalizing a non-direct partition.
    public Partition normalizePosition(int generation) {
        Set<Partition> options = new HashSet<Partition>();

        for (int permute = 0; permute <= 7; permute++) {
            // tags: <fv><fh><sa>
            boolean fv = (permute & 0x4) > 0;
            boolean fh = (permute & 0x2) > 0;
            boolean sa = (permute & 0x1) > 0;
            // To avoid operating on all coordinates, let's determine if we need
            // the maximum or minimum row and column.

            // We need to find the maximum row IF "fh"
            // We need to find the maximum col IF "fv"

            // We will swap the offsets for the swapAxis setting after calculating
            // them.
            int iniRowOffset = 10;
            int iniColOffset = 10;
            for (int i : _enclosedSet) {
                if ((fv ? -1 : 1) * (i / 10) < iniRowOffset) {
                    iniRowOffset = (fv ? -1 : 1) * (i / 10);
                }
                if ((fh ? -1 : 1) * (i % 10) < iniColOffset) {
                    iniColOffset = (fh ? -1 : 1) * (i % 10);
                }
            }
            // Swap the offsets, if needed.
            if (sa) {
                int t = iniRowOffset;
                iniRowOffset = iniColOffset;
                iniColOffset = t;
            }
            // Add the new permutation!
            options.add(new Partition(_refSet,
                    modifyEnclosed(iniRowOffset, iniColOffset, fv, fh, sa),
                    _gen, iniRowOffset, iniColOffset, fv, fh, sa));
        }

        // Time to choose a partition!
        // Use ordering of stateToChar().
        for (int i = 0; i <= 99; i++) {
            char maxValue = 0;
            for (Partition p : options) {
                if (p.containsNode(i)) {
                    if (Node.stateToChar(p.getNodeState(i, generation, false)) > maxValue) {
                        maxValue = Node.stateToChar(p.getNodeState(i, generation, false));
                    }
                }
            }
            if (maxValue > 0) {
                for (Partition p : options) {
                    if (p.containsNode(i)) {
                        if (Node.stateToChar(p.getNodeState(i, generation, false)) < maxValue) {
                            options.remove(p);
                        }
                    } else {
                        options.remove(p);
                    }
                }
            }
        }
        // There will always be at least one item in the set at this point.
        // If there are IDENTICAL permutations, there will be multiple items.
        // Luckily, we can return any of them!
        return options.iterator().next();
    }

    private Set<Partition> forkPartition(int index, NodeState newState, boolean cache) {
        // First off, check to make sure this isn't unblocking a node.
        if (getNodeState(index, cache) == NodeState.BLOCKED && newState != NodeState.BLOCKED) {
            throw new IllegalArgumentException("Cannot unblock a node in a partition.");
        } else {
            // Make the change in the refSet.
            int nextGen = _refSet.forkNode(index, _gen, newState);

            Map<Integer,IntPtr> partMap = new HashMap<Integer,IntPtr>(100);
            int nextPart = 0;
            for (int i : _enclosedSet) {
                // note:  this should be in order.
                NodeState iState = getFutureNodeState(i, nextGen, cache);
                if (iState != NodeState.BLOCKED) {
                    // the state is not blocked.
                    // check north node.
                    if (partMap.containsKey(i-10)) {
                        // the north node is in a partition - use it
                        partMap.put(i, partMap.get(i-10));
                        // check west node.
                        if ((i % 10) > 0 && partMap.containsKey(i-1)) {
                            // Set it to point to this partition.
                            partMap.get(i-1).set(partMap.get(i-10).get());
                        }
                    } else if ((i % 10) > 0 && partMap.containsKey(i-1)) {
                        partMap.put(i, partMap.get(i-1));
                    } else {
                        // make a new one.
                        partMap.put(i, new IntPtr(nextPart));
                        nextPart++;
                    }
                }
            }
            // collect partition sets.
            Map<Integer,SortedSet<Integer>> setMap = new HashMap<Integer, SortedSet<Integer>>();
            for (int i : partMap.keySet()) {
                int p = partMap.get(i).get();
                if (!setMap.containsKey(p)) {
                    setMap.put(p, new TreeSet<Integer>());
                }
                setMap.get(p).add(i);
            }
            // Create partitions.
            Set<Partition> parts = new HashSet<Partition>();
            if (_direct) {
                for (int p : setMap.keySet()) {
                    parts.add(new Partition(_refSet, setMap.get(p), nextGen));
                }
            } else {
                for (int p : setMap.keySet()) {
                    parts.add(new Partition(_refSet, setMap.get(p), nextGen,
                            _rowOffset, _colOffset, _flipVertical, _flipHorizontal, _swapAxis));
                }
            }
            return parts;
        }
    }
    private class IntPtr {
        private int _val;
        public IntPtr(int val) { _val = val; }
        public int get() { return _val; }
        public void set(int val) { _val = val; }
    }

    /***************************************************************************
     * ALGORITHMS                                                              *
     ***************************************************************************
     */

    public List<Integer> getReachableIndicies(int row, int col, boolean cache) {
        List<Integer> retList = new LinkedList<Integer>();
        int offset = 0;
        retList.add(Node.getIndex(row, col));
        // UP
        while (containsNode(row - offset, col) &&
                getNodeState(row - offset, col, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col));
            offset++;
        }
        offset = 0;
        // UP RIGHT
        while (containsNode(row - offset, col + offset) &&
                getNodeState(row - offset, col + offset, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col + offset));
            offset++;
        }
        offset = 0;
        // RIGHT
        while (containsNode(row, col + offset) &&
                getNodeState(row, col + offset, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row, col + offset));
            offset++;
        }
        offset = 0;
        // DOWN RIGHT
        while (containsNode(row + offset, col + offset) &&
                getNodeState(row + offset, col + offset, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col + offset));
            offset++;
        }
        offset = 0;
        // DOWN
        while (containsNode(row + offset, col) &&
                getNodeState(row + offset, col, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col));
            offset++;
        }
        offset = 0;
        // DOWN LEFT
        while (containsNode(row + offset, col - offset) &&
                getNodeState(row + offset, col - offset, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col - offset));
            offset++;
        }
        offset = 0;
        // LEFT
        while (containsNode(row, col - offset) &&
                getNodeState(row, col - offset, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row, col - offset));
            offset++;
        }
        offset = 0;
        // UP LEFT
        while (containsNode(row - offset, col - offset) &&
                getNodeState(row - offset, col - offset, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col - offset));
            offset++;
        }
        return retList;
    }

    public List<Integer> getReachableIndicies(int index, boolean cache) {
        return getReachableIndicies(index, cache);
    }

    public List<Node> getReachableNodes(int row, int col, boolean cache) {
        List<Node> retList = new LinkedList<Node>();
        int offset = 0;
        Node t;
        retList.add(getNode(row, col, cache));
        // UP
        while (containsNode(row - offset, col) &&
               (t = getNode(row - offset, col, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // UP RIGHT
        while (containsNode(row - offset, col + offset) &&
               (t = getNode(row - offset, col + offset, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // RIGHT
        while (containsNode(row, col + offset) &&
               (t = getNode(row, col + offset, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // DOWN RIGHT
        while (containsNode(row + offset, col + offset) &&
               (t = getNode(row + offset, col + offset, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // DOWN
        while (containsNode(row + offset, col) &&
               (t = getNode(row + offset, col, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // DOWN LEFT
        while (containsNode(row + offset, col - offset) &&
               (t = getNode(row + offset, col - offset, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // LEFT
        while (containsNode(row, col - offset) &&
               (t = getNode(row, col - offset, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // UP LEFT
        while (containsNode(row - offset, col - offset) &&
               (t = getNode(row - offset, col - offset, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        return retList;
    }

    public List<Node> getReachableNodes(int index, boolean cache) {
        return getReachableNodes(index, cache);
    }

    public List<Integer> getNeighboringIndicies(int row, int col, boolean cache) {
        List<Integer> retList = new ArrayList<Integer>(8);
        for (int pos = 0; pos <= 8; pos ++) {
            if (pos != 4) {
                int ro = (pos / 3) - 1;
                int co = (pos % 3) - 1;
                int index = Node.getIndex(row + ro, col + co);
                if (containsNode(index) && getNodeState(index, cache) == NodeState.EMPTY) {
                    retList.add(index);
                }
            }
        }
        return retList;
    }

    public List<Integer> getNeighboringIndicies(int index, boolean cache) {
        return getNeighboringIndicies(index, cache);
    }

    public List<Node> getNeighboringNodes(int row, int col, boolean cache) {
        List<Node> retList = new ArrayList<Node>(8);
        for (int pos = 0; pos <= 8; pos ++) {
            if (pos != 4) {
                int ro = (pos / 3) - 1;
                int co = (pos % 3) - 1;
                Node n;
                if (containsNode(row + ro, col + co) &&
                        (n = getNode(row + ro, col + co, cache)).getState() == NodeState.EMPTY) {
                    retList.add(n);
                }
            }
        }
        return retList;
    }

    public List<Node> getNeighboringNodes(int index, boolean cache) {
        return getNeighboringNodes(index, cache);
    }

    public PartitionState getPartitionState(int gen, boolean cache) {
        boolean blackFound = false;
        boolean whiteFound = false;
        for (int i : _enclosedSet) {
            NodeState iState = getNodeState(i, gen, cache);
            if (iState == NodeState.BLACK) {
                blackFound = true;
            } else if (iState == NodeState.WHITE) {
                whiteFound = true;
            }
        }
        if (blackFound) {
            if (whiteFound) {
                return PartitionState.CONTESTED;
            } else {
                return PartitionState.BLACK_OWNED;
            }
        } else {
            if (whiteFound) {
                return PartitionState.WHITE_OWNED;
            } else {
                return PartitionState.DEAD;
            }
        }
    }

    public int getFreeStates(int gen, boolean cache) {
        int freeCount = 0;
        for (int i : _enclosedSet) {
            NodeState iState = getNodeState(i, gen, cache);
            if (iState == NodeState.EMPTY) {
                freeCount++;
            }
        }
        return freeCount;
    }

    public Set<Integer> getWhiteQueens(int gen, boolean cache) {
        Set<Integer> indicies = new HashSet<Integer>();
        for (int i : _enclosedSet) {
            if (getNodeState(i, gen, cache) == NodeState.WHITE) {
                indicies.add(i);
            }
        }
        return indicies;
    }

    public Set<Integer> getBlackQueens(int gen, boolean cache) {
        Set<Integer> indicies = new HashSet<Integer>();
        for (int i : _enclosedSet) {
            if (getNodeState(i, gen, cache) == NodeState.BLACK) {
                indicies.add(i);
            }
        }
        return indicies;
    }
}
