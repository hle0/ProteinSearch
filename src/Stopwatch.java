/**
 * Utility class for timing things. Millisecond precision.
 */
public class Stopwatch {
    // The value of System.currentTimeMillis() when this stopwatch was created.
    private long millis;

    private Stopwatch(long millis) {
        this.millis = millis;
    }

    /**
     * Create a new Stopwatch starting at the current time.
     * @return A new Stopwatch. Use it quick!
     */
    public static Stopwatch tick() {
        return new Stopwatch(System.currentTimeMillis());
    }

    /**
     * Get the amount of milliseconds that have passed since the Stopwatch was created.
     * @return The number of milliseconds since inception.
     */
    public long tock() {
        return System.currentTimeMillis() - this.millis;
    }

    /**
     * Time a Runnable and return the number of milliseconds it took to complete.
     * @param runnable The Runnable to time.
     * @return The number of milliseconds that have passed.
     */
    public static long time(Runnable runnable) {
        Stopwatch watch = tick();
        runnable.run();
        return watch.tock();
    }
}
