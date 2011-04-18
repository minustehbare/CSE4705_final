package CSE4705_final.Client;

/***
 * Used by the client to communicate with an AI.  To work with the client, an AI
 * must provide an implementation of this interface to give to the client as
 * an argument to Play().
 *
 * @author Ethan Levine
 */
public interface ClientInterface {

    /***
     * Register an opponent's move.  This method is invoked whenever the opponent
     * makes a move.  It is the AI's responsibility to take this information and
     * keep track of the current state of the game.
     *
     * @param move the opponent's move
     */
    void opponentMove(ClientMove move);

    /***
     * Generate a move.  This method is invoked whenever the server asks for our
     * next move.
     *
     * @param timer the number of seconds left for us to play
     * @return our next move
     */
    ClientMove getMove(int timer);
}
