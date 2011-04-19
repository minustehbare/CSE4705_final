package CSE4705_final.State;

/**
 * A representation of the state of a node.  It can either be empty, blocked, or
 * contain either the black or white queen.
 *
 * @author Ethan Levine
 */
public enum NodeState {
    EMPTY,
    BLOCKED,
    BLACK,
    WHITE
}
