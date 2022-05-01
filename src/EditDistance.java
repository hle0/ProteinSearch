import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for computing edit distance between Strings.
 */
public class EditDistance {
    /**
     * AbstractRuler measures the Levenshtein edit distance between two Strings.
     * The reason this is not simply a static method is two-fold:
     * - Different implementations can present a common interface
     * - Each implementation may want to hold some state (e.g. a cache)
     */
    protected static abstract class AbstractRuler {
        // the first string.
        protected String s;
        // the second string.
        protected String t;

        AbstractRuler(String s, String t) {
            this.s = s;
            this.t = t;
        }

        /**
         * Measure the edit distance between s and t.
         * @return The edit distance, as the sum of insertions, deletions, and substitutions.
         */
        public abstract int measure();
    }

    /**
     * A recursive implementation of AbstractRuler. Slow. Very slow.
     */
    protected static class RecursiveRuler extends AbstractRuler {
        // cache as specified in project proposal
        // it has to use Pairs instead of Points because it hinges on having stupid fast hashCode speed
        // this is honestly an awful way to compute edit distance, but I didn't have time to submit another proposal
        Map<Pair<Integer>, Integer> cache;

        RecursiveRuler(String s, String t) {
            super(s, t);
            // If you'd like, you can try using this TreeMap instead of the HashMap.
            // It wasn't faster in my experience.
            /*
            this.cache = new TreeMap<>(new Comparator<Pair<Integer>>() {
                @Override
                public int compare(Pair<Integer> arg0, Pair<Integer> arg1) {
                    int r = arg0.a - arg1.a;
                    if (r == 0) {
                        r = arg0.b - arg1.b;
                    }
                    return r;
                }
            });
            */
            this.cache = new HashMap<>();
        }

        /**
         * Measure the edit distance between strings starting at the specified offsets
         * @param x The offset into s to start at (s.substring(x))
         * @param y The offset into t to start at (t.substring(y))
         * @return The total edit distance
         */
        private int _measure(int x, int y) {
            // if the substring of s is zero length
            if (x == s.length()) {
                // return the number of remaining characters in t
                return t.length() - y;
            }

            // if the substring of t is zero length
            if (y == t.length()) {
                // return the number of remaining characters in s
                return s.length() - x;
            }

            // The actual substrings
            String ss = s.substring(x);
            String tt = t.substring(y);

            if (ss.equals(tt)) {
                // the substrings are equal
                return 0;
            }

            // The first characters of the substrings
            char a = ss.charAt(0);
            char b = tt.charAt(0);

            if (a == b) {
                // the first chars are equal
                return this.measure(new Pair<>(x + 1, y + 1));
            } else {
                int min = this.measure(new Pair<>(x + 1, y + 1));
                min = Math.min(min, this.measure(new Pair<>(x, y + 1)));
                min = Math.min(min, this.measure(new Pair<>(x + 1, y)));
                return 1 + min;
            }
        }

        // Same as _measure but caches results
        private int measure(Pair<Integer> p) {
            Integer n = cache.get(p);
            if (n == null) {
                n = this._measure(p.a, p.b);
                cache.put(p, n);
            }
            return n;
        }

        @Override
        public int measure() {
            return this.measure(new Pair<Integer>(0, 0));
        }
    }

    /**
     * A much faster, more optimized implementation of AbstractRuler,
     * but one that doesn't follow the pattern outlined in the original project proposal.
     */
    protected static class IterativeRuler extends AbstractRuler {
        IterativeRuler(String s, String t) {
            super(s, t);
        }

        @Override
        public int measure() {
            // https://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_two_matrix_rows
            int m = s.length();
            int n = t.length();
            
            int v0[] = new int[n + 1];
            int v1[] = new int[n + 1];

            for (int i = 0; i <= n; i++) {
                v0[i] = i;
            }

            for (int i = 0; i <= m - 1; i++) {
                v1[0] = i + 1;

                for (int j = 0; j <= n - 1; j++) {
                    int deletionCost = v0[j + 1] + 1;
                    int insertionCost = v1[j] + 1;
                    int substitutionCost = v0[j] + (s.charAt(i) == t.charAt(j) ? 0 : 1);
                    
                    v1[j + 1] = Math.min(Math.min(deletionCost, insertionCost), substitutionCost);
                }

                int tmp[] = v0;
                v0 = v1;
                v1 = tmp;
            }

            return v0[n];
        }
    }


    /**
     * A unit test helper for verifying that the rulers work as intended.
     * You can run the main method here to perform the tests.
     */
    private static class RulerTest {
        /**
         * Test both implementations to ensure that they produce the expect output.
         */
        private static void test(String s, String t, int expected) {
            assert new EditDistance.IterativeRuler(s, t).measure() == expected;
            assert new EditDistance.IterativeRuler(t, s).measure() == expected;
            assert new EditDistance.RecursiveRuler(s, t).measure() == expected;
            assert new EditDistance.RecursiveRuler(t, s).measure() == expected;
        }

        public static void main(String[] args) {
            test("abc", "abc", 0);
            test("abc", "xyz", 3);
            test("abcd", "abc", 1);
            test("abc", "abd", 1);
            test("abcdef", "abc", 3);
            System.out.println("Passed tests!");
        }
    }

    public static int measure(String s, String t) {
        DebugHelper.getInstance().hit("EditDistance.measure");
        // For iterative ruler:
        AbstractRuler ruler = new IterativeRuler(s, t);
        // For recursive ruler, comment out the above line, and uncomment the following:
        //AbstractRuler ruler = new RecursiveRuler(s, t);
        return ruler.measure();
    }
}
