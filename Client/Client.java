package CSE4705_final.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/***
 * <p>Used to connect to an Amazons server.  This class is responsible for managing
 * a connection to an Amazons server.  Each instance of this object can connect
 * to a different server, with a different username.</p>
 *
 * <p>Over the lifetime of a Client object, four methods should be called.  First,
 * Connect() should be called to initiate a connection with an Amazons server.
 * Next, LoginAndMatch() is called to authenticate the client and choose an
 * opponent.  After this, Play() is called and handed an object that implements
 * the ClientInterface interface.  It is through this object that the client plays
 * the game.</p>
 *
 * <p>Typically, all four of these methods would be called from the same block of
 * code.</p>
 *
 * <p>The outcome of the game is returned as a boolean from the Play() method.
 * If the game ended in an error, a ClientException will be thrown.</p>
 *
 * <p>This class is designed to have the various querying methods (like getState())
 * be called during execution of the Play() method, which may take up to 10 minutes.
 * The user may wish to launch the game in a separate thread.  To facilitate this,
 * this class is thread-safe.</p>
 *
 * <p>Note that Client objects are NOT reusable.  If an error occurs or the game
 * ends, a new Client object must be created to play another game.</p>
 *
 * @author Ethan Levine
 */
public class Client {

    // These three fields store the state of this Client object.
    private ClientState _state;
    private boolean _isBlack;
    private int _gameID;

    // These three fields are used to perform IO with the Amazons server.
    private Socket _socket;
    private BufferedReader _inStream;
    private PrintWriter _outStream;

    // These three fields are used to provide log facilities.
    private StringBuilder _log;
    private boolean _enableLog;
    private SimpleDateFormat _sdf;
    private final Object _logMutex = new Object();

    /***
     * Creates a new, "fresh" Client object.  This object is initially not
     * connected to any server.
     */
    public Client() {
        _state = ClientState.Fresh;
        _log = new StringBuilder();
    }

    /***
     * Gets the current IO state of this Client object.
     * @return the IO state this Client object is in
     */
    public ClientState getState() { return _state; }

    /***
     * Gets the client's player's color.
     * @return true if the client's player is black, false otherwise
     */
    public boolean isBlack() { return _isBlack; }

    /***
     * Gets the game ID returned by the server.
     * @return the unique ID of the current game
     */
    public int getGameID() { return _gameID; }

    // Appends a message to the log if it is enabled.
    private void _logMessage(String msg) {
        if (_enableLog) {
            synchronized(_logMutex) {
                _log.append(_sdf.format(Calendar.getInstance().getTime()));
                _log.append(msg);
                _log.append("\n");
            }
        }
    }

    /***
     * Enables the logging facility of this client.  Note that logging is disabled
     * by default to avoid overhead.
     */
    public void enableLog() {
        _enableLog = true;
        if (_sdf == null) {
            _sdf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ");
        }
        _logMessage("Enabled log.");
    }

    /***
     * Disables the logging facility of this client.  Note that logging is disabled
     * by default to avoid overhead.
     */
    public void disableLog() {
        _logMessage("Disabled log.");
        _enableLog = false;
    }

    /***
     * Get a the current log.  This method should be called sparingly because it
     * forces a StringBuilder to assemble a very large String.
     * @return the contents of the log
     */
    public String getLog() {
        synchronized (_logMutex) {
            return _log.toString();
        }
    }

    /*
     * Attempts to read a line from the Amazons server.  This also echoes the
     * line to the log, prefixed with "RECV: ".  This method passed IOExceptions
     * to the caller.
     */
    private String _readLine()
            throws IOException {
        String in = _inStream.readLine();
        _logMessage("RECV: " + in);
        return in;
    }

    /*
     * Attempts to write a line to the Amazons server.  This also echoes the line
     * to the log, prefixed with "SEND: ".
     */
    private void _writeLine(String str) {
        _logMessage("SEND: " + str);
        _outStream.write(str + "\r\n");
        _outStream.flush();
    }

    /***
     * Attempt to connect this client to an Amazons server.  Note that this
     * method only makes the initial connection to the server, and does not try
     * to authenticate or initiate a game.
     * 
     * @param serverHost the hostname or IP address of the Amazons server
     * @param port the remote port to connect to
     * @throws ClientException if an IO failure occurs
     */
    public void Connect(String serverHost, int port)
            throws ClientException {
        if (_state == ClientState.Fresh) {
            _logMessage("Attempting to connect to \"" + serverHost + "\" on port " + port);
            _state = ClientState.Connecting;
            try {
                _socket = new Socket(serverHost, port);
                _outStream = new PrintWriter(_socket.getOutputStream(), true);
                _inStream = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
                // Eat a line of input, which should be "THESEUS v0.1"
                _readLine();
                _state = ClientState.Connected;
            } catch (UnknownHostException e) {
                _state = ClientState.Error;
                throw new ClientException("Unknown host: " + serverHost);
            } catch (IOException e) {
                _state = ClientState.Error;
                throw new ClientException("General IO failure");
            }
        } else {
            throw new ClientException("Attempted to call Connect() when the state wasn't Fresh.");
        }
    }

