/**
 * Random static utilities.
 * 
 * @author Henry Leonard
 */
public class Util {
    /**
     * Basically the same as `assert condition`, but evaluates condition, even if assertions are disabled.
     * @param condition The condition to assert (if assertions are enabled)
     */
    public static void ensure(boolean condition) {
        assert condition;
    }
}
