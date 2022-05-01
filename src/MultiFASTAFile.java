import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MultiFASTAFile extends FASTAFile {
    // To run in a reasonable amount of time.
    private static HashMap<String, FileInputStream> CACHE = new HashMap<>();

    private long offset;

    public MultiFASTAFile(String location, long offset) {
        super(location);
        this.offset = offset;
    }
    
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

    @Override
    public String toString() {
        return super.toString() + ":" + offset;
    }

    public static List<MultiFASTAFile> readFiles(String location) throws IOException {
        ArrayList<MultiFASTAFile> list = new ArrayList<>();

        try (FileReader fileReader = new FileReader(location)) {
            try (BufferedReader reader = new BufferedReader(fileReader)) {
                int c;
                for (int i = 0; (c = reader.read()) != -1; i++) {
                    if (c == '>') {
                        list.add(new MultiFASTAFile(location, i));
                    }
                }
            }
        }

        return list;
    }
}
