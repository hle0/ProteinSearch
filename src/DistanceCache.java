import java.util.HashMap;

/**
 * A smart wrapper for EditDistance that computes upper and lower bounds for distance
 * using the triangle inequality rule.
 * This can remove the need for quite a few expensive distance calculations.
 * 
 * Each instance of this class corresponds to a single point to calculate distance to.
 */
public class DistanceCache<T extends VantagePointTree.Node> extends HashMap<VantagePointTree<T>, Pair<Integer>> {
    private String target;

    /**
     * Create a new instance.
     * @param target the point to/from which distances are calculated
     */
    public DistanceCache(String target) {
        this.target = target;
    }
    
    /**
     * Calculate the exact edit distance.
     * @param tree the tree node to calculate distance to
     * @return the exact edit distance from target to tree
     */
    public int distance(VantagePointTree<T> tree) {
        DebugHelper.getInstance().hit("DistanceCache.distance");

        Pair<Integer> value = this.getBounds(tree);
        if (value == null || !value.a.equals(value.b)) {
            DebugHelper.getInstance().hit("DistanceCache.distance/body");
            // we probably can't cheese the distance calculations via exact lower = upper bound
            // so just compute it expensively
            int exact = EditDistance.measure(target, tree.root.getRawData());
            value = new Pair<Integer>(exact, exact);
            this.put(tree, value);
        }

        return value.a; // which is the same as value.b
    }

    /**
     * Get very loose lower and upper bounds on edit distance to/from tree
     * @param tree the point of interest
     * @return a pair of lower and upper bounds
     */
    private Pair<Integer> getNaiveBounds(VantagePointTree<T> tree) {
        return new Pair<Integer>(
            Math.abs(tree.root.getLength() - target.length()), // just additions/deletions
            Math.max(tree.root.getLength(), target.length()) // as many substitutions as possible
        );
    }

    /**
     * Calculate intelligent upper/lower bounds without making calculations
     * @param tree distance is measured from this point to the target
     * @return lower and upper bounds for distance
     */
    private Pair<Integer> getBounds(VantagePointTree<T> tree) {
        Pair<Integer> result = this.get(tree);

        if (result != null) {
            // we have them already calculated
            return result;
        }

        // get naive bounds
        result = getNaiveBounds(tree);

        if (tree.getParent() != null && tree.getParent().getLeft() == tree) {
            // see if we can get stricter bounds
            Pair<Integer> parentBounds = this.getBounds((VantagePointTree<T>) tree.getParent());
            int parentRadius = tree.getParent().root.threshold;

            // per triangle inequality, i think
            result = new Pair<Integer>(
                Math.max(result.a, parentBounds.a - parentRadius),
                Math.min(result.b, parentBounds.b + parentRadius)
            );
        }

        // update the bounds cache
        this.put(tree, result);

        // the lower bound shouldn't be bigger than the upper bound
        assert result.a <= result.b;

        return result;
    }

    /**
     * Get a lower bound for the distance from the target point to a node
     * @param tree the node in question
     * @return the lower bound of distance
     */
    public int getLowerBound(VantagePointTree<T> tree) {
        return this.getBounds(tree).a;
    }

    /**
     * Get an upper bound for the distance from the target point to a node
     * @param tree the node in question
     * @return the upper bound of distance
     */
    public int getUpperBound(VantagePointTree<T> tree) {
        return this.getBounds(tree).b;
    }

    /**
     * Get a recursive lower bound for a subtree - i.e., the lowest lower bound for any of the descendants
     * @param tree the subtree to search
     * @return the recursive lower bound
     */
    public int getRecursiveLowerBound(VantagePointTree<T> tree) {
        int min = this.getLowerBound(tree);

        VantagePointTree<T> left  = (VantagePointTree<T>) tree.getLeft();
        VantagePointTree<T> right = (VantagePointTree<T>) tree.getRight();

        if (left  != null) min = Math.min(min, this.getRecursiveLowerBound(left ));
        if (right != null) min = Math.min(min, this.getRecursiveLowerBound(right));

        return min;
    }
}
