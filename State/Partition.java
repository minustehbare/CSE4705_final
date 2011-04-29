package CSE4705_final.State;

import java.util.*;

import CSE4705_final.Client.*;

/**
 *
 * @author Ethan Levine
 */
public class Partition {

    private final NodeSet _refSet;
    private final SortedSet<Integer> _enclosedSet;
    private final int _gen;
    private final ModificationSet _modSet;

    public static final int SAVE_DEPTH = 3;
    private static final boolean CACHE_ENABLED = true;
    
    private PartitionState _c_state;
    private LinkedList<Integer> _c_blackQueens;
    private LinkedList<Integer> _c_whiteQueens;

    public Partition(NodeSet refSet, SortedSet<Integer> enclosedNodes, int gen) {
        _refSet = refSet;
        _enclosedSet = enclosedNodes;
        _gen = gen;
        _modSet = new ModificationSet();
    }

    private Partition(NodeSet refSet, SortedSet<Integer> enclosedNodes, int gen,
            ModificationSet modSet) {
        _refSet = refSet;
        _enclosedSet = enclosedNodes;
        _gen = gen;
        _modSet = modSet;
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

    public NodeState getNodeState(int row, int col) {
        return _refSet.getNodeState(_modSet.modifyIndex(row,col), _gen, CACHE_ENABLED);
    }

    public NodeState getNodeState(int index) {
        return _refSet.getNodeState(_modSet.modifyIndex(index), _gen, CACHE_ENABLED);
    }

    private NodeState getFutureNodeState(int index, int gen) {
        return _refSet.getNodeState(_modSet.modifyIndex(index), gen, CACHE_ENABLED);
    }

    public Node getNode(int row, int col) {
        return _refSet.getNode(_modSet.modifyIndex(row,col), _gen, CACHE_ENABLED);
    }

    public Node getNode(int index) {
        return _refSet.getNode(_modSet.modifyIndex(index), _gen, CACHE_ENABLED);
    }

    public boolean containsNode(int row, int col) {
        return _enclosedSet.contains(Node.getIndex(row, col));
    }

    public boolean containsNode(int index) {
        return _enclosedSet.contains(index);
    }

    // TODO - resolve issue with normalizing a non-direct partition.
    public Partition normalizePosition() {
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

            iniRowOffset = -iniRowOffset;
            iniColOffset = -iniColOffset;
            // Add the new permutation!
            options.add(new Partition(_refSet,
                    modifyEnclosed(iniRowOffset, iniColOffset, fv, fh, sa),
                    _gen, new ModificationSet(iniRowOffset, iniColOffset, fv, fh, sa)));
        }

        // Time to choose a partition!
        // Use ordering of stateToChar().
        Set<Partition> removable = new HashSet<Partition>();
        for (int i = 0; i <= 99; i++) {
            char maxValue = 0;
            for (Partition p : options) {
                if (p.containsNode(i)) {
                    if (Node.stateToChar(p.getNodeState(i)) > maxValue) {
                        maxValue = Node.stateToChar(p.getNodeState(i));
                    }
                }
            }
            if (maxValue > 0) {
                for (Partition p : options) {
                    if (p.containsNode(i)) {
                        if (Node.stateToChar(p.getNodeState(i)) < maxValue) {
                            removable.add(p);
                        }
                    } else {
                        removable.add(p);
                    }
                }
                for (Partition p : removable) {
                    options.remove(p);
                }
                removable.clear();
            }
        }
        // There will always be at least one item in the set at this point.
        // If there are IDENTICAL permutations, there will be multiple items.
        // Luckily, we can return any of them!
        return options.iterator().next();
    }

