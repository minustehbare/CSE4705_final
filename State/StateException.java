package CSE4705_final.State;

/**
 * A runtime exception for issues within the NodeSet and Partition classes.
 * Ideally, this should never be thrown - it will only be thrown in the case of
 * an error in either the programming or consumption of these classes.
 *
 * @author Ethan Levine
 */
public class StateException extends RuntimeException {

    /**
     * Creates a new instance of <code>StateException</code> without detail message.
     */
    public StateException() {
    }


    /**
     * Constructs an instance of <code>StateException</code> with the specified detail message.
     * 
     * @param msg the detail message
     */
    public StateException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>StateException</code> with the specified
     * detail message and cause.
     *
     * @param msg the detail message
     * @param cause the underlying cause
     */
    public StateException(String msg, Exception cause) {
        super(msg, cause);
    }
}
