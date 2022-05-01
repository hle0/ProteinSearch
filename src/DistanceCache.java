import java.util.HashMap;

public class DistanceCache<T extends VantagePointTree.Node> extends HashMap<VantagePointTree<T>, Pair<Integer>> {
    private String target;

    public DistanceCache(String target) {
        this.target = target;
    }
    
    public int distance(VantagePointTree<T> tree) {
        DebugHelper.getInstance().hit("DistanceCache.distance");
        Pair<Integer> value = this.getBounds(tree);
        if (value == null || !value.a.equals(value.b)) {
            DebugHelper.getInstance().hit("DistanceCache.distance/body");
            // we probably can't cheese the distance calculations via exact lower = upper bound
            int exact = EditDistance.measure(target, tree.root.getRawData());
            value = new Pair<Integer>(exact, exact);
            this.put(tree, value);
        }

        return value.a;
    }

    private Pair<Integer> getNaiveBounds(VantagePointTree<T> tree) {
        return new Pair<Integer>(
            Math.abs(tree.root.getLength() - target.length()), // just additions/deletions
            Math.max(tree.root.getLength(), target.length()) // as many substitutions as possible
        );
    }

    private Pair<Integer> getBounds(VantagePointTree<T> tree) {
        Pair<Integer> result = this.get(tree);

        if (result != null) {
            return result;
        }

        result = getNaiveBounds(tree);

        if (tree.parent != null && tree.parent.left == tree) {
            Pair<Integer> parentBounds = this.getBounds((VantagePointTree<T>) tree.parent);

            // per triangle inequality, i think
            result = new Pair<Integer>(
                Math.max(result.a, parentBounds.a - tree.parent.root.threshold),
                Math.min(result.b, parentBounds.b + tree.parent.root.threshold)
            );
        }

        this.put(tree, result);

        assert result.a <= result.b;

        return result;
    }

    public int getLowerBound(VantagePointTree<T> tree) {
        return this.getBounds(tree).a;
    }

    public int getUpperBound(VantagePointTree<T> tree) {
        return this.getBounds(tree).b;
    }

    public int getRecursiveLowerBound(VantagePointTree<T> tree) {
        int min = this.getLowerBound(tree);

        if (tree.left != null) {
            min = Math.min(min, this.getLowerBound((VantagePointTree<T>) tree.left));
        }

        if (tree.right != null) {
            min = Math.min(min, this.getLowerBound((VantagePointTree<T>) tree.right));
        }

        return min;
    }
}
