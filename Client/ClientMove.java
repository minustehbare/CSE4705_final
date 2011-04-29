package CSE4705_final.Client;

import CSE4705_final.State.*;

/***
 * <p>Represents an anonymous Amazons move.  This class represents a simple move,
 * consisting of a starting location "from", a new location "to", and a shooting
 * location "shoot".  Each location is stored as a pair of integers for the row
 * and column.</p>
 *
 * <p>This object is an anonymous move, so the player making this move is not
 * stored in the move itself.  This information is managed by the client.</p>
 *
 * <p>This object is immutable, and thus thread-safe.</p>
 *
 * <p>This class does not verify moves.  It is up to the AI to provide valid
 * moves.  Any invalid moves will be passed to the server and will cause the
 * player to lose.</p>
 *
 * @author Ethan Levine
 */
public class ClientMove {
    // The old location of the queen we wish to move.
    private final int _fromIndex;

    // The new location of the queen we wish to move.
    private final int _toIndex;

    // The location we wish to shoot an arrow.
    private final int _shootIndex;

    private final int _value;

    /***
     * Directly creates a new move.  Keep in mind that this object is immutable.
     * @param fromRow the old row of the moving queen
     * @param fromCol the old column of the moving queen
     * @param toRow the new row of the moving queen
     * @param toCol the new column of the moving queen
     * @param shootRow the row to shoot at
     * @param shootCol the column to shoot at
     */
    public ClientMove(int fromRow, int fromCol, int toRow, int toCol,
            int shootRow, int shootCol)
    {
        _fromIndex = Node.getIndex(fromRow, fromCol);
        _toIndex = Node.getIndex(toRow, toCol);
        _shootIndex = Node.getIndex(shootRow, shootCol);
        _value = 0;
    }
    
    public ClientMove(int fromIndex, int toIndex, int shootIndex) {
        _fromIndex = fromIndex;
        _toIndex = toIndex;
        _shootIndex = shootIndex;
        _value = 0;
    }
    
    public ClientMove(int fromIndex, int toIndex, int shootIndex, int value) {
        _fromIndex = fromIndex;
        _toIndex = toIndex;
        _shootIndex = shootIndex;
        _value = value;
    }

    /***
     * Creates a new move by parsing a string.  The format of the string must be
     * IDENTICAL to the string sent by the server in response to a move.  This
     * constructor is only intended to be used by the client when parsing
     * opponent moves.
     *
     * @param echo the string representation of a move
     */
    public ClientMove(String echo) {
        // Example:
        // Move:White:(6:0):(1:5):(5:9)
        // 1234567890123456789012345678
        this(Integer.parseInt(echo.substring(12,13)),
             Integer.parseInt(echo.substring(14,15)),
             Integer.parseInt(echo.substring(18,19)),
             Integer.parseInt(echo.substring(20,21)),
             Integer.parseInt(echo.substring(24,25)),
             Integer.parseInt(echo.substring(26,27)));
    }

    /***
     * Creates a string representation of this move.  This string is the exact
     * format required by the Amazons server for a move to be correctly sent.
     * This is intended to be used by the client when pushing player moves to
     * the server.
     *
     * @return a printout of the move suitable to send to the server
     */
    public String getSendingPrintout() {
        return String.format("(%1:%2):(%3:%4):(%5:%6)",
                getFromRow(), getFromCol(), getToRow(), getToCol(), getShootRow(), getShootCol());
    }

    // These are basic accessors for the fields in the ClientMove object.
    public int getFromIndex() { return _fromIndex; }
    public int getFromRow() { return _fromIndex / 10; }
    public int getFromCol() { return _fromIndex % 10; }
    public int getToIndex() { return _toIndex; }
    public int getToRow() { return _toIndex / 10; }
    public int getToCol() { return _toIndex % 10; }
    public int getShootIndex() { return _shootIndex; }
    public int getShootRow() { return _shootIndex / 10; }
    public int getShootCol() { return _shootIndex % 10; }
    public int getValue() { return _value; }
}
