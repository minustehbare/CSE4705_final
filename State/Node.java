package CSE4705_final.State;

/**
 * An immutable node representation.  This contains the row, column, generation,
 * and state of a node.
 *
 * @author Ethan Levine
 */
public class Node {

    // The variables of this node state
    private final NodeState _state;
    private final int _index;
    private final int _gen;

    /**
     * Creates a new node state.  Note that node states are immutable.
     *
     * @param row the row of the node (between 0 and 9)
     * @param col the column of the node (between 0 and 9)
     * @param state the state of the node
     * @param generation the generation of the node
     */
    public Node(int row, int col, NodeState state, int generation) {
        this(Node.getIndex(row, col), state, generation);
    }
    
    public Node(int index, NodeState state, int generation) {
        _index = index;
        _state = state;
        _gen = generation;
    }

    /**
     * Gets the row of this node.
     *
     * @return the row of this node (between 0 and 9)
     */
    public int getRow() {
        return _index / 10;
    }

    /**
     * Gets the column of this node.
     *
     * @return the column of this node (between 0 and 9)
     */
    public int getCol() {
        return _index % 10;
    }

    /**
     * Gets the state of this node.
     *
     * @return the NodeState representing the state of this node
     */
    public NodeState getState() {
        return _state;
    }

    /**
     * Gets the generation to which this node belongs.
     *
     * @return the generation of this node
     */
    public int getGen() {
        return _gen;
    }

    /**
     * Creates a new node by modifying the state of this one.  Since nodes are
     * immutable, this instantiates a new node.
     *
     * @param newState the state to update to
     * @return the new node object
     */
    public Node forkState(NodeState newState) {
        return new Node(_index, newState, _gen);
    }

    /**
     * Helper method for converting a row and column into an index.
     *
     * @param row the row to query
     * @param col the column to query
     * @return the queried index
     */
    public static int getIndex(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return -1;
        } else {
            return col + row*10;
        }
    }

    public int getIndex() {
        return _index;
    }

    /**
     * Helper method for converting NodeState to a value.  Converts a NodeState
     * into a human-readable character.
     *
     * @param s the node state
     * @return a character representation of the state
     */
    public static char stateToChar(NodeState s) {
        switch (s) {
            case BLACK:
                return 'B';
            case WHITE:
                return 'W';
            case BLOCKED:
                return 'X';
            default:
                return ' ';
        }
    }
}
