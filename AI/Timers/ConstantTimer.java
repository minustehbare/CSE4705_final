package CSE4705_final.AI.Timers;

/**
 *
 * @author Ethan
 */
public class ConstantTimer implements AITimer {
    
    private final int _returnTime;
    
    public ConstantTimer(int returnTime) {
        _returnTime = returnTime;
    }
    
    @Override
    public int getMillisecondsAvailable(int secondsRemaining, int moveCount) {
        return _returnTime;
    }
    
}