    private List<Partition> getSplitPartitions(int futureGen) {

            Map<Integer,IntPtr> partMap = new HashMap<Integer,IntPtr>(100);
            int nextPart = 0;
            for (int i : _enclosedSet) {
                // note:  this should be in order.
                NodeState iState = getFutureNodeState(i, futureGen);
                if (iState != NodeState.BLOCKED) {
                    // the state is not blocked.
                    // check north node.
                    if (partMap.containsKey(i-10)) {
                        // the north node is in a partition - use it
                        partMap.put(i, partMap.get(i-10));
                        // check northwest node.
                        if ((i % 10) > 0 && partMap.containsKey(i-11)) {
                            // Set it to point to this partition.
                            partMap.get(i-11).set(partMap.get(i-10).get());
                        }
                        // check west node.
                        if ((i % 10) > 0 && partMap.containsKey(i-1)) {
                            // Set it to point to this partition.
                            partMap.get(i-1).set(partMap.get(i-10).get());
                        }
                        // check northeast node.
                        if ((i % 10) < 9 && partMap.containsKey(i-9)) {
                            // Set it to point to this partition.
                            partMap.get(i-9).set(partMap.get(i-10).get());
                        }
                    // check northwest node
                    } else if ((i % 10) > 0 && partMap.containsKey(i-11)) {
                        // reuse it
                        partMap.put(i, partMap.get(i-11));
                        // check west node.
                        if ((i % 10) > 0 && partMap.containsKey(i-1)) {
                            // Set it to point to this partition.
                            partMap.get(i-1).set(partMap.get(i-11).get());
                        }
                        // check northeast node.
                        if ((i % 10) < 9 && partMap.containsKey(i-9)) {
                            // set it to point to this
                            partMap.get(i-9).set(partMap.get(i-11).get());
                        }
                    // check northeast node
                    } else if ((i % 10) < 9 && partMap.containsKey(i-9)) {
                        // reuse it
                        partMap.put(i, partMap.get(i-9));
                        // check west node.
                        if ((i % 10) > 0 && partMap.containsKey(i-1)) {
                            partMap.get(i-1).set(partMap.get(i-9).get());
                        }
                    // check west node
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
            List<Partition> parts = new LinkedList<Partition>();
            for (int p : setMap.keySet()) {
                parts.add(new Partition(_refSet, setMap.get(p), futureGen,
                        _modSet));
            }
            return parts;
    }

    public List<Partition> forkMove(ClientMove move, boolean isMovingPlayerBlack) {
        // This always blocks a state.
        int nextGen = _refSet.forkMove(move, isMovingPlayerBlack, _gen);
        return getSplitPartitions(nextGen);
    }

    public List<Partition> forkNode(int index, NodeState newState) {
        // First off, check to make sure this isn't unblocking a node.
        if (getNodeState(index) == NodeState.BLOCKED && newState != NodeState.BLOCKED) {
            throw new IllegalArgumentException("Cannot unblock a node in a partition.");

        // If the change is NOT to block something, then we don't need to search
        // for a split partition.
        } else if (newState != NodeState.BLOCKED) {
            int nextGen = _refSet.forkNode(index, _gen, newState);
//            SortedSet<Integer> newEnclosedSet = new TreeSet<Integer>(_enclosedSet);
//            newEnclosedSet.remove(index);
            Partition retPart = new Partition(_refSet, _enclosedSet, nextGen);
            List<Partition> retList = new LinkedList<Partition>();
            retList.add(retPart);
            return retList;
        } else {
            // Make the change in the refSet.
            int nextGen = _refSet.forkNode(index, _gen, newState);

            return getSplitPartitions(nextGen);
        }
    }

    private class IntPtr {
        private int _val;
        public IntPtr(int val) { _val = val; }
        public int get() { return _val; }
        public void set(int val) { _val = val; }
    }

    private class ModificationSet {
        private final int _rowOffset;
        private final int _colOffset;
        private final boolean _flipRows;
        private final boolean _flipCols;
        private final boolean _swapAxis;
        private final boolean _direct;

        public ModificationSet() {
            _rowOffset = 0;
            _colOffset = 0;
            _flipRows = false;
            _flipCols = false;
            _swapAxis = false;
            _direct = true;
        }

        public ModificationSet(int rowOffset, int colOffset, boolean flipRows,
                boolean flipCols, boolean swapAxis) {
            _rowOffset = rowOffset;
            _colOffset = colOffset;
            _flipRows = flipRows;
            _flipCols = flipCols;
            _swapAxis = swapAxis;
            _direct = false;
        }

        public int modifyIndex(int unmodRow, int unmodCol) {
            if (_direct) {
                return Node.getIndex(unmodRow, unmodCol);
            } else {
                // FIRST, flips rows/cols.
                int row = (_flipRows ? -unmodRow : unmodRow);
                int col = (_flipCols ? -unmodCol : unmodCol);
                // SECOND, swap row/col.
                if (_swapAxis) {
                    int t = row;
                    row = col;
                    col = t;
                }
                // THIRD, apply offsets.
                return Node.getIndex(row + _rowOffset, col + _colOffset);
            }
        }

        public int modifyIndex(int unmodIndex) {
            return modifyIndex(unmodIndex / 10, unmodIndex % 10);
        }

        public int unmodifyIndex(int modRow, int modCol) {
            if (_direct) {
                return Node.getIndex(modRow, modCol);
            } else {
                // FIRST, subtract the offsets.
                int row = modRow - _rowOffset;
                int col = modCol - _colOffset;
                // SECOND, swap row/col.
                if (_swapAxis) {
                    int t = row;
                    row = col;
                    col = t;
                }
                // THIRD, flip rows/cols.
                row = (_flipRows ? -row : row);
                col = (_flipCols ? -col : col);
                return Node.getIndex(row, col);
            }
        }

        public int unmodifyIndex(int modIndex) {
            return unmodifyIndex(modIndex / 10, modIndex % 10);
        }

        public boolean isDirect() {
            return _direct;
        }
    }

    /***************************************************************************
     * ALGORITHMS                                                              *
     ***************************************************************************
     */

    public List<Integer> getReachableIndicies(int row, int col) {
        List<Integer> retList = new LinkedList<Integer>();
        int offset = 1;
        retList.add(Node.getIndex(row, col));
        // UP
        while (containsNode(row - offset, col) &&
                getNodeState(row - offset, col) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col));
            offset++;
        }
        offset = 1;
        // UP RIGHT
        while (containsNode(row - offset, col + offset) &&
                getNodeState(row - offset, col + offset) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col + offset));
            offset++;
        }
        offset = 1;
        // RIGHT
        while (containsNode(row, col + offset) &&
                getNodeState(row, col + offset) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row, col + offset));
            offset++;
        }
        offset = 1;
        // DOWN RIGHT
        while (containsNode(row + offset, col + offset) &&
                getNodeState(row + offset, col + offset) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col + offset));
            offset++;
        }
        offset = 1;
        // DOWN
        while (containsNode(row + offset, col) &&
                getNodeState(row + offset, col) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col));
            offset++;
        }
        offset = 1;
        // DOWN LEFT
        while (containsNode(row + offset, col - offset) &&
                getNodeState(row + offset, col - offset) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col - offset));
            offset++;
        }
        offset = 1;
        // LEFT
        while (containsNode(row, col - offset) &&
                getNodeState(row, col - offset) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row, col - offset));
            offset++;
        }
        offset = 1;
        // UP LEFT
        while (containsNode(row - offset, col - offset) &&
                getNodeState(row - offset, col - offset) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col - offset));
            offset++;
        }
        return retList;
    }

    public List<Integer> getReachableIndicies(int index) {
        return getReachableIndicies(index / 10, index % 10);
    }

    public List<Node> getReachableNodes(int row, int col) {
        List<Node> retList = new LinkedList<Node>();
        int offset = 1;
        Node t;
        retList.add(getNode(row, col));
        // UP
        while (containsNode(row - offset, col) &&
               (t = getNode(row - offset, col)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // UP RIGHT
        while (containsNode(row - offset, col + offset) &&
               (t = getNode(row - offset, col + offset)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // RIGHT
        while (containsNode(row, col + offset) &&
               (t = getNode(row, col + offset)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // DOWN RIGHT
        while (containsNode(row + offset, col + offset) &&
               (t = getNode(row + offset, col + offset)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // DOWN
        while (containsNode(row + offset, col) &&
               (t = getNode(row + offset, col)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // DOWN LEFT
        while (containsNode(row + offset, col - offset) &&
               (t = getNode(row + offset, col - offset)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // LEFT
        while (containsNode(row, col - offset) &&
               (t = getNode(row, col - offset)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 1;
        // UP LEFT
        while (containsNode(row - offset, col - offset) &&
               (t = getNode(row - offset, col - offset)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        return retList;
    }

    public List<Node> getReachableNodes(int index) {
        return getReachableNodes(index / 10, index % 1);
    }

    public List<Integer> getNeighboringIndicies(int row, int col) {
        List<Integer> retList = new ArrayList<Integer>(8);
        for (int pos = 0; pos <= 8; pos ++) {
            if (pos != 4) {
                int ro = (pos / 3) - 1;
                int co = (pos % 3) - 1;
                int index = Node.getIndex(row + ro, col + co);
                if (containsNode(index) && getNodeState(index) == NodeState.EMPTY) {
                    retList.add(index);
                }
            }
        }
        return retList;
    }

    public List<Integer> getNeighboringIndicies(int index) {
        return getNeighboringIndicies(index / 10, index % 1);
    }

    public List<Node> getNeighboringNodes(int row, int col) {
        List<Node> retList = new ArrayList<Node>(8);
        for (int pos = 0; pos <= 8; pos ++) {
            if (pos != 4) {
                int ro = (pos / 3) - 1;
                int co = (pos % 3) - 1;
                Node n;
                if (containsNode(row + ro, col + co) &&
                        (n = getNode(row + ro, col + co)).getState() == NodeState.EMPTY) {
                    retList.add(n);
                }
            }
        }
        return retList;
    }

    public List<Node> getNeighboringNodes(int index) {
        return getNeighboringNodes(index / 10, index % 1);
    }

    public PartitionState getPartitionState() {
        if (_c_state != null) {
            return _c_state;
        } else {
            boolean blackFound = false;
            boolean whiteFound = false;
            for (int i : _enclosedSet) {
                NodeState iState = getNodeState(i);
                if (iState == NodeState.BLACK) {
                    blackFound = true;
                } else if (iState == NodeState.WHITE) {
                    whiteFound = true;
                }
            }
            if (blackFound) {
                if (whiteFound) {
                    _c_state = PartitionState.CONTESTED;
                } else {
                    _c_state = PartitionState.BLACK_OWNED;
                }
            } else {
                if (whiteFound) {
                    _c_state = PartitionState.WHITE_OWNED;
                } else {
                    _c_state = PartitionState.DEAD;
                }
            }
            return _c_state;
        }
    }

    public int getFreeStates() {
        int freeCount = 0;
        for (int i : _enclosedSet) {
            NodeState iState = getNodeState(i);
            if (iState == NodeState.EMPTY) {
                freeCount++;
            }
        }
        return freeCount;
    }

    public List<Integer> getWhiteQueens() {
        if (_c_whiteQueens != null) {
            return _c_whiteQueens;
        } else {
            _c_whiteQueens = new LinkedList<Integer>();
            for (int i : _enclosedSet) {
                if (getNodeState(i) == NodeState.WHITE) {
                    _c_whiteQueens.add(i);
                }
            }
            return _c_whiteQueens;
        }
    }

    public List<Integer> getBlackQueens() {
        if (_c_blackQueens != null) {
            return _c_blackQueens;
        } else {
            _c_blackQueens = new LinkedList<Integer>();
            for (int i : _enclosedSet) {
                if (getNodeState(i) == NodeState.BLACK) {
                    _c_blackQueens.add(i);
                }
            }
            return _c_blackQueens;
        }
    }

    private String getFullName() {
        StringBuilder ret = new StringBuilder();
        for (int index = 0; index <= 99; index++) {
            NodeState iState = getNodeState(index);
            if (iState != NodeState.EMPTY) {
                if (index <= 9) {
                    ret.append(0);
                }
                ret.append(index);
                ret.append(Node.stateToChar(iState));
            }
        }
        return ret.toString();
    }

    public String getNamePrefix() {
        String fullName = getFullName();
        //return fullName.substring(0,fullName.length() / (3 * SAVE_DEPTH));
        return fullName.substring(0,fullName.length() - (fullName.length() % SAVE_DEPTH));
    }

    public String getNameSuffix() {
        String fullName = getFullName();
        //return fullName.substring(fullName.length() / (3 * SAVE_DEPTH));
        String suffix = fullName.substring(fullName.length() - (fullName.length() % SAVE_DEPTH));
        
        //if the suffix is empty, return a default suffix that will never normally show up.
        //This allows me to parse it the data a little easier when reading from the
        //file.  If you would rather me do this on my end, let me know.
        if (suffix.length()==0)
          return("xx");
        
        return suffix;
    }
    public void print(){
      System.out.print(_refSet.printGen(_gen));
    }
}
