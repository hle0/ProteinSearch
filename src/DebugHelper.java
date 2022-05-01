import java.util.HashMap;
import java.util.TreeMap;

public class DebugHelper {
    public static final boolean DEBUG_SUPPORT = true;
    private static DebugHelper INSTANCE = new DebugHelper();

    public static DebugHelper getInstance() {
        return INSTANCE;
    }

    private TreeMap<String, Integer> stats = new TreeMap<>();

    public DebugHelper() {
        stats.clear();
    }

    /**
     * Print out the numeric statistics.
     */
    public void print() {
        if (!DEBUG_SUPPORT) {
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
        if (!DEBUG_SUPPORT) {
            return;
        }

        stats.putIfAbsent(event, 0);
        stats.compute(event, (k, i) -> i + 1);
    }
}
