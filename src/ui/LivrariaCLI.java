package ui;

import model.Carrinho;
import model.EBook;
import model.Livro;
import model.LivroFisico;
import ui.util.ANSI;
import ui.util.Screen;
import ui.util.Text;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ui.util.ANSI.*;

public class LivrariaCLI {

    private static final Set<String> EXIT_SEQUENCES = Set.of("q", "quit", "exit");
    public static final String GO_BACK_OPTION = ".";
    private static final String GO_BACK = "[.] Voltar";

    private static final int BANNER_SIZE = 70;
    private static final String LINE_SEPARATOR = "-";
    private static final String BANNER_OUTLINE = "=";

    private final Carrinho carrinho;
    private final List<Livro> livraria;
    private final Stack<Screen> screens;

    private final Scanner scan;
    private final PrintStream out;
    private final boolean ansi;

    private final Map<String, Supplier<Boolean>> bookOptions;
    private final Map<String, Function<Livro, ?>> cartOptions;

    private final Integer NO_SELECTION = -1;
    private Integer bookSelectionIndex;

    public LivrariaCLI(InputStream in, PrintStream out) {
        this(in, out, true, new Carrinho());
    }

    public LivrariaCLI(InputStream in, PrintStream out, boolean useAnsi, Carrinho carrinho) {
        this.out = out;
        this.scan = new Scanner(in);
        this.ansi = useAnsi;
        this.screens = new Stack<>();
        this.livraria = new ArrayList<>();
        this.carrinho = carrinho;
        this.bookSelectionIndex = NO_SELECTION;
        livraria.add(new LivroFisico("Como fritar um hamburger", "Pudim", "57", 59.9, 0.43, 2.0));
        livraria.add(new LivroFisico("Como cozinhar sem oleo", "Roberto seila", "34", 19.9, 0.83, 1.0));
        livraria.add(new LivroFisico("Fiquei preso no freezer, e agora?", "Roberto seila", "24", 49.8, 2.1, 15.0));
        livraria.add(new EBook("Como não ser frito", "Pudim", "72", 120.3, 2.1));
        livraria.add(new EBook("Como cozinhar sem cozinha", "Roberto seila", "55", 69.9, 0.5));
        livraria.add(new EBook("Como fritar um ovo", "Pudim", "48", 99.9, 1.1));

        this.bookOptions = new LinkedHashMap<>();
        this.bookOptions.put("[a] Novo livro", this::addBook);
        this.bookOptions.put("[r] Remove livro", this::removeBook);
        this.bookOptions.put("[q] Fecha o programa", this::quit);
        this.bookOptions.put("[j] Seleciona próximo livro", this::nextBook);
        this.bookOptions.put("[k] Seleciona livro anterior", this::prevBook);

        this.cartOptions = new LinkedHashMap<>();
        this.cartOptions.put("[a] Adiciona livro ao carrinho", carrinho::addBook);
        this.cartOptions.put("[r] Remove livro do carrinho", carrinho::removeBook);

        this.screens.push(setupBookScreen());
    }

    private boolean addBook() {
        String LIVRO_FISICO = "LIVRO FISICO",
                EBOOK = "EBOOK";
        String selected = readInputOptions(List.of(LIVRO_FISICO, EBOOK));

        if (selected.isBlank())
            return false;

        String title = readInputText("Insira o titulo para o novo livro");
        if (title.isBlank()) return false;

        String author = readInputText("Insira o nome do autor");
        if (author.isBlank()) return false;

        String isbn = readInputText("Insira o isbn do livro");
        if (isbn.isBlank()) return false;

        String priceInput = readInputText("Insira o valor do livro");
        if (priceInput.isBlank() || !isDouble(priceInput))
            return false;

        final double price = Double.parseDouble(priceInput);
        if (selected.equals(LIVRO_FISICO)) {
            String weightInput = readInputText("Insira o peso do livro");
            if (weightInput.isBlank() || !isDouble(weightInput))
                return false;

            String freightInput = readInputText("Insira o frete do livro");
            if (freightInput.isBlank() || !isDouble(freightInput))
                return false;

            final double weight = Double.parseDouble(weightInput);
            final double freight = Double.parseDouble(freightInput);
            livraria.add(new LivroFisico(title, author, isbn, price, weight, freight));

        } else if (selected.equals(EBOOK)) {
            String sizeInMbInput = readInputText("Insira o tamanho em MB do livro");
            if (sizeInMbInput.isBlank() || !isDouble(sizeInMbInput))
                return false;

            final double sizeInMb = Double.parseDouble(sizeInMbInput);
            livraria.add(new EBook(title, author, isbn, price, sizeInMb));
        }
        select(livraria.size() - 1);
        getCurrentScreen().setMessage("Livro adicionado com sucesso!");

        return true;
    }

    private boolean removeBook() {
        Optional<Livro> selectedBook = getSelectedBook();
        final boolean bookFound = selectedBook.isPresent();

        String message = selectedBook.map((book) -> {
                    livraria.remove(book);
                    select(livraria.size() - 1);
                    return "Livro removido com sucesso!";
                })
                .orElse("Nenhum livro selecionado");
        getCurrentScreen().setMessage(message);
        return bookFound;
    }

