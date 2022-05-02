import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * The whole enchalada.
 * 
 * i.e., the main application
 */
public class ProteinSearch {
    Scanner scanner = new Scanner(System.in);
    VantagePointTree<FASTAFile> tree;
    String directory;

    /**
     * Instantiate a new instance of the CLI app, and build the index.
     * @param directory The directory to index FASTA files from
     */
    public ProteinSearch(String directory) {
        this.directory = directory;
        this.preStartup();
        this.buildIndex();
    }

    /**
     * Allow the user to change performance settings before the tree is indexed
     */
    public void preStartup() {
        Prompt prompt = new Prompt(this.scanner);
        prompt.setQuery("What do you want to do?");
        prompt.addVoidOption("[debug] change config options", ConfigMenu::displayMenu);
        prompt.addDoneOption("continue normal startup");
        prompt.promptUntilDone();
    }

    /**
     * (re?)build the index.
     */
    public void buildIndex() {
        DebugHelper.getInstance().lap();

        System.out.println("Building index...");
        Stopwatch watch = Stopwatch.tick(); // Time is ticking!

        File[] files = new File(directory).listFiles();
        List<File> fileList = Arrays.asList(files);
        // We do this to make the tree behavior a bit better
        // Sequential filenames are usually related proteins
        Collections.shuffle(fileList);
        
        tree = VantagePointTree.buildFromIterator(
            fileList.stream()
            .filter(f -> f.isFile()) // make sure it's not a directory or something weird
            .<FASTAFile>flatMap(f -> {
                if (f.getName().endsWith(".fasta")) {
                    // load normal .fasta files
                    FASTAFile fasta = new FASTAFile(f.getAbsolutePath());

                    try {
                        // if this fails, the file is definitely not going to work
                        fasta.getFASTAData();
                        // make it in a stream of itself (we're flatmapping here)
                        return Stream.of(fasta);
                    } catch (Exception e) {
                        System.out.printf("Encountered error while loading %s (skipping):%n", f.getAbsolutePath());
                        e.printStackTrace();
                    }

                    // Loading the file didn't work.
                } else if (ConfigMenu.LOAD_MULTIFASTA && f.getName().endsWith(".multifasta")) {
                    // load .multifasta files
                    try {
                        // if this fails, the file is definitely not going to work
                        // read each segment of the multifile
                        return MultiFASTAFile.readFiles(f.getAbsolutePath()).stream()
                            .map(fasta -> {
                                try {
                                    // ensure that we can actually load each segment
                                    fasta.getFASTAData();
                                    return (FASTAFile) fasta;
                                } catch (Exception e) {
                                    System.out.printf("Encountered error while loading %s (skipping):%n", fasta.toString());
                                }

                                // if not, don't include it
                                return null;
                            })
                            .filter(Objects::nonNull); // remove failed entries
                    } catch (Exception e) {
                        // the whole .multifasta failed
                        System.out.printf("Encountered error while loading %s (skipping):%n", f.getAbsolutePath());
                        e.printStackTrace();
                    }
                }

                // Return an empty stream if we couldn't get it working.
                return Stream.empty();
            })
            .filter(Objects::nonNull) // remove all nulls (there shouldn't be any)
            .iterator()
        );
        DebugHelper.getInstance().lap();
        System.out.printf("Done in %d ms. (%d nodes)%n", watch.tock(), tree.getSize());
    }

    /**
     * Perform a search for a close FASTA sequence.
     * @param scanner The scanner used for user input.
     * @param exhaustive Whether to perform an exhaustive search.
     */
    public void doSearch(boolean exhaustive) {
        // get the filename of the sequence we want
        String fn = Prompt.nextLine(scanner, "FASTA Filename:");

        DebugHelper.getInstance().lap();
        FASTAFile query = new FASTAFile(fn);

        try {
            String data = query.getFASTAData();
            System.out.printf("Loaded FASTA sequence: %s%n", query.toFancyString());

            DebugHelper.getInstance().lap();
            FASTAFile.clearCache();

            Stopwatch watch = Stopwatch.tick();
            List<AssociatedPriorityQueue.Item<VantagePointTree<FASTAFile>>> results = tree.search(data, ConfigMenu.NUM_NEIGHBORS, exhaustive);
            DebugHelper.getInstance().lap();

            System.out.printf("Found these results in %d ms:%n", watch.tock());
            for (int i = 0; i < results.size(); i++) {
                AssociatedPriorityQueue.Item<VantagePointTree<FASTAFile>> item = results.get(i);

                System.out.printf("%2d) Distance %5d, %s%n", i + 1, item.priority, item.data.root.toFancyString());
            }
        } catch (IOException e) {
            System.out.println("Failed to load FASTA sequence! Try again.");
            e.printStackTrace();
        }
    }

    /**
     * Run the CLI app until the user exits.
     */
    public void run() {
        Prompt prompt = new Prompt(scanner);
        prompt.setQuery("What do you want to do?");

        prompt.addVoidOption("Perform a search", p -> doSearch(false));
        prompt.addVoidOption("[debug] Perform a long, exhaustive search", p -> doSearch(true));
        prompt.addVoidOption("[debug] Change configuration options", ConfigMenu::displayMenu);
        prompt.addVoidOption("[debug] Print tree (probably a bad idea)", p -> tree.print());
        prompt.addVoidOption("[debug] Rebuild tree", p -> {
            String newPath = Prompt.nextLine(p.getScanner(), "Enter the new directory path (leave blank for same):");

            directory = newPath.isBlank() ? directory : newPath;
            
            this.buildIndex();
        });
        prompt.addDoneOption("Quit");

        prompt.promptUntilDone();
    }

    public static void main(String[] args) {
        // Our awful recursive edit distance implementation loves to blow through the stack.
        // Who would have thought...
        // It's probably too late to submit another project proposal, so let's hack together some other solution...
        // We can just run this in a new Thread with a different stack size.
        // You can change to a better implementation in the debug settings
        Thread workaroundThread = new Thread(null, new Runnable() {
            @Override
            public void run() {
                String dir = args.length > 0 ? args[0] : ".";
                new ProteinSearch(dir).run();
            }
        }, "'why did i have to make it recursive' Thread", 1L << 28);
        
        workaroundThread.start();

        try {
            workaroundThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
