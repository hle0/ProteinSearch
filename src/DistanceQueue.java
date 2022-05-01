import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DistanceQueue<T> {
    public static class Item<T> implements Comparable<Item<T>> {
        public int priority;
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

            if (obj == null || this.hashCode() != obj.hashCode() || obj.getClass() != this.getClass()) {
                return false;
            }

            Item<T> temp = (Item<T>) obj;

            return temp.data.equals(this.data) && temp.priority == this.priority;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.data, this.priority);
        }

        @Override
        public int compareTo(Item<T> other) {
            return this.priority - other.priority;
        }
    }

    private LinkedList<Item<T>> queue;
    private int amount;

    public DistanceQueue(int amount) {
        this.amount = amount;
        this.queue = new LinkedList<>();
        this.clear();
    }

    public void clear() {
        this.queue.clear();
    }

    private void sort() {
        // just have Java sort this very badly
        queue.sort(new Comparator<Item<T>>() {
            @Override
            public int compare(Item<T> arg0, Item<T> arg1) {
                return arg0.priority - arg1.priority;
            }
        });
    }
    
    public void add(int priority, T data) {
        Item<T> toAdd = new Item<>(priority, data);
        if (amount <= 0) {
            // there's no way we could possibly add anything
            return;
        }

        if (queue.size() < amount) {
            // it's quicker to check for this after checking size
            if (queue.contains(toAdd)) return;
            queue.add(toAdd);
            this.sort();
        } else if (getWorstPriority() > priority) {
            // if the queue is full but the element to be added is still better than the worst element
            // implied that queue.size() == amount

            // it's still quicker to check for this after checking all the previous stuff
            if (queue.contains(toAdd)) return;
            
            // remove the worst element
            queue.removeLast();

            // add this better one
            queue.add(toAdd);

            // sort everything again
            this.sort();
        }
    }

    public int getWorstPriority() {
        return queue.getLast().priority;
    }

    public List<Item<T>> getAll() {
        return this.queue;
    }

    public int size() {
        return this.queue.size();
    }
}