    /***
     * Attempts to authenticate this client and pick an opponent.  Note that this
     * method doesn't terminate until a game begins.  This means that this method
     * may have control of the current thread for an indeterminate amount of time.
     * This also means that as soon as this method returns, the game timer will
     * begin.  Therefore, it is recommended to call Play() right after this method.
     *
     * @param userID the user ID for the client to authenticate with
     * @param password the password for the client to authenticate with
     * @param opponent the user ID of the desired opponent, or 0 to play the empty AI
     * @throws ClientException if an IO failure occurs
     */
    public void LoginAndMatch(String userID, String password, String opponent)
            throws ClientException {
        if (_state == ClientState.Connected) {
            _state = ClientState.Authenticating;
            try {
                // Read the username prompt.
                _readLine();
                // Write the username.
                _writeLine(userID);
                // Read the password prompt.
                _readLine();
                // Write the password.
                _writeLine(password);
                _state = ClientState.Authenticated;
                // Read the opponent prompt
                _readLine();
                // Write the opponent.
                _writeLine(opponent);
                _state = ClientState.Matching;
                // Wait for a response.
                String matchResponse = _readLine();
                // Set the gameID.
                _gameID = Integer.parseInt(matchResponse.substring(5));
                // Get the player's color.
                matchResponse = _readLine();
                _isBlack = matchResponse.equals("Color:Black");
                _state = ClientState.Matched;
            } catch (IOException e) {
                _state = ClientState.Error;
                throw new ClientException("General IO failure");
            }
        } else {
            throw new ClientException("Attempted to call LoginAndMatch() when state wasn't Connected.");
        }
    }

    /***
     * <p>Plays an entire Amazons game.  This method plays an Amazons game from start
     * to finish, and only returns after the game is over.  Therefore, this method
     * may have control of the current thread for an indeterminate amount of time
     * (although this time is realistically capped by the Amazons server, typically
     * at 10 minutes).</p>
     *
     * <p>All moves are handled through the argument, impl.  This object is
     * responsible for somehow keeping track of the board and generating moves
     * when requested (using the getMove() method).</p>
     *
     * @param impl a handle into the AI implementation
     * @return true if the client's player wins, false otherwise
     * @throws ClientException if an error occurs during play (such as a bad move)
     *                         or a general IO failure occurs
     */
    public boolean Play(ClientInterface impl)
            throws ClientException {
        if (_state == ClientState.Matched) {
            if (_isBlack) {
                // computer goes first - set state to Waiting.
                _state = ClientState.Waiting;
            } else {
                // we go first - set state to Moving.
                _state = ClientState.Moving;
            }
            // NOTE:  This infinite loop must terminate by returning in a winning
            // scenario.
            try {
                while (true) {
                    String prompt = _readLine();
                    if (prompt == null) {
                        _logMessage("NULL response from server.");
                    } else if (prompt.startsWith("Result")) {
                        // Someone won!
                        _state = ClientState.Ended;
                        if (prompt.endsWith("Black")) {
                            Disconnect();
                            return _isBlack;
                        } else {
                            Disconnect();
                            return !_isBlack;
                        }
                    } else if (prompt.startsWith("?Move")) {
                        // It is OUR turn.
                        // The prompt will tell us how much time we have left.
                        int secondsLeft = Integer.parseInt(
                                prompt.substring(6,prompt.length() - 2));
                        ClientMove move = impl.getMove(secondsLeft);
                        // Write the move.
                        _writeLine(move.getSendingPrintout());
                        // Eat the echo.
                        _readLine();
                        // Set state to waiting.
                        _state = ClientState.Waiting;
                    } else if (prompt.startsWith("Move")) {
                        // The opponent moved.
                        ClientMove move = new ClientMove(prompt);
                        impl.opponentMove(move);
                        _state = ClientState.Moving;
                    } else if (prompt.startsWith("Error")) {
                        Disconnect();
                        _state = ClientState.Error;
                        throw new ClientException(prompt);
                    }
                }
            } catch (IOException e) {
                Disconnect();
                _state = ClientState.Error;
                throw new ClientException("General IO failure");
            }
        } else {
            Disconnect();
            throw new ClientException("Attempted to call Play() when state wasn't Matched.");
        }
    }

    /***
     * Disconnects the client from the Amazons server.  This closes the network
     * stream with the Amazons server.  If an IOException occurs, the exception
     * is sunk, because nothing can be done about it.
     */
    private void Disconnect() {
        if (_state == ClientState.Ended) {
            _state = ClientState.Disconnected;
            try {
                _inStream.close();
                _outStream.close();
                _socket.close();
            } catch (IOException e) {
                // There's really no reason to throw an exception.
                // What could possibly be done?
            }
        }
    }
}