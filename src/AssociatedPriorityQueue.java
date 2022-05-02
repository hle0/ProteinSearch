import java.util.Collections;
import java.util.LinkedList;

/**
 * A reimplementation of PriorityQueue that uses an "associated" priority value for each item
 * instead of requiring the items to implement Comparable.
 */
public class AssociatedPriorityQueue<T> extends LinkedList<AssociatedPriorityQueue.Item<T>> {
    /**
     * Data with an associated priority.
     */
    public static class Item<T> implements Comparable<Item<T>> {
        /** The priority; lower values are more important / better / favored */
        public int priority;
        /**  */
        public T data;
    
        public Item(int priority, T data) {
            this.priority = priority;
            this.data = data;
        }
    
        @Override
        public boolean equals(Object obj) {
            // https://stackoverflow.com/questions/7655396/implementing-the-equals-method-in-java
            if (this == obj) {
                return true;
            }
    
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
    
            Item<T> temp = (Item<T>) obj;
    
            return temp.data.equals(this.data) && temp.priority == this.priority;
        }
    
        @Override
        public int hashCode() {
            return this.data.hashCode() ^ this.priority;
        }
    
        @Override
        public int compareTo(Item<T> other) {
            if (this.equals(other)) {
                return 0;
            }

            int c = this.priority - other.priority;

            if (c == 0 && !this.data.equals(other.data)) {
                // depending on implementation, this can theoretically cause collisions, but probably not
                c = System.identityHashCode(this.data) - System.identityHashCode(other.data);
            }

            return c == 0
                ? 0
                : c > 0
                    ?  1
                    : -1;
        }
    }

    /**
     * The maximum amount of items to hold before items need to be discarded.
     * If <= 0, no bounds.
     */
    protected int maxAmount = 0;

    public AssociatedPriorityQueue() {}

    public AssociatedPriorityQueue(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public boolean prioritize(Item<T> item) {
        DebugHelper.getInstance().hit("AssociatedPriorityQueue.prioritize");

        if (item == null || item.data == null) {
            DebugHelper.getInstance().hit("AssociatedPriorityQueue.prioritize/nullReturn");
            return false;
        }

        int pos = this.isEmpty() ? -1 : Collections.binarySearch(this, item);

        if (pos >= 0) {
            DebugHelper.getInstance().hit("AssociatedPriorityQueue.prioritize/existsReturn");
            // this key already exists!
            return false;
        } else {
            int insertionPoint = -(pos + 1);

            if (this.atCapacity()) {
                if (item.priority >= this.getWorstPriority()) {
                    // this is worse than all the other elements, and we're out of space.
                    DebugHelper.getInstance().hit("AssociatedPriorityQueue.prioritize/body/1");
                    return false;
                } else {
                    // we have to remove the worst item to add this one.
                    DebugHelper.getInstance().hit("AssociatedPriorityQueue.prioritize/body/2");
                    this.add(insertionPoint, item);
                    this.removeLast();
                    return true;
                }
            } else {
                DebugHelper.getInstance().hit("AssociatedPriorityQueue.prioritize/body/3");
                this.add(insertionPoint, item);
                return true;
            }
        }
    }

    /**
     * Convenience wrapper around add to construct an Item.
     * @param priority The priority of this item
     * @param data The actually data for the item
     * @return true if the item was added; false otherwise
     */
    public boolean prioritizeItem(int priority, T data) {
        return this.prioritize(new Item<T>(priority, data));
    }

    /**
     * Poll the last item from the priority queue, and unwrap it (discard the priority value)
     * @return the unwrapped item
     */
    public T pollData() {
        AssociatedPriorityQueue.Item<T> result = this.poll();

        return result == null ? null : result.data;
    }

    /**
     * Get the priority of the worst element.
     * @return the priority of the worst element
     */
    public int getWorstPriority() {
        return this.getLast().priority;
    }

    /**
     * Determine whether the queue is at capacity.
     * @return true if the queue is at or exceeding capacity; false if it is not, or if the queue is unbounded.
     */
    public boolean atCapacity() {
        return maxAmount > 0 && this.size() >= maxAmount;
    }
}
