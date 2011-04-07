package CSE4705_final.Client;

/***
 * Thrown when any network- or game-level error occurs.  This can be thrown in
 * response to an IO issue with the server connection or a game issue such as
 * a bad or malformed move.
 * 
 * @author Ethan Levine
 */
public class ClientException extends Exception {

    public ClientException() {
    }

    public ClientException(String msg) {
        super(msg);
    }
}
