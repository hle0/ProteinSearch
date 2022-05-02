/**
 * This class holds some global runtime debug settings and the debug/config menu.
 * If you want to play around with the code, you might need to get familiar with this.
 */
public final class ConfigMenu {
    /**
     * Whether to count and display debug statistics/instrumentation.
     * See DebugHelper.java.
     */
    public static boolean SHOW_DEBUG_STATS = false;

    /**
     * The number of nearest neighbors to find.
     * The project proposal says this should be 3, but you can change it if debug menu is enabled.
     */
    public static int NUM_NEIGHBORS = 3;

    /**
     * If true, use an edit distance calculation that is super fast but incorrectly implemented according to the proposal.
     * If false, use the slow, recursive edit distance calculation that satisfies the proposal.
     * Both give identical results.
     */
    public static boolean USE_FAST_EDIT_DISTANCE = true;

    /**
     * If true, optimize the tree upon loading the index.
     * If false, don't optimize, just balance.
     */
    public static boolean OPTIMIZE_TREE = false;

    /**
     * If true, load .multifasta files. Technically out of spec (?)
     * If false, just ignore them.
     */
    public static boolean LOAD_MULTIFASTA = true;

    /**
     * Display the menu by forking an existing Prompt object.
     * @param parent The existing Prompt object to fork.
     */
    public static void displayMenu(Prompt parent) {
        Prompt prompt = parent.fork();

        prompt.setQuery("What do you want to change?");

        prompt.addVoidOption("Enable/disable debug stats", p -> {
            Prompt subPrompt = prompt.fork();
            subPrompt.setQuery(String.format("Currently, debug stats are %s.", SHOW_DEBUG_STATS ? "enabled" : "disabled"));
            
            subPrompt.addVoidOption("Enable", sp -> {
                SHOW_DEBUG_STATS = true;
            });
            subPrompt.addVoidOption("Disable", sp -> {
                SHOW_DEBUG_STATS = false;
            });

            subPrompt.doPrompt();

            DebugHelper.getInstance().clear();
        });

        prompt.addVoidOption("Number of neighbors to search for", p -> {
            System.out.printf("The current value is %d.%n", NUM_NEIGHBORS);
            NUM_NEIGHBORS = Prompt.nextInt(prompt.getScanner(), "New value:", 1, 25);
        });

        prompt.addVoidOption("Edit distance calculation method", p -> {
            Prompt subPrompt = prompt.fork();
            subPrompt.setQuery(String.format("Currently using %s edit distance method.", USE_FAST_EDIT_DISTANCE ? "fast" : "compliant"));

            subPrompt.addVoidOption("Use fast", sp -> {
                USE_FAST_EDIT_DISTANCE = true;
            });

            subPrompt.addVoidOption("Use compliant", sp -> {
                USE_FAST_EDIT_DISTANCE = false;
            });

            subPrompt.doPrompt();
        });

        prompt.addVoidOption("Enable/disable tree optimization", p -> {
            Prompt subPrompt = prompt.fork();
            subPrompt.setQuery(String.format("Currently, tree optimization during index rebuilding is %s.", OPTIMIZE_TREE ? "enabled" : "disabled"));
            
            subPrompt.addVoidOption("Enable", sp -> {
                OPTIMIZE_TREE = true;
            });
            subPrompt.addVoidOption("Disable", sp -> {
                OPTIMIZE_TREE = false;
            });

            subPrompt.doPrompt();
        });

        prompt.addVoidOption("Enable/disable loading of .multifasta files", p -> {
            Prompt subPrompt = prompt.fork();
            subPrompt.setQuery(String.format("Currently, .multifasta files are %s.", LOAD_MULTIFASTA ? "enabled" : "disabled"));
            
            subPrompt.addVoidOption("Enable", sp -> {
                LOAD_MULTIFASTA = true;
            });
            subPrompt.addVoidOption("Disable", sp -> {
                LOAD_MULTIFASTA = false;
            });

            subPrompt.doPrompt();
        });

        prompt.addDoneOption();
        prompt.promptUntilDone();
    }
}