    private Boolean quit() {
        while (!this.screens.isEmpty()) this.screens.pop();
        return true;
    }

    private Boolean prevBook() {
        if (livraria.isEmpty()) {
            select(NO_SELECTION);
            getCurrentScreen().setMessage("Lista de livros vazia!");
            return false;
        }
        this.bookSelectionIndex = bookSelectionIndex - 1 < 0 ? livraria.size() - 1 : bookSelectionIndex - 1;
        return true;
    }

    private Boolean nextBook() {
        if (livraria.isEmpty()) {
            select(NO_SELECTION);
            getCurrentScreen().setMessage("Lista de livros vazia!");
            return false;
        }
        this.bookSelectionIndex = bookSelectionIndex + 1 >= livraria.size() ? 0 : bookSelectionIndex + 1;
        return true;
    }

    private Screen setupBookScreen() {
        String header = Text.banner("Books", BANNER_SIZE, BANNER_OUTLINE);
        Set<String> bookOptions = this.bookOptions.keySet();
        Supplier<String> content = () -> livraria.stream()
                .map(livro ->
                        getSelectedBook().filter(livro::equals)
                                .map(selected -> color(selected.toString(), BG_DARK_GRAY, BOLD))
                                .orElseGet(livro::toString)
                )
                .collect(Collectors.joining("\n"));
        return new Screen(header, content, bookOptions, "");
    }

    public void repl() {
        String line = "";
        do {
            selectOption(line);
            printScreen();
            line = scan.nextLine().trim();
        } while (!EXIT_SEQUENCES.contains(line) && !screens.isEmpty());
    }

    private String readInputText(String title) {
        String header = Text.banner(title, BANNER_SIZE, BANNER_OUTLINE);
        this.screens.push(new Screen(header, List.of(GO_BACK), ""));

        String line = "";
        do {
            printScreen();
            line = scan.nextLine().trim();

            if (line.isBlank()) getCurrentScreen().setMessage("A entrada não deve ser vazia");
        } while (!line.equals(GO_BACK_OPTION) && line.isBlank());

        this.screens.pop();
        if (line.equals(GO_BACK_OPTION))
            return "";

        return line;
    }

    private String readInputOptions(List<String> options) {
        List<String> menuOptions = new ArrayList<>(options);
        for (int i = 0; i < menuOptions.size(); i++) {
            menuOptions.set(i, String.format("[%d] %s", i, menuOptions.get(i)));
        }
        menuOptions.add(GO_BACK);

        List<String> optionInputs = extractOptionKeys(menuOptions);
        String header = Text.banner("Selecione uma das opções", BANNER_SIZE, BANNER_OUTLINE);
        this.screens.push(new Screen(header, menuOptions, ""));

        String line = "";
        do {
            printScreen();
            line = scan.nextLine().trim();
        } while (!optionInputs.contains(line));

        this.screens.pop();
        if (line.equals(GO_BACK_OPTION))
            return "";

        return options.get(optionInputs.indexOf(line));
    }

    private List<String> extractOptionKeys(List<String> options) {
        return options.stream()
                .map(option -> {
                    final int open = option.indexOf('[') + 1;
                    final int close = option.indexOf(']');
                    return option.substring(open, close);
                })
                .toList();
    }

    private void selectOption(String line) {
        if (line.isBlank()) {
            getCurrentScreen().setMessage("");
            return;
        }
        ;
        this.screens.peek().getOptions().stream()
                .filter(option -> {
                    final int open = option.indexOf('[') + 1;
                    final int close = option.indexOf(']');
                    return option.substring(open, close).equals(line);
                })
                .findFirst()
                .ifPresent(selected -> {
                    getCurrentScreen().setMessage("");

                    if (bookOptions.containsKey(selected)) {
                        Boolean success = bookOptions.get(selected).get();
                        String message = success ? "Operação realizada com sucesso!" : "Operação abortada.";
                        if (getCurrentScreen().getMessage().isBlank())
                            getCurrentScreen().setMessage(message);
                    }
//                    } else if (cartOptions.containsKey(selected)) {
//                        cartOptions.get(selected).apply();
//                    }
                });
    }

    private Screen getCurrentScreen() {
        return screens.peek();
    }

    private void printScreen() {
        if (this.screens.isEmpty()) return;
        if (ansi) out.println(ANSI.CLEAR);
        out.print(this.screens.peek().refreshScreen());
    }

    private void select(int index) {
        if (isOutOfBounds(livraria, index)) {
            this.bookSelectionIndex = NO_SELECTION;
            return;
        }

        this.bookSelectionIndex = index;
    }

    private boolean isOutOfBounds(Collection<?> collection, int index) {
        return index < 0 || index >= collection.size();
    }

    private Optional<Livro> getSelectedBook() {
        if (isOutOfBounds(livraria, this.bookSelectionIndex))
            return Optional.empty();
        return Optional.of(this.livraria.get(this.bookSelectionIndex));
    }

    private boolean isDouble(String valor) {
        return valor.replaceFirst("\\.", "").chars()
                .mapToObj(ch -> (char) ch)
                .allMatch(Character::isDigit);
    }

    public void close() {
        this.scan.close();
    }

}
