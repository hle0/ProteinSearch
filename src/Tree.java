import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A generic binary tree.
 * 
 * Implements common operations such as tree rotations.
 */
public class Tree<T> {
    /** The node for this tree. Not necessarily the root for the entire tree (this tree may be a subtree). */
    public T root = null;

    /** Our parent tree, if we have one, or null. */
    private Tree<T> parent = null;

    /** The left subtree, if we have one, or null. */
    private Tree<T> left = null;
    /** The right subtree, if we have one, or null. */
    private Tree<T> right = null;

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

    public Tree<T> getParent() {
        return parent;
    }

    public Tree<T> getLeft() {
        return left;
    }

    public Tree<T> getRight() {
        return right;
    }

    /**
     * Set the left side of this tree, and orphan the old left side if applicable
     * @param tree the new left side of this tree
     */
    public void setLeft(Tree<T> tree) {
        // fixes oldLeft, oldLeft.parent
        if (left != null) {
            left.parent = null;
            left = null;
        }

        left = tree;

        // fixes oldLeft
        if (left != null) {
            Tree<T> oldParent = left.parent;
            left.parent = this;

            // fixes oldLeft.parent
            if (oldParent != null) {
                assert oldParent.left == left || oldParent.right == left;
                if (oldParent.left == left) {
                    oldParent.left = null;    
                } else {
                    oldParent.right = null;
                }
            }
        }
    }

    /**
     * Set the right side of this tree, and orphan the old right side if applicable
     * @param tree the new right side of this tree
     */
    public void setRight(Tree<T> tree) {
        // fixes oldRight, oldRight.parent
        if (right != null) {
            right.parent = null;
            right = null;
        }

        right = tree;

        // fixes newRight
        if (right != null) {
            Tree<T> oldParent = right.parent;
            right.parent = this;
            
            // fixes newRight.parent
            if (oldParent != null) {
                assert oldParent.left == right || oldParent.right == right;
                if (oldParent.left == right) {
                    oldParent.left = null;    
                } else {
                    oldParent.right = null;
                }
            }
        }
    }

    public void replaceChild(Tree<T> from, Tree<T> to) {
        assert from == this.left || from == this.right;

        if (from == this.left) {
            this.setLeft(to);
        } else {
            this.setRight(to);
        }
    }

    /**
     * Perform a left tree rotation with this node as the root.
     * @throws NoSuchElementException if there are not enough subtrees to perform this operation
     */
    public void rotateLeft() throws NoSuchElementException {
        // https://en.wikipedia.org/wiki/Tree_rotation#/media/File:Tree_Rotations.gif

        if (left == null || right == null || right.left == null || right.right == null) {
            throw new NoSuchElementException();
        }

        DebugHelper.getInstance().hit("Tree.rotateLeft");

        Tree<T> newRoot = this.right;

        this.setRight(this.right.left);
        
        Tree<T> oldParent = this.parent;

        oldParent.replaceChild(this, newRoot);
        
        newRoot.setLeft(this);
    }

    /**
     * Perform a right tree rotation with this node as the root.
     * @throws NoSuchElementException if there are not enough subtrees to perform this operation
     */
    public void rotateRight() throws NoSuchElementException {
        // https://en.wikipedia.org/wiki/Tree_rotation#/media/File:Tree_Rotations.gif

        if (left == null || right == null || left.left == null || left.right == null) {
            throw new NoSuchElementException();
        }

        DebugHelper.getInstance().hit("Tree.rotateRight");

        Tree<T> newRoot = this.left;

        this.setLeft(this.left.right);

        Tree<T> oldParent = this.parent;

        oldParent.replaceChild(this, newRoot);

        newRoot.setRight(this);
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
                    left.rotateLeft();
                }

                while (left.getTilt() < -1) {
                    left.rotateRight();
                }
            } catch (NoSuchElementException e) {}
        }

        if (right.isFull()) {
            try {
                while (right.getTilt() > 1) {
                    right.rotateLeft();
                }

                while (right.getTilt() < -1) {
                    right.rotateRight();
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
