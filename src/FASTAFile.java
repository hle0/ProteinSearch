import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * A Node representing a FASTA protein sequence
 */
public class FASTAFile extends VantagePointTree.Node {
    // To run in a reasonable amount of time, cache some of the files.
    // This can skew the results quite a bit though so it is frequently cleared.
    private static LossyHashMap<String, List<String>> CACHE = new LossyHashMap<>(1024);

    public static void clearCache() {
        CACHE.clear();
    }

    // The file path.
    private final String location;
    // The FASTA description.
    private String description = null;
    // The length of the sequence.
    private int length;

    public FASTAFile(String location) {
        this.location = location;
    }

    /**
     * Load the file, populate some cached fields, and return the full FASTA sequence.
     * @return The FASTA protein sequence
     * @throws IOException
     */
    public String getFASTAData() throws IOException {
        List<String> lines;

        if (CACHE != null && CACHE.containsKey(location)) {
            lines = CACHE.get(location);
        } else {
            lines = Files.readAllLines(new File(location).toPath());
            if (CACHE != null) CACHE.put(location, lines);
        }

        if (lines.get(0).startsWith(">")) {
            description = lines.get(0).substring(1);
        }

        StringBuffer buffer = new StringBuffer();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).toUpperCase();
            if (line.startsWith(">")) {
                // start of a new sequence
                break;
            }

            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);

                if ('A' <= c && c <= 'Z') {
                    // only copy peptides
                    buffer.append(c);
                }
            }
        }

        String result = buffer.toString();
        length = result.length();

        return result;
    }

    // A quick implementation of toString that doesn't actually open the file
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

    // true if we've already filled out the lazily loaded fields
    private boolean isLoaded() {
        return description != null;
    }

    // load the lazy fields if necessary
    private void load() throws IOException {
        if (!isLoaded()) getFASTAData();
    }

    // Get the in-file protein description
    public String getDescription() throws IOException {
        load();
        
        return description;
    }

    // Get the number of amino acids
    @Override
    int getLength() {
        try {
            load();
        } catch (IOException e) {
            // shouldn't happen
            e.printStackTrace();
        }

        return length;
    }

    // Return a pretty human-friendly String including the length and protein description
    public String toFancyString() throws IOException {
        return String.format("[length %d] '%s'", getLength(), getDescription());
    }
}
