import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of a Vantage-Point binary metric tree.
 */
public class VantagePointTree<T extends VantagePointTree.Node> extends Tree<T> {
    /**
     * A node of the tree.
     * To conserve on memory, the raw data of each Node is not cached and is instead lazily loaded.
     * toString() should return a String (e.g. a file path) that can be used
     * for checking equality without loading large amounts of data.
     */
    public static abstract class Node {
        /**
         * The threshold distance.
         * Nodes with distances to root less than or equal to the threshold distance are in the left subtree.
         * Nodes with distances to root greater than the threshold distance are in the right subtree.
         */
        public int threshold = 0;

        // Concrete subclasses should @Override
        // String toString();

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (this.getClass() != obj.getClass()) return false;
            return obj.toString().equals(this.toString());
        }

        /**
         * Get the underlying (possibly big) string data, which may be more expensive than checking equality.
         * Used for distance calculations.
         * @return the underlying string that this node refers to.
         */
        abstract String getRawData();

        /**
         * Get the length of the underlying data. Should be cached.
         * @return the length, in characters
         */
        abstract int getLength();
    }

    /**
     * Create a new tree
     * @param node the root node of this tree.
     */
    public VantagePointTree(T node) {
        super(node);
    }

    /**
     * Add a new node to this tree (or one of the subtrees if this tree already has a left and right subtree).
     * @param node The node to add.
     */
    public void add(T node) {
        int dist = EditDistance.measure(node.getRawData(), root.getRawData());

        if (getLeft() == null) {
            // If we don't have a left subtree, we definitely don't have a right subtree.
            // Make this the left subtree and make the threshold distance equal to the distance from the added node to root.
            root.threshold = dist;
            setLeft(new VantagePointTree<>(node));
        } else {
            if (getRight() == null) {
                // We have a left subtree but not a right subtree.
                if (dist < root.threshold) {
                    // case A
                    // the added node is closer to the root node than the left subtree is to the root node
                    // so this new node should actually be on the left
                    root.threshold = dist;
                    // this is fine since all the nodes that could be on the left tree must be the same edit distance away from root
                    // this is because the only case that could have been invoked prior to this one is case C (if it was invoked at all)
                    // otherwise we would have a right subtree already
                    setRight(getLeft());
                    setLeft(new VantagePointTree<>(node));
                } else if (dist > root.threshold) {
                    // case B
                    // the added node is farther from the root node than the left subtree is from the root node
                    // so we should just make this the right subtree and everything will be fine
                    setRight(new VantagePointTree<>(node));
                } else {
                    // case C
                    // both the left subtree and the new node are the same distance from the root node
                    // so add the new node to the left subtree
                    ((VantagePointTree<T>) getLeft()).add(node);
                }
            } else {
                // we have both subtrees != null
                if (dist <= root.threshold) {
                    // this belongs on the left
                    ((VantagePointTree<T>) getLeft()).add(node);
                } else {
                    // this belongs on the right
                    ((VantagePointTree<T>) getRight()).add(node);
                }
            }
        }
    }

    /**
     * Search the tree for nearest neighbors to a given String
     * @param query The String to search for
     * @param nns The maximum number of neighbors to return
     * @param exhaustive If true, go through every single subtree, including subtrees that shouldn't need to be searched
     * @return A list containing the nearest neighbors
     */
    public List<AssociatedPriorityQueue.Item<VantagePointTree<T>>> search(String query, int nns, boolean exhaustive) {
        // https://fribbels.github.io/vptree/writeup

        DebugHelper.getInstance().hit("VantagePointTree.search");

        // Don't use Integer.MAX_VALUE because it leads to all sorts of weird issues due to int overflow
        // Wish I had thought of that 4 hours ago...
        int tau = 1_000_000_000;//Integer.MAX_VALUE;
        AssociatedPriorityQueue<VantagePointTree<T>> toSearch = new AssociatedPriorityQueue<>();
        assert toSearch.prioritizeItem(0, this);
        assert !toSearch.isEmpty(); // you'd be surprised how much this has driven me mad

        AssociatedPriorityQueue<VantagePointTree<T>> results = new AssociatedPriorityQueue<>(nns);
        DistanceCache<T> distanceCache = new DistanceCache<>(query);

        while (
            !toSearch.isEmpty()
            // If the following condition is false, there will not be any better elements,
            // since the recursive lower bounds are all too high.
            //
            // This is an optimization not in the original writeup.
            //&& toSearch.peek().priority <= tau
        ) {
            DebugHelper.getInstance().hit("VantagePointTree.search/body");

            AssociatedPriorityQueue.Item<VantagePointTree<T>> currentItem = toSearch.poll();

            VantagePointTree<T> current  = currentItem.data;
            VantagePointTree<T> curLeft  = (VantagePointTree<T>) current.getLeft();
            VantagePointTree<T> curRight = (VantagePointTree<T>) current.getRight();

            if (exhaustive) {
                // search every single node
                results.prioritizeItem(distanceCache.distance(current), current);
                if (curLeft  != null) assert toSearch.prioritizeItem(0, (VantagePointTree<T>)  curLeft);
                if (curRight != null) assert toSearch.prioritizeItem(0, (VantagePointTree<T>) curRight);
            } else {
                int dist;
                // search intelligently
                if (
                    distanceCache.getLowerBound(current) <= tau
                    && (dist = distanceCache.distance(current)) <= tau
                ) {
                    DebugHelper.getInstance().hit("VantagePointTree.search/body/1");
                    results.prioritizeItem(dist, current);
                    if (results.atCapacity()) {
                        tau = results.getWorstPriority();
                    }
                }

                if (
                    curLeft != null
                    && distanceCache.getLowerBound(current) <= current.root.threshold + tau
                    && (dist = distanceCache.distance(current)) <= current.root.threshold + tau
                ) {
                    DebugHelper.getInstance().hit("VantagePointTree.search/body/2");
                    toSearch.prioritizeItem(distanceCache.getRecursiveLowerBound(curLeft), curLeft);
                }

                if (
                    curRight != null
                    && distanceCache.getUpperBound(current) >= current.root.threshold - tau
                    && (dist = distanceCache.distance(current)) >= current.root.threshold - tau
                ) {
                    DebugHelper.getInstance().hit("VantagePointTree.search/body/3");
                    toSearch.prioritizeItem(distanceCache.getRecursiveLowerBound(curRight), curRight);
                }
            }
        }

        if (results.size() < nns) {
            // This shouldn't happen, hopefully
            System.out.println("NOTE: Some results may have been omitted due to tree layout.");
        }

        return results;
    }

    /**
     * Build a vp-tree from an iterator
     * @param <X> The Node type
     * @param iterator The nodes to add
     * @return A fully-formed vp-tree
     */
    public static <X extends Node> VantagePointTree<X> buildFromIterator(Iterator<X> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }

        int i = 1;
        VantagePointTree<X> output = new VantagePointTree<>(iterator.next());

        while (iterator.hasNext()) {
            output.add(iterator.next());
            System.out.printf("Processed %6d files so far...\r", ++i);
        }

        System.out.println();

        // It turns out tree rotations break a lot of things, but I didn't notice because assertions were disabled.
        //output.balance();

        output.verify();

        if (ConfigMenu.OPTIMIZE_TREE) {
            System.out.println("Optimizing tree, this may take a while. Go grab some coffee...");

            output = output.optimize();
        }

        return output;
    }

    /**
     * Create a new imperfectly-optimized vp-tree with the contents of this one.
     * @return a new vp-tree
     */
    public VantagePointTree<T> optimize() {
        LinkedList<T> nodes = this.getAllNodes();

        VantagePointTree<T> optimized = new VantagePointTree<>(nodes.poll());

        int i = 1;

        while (nodes.size() > 0) {
            optimized.add(nodes.poll());
            System.out.printf("Optimized %6d nodes so far...\r", ++i);
        }

        // It turns out tree rotations break a lot of things, but I didn't notice because assertions were disabled.
        //optimized.balance();

        optimized.verify();

        return optimized;
    }

    @Override
    public void verify() {
        if (getLeft() != null) {
            int leftDist = EditDistance.measure(getLeft().root.getRawData(), this.root.getRawData());
            assert leftDist <= root.threshold;
            if (getRight() != null) {
                int rightDist = EditDistance.measure(getRight().root.getRawData(), this.root.getRawData());
                assert rightDist > root.threshold;
                assert rightDist > leftDist;
            }
        }

        super.verify();
    }
}