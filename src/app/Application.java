package app;

import model.Carrinho;
import ui.LivrariaCLI;

public class Application {

    private static final boolean USE_ANSI = true;

    public static void main(String[] args) {
        LivrariaCLI cli = new LivrariaCLI(System.in, System.out, USE_ANSI, new Carrinho());
        cli.repl();
        cli.close();
    }
}
