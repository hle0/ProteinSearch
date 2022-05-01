import java.util.PriorityQueue;

public class AssociatedPriorityQueue<T> extends PriorityQueue<DistanceQueue.Item<T>> {
    public void add(int priority, T data) {
        this.add(new DistanceQueue.Item<T>(priority, data));
    }

    public T pollData() {
        DistanceQueue.Item<T> result = this.poll();

        return result == null ? null : result.data;
    }
}
