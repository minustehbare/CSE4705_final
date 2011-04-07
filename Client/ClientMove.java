package CSE4705_final.Client;

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
    private int _fromRow;
    private int _fromCol;

    // The new location of the queen we wish to move.
    private int _toRow;
    private int _toCol;

    // The location we wish to shoot an arrow.
    private int _shootRow;
    private int _shootCol;

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
        _fromRow = fromRow;
        _fromCol = fromCol;
        _toRow = toRow;
        _toCol = toCol;
        _shootRow = shootRow;
        _shootCol = shootCol;
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
        _fromRow = Integer.parseInt(echo.substring(12,13));
        _fromCol = Integer.parseInt(echo.substring(14,15));
        _toRow = Integer.parseInt(echo.substring(18,19));
        _toCol = Integer.parseInt(echo.substring(20,21));
        _shootRow = Integer.parseInt(echo.substring(24,25));
        _shootCol = Integer.parseInt(echo.substring(26,27));
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
                _fromRow, _fromCol, _toRow, _toCol, _shootRow, _shootCol);
    }

    // These are basic accessors for the fields in the ClientMove object.
    public int getFromRow() { return _fromRow; }
    public int getFromCol() { return _fromCol; }
    public int getToRow() { return _toRow; }
    public int getToCol() { return _toCol; }
    public int getShootRow() { return _shootRow; }
    public int getShootCol() { return _shootCol; }
}
