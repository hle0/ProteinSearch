import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * UniProt likes to package their protein database as one big file
 * instead of hundreds of thousands of separate files (which makes sense).
 * If you want to read that file without separating it, you can use this class.
 * 
 * It does seem to incur some sort of additional overhead on my system,
 * (which seems to be related to seeking?) and I'm not entirely sure why.
 */
public class MultiFASTAFile extends FASTAFile {
    /** To run in a reasonable amount of time. */
    private static HashMap<String, FileInputStream> CACHE = new HashMap<>();

    /** Offset from the start of the large container file, in characters, to the section we want */
    private long offset;

    public MultiFASTAFile(String location, long offset) {
        super(location);
        this.offset = offset;
    }
    
    /**
     * Open the file and put it in the list.
     * This currently leaks file descriptors,
     * but they should automatically close on program end anyway.
     * @return the FileInputStream for this MultiFASTAFile.
     */
    protected FileInputStream getFile() {
        return CACHE.computeIfAbsent(location, l -> {
            try {
                return new FileInputStream(l);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    /**
     * We override this because we only want specific lines in the file.
     */
    @Override
    protected List<String> getLines() throws IOException {
        LinkedList<String> lines = new LinkedList<>();

        FileInputStream fileInputStream = this.getFile();
        fileInputStream.getChannel().position(offset);

        try (InputStreamReader reader = new InputStreamReader(fileInputStream)) {
            try (BufferedReader buffered = new BufferedReader(reader)) {
                String line = buffered.readLine();
                if (line == null) {
                    return null;
                }
        
                do {
                    lines.add(line);
                    line = buffered.readLine();
                } while (line != null && !line.startsWith(">"));
        
                return lines;
            }
        }
        
    }

    /**
     * Override this so that toString() is unique.
     */
    @Override
    public String toString() {
        return super.toString() + ":" + offset;
    }

    /**
     * Read all the FASTA files embedded inside a "multi-FASTA" file.
     * @param location The path to the multi-FASTA file
     * @return All the "sub-FASTAs" in this multi-FASTA file, as a List
     * @throws IOException if the file could not be read
     */
    public static List<MultiFASTAFile> readFiles(String location) throws IOException {
        ArrayList<MultiFASTAFile> list = new ArrayList<>();

        try (FileReader fileReader = new FileReader(location)) {
            try (BufferedReader reader = new BufferedReader(fileReader)) {
                int c;
                for (int i = 0; (c = reader.read()) != -1; i++) {
                    // split on '>'
                    if (c == '>') {
                        // i is actually one less than the index of the '>'
                        list.add(new MultiFASTAFile(location, i));
                    }
                }
            }
        }

        return list;
    }
}
