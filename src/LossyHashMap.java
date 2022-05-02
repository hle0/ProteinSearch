import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A HashMap that loses elements over time according to an approximately FIFO cache policy.
 * Useful for caching things without using absurd amounts of memory.
 * 
 * This only works correctly when adding elements via put().
 */
public class LossyHashMap<T, U> extends HashMap<T, U> {
    /**
     * Items at the head of the queue are doomed for removal when new items are added.
     * These are the keys of the doomed items.
     */
    Queue<T> removalQueue = new LinkedList<>();

    /**
     * The maximum number of key-value pairs to hold before we start dropping them.
     */
    private int maxSize;

    /**
     * Create a new LossyHashMap.
     * @param maxSize The maximum number of items to store.
     */
    public LossyHashMap(int maxSize) {
        this.maxSize = maxSize;
    }
    
    @Override
    public U put(T key, U value) {
        if (!this.containsKey(key)) {
            removalQueue.add(key);
            
            if (this.removalQueue.size() >= maxSize) {
                T doomedKey = removalQueue.poll();
                this.remove(doomedKey);
            }
        }

        return super.put(key, value);
    }

    @Override
    public void clear() {
        super.clear();
        removalQueue.clear();
    }
}
