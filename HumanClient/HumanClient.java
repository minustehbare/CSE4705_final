package CSE4705_final.HumanClient;

import CSE4705_final.Client.*;
import CSE4705_final.State.*;

import java.io.*;

public class HumanClient {

    private NodeSet _board;
    private int _currentGen;
    private BufferedReader _in;

    public HumanClient(boolean isPlayerBlack) {
        _board = new NodeSet(isPlayerBlack);
        // player is ALWAYS "white".
        _currentGen = 0;
        _in = new BufferedReader(new InputStreamReader(System.in));
    }

    private void registerOpponentMove(ClientMove move) {
        _currentGen = _board.forkNode(move.getFromRow(), move.getFromCol(), _currentGen, NodeState.EMPTY);
        _currentGen = _board.forkNode(move.getToRow(), move.getToCol(), _currentGen, NodeState.BLACK);
        _currentGen = _board.forkNode(move.getShootRow(), move.getShootCol(), _currentGen, NodeState.BLOCKED);
    }

    private void registerPlayerMove(ClientMove move) {
        _currentGen = _board.forkNode(move.getFromRow(), move.getFromCol(), _currentGen, NodeState.EMPTY);
        _currentGen = _board.forkNode(move.getToRow(), move.getToCol(), _currentGen, NodeState.WHITE);
        _currentGen = _board.forkNode(move.getShootRow(), move.getShootCol(), _currentGen, NodeState.BLOCKED);
    }

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

    private void isolateBoard() {
        _board = _board.isolateGen(_currentGen);
        _currentGen = 0;
    }

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
