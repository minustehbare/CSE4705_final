package CSE4705_final.HumanClient;

import CSE4705_final.Client.*;
import CSE4705_final.State.*;

import java.io.*;

/**
 * <p>A human-player AI.  This AI simply repeatedly prompts the user for moves until
 * the game is won, or an error occurs.  This AI does not attempt to verify moves,
 * so you must be careful with what you type!  This is meant strictly for
 * testing purposes, and is not meant to be used in an actual game.</p>
 *
 * <p>This AI uses the NodeSet class directly.  This is not recommended for a
 * real AI - the Partition class should be used instead.</p>
 * 
 * @author Ethan Levine
 */
public class HumanClient {

    // The game board.
    private NodeSet _board;

    // The current generation of the board.
    private int _currentGen;

    // A convenience to read lines from the console.
    private BufferedReader _in;

    /**
     * Creates a new human-player AI.  This AI will set the game so the player
     * is always WHITE - if the player is black, the positions will be swapped.
     *
     * @param isPlayerBlack swaps the white and black positions if true
     */
    public HumanClient(boolean isPlayerBlack) {
        _board = new NodeSet(isPlayerBlack);
        // player is ALWAYS "white".
        _currentGen = 0;
        _in = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Registers an opponent's move.  The opponent is always black.
     *
     * @param move the move to register
     */
    private void registerOpponentMove(ClientMove move) {
        _currentGen = _board.forkNode(move.getFromRow(), move.getFromCol(), _currentGen, NodeState.EMPTY);
        _currentGen = _board.forkNode(move.getToRow(), move.getToCol(), _currentGen, NodeState.BLACK);
        _currentGen = _board.forkNode(move.getShootRow(), move.getShootCol(), _currentGen, NodeState.BLOCKED);
    }

    /**
     * Registers our own move.  We are always white.
     *
     * @param move the move to register
     */
    private void registerPlayerMove(ClientMove move) {
        _currentGen = _board.forkNode(move.getFromRow(), move.getFromCol(), _currentGen, NodeState.EMPTY);
        _currentGen = _board.forkNode(move.getToRow(), move.getToCol(), _currentGen, NodeState.WHITE);
        _currentGen = _board.forkNode(move.getShootRow(), move.getShootCol(), _currentGen, NodeState.BLOCKED);
    }

    /**
     * Gets a the player's next move.  This reads the next move from the console,
     * after printing out the board.  This does not try to sanitize or handle
     * incorrect input - type at your own risk!
     *
     * @return the player's next move, as read from the console
     */
    private ClientMove getPlayerMove() {
        // Print out the board.
        try {
            System.out.println(_board.printGen(_currentGen));
            System.out.println("move? (#:#):(#:#):(#:#)");
            String move = _in.readLine();
            return new ClientMove(move);
        } catch (IOException e) {
            System.out.println("IO failure.");
            System.exit(1);
            return null;
        }
    }

    /**
     * Attempts to save memory by isolating the board.  This is currently not
     * invoked - it is mostly here to demonstrate how to use it.
     */
    private void isolateBoard() {
        _board = _board.isolateGen(_currentGen);
        _currentGen = 0;
    }

    /**
     * Gets the ClientInterface for this AI.  This returns an implementation of
     * ClientInterface that links into this object.
     *
     * @return the client interface of this object
     */
    public ClientInterface getInterface() {
        return new ClientInterface() {
            public ClientMove getMove(int timeLeft) {
                ClientMove nextMove = getPlayerMove();
                registerPlayerMove(nextMove);
                return nextMove;
            }

            public void opponentMove(ClientMove opponentMove) {
                registerOpponentMove(opponentMove);
            }
        };
    }
}
