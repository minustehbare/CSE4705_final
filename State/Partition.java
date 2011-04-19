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

    public int enclosedCount() {
        return _enclosedSet.size();
    }

    public NodeState getNodeState(int row, int col, int generation) {
        return _refSet.getNodeState(getModifiedIndex(row,col), generation);
    }

    public NodeState getNodeState(int index, int generation) {
        return _refSet.getNodeState(getModifiedIndex(index), generation);
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
        return _enclosedSet.contains(getModifiedIndex(row, col));
    }

    public boolean containsNode(int index) {
        return _enclosedSet.contains(getModifiedIndex(index));
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
            options.add(new Partition(_refSet, _enclosedSet, iniRowOffset,
                    iniColOffset, fv, fh, sa));
        }

        // Time to choose a partition!
        // Use ordering of stateToChar().
        for (int i = 0; i <= 99; i++) {
            char maxValue = 0;
            for (Partition p : options) {
                if (p.containsNode(i)) {
                    if (Node.stateToChar(p.getNodeState(i, generation)) > maxValue) {
                        maxValue = Node.stateToChar(p.getNodeState(i, generation));
                    }
                }
            }
            if (maxValue > 0) {
                for (Partition p : options) {
                    if (p.containsNode(i)) {
                        if (Node.stateToChar(p.getNodeState(i, generation)) < maxValue) {
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

//    private class IntPair {
//        private final int _first;
//        private final int _second;
//
//        public IntPair(int first, int second) {
//            _first = first;
//            _second = second;
//        }
//
//        public int first() {
//            return _first;
//        }
//
//        public int second() {
//            return _second;
//        }
//    }
}
