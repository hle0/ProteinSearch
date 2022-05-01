import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

        // String toString();

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass() && obj.toString().equals(this.toString());
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

        if (left == null) {
            // If we don't have a left subtree, we definitely don't have a right subtree.
            // Make this the left subtree and make the threshold distance equal to the distance from the added node to root.
            root.threshold = dist;
            left = new VantagePointTree<>(node);
        } else {
            if (right == null) {
                // We have a left subtree but not a right subtree.
                if (dist < root.threshold) {
                    // case A
                    // the added node is closer to the root node than the left subtree is to the root node
                    // so this new node should actually be on the left
                    root.threshold = dist;
                    // this is fine since all the nodes that could be on the left tree must be the same edit distance away from root
                    // this is because the only case that could have been invoked prior to this one is case C (if it was invoked at all)
                    // otherwise we would have a right subtree already
                    right = left;
                    left = new VantagePointTree<>(node);
                } else if (dist > root.threshold) {
                    // case B
                    // the added node is farther from the root node than the left subtree is from the root node
                    // so we should just make this the right subtree and everything will be fine
                    right = new VantagePointTree<>(node);
                } else {
                    // case C
                    // both the left subtree and the new node are the same distance from the root node
                    // so add the new node to the left subtree
                    ((VantagePointTree<T>) left).add(node);
                }
            } else {
                // we have both subtrees != null
                if (dist <= root.threshold) {
                    // this belongs on the left
                    ((VantagePointTree<T>) left).add(node);
                } else {
                    // this belongs on the right
                    ((VantagePointTree<T>) right).add(node);
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
    public List<DistanceQueue.Item<VantagePointTree<T>>> search(String query, int nns, boolean exhaustive) {
        // https://fribbels.github.io/vptree/writeup
        int tau = 1_000_000_000;//Integer.MAX_VALUE;
        Queue<VantagePointTree<T>> toSearch = new LinkedList<>();
        toSearch.add(this);

        DistanceQueue<VantagePointTree<T>> results = new DistanceQueue<>(nns);
        DistanceCache<T> distanceCache = new DistanceCache<>(query);

        while (toSearch.size() > 0) {
            VantagePointTree<T> current = toSearch.poll();
            if (current == null) {
                continue;
            }

            DebugHelper.getInstance().hit("VantagePointTree.search/body");

            if (exhaustive) {
                // search every single node
                results.add(distanceCache.distance(current), current);
                toSearch.add((VantagePointTree<T>) current.left);
                toSearch.add((VantagePointTree<T>) current.right);
            } else {
                int dist;
                // search intelligently
                if (
                    distanceCache.getLowerBound(current) < tau
                    && (dist = distanceCache.distance(current)) < tau
                ) {
                    DebugHelper.getInstance().hit("VantagePointTree.search/body/1");
                    results.add(dist, current);
                    if (results.size() == nns) {
                        tau = results.getWorstPriority();
                    }
                    // search again? edit: no this causes an infinite loop
                    //toSearch.add(current);
                }

                if (
                    distanceCache.getLowerBound(current) < current.root.threshold + tau
                    && (dist = distanceCache.distance(current)) < current.root.threshold + tau
                ) {
                    DebugHelper.getInstance().hit("VantagePointTree.search/body/2");
                    toSearch.add((VantagePointTree<T>) current.left);
                }

                if (
                    distanceCache.getUpperBound(current) >= current.root.threshold - tau
                    && (dist = distanceCache.distance(current)) >= current.root.threshold - tau
                ) {
                    DebugHelper.getInstance().hit("VantagePointTree.search/body/3");
                    toSearch.add((VantagePointTree<T>) current.right);
                }
            }
        }

        List<DistanceQueue.Item<VantagePointTree<T>>> l = results.getAll();

        if (l.size() < nns) {
            System.out.println("NOTE: Some results may have been omitted due to tree layout.");
        }

        return l;
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

        output.balance();

        output.verify();

        return output;
    }

    /**
     * Create a new imperfectly-optimized vp-tree with the contents of this one.
     * @return a new vp-tree
     */
    public VantagePointTree<T> optimize() {
        LinkedList<T> nodes = this.getAllNodes();

        VantagePointTree<T> optimized = new VantagePointTree<>(nodes.poll());

        while (nodes.size() > 0) {
            optimized.add(nodes.poll());
        }

        optimized.verify();

        return optimized;
    }

    @Override
    public void verify() {
        if (left != null) {
            int leftDist = EditDistance.measure(left.root.getRawData(), root.getRawData());
            assert leftDist <= root.threshold;
            if (right != null) {
                int rightDist = EditDistance.measure(right.root.getRawData(), root.getRawData());
                assert rightDist > root.threshold;
                assert rightDist > leftDist;
            }
        }

        super.verify();
    }
}