package CSE4705_final.State;

import java.util.concurrent.atomic.*;
import java.util.*;

/**
 *
 * @author Ethan Levine
 */
public class Partition {

    private final NodeSet _refSet;
    private final Set<Integer> _enclosedSet;
    private final int _rowOffset;
    private final int _colOffset;
    private final boolean _flipVertical;
    private final boolean _flipHorizontal;
    private final boolean _swapAxis;

    public Partition(NodeSet refSet, Set<Integer> enclosedNodes) {
        _refSet = refSet;
        _partID = _NextID.getAndIncrement();
        _enclosedSet = enclosedNodes;
        _rowOffset = 0;
        _colOffset = 0;
        _flipVertical = false;
        _flipHorizontal = false;
        _swapAxis = false;
    }

    private Partition(NodeSet refSet, Set<Integer> enclosedNodes, int ro, int co,
            boolean fv, boolean fh, boolean sa) {
        _refSet = refSet;
        _partID = _NextID.getAndIncrement();
        _enclosedSet = enclosedNodes;
        _rowOffset = ro;
        _colOffset = co;
        _flipVertical = fv;
        _flipHorizontal = fh;
        _swapAxis = sa;
    }

    private Set<Integer> modifyEnclosed(int ro, int co, boolean fv, boolean fh, boolean sa) {
        Set<Integer> newSet = new HashSet<Integer>();
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

    public NodeState getNodeState(int row, int col, int generation, boolean cache) {
        return _refSet.getNodeState(getModifiedIndex(row,col), generation, cache);
    }

    public NodeState getNodeState(int index, int generation, boolean cache) {
        return _refSet.getNodeState(getModifiedIndex(index), generation, cache);
    }

    public Node getNode(int row, int col, int generation, boolean cache) {
        return _refSet.getNode(getModifiedIndex(row,col), generation, cache);
    }

    public Node getNode(int index, int generation, boolean cache) {
        return _refSet.getNode(getModifiedIndex(index), generation, cache);
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
                    iniRowOffset, iniColOffset, fv, fh, sa));
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

    // BOOK-KEEPING FOR IDENTIFYING PARTITIONS.
    private static AtomicInteger _NextID = new AtomicInteger(0);
    private final int _partID;
    public int getPartID() {
        return _partID;
    }
    @Override
    public boolean equals(Object other) {
        return other.getClass().equals(Partition.class) &&
               _partID == ((Partition) other).getPartID();
    }
    @Override
    public int hashCode() {
        return _partID;
    }

    /***************************************************************************
     * ALGORITHMS                                                              *
     ***************************************************************************
     */

    public List<Integer> getReachableIndicies(int row, int col, int gen, boolean cache) {
        List<Integer> retList = new LinkedList<Integer>();
        int offset = 0;
        retList.add(Node.getIndex(row, col));
        // UP
        while (containsNode(row - offset, col) &&
                getNodeState(row - offset, col, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col));
            offset++;
        }
        offset = 0;
        // UP RIGHT
        while (containsNode(row - offset, col + offset) &&
                getNodeState(row - offset, col + offset, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col + offset));
            offset++;
        }
        offset = 0;
        // RIGHT
        while (containsNode(row, col + offset) &&
                getNodeState(row, col + offset, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row, col + offset));
            offset++;
        }
        offset = 0;
        // DOWN RIGHT
        while (containsNode(row + offset, col + offset) &&
                getNodeState(row + offset, col + offset, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col + offset));
            offset++;
        }
        offset = 0;
        // DOWN
        while (containsNode(row + offset, col) &&
                getNodeState(row + offset, col, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col));
            offset++;
        }
        offset = 0;
        // DOWN LEFT
        while (containsNode(row + offset, col - offset) &&
                getNodeState(row + offset, col - offset, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row + offset, col - offset));
            offset++;
        }
        offset = 0;
        // LEFT
        while (containsNode(row, col - offset) &&
                getNodeState(row, col - offset, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row, col - offset));
            offset++;
        }
        offset = 0;
        // UP LEFT
        while (containsNode(row - offset, col - offset) &&
                getNodeState(row - offset, col - offset, gen, cache) == NodeState.EMPTY) {
            retList.add(Node.getIndex(row - offset, col - offset));
            offset++;
        }
        return retList;
    }

    public List<Integer> getReachableIndicies(Node center, boolean cache) {
        return getReachableIndicies(center.getRow(), center.getCol(), center.getGen(), cache);
    }

    public List<Node> getReachableNodes(int row, int col, int gen, boolean cache) {
        List<Node> retList = new LinkedList<Node>();
        int offset = 0;
        Node t;
        retList.add(getNode(row, col, gen, cache));
        // UP
        while (containsNode(row - offset, col) &&
               (t = getNode(row - offset, col, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // UP RIGHT
        while (containsNode(row - offset, col + offset) &&
               (t = getNode(row - offset, col + offset, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // RIGHT
        while (containsNode(row, col + offset) &&
               (t = getNode(row, col + offset, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // DOWN RIGHT
        while (containsNode(row + offset, col + offset) &&
               (t = getNode(row + offset, col + offset, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // DOWN
        while (containsNode(row + offset, col) &&
               (t = getNode(row + offset, col, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // DOWN LEFT
        while (containsNode(row + offset, col - offset) &&
               (t = getNode(row + offset, col - offset, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // LEFT
        while (containsNode(row, col - offset) &&
               (t = getNode(row, col - offset, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        offset = 0;
        // UP LEFT
        while (containsNode(row - offset, col - offset) &&
               (t = getNode(row - offset, col - offset, gen, cache)).getState() == NodeState.EMPTY) {
            retList.add(t);
            offset++;
        }
        return retList;
    }

    public List<Node> getReachableNodes(Node center, boolean cache) {
        return getReachableNodes(center.getRow(), center.getCol(), center.getGen(), cache);
    }

    public List<Integer> getNeighboringIndicies(int row, int col, int gen, boolean cache) {
        List<Integer> retList = new ArrayList<Integer>(8);
        for (int pos = 0; pos <= 8; pos ++) {
            if (pos != 4) {
                int ro = (pos / 3) - 1;
                int co = (pos % 3) - 1;
                int index = Node.getIndex(row + ro, col + co);
                if (containsNode(index) && getNodeState(index, gen, cache) == NodeState.EMPTY) {
                    retList.add(index);
                }
            }
        }
        return retList;
    }

    public List<Integer> getNeighboringIndicies(Node center, boolean cache) {
        return getNeighboringIndicies(center.getRow(), center.getCol(), center.getGen(), cache);
    }

    public List<Node> getNeighboringNodes(int row, int col, int gen, boolean cache) {
        List<Node> retList = new ArrayList<Node>(8);
        for (int pos = 0; pos <= 8; pos ++) {
            if (pos != 4) {
                int ro = (pos / 3) - 1;
                int co = (pos % 3) - 1;
                Node n;
                if (containsNode(row + ro, col + co) &&
                        (n = getNode(row + ro, col + co, gen, cache)).getState() == NodeState.EMPTY) {
                    retList.add(n);
                }
            }
        }
        return retList;
    }

    public List<Node> getNeighboringNodes(Node center, boolean cache) {
        return getNeighboringNodes(center.getRow(), center.getCol(), center.getGen(), cache);
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
