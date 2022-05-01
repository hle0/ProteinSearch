import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

public class ProteinSearch {
    Scanner scanner = new Scanner(System.in);
    VantagePointTree<FASTAFile> tree;

    /**
     * Instantiate a new instance of the CLI app, and build the index.
     * @param directory The directory to index FASTA files from
     */
    public ProteinSearch(String directory) {
        this.preStartup();
        this.buildIndex(directory);
    }

    public void preStartup() {
        Prompt prompt = new Prompt(this.scanner);
        prompt.setQuery("What do you want to do?");
        prompt.addVoidOption("[debug] change config options", ConfigMenu::displayMenu);
        prompt.addDoneOption("continue normal startup");
        prompt.promptUntilDone();
    }

    /**
     * (re?)build the index.
     * @param directory The directory to index FASTA files from
     */
    public void buildIndex(String directory) {
        DebugHelper.getInstance().lap();

        System.out.println("Building index...");
        Stopwatch watch = Stopwatch.tick();

        File[] files = new File(directory).listFiles();
        List<File> fileList = Arrays.asList(files);
        // We do this to make the tree behavior a bit better
        // Sequential filenames are usually related proteins
        Collections.shuffle(fileList);

        
        tree = VantagePointTree.buildFromIterator(
            fileList.stream()
            .filter(f -> f.isFile())
            .<FASTAFile>flatMap(f -> {
                if (f.getName().endsWith(".fasta")) {
                    FASTAFile fasta = new FASTAFile(f.getAbsolutePath());

                    try {
                        // if this fails, the file is definitely not going to work
                        fasta.getFASTAData();
                        return Stream.of(fasta);
                    } catch (Exception e) {
                        System.out.printf("Encountered error while loading %s (skipping):%n", f.getAbsolutePath());
                        e.printStackTrace();
                    }

                    return Stream.<FASTAFile>of(fasta);
                } else if (f.getName().endsWith(".multifasta")) {
                    try {
                        // if this fails, the file is definitely not going to work
                        return MultiFASTAFile.readFiles(f.getAbsolutePath()).stream()
                            .map(fasta -> {
                                try {
                                    fasta.getFASTAData();
                                    return (FASTAFile) fasta;
                                } catch (Exception e) {
                                    System.out.printf("Encountered error while loading %s (skipping):%n", fasta.toString());
                                }

                                return null;
                            })
                            .filter(Objects::nonNull);
                    } catch (Exception e) {
                        System.out.printf("Encountered error while loading %s (skipping):%n", f.getAbsolutePath());
                        e.printStackTrace();
                    }
                }

                return Stream.empty();
            })
            .filter(Objects::nonNull)
            .iterator()
        );
        DebugHelper.getInstance().lap();
        System.out.printf("Done building index in %d ms.%n", watch.tock());

        System.out.println("Optimizing index (this may take a while)...");
        //tree = tree.optimize();
        DebugHelper.getInstance().lap();
        System.out.printf("Done in %d ms. (%d nodes)%n", watch.tock(), tree.getSize());
    }

    /**
     * Perform a search for a close FASTA sequence.
     * @param scanner The scanner used for user input.
     * @param exhaustive Whether to perform an exhaustive search.
     */
    public void doSearch(boolean exhaustive) {
        String fn = Prompt.nextLine(scanner, "FASTA Filename:");

        DebugHelper.getInstance().lap();
        FASTAFile query = new FASTAFile(fn);

        try {
            String data = query.getFASTAData();
            System.out.printf("Loaded FASTA sequence: %s%n", query.toFancyString());

            DebugHelper.getInstance().lap();
            FASTAFile.clearCache();

            Stopwatch watch = Stopwatch.tick();
            List<DistanceQueue.Item<VantagePointTree<FASTAFile>>> results = tree.search(data, ConfigMenu.NUM_NEIGHBORS, exhaustive);
            long timeSpent = watch.tock();
            DebugHelper.getInstance().lap();

            System.out.printf("Found these results in %d ms:%n", timeSpent);
            for (int i = 0; i < results.size(); i++) {
                DistanceQueue.Item<VantagePointTree<FASTAFile>> item = results.get(i);

                System.out.printf("%2d) Distance %5d, %s%n", i + 1, item.priority, item.data.root.toFancyString());
            }
        } catch (IOException e) {
            System.out.println("Failed to load FASTA sequence!");
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
        prompt.addDoneOption("Quit");

        prompt.promptUntilDone();
    }

    public static void main(String[] args) {
        // Our awful recursive edit distance implementation loves to blow through the stack.
        // Who would have thought...
        // It's probably too late to submit another project proposal, so let's hack together some other solution...
        // We can just run this in a new Thread with a different stack size.
        // Note that the default is to just not use the recursive implementation;
        // if you want to enable it, edit the relevant code in EditDistance.measure(...).
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
