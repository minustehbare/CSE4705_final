package CSE4705_final.Client;

/***
 * <p>A representation of the IO state of a Client object.  The state of a client
 * evolves almost linearly.  The states of a client during its entire lifetime
 * are listed below, from first to last.</p>
 *
 * <ol>
 * <li>Fresh</li>
 * <li>Connecting*</li>
 * <li>Connected</li>
 * <li>Authenticating*</li>
 * <li>Authenticated*</li>
 * <li>Matching</li>
 * <li>Matched</li>
 * <li>Waiting / Moving</li>
 * <li>Ended</li>
 * <li>Disconnected</li>
 * </ol>
 *
 * <p>States with an asterisk are transient states - the client should not be
 * in any of these states for any appreciable amount of time.</p>
 *
 * <p>The Error state is set when an error of any kind arises.  All errors are
 * unrecoverable.</p>
 *
 * @author Ethan Levine
 */
public enum ClientState {
    /**
     * The initial state of a client
     */
    Fresh,
    
    /**
     * The client is in the process of connecting to an Amazons server.
     */
    Connecting,

    /**
     * The client has successfully connected to an Amazons server.
     */
    Connected,
    
    /**
     * The client is in the process of authenticating with the server.
     */
    Authenticating,

    /**
     * The client has successfully authenticated with the server.
     */
    Authenticated,

    /**
     * The client is searching for an opponent to play.
     */
    Matching,
    
    /**
     * The client has found an opponent and is waiting for Play() to be invoked.
     */
    Matched,

    /**
     * It is the opponent's turn and the client is waiting for the opponent to
     * make a move.
     */
    Waiting,
    
    /**
     * It is our turn and the client is waiting for a move from the AI.
     */
    Moving,

    /**
     * The game has successfully ended and a victor has been declared.
     */
    Ended,
    
    /**
     * The client has successfully disconnected from the Amazons server.
     */
    Disconnected,

    /**
     * An error (either IO or game-specific) has occurred.  The client object can
     * no longer be used.
     */
    Error};