/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CSE4705_final.State;

/**
 *
 * @author Ethan
 */
public class StateException extends RuntimeException {

    /**
     * Creates a new instance of <code>StateException</code> without detail message.
     */
    public StateException() {
    }


    /**
     * Constructs an instance of <code>StateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StateException(String msg) {
        super(msg);
    }

    public StateException(String msg, Exception cause) {
        super(msg, cause);
    }
}
