import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Tree<T> {
    // The node for this tree. Not necessarily the root for the entire tree (this tree may be a subtree).
    public T root = null;

    public Tree<T> parent = null;
    public Tree<T> left = null;
    public Tree<T> right = null;

    public Tree(T root) {
        this.root = root;
    }

    /**
     * Pretty-print the tree
     * @param indent the indenting String to use for this depth
     */
    protected void print(String indent) {
        System.out.printf("%s- %s%n", indent, root);

        if (left != null) {
            left.print(indent + "  ");
        }

        if (right != null) {
            right.print(indent + " ");
        }
    }
    
    /**
     * Pretty-print the tree
     */
    public void print() {
        print("");
    }

    public void makeOrphan() {
        if (this.parent != null) {
            if (this.parent.left == this) {
                this.parent.setLeft(null);
            } else if (this.parent.right == this) {
                this.parent.setRight(null);
            }

            this.parent = null;
        }
    }

    public void setLeft(Tree<T> tree) {
        if (left != null && left.parent == this) {
            left.parent = null;
            left = null;
        }

        if (tree != null) {
            tree.makeOrphan();

            tree.parent = this;
        }

        left = tree;
    }

    public void setRight(Tree<T> tree) {
        if (right != null && right.parent == this) {
            right.parent = null;
            right = null;
        }

        if (tree != null) {
            tree.makeOrphan();

            tree.parent = this;
        }

        right = tree;
    }

    /**
     * Perform a left tree rotation with this node as the root.
     * @return The new tree to replace this one
     * @throws NoSuchElementException if there are not enough subtrees to perform this operation
     */
    public Tree<T> rotateLeft() throws NoSuchElementException {
        // https://en.wikipedia.org/wiki/Tree_rotation#/media/File:Tree_rotation.svg

        if (left == null || right == null || right.left == null || right.right == null) {
            throw new NoSuchElementException();
        }

        DebugHelper.getInstance().hit("Tree.rotateLeft");

        Tree<T> p = this;
        Tree<T> q = this.right;

        Tree<T> a = p.left;
        Tree<T> b = q.left;
        Tree<T> c = q.right;

        // already done
        //p.left = a;
        q.setLeft(p);
        // already done
        //q.right = c;
        p.setRight(b);

        return q;
    }

    /**
     * Perform a right tree rotation with this node as the root.
     * @return The new tree to replace this one
     * @throws NoSuchElementException if there are not enough subtrees to perform this operation
     */
    public Tree<T> rotateRight() throws NoSuchElementException {
        // https://en.wikipedia.org/wiki/Tree_rotation#/media/File:Tree_rotation.svg

        if (left == null || right == null || left.left == null || left.right == null) {
            throw new NoSuchElementException();
        }

        DebugHelper.getInstance().hit("Tree.rotateRight");

        Tree<T> q = this;
        Tree<T> p = this.left;

        Tree<T> a = p.left;
        Tree<T> b = p.right;
        Tree<T> c = q.right;

        // already done
        //p.left = a;
        q.setLeft(b);
        // already done
        //q.right = c;
        p.setRight(q);

        return p;
    }

    /**
     * Get the maximum depth of either subtree.
     * @return the maximum depth of either subtree
     */
    public int getDepth() {
        if (left == null && right == null) {
            return 1;
        } else if (left == null) {
            return 1 + right.getDepth();
        } else if (right == null) {
            return 1 + left.getDepth();
        } else {
            return 1 + Math.max(left.getDepth(), right.getDepth());
        }
    }

    /**
     * Get the "tilt" of the tree.
     * @return The depth of the right side of the tree minus the depth of the left side.
     */
    public int getTilt() {
        int leftDepth = left.getDepth();
        int rightDepth = right.getDepth();

        return rightDepth - leftDepth;
    }


    /**
     * Get the "fullness" of the tree.
     * @return true if both the left and right subtrees exist
     */
    public boolean isFull() {
        return left != null && right != null;
    }

    /**
     * Perform tree rotations to roughly equalize depth.
     */
    public void balance() {
        if (left == null || right == null) {
            return;
        }

        left.balance();
        right.balance();

        if (left.isFull()) {
            try {
                while (left.getTilt() > 1) {
                    setLeft(left.rotateLeft());
                }

                while (left.getTilt() < -1) {
                    setLeft(left.rotateRight());
                }
            } catch (NoSuchElementException e) {}
        }

        if (right.isFull()) {
            try {
                while (right.getTilt() > 1) {
                    setRight(right.rotateLeft());
                }

                while (right.getTilt() < -1) {
                    setRight(right.rotateRight());
                }
            } catch (NoSuchElementException e) {
                // do nothing
            }
        }
    }

    /**
     * Verify the integrity of the tree.
     */
    public void verify() {
        if (parent != null) {
            assert parent.left == this || parent.right == this;
        }

        if (left != null) {
            assert left.parent == this;
            left.verify();
        }

        if (right != null) {
            assert right.parent == this;
            right.verify();
        }
    }

    /**
     * Get all nodes in this tree, recursively, starting with the leftmost nodes.
     * @return a LinkedList<T> of all nodes
     */
    public LinkedList<T> getAllNodes() {
        LinkedList<T> l = new LinkedList<>();

        if (left != null) {
            l.addAll(left.getAllNodes());
        }

        l.add(root);

        if (right != null) {
            l.addAll(right.getAllNodes());
        }

        return l;
    }

    /**
     * Get the number of nodes in this subtree.
     * @return the number of nodes in this subtree
     */
    public int getSize() {
        return 1 + (left == null ? 0 : left.getSize()) + (right == null ? 0 : right.getSize());
    }
}
