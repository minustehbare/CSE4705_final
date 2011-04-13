/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.State;

/**
 *
 * @author Ethan
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
}
