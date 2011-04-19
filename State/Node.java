package CSE4705_final.State;

/**
 *
 * @author Ethan Levine
 */
public class Node {
    private final NodeState _state;
    private final int _row;
    private final int _col;
    private final int _gen;

    public Node(int row, int col, NodeState state, int generation) {
        _row = row;
        _col = col;
        _state = state;
        _gen = generation;
    }

    public int getRow() {
        return _row;
    }

    public int getCol() {
        return _col;
    }

    public NodeState getState() {
        return _state;
    }

    public int getGen() {
        return _gen;
    }

    public Node forkState(NodeState newState) {
        return new Node(_row, _col, newState, _gen);
    }

    public static int getIndex(int row, int col) {
        return col + row*10;
    }


    /**
     * Helper method for printGen.  Converts a NodeState into a human-readable
     * character.
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
