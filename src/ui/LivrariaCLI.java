package ui;

import ui.util.ANSI;
import ui.util.Text;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class LivrariaCLI {

    private static final String SELECTION_FORMAT = "--> %s <--";
    private static final Set<String> EXIT_SEQUENCES = Set.of("q", "quit", "exit");

    private static final int BANNER_SIZE = 40;
    private static final String LINE_SEPARATOR = "-";
    private static final String BANNER_OUTLINE = "=";

    private final Stack<String> screens;

    private final Scanner scan;
    private final PrintStream out;
    private final boolean ansi;

    public LivrariaCLI(InputStream in, PrintStream out) {
        this(in, out, true);
    }

    public LivrariaCLI(InputStream in, PrintStream out, boolean useAnsi) {
        this.out = out;
        this.scan = new Scanner(in);
        this.ansi = useAnsi;
        this.screens = new Stack<>();
    }

    public void repl() {
        printMenu();
        String line = "";
        do {
            selectOption(line);
            printMenu();
            line = scan.nextLine().trim();
        } while (!EXIT_SEQUENCES.contains(line) && !screens.isEmpty());
    }

    private void selectOption(String line) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'selectOption'");
    }

    private void printMenu() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'printMenu'");
    }

    public void close() {
        this.scan.close();
    }

}
