package CSE4705_final.Client;

public class ClientMove {
    private int _fromRow;
    private int _fromCol;
    private int _toRow;
    private int _toCol;
    private int _shootRow;
    private int _shootCol;

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

    public String getSendingPrintout() {
        return String.format("(%1:%2):(%3:%4):(%5:%6)",
                _fromRow, _fromCol, _toRow, _toCol, _shootRow, _shootCol);
    }

    public int getFromRow() { return _fromRow; }
    public int getFromCol() { return _fromCol; }
    public int getToRow() { return _toRow; }
    public int getToCol() { return _toCol; }
    public int getShootRow() { return _shootRow; }
    public int getShootCol() { return _shootCol; }
}
