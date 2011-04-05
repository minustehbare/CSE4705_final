package CSE4705_final.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private ClientState _state;
    private boolean _isBlack;
    private int _gameID;

    private Socket _socket;
    private BufferedReader _inStream;
    private PrintWriter _outStream;

    public Client() {
        _state = ClientState.Fresh;
    }

    public ClientState getState() { return _state; }
    public boolean isBlack() { return _isBlack; }
    public int getGameID() { return _gameID; }

    public void Connect(String serverHost, int port)
            throws ClientException {
        _state = ClientState.Connecting;
        try {
            _socket = new Socket(serverHost, port);
            _outStream = new PrintWriter(_socket.getOutputStream(), true);
            _inStream = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            // Eat a line of input, which should be "THESEUS v0.1"
            _inStream.readLine();
            _state = ClientState.Connected;
        } catch (UnknownHostException e) {
            _state = ClientState.Error;
            throw new ClientException("Unknown host: " + serverHost);
        } catch (IOException e) {
            _state = ClientState.Error;
            throw new ClientException("General IO failure");
        }
    }

    public void LoginAndMatch(String userID, String password, String opponent)
            throws ClientException {
        _state = ClientState.Authenticating;
        try {
            // Read the username prompt.
            _inStream.readLine();
            // Write the username.
            _outStream.print(userID + "\r\n");
            _outStream.flush();
            // Read the password prompt.
            _inStream.readLine();
            // Write the password.
            _outStream.print(password + "\r\n");
            _outStream.flush();
            _state = ClientState.Authenticated;
            // Read the opponent prompt
            _inStream.readLine();
            // Write the opponent.
            _outStream.print(opponent + "\r\n");
            _outStream.flush();
            _state = ClientState.Matching;
            // Wait for a response.
            String matchResponse = _inStream.readLine();
            // Set the gameID.
            _gameID = Integer.parseInt(matchResponse.substring(5));
            // Get the player's color.
            matchResponse = _inStream.readLine();
            _isBlack = matchResponse.equals("Color:Black");
            _state = ClientState.Matched;
        } catch (IOException e) {
            _state = ClientState.Error;
            throw new ClientException("General IO failure");
        }
    }

    public boolean Play(ClientInterface impl)
            throws ClientException {
        if (_isBlack) {
            // computer goes first - set state to Waiting.
            _state = ClientState.Waiting;
        } else {
            // we go first - set state to Moving.
            _state = ClientState.Moving;
        }
        impl.initialize();
        // NOTE:  This infinite loop must terminate by returning in a winning
        // scenario.
        try {
            while (true) {
                String prompt = _inStream.readLine();
                if (prompt.startsWith("Result")) {
                    // Someone won!
                    _state = ClientState.Ended;
                    if (prompt.endsWith("Black")) {
                        return _isBlack;
                    } else {
                        return !_isBlack;
                    }
                } else if (prompt.startsWith("?Move")) {
                    // It is OUR turn.
                    // The prompt will tell us how much time we have left.
                    int secondsLeft = Integer.parseInt(
                            prompt.substring(6,prompt.length() - 2));
                    ClientMove move = impl.getMove(secondsLeft);
                    // Write the move.
                    _outStream.write(move.getSendingPrintout());
                    _outStream.flush();
                    // Eat the echo.
                    _inStream.readLine();
                    // Set state to waiting.
                    _state = ClientState.Waiting;
                } else if (prompt.startsWith("Move")) {
                    // The opponent moved.
                    ClientMove move = new ClientMove(prompt);
                    impl.opponentMove(move);
                    _state = ClientState.Moving;
                } else if (prompt.startsWith("Error")) {
                    _state = ClientState.Error;
                    throw new ClientException(prompt);
                }
            }
        } catch (IOException e) {
            _state = ClientState.Error;
            throw new ClientException("General IO failure");
        }
    }

    public void Disconnect() {
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