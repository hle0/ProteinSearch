/**
 * Pair represents a pair of two objects of the same supertype.
 * This is very similar to the java.awt.Point class, but it is generic, and has a faster hashCode method.
 * Immutable.
 */
public class Pair<T> {
    /** The first element. */
    public final T a;
    /** The second element. */
    public final T b;
    /** The cached hashCode. */
    private final int cachedHashCode;

    public Pair(T a, T b) {
        this.a = a;
        this.b = b;
        this.cachedHashCode = this._hashCode();
    }

    /** Get the first component. */
    public T getA() {
        return a;
    }

    /** Get the second component. */
    public T getB() {
        return b;
    }

    @Override
    public boolean equals(Object obj) {
        // https://stackoverflow.com/questions/7655396/implementing-the-equals-method-in-java
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.hashCode() != this.hashCode() || obj.getClass() != this.getClass()) {
            return false;
        }

        Pair<T> temp = (Pair<T>) obj;

        return a.equals(temp.a) && b.equals(temp.b);
    }

    /** Actually compute the hashCode and return it. */
    private int _hashCode() {
        // prepare yourself for an awful but fast hash code
        return this.a.hashCode() + (this.b.hashCode() << 16) + (this.b.hashCode() >> 16);
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    /**
     * An extension of Pair<T> where the order does not matter when checking for equality or accessing elements.
     * Does not work for types where a.compareTo(b) does not imply a.equals(b).
     */
    public static class Orderless<T extends Comparable<T>> extends Pair<T> {
        public Orderless(T a, T b) {
            // always the lesser one first
            super(
                a.compareTo(b) < 0 ? a : b,
                a.compareTo(b) < 0 ? b : a
            );
        }
    }
}
