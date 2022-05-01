import java.util.TreeMap;

public class DebugHelper {
    private static DebugHelper INSTANCE = new DebugHelper();

    /**
     * Get the global DebugHelper instance.
     * @return the instance
     */
    public static DebugHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Event counts.
     * We use TreeMap so it's alphabetical by default.
     * You could use HashMap and sort the keys yourself if you wanted to.
     */
    private TreeMap<String, Integer> stats = new TreeMap<>();

    public DebugHelper() {
        clear();
    }

    /**
     * Print out the numeric statistics.
     */
    public void print() {
        // Don't clutter up stdout with useless info.
        if (!ConfigMenu.SHOW_DEBUG_STATS) {
            return;
        }

        System.out.println("<stats>");
        for (String key : stats.keySet()) {
            System.out.printf("  '%s': %s%n", key, stats.get(key));
        }
        System.out.println("</stats>");
    }

    /**
     * Clear all the stats.
     */
    public void clear() {
        stats.clear();
    }

    /**
     * Same as calling print() then clear().
     */
    public void lap() {
        print();
        clear();
    }

    /**
     * Increment event count.
     * @param event the statistics key for this event
     */
    public void hit(String event) {
        if (!ConfigMenu.SHOW_DEBUG_STATS) {
            // This can get expensive.
            return;
        }

        // Start the count at zero if we haven't counted this event yet.
        stats.putIfAbsent(event, 0);
        // Increment the event count.
        stats.compute(event, (k, i) -> i + 1);
    }
}
