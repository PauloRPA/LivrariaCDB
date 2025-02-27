package app;

public class Application {

    private static final boolean USE_ANSI = true;

    public static void main(String[] args) {
        LivrariaCLI cli = new LivrariaCLI(System.in, System.out, USE_ANSI);
        cli.repl();
        cli.close();
    }
}
