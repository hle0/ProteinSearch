import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * A Node representing a FASTA protein sequence
 */
public class FASTAFile extends VantagePointTree.Node {
    /**
     * To run in a reasonable amount of time, cache some of the files.
     * This can skew the timing results quite a bit though so it is frequently cleared.
     */
    private static LossyHashMap<String, List<String>> CACHE = new LossyHashMap<>(1024);

    public static void clearCache() {
        CACHE.clear();
    }

    /** The file path. */
    protected final String location;
    /** The FASTA description. */
    private String description = null;
    /** The length of the sequence. */
    private int length = 0;

    public FASTAFile(String location) {
        this.location = location;
    }

    protected List<String> getLines() throws IOException {
        return Files.readAllLines(new File(location).toPath());
    }

    /**
     * Load the file, populate some cached fields, and return the full FASTA sequence.
     * @return The FASTA protein sequence
     * @throws IOException
     */
    public String getFASTAData() throws IOException {
        List<String> lines;

        // Are the lines in the cache?
        if (CACHE != null && CACHE.containsKey(location)) {
            // Yes!
            lines = CACHE.get(location);
        } else {
            // No :(
            lines = this.getLines();
            if (CACHE != null) CACHE.put(location, lines);
        }

        // get the description
        if (lines.get(0).startsWith(">")) {
            description = lines.get(0).substring(1);
        } else {
            throw new IOException("no description!");
        }

        StringBuffer buffer = new StringBuffer();

        // add ALL the things (amino acids)!
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).toUpperCase();
            if (line.startsWith(">")) {
                // start of a new sequence
                break;
            }

            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);

                if ('A' <= c && c <= 'Z') {
                    // only copy peptides (i.e. letters)
                    buffer.append(c);
                }
            }
        }

        String result = buffer.toString();
        length = result.length();

        return result;
    }

    /**
     * A quick implementation of toString that doesn't actually open the file
     * used in .equals(...)
     */
    @Override
    public String toString() {
        return location;
    }

    @Override
    String getRawData() {
        try {
            return this.getFASTAData();
        } catch (IOException e) {
            e.printStackTrace();
            // this should never really happen
            return "";
        }
    }

    /**
     * @return true if we've already filled out the lazily loaded fields; false otherwise
     */
    private boolean isLoaded() {
        return description != null;
    }

    /**
     * load the description and length, if necessary
     * @throws IOException if the file could not be loaded
     */
    private void load() throws IOException {
        if (!isLoaded()) getFASTAData();
    }

    /**
     * Get the in-file protein description
     * @return string description of the sequence
     * @throws IOException if the file could not be loaded
     */
    public String getDescription() throws IOException {
        load();
        
        return description;
    }

    /**
     * Get the protein length
     * @return length in amino acids
     */
    @Override
    int getLength() {
        // can't throw an IOException here
        assert isLoaded();

        return length;
    }

    /**
     * Return a pretty human-friendly String including the length and protein description
     * @return a human-friendly string
     * @throws IOException the file could not be loaded
     */
    public String toFancyString() throws IOException {
        load();

        return String.format("[length %d] '%s'", getLength(), getDescription());
    }
}
