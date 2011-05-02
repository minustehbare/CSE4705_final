package CSE4705_final.AI.Timers;

/**
 *
 * @author Ethan
 */
public class ExpectedScaleTimer implements AITimer {
    
    private final double _scale;
    
    public ExpectedScaleTimer(double scale) {
        _scale = scale;
    }
    
    public int getMillisecondsAvailable(int secondsRemaining, int moveCount) {
        return (int) ((_scale * 1000 * secondsRemaining) / (92 - moveCount));
    }
    
}
