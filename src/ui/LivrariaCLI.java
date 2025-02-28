package ui;

import model.Carrinho;
import model.EBook;
import model.Livro;
import model.LivroFisico;
import ui.util.ANSI;
import ui.util.Menu;
import ui.util.Text;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ui.util.ANSI.*;

public class LivrariaCLI {

    private static final Set<String> EXIT_SEQUENCES = Set.of("q", "quit", "exit");
    private static final String GO_BACK_OPTION = ".";
    private static final String GO_BACK = "[.] Voltar";
    private static final String DOUBLE_INPUT_REQUISITE = "[Double]";
    private static final String CART_MARK_ON = "[X] ";
    private static final String CART_MARK_OFF = "[ ] ";

    private static final int BANNER_SIZE = 70;
    private static final String LINE_SEPARATOR = "-";
    private static final String BANNER_OUTLINE = "=";

    private final Carrinho carrinho;
    private final List<Livro> livraria;
    private final Stack<Menu> menus;

    private final Scanner scan;
    private final PrintStream out;
    private final boolean ansi;

    private final Map<String, Supplier<Boolean>> bookOptions;
    private final Map<String, Supplier<Boolean>> cartOptions;

    private final Integer NO_SELECTION = -1;
    private Integer bookSelectionIndex;
    private Integer cartSelectionIndex;

    public LivrariaCLI(InputStream in, PrintStream out) {
        this(in, out, true, new Carrinho());
    }

    public LivrariaCLI(InputStream in, PrintStream out, boolean useAnsi, Carrinho carrinho) {
        this.out = out;
        this.scan = new Scanner(in);
        this.ansi = useAnsi;
        this.menus = new Stack<>();
        this.livraria = new ArrayList<>();
        this.carrinho = carrinho;
        this.bookSelectionIndex = NO_SELECTION;
        this.cartSelectionIndex = NO_SELECTION;
        this.bookOptions = new LinkedHashMap<>();
        this.bookOptions.put("[a] Novo livro", this::addBook);
        this.bookOptions.put("[r] Remove livro", this::removeBook);
        this.bookOptions.put("[q] Fecha o programa", this::quit);
        this.bookOptions.put("[j] Seleciona próximo livro", this::nextBook);
        this.bookOptions.put("[k] Seleciona livro anterior", this::prevBook);
        this.bookOptions.put("[c] Adiciona livro ao carrinho", this::addToCart);
        this.bookOptions.put("[d] Remove livro do carrinho", this::removeFromCart);
        this.bookOptions.put("[>] Ver carrinho", this::switchToCart);

        this.cartOptions = new LinkedHashMap<>();
        this.cartOptions.put("[r] Remove livro do carrinho", this::removeCartItem);
        this.cartOptions.put("[j] Seleciona próximo item", this::nextCartItem);
        this.cartOptions.put("[k] Seleciona item anterior", this::prevCartItem);
        this.cartOptions.put("[l] Limpar carrinho", this::clearCart);
        this.cartOptions.put(GO_BACK, this::goBack);

        this.menus.push(setupBookMenu());
    }

    private Boolean switchToCart() {
        String header = Text.banner("Carrinho", BANNER_SIZE, BANNER_OUTLINE);
        Set<String> cartOptions = this.cartOptions.keySet();

        Supplier<String> content = () -> {
            StringBuilder text = new StringBuilder();
            text.append(carrinho.getBooks().stream()
                    .map(livro ->
                            getSelectedCartItem().filter(livro::equals)
                                    .map(selected -> color(selected.toString(), BG_DARK_GRAY, BOLD))
                                    .orElseGet(livro::toString)
                    )
                    .collect(Collectors.joining("\n")));
            text.append(String.format("\n\nFrete total: %.2f\n", carrinho.calcularFreteTotal()));
            text.append(String.format("Subtotal: %.2f\n", carrinho.calcularSubTotal()));
            text.append(String.format("Total: %.2f\n", carrinho.calcularTotal()));
            return text.toString();
        };
        this.menus.push(new Menu(header, content, cartOptions, ""));
        return true;
    }

    private boolean addBook() {
        String LIVRO_FISICO = "LIVRO FISICO", EBOOK = "EBOOK";
        String selected = readInputOptions(List.of(LIVRO_FISICO, EBOOK));
        if (selected.isBlank())
            return false;

        List<String> inputMessages = new ArrayList<>();
        inputMessages.add("Insira o titulo para o novo livro");
        inputMessages.add("Insira o nome do autor");
        inputMessages.add("Insira o isbn do livro");
        inputMessages.add("Insira o valor do livro " + DOUBLE_INPUT_REQUISITE);

        if (selected.equals(LIVRO_FISICO)) {
            inputMessages.add("Insira o peso do livro " + DOUBLE_INPUT_REQUISITE);
            inputMessages.add("Insira o frete do livro " + DOUBLE_INPUT_REQUISITE);
        } else if (selected.equals(EBOOK)) {
            inputMessages.add("Insira o tamanho em MB do livro " + DOUBLE_INPUT_REQUISITE);
        }

        List<String> userInput = readInputSequence(inputMessages);
        if (userInput.isEmpty()) return false;

        int index = 0;
        String title = userInput.get(index++);
        String author = userInput.get(index++);
        String isbn = userInput.get(index++);
        final double price = Double.parseDouble(userInput.get(index++));

        if (selected.equals(LIVRO_FISICO)) {
            final double weight = Double.parseDouble(userInput.get(index++));
            final double freight = Double.parseDouble(userInput.get(index));
            livraria.add(new LivroFisico(title, author, isbn, price, weight, freight));
        } else if (selected.equals(EBOOK)) {
            final double sizeInMb = Double.parseDouble(userInput.get(index));
            livraria.add(new EBook(title, author, isbn, price, sizeInMb));
        }

        this.bookSelectionIndex = select(livraria.size() - 1, livraria);
        getCurrentMenu().setMessage("Livro adicionado com sucesso!");
        return true;
    }

    private boolean removeBook() {
        Optional<Livro> selectedBook = getSelectedBook();
        final boolean bookFound = selectedBook.isPresent();

        String message = selectedBook.map((book) -> {
                    livraria.remove(book);
                    carrinho.removeBook(book);
                    this.bookSelectionIndex = select(livraria.size() - 1, livraria);
                    return "Livro removido com sucesso!";
                })
                .orElse("Nenhum livro selecionado");
        getCurrentMenu().setMessage(message);
        return bookFound;
    }

    private Boolean removeFromCart() {
        boolean notPresent = !getSelectedBook().map(this.carrinho::removeBook).orElse(false);
        if (notPresent) {
            getCurrentMenu().setMessage("O livro ja não estava carrinho");
            return false;
        }
        getCurrentMenu().setMessage("Livro removido do carrinho com sucesso");
        this.cartSelectionIndex = select(carrinho.getBooks().size() - 1, carrinho.getBooks());
        return true;
    }

    private Boolean addToCart() {
        boolean notPresent = getSelectedBook().filter(this.carrinho::addBook).isEmpty();
        if (notPresent) {
            getCurrentMenu().setMessage("O livro ja esta no carrinho");
        }
        if (this.carrinho.getBooks().size() == 1)
            this.cartSelectionIndex = select(0, this.carrinho.getBooks());
        getCurrentMenu().setMessage("Livro adicionado ao carrinho com sucesso");
        return true;
    }

    private Boolean removeCartItem() {
        if (getSelectedCartItem().isEmpty())
            return false;

        getSelectedCartItem().ifPresent(carrinho::removeBook);
        this.cartSelectionIndex = select(Math.max(this.cartSelectionIndex - 1, 0), carrinho.getBooks());
        getCurrentMenu().setMessage("Item removido com sucesso");
        return true;
    }

    private Boolean clearCart() {
        this.cartSelectionIndex = NO_SELECTION;
        getCurrentMenu().setMessage("Carrinho esvaziado!");
        return carrinho.clear();
    }

    private Boolean prevCartItem() {
        this.cartSelectionIndex = prevSelection(carrinho.getBooks(), cartSelectionIndex);
        if (cartSelectionIndex.equals(NO_SELECTION)) {
            getCurrentMenu().setMessage("O carrinho esta vazio");
            return false;
        }

        getCurrentMenu().setMessage("/\\");
        return true;
    }

    private Boolean nextCartItem() {
        this.cartSelectionIndex = nextSelection(carrinho.getBooks(), cartSelectionIndex);
        if (cartSelectionIndex.equals(NO_SELECTION)) {
            getCurrentMenu().setMessage("O carrinho esta vazio");
            return false;
        }

        getCurrentMenu().setMessage("\\/");
        return true;
    }

    private Boolean prevBook() {
        this.bookSelectionIndex = prevSelection(livraria, bookSelectionIndex);
        if (bookSelectionIndex.equals(NO_SELECTION)) {
            getCurrentMenu().setMessage("Lista de livros vazia!");
            return false;
        }

        getCurrentMenu().setMessage("/\\");
        return true;
    }

    private Boolean nextBook() {
        this.bookSelectionIndex = nextSelection(livraria, bookSelectionIndex);
        if (bookSelectionIndex.equals(NO_SELECTION)) {
            getCurrentMenu().setMessage("Lista de livros vazia!");
            return false;
        }

        getCurrentMenu().setMessage("\\/");
        return true;
    }

    private Integer nextSelection(List<Livro> list, Integer index) {
        if (list.isEmpty())
            return NO_SELECTION;
        return index + 1 >= list.size() ? 0 : index + 1;
    }

    private int prevSelection(List<?> list, Integer index) {
        if (list.isEmpty())
            return NO_SELECTION;
        return index - 1 < 0 ? list.size() - 1 : index - 1;
    }

    private Boolean quit() {
        while (!this.menus.isEmpty()) this.menus.pop();
        return true;
    }

    private Menu setupBookMenu() {
        String header = Text.banner("Books", BANNER_SIZE, BANNER_OUTLINE);
        Set<String> bookOptions = this.bookOptions.keySet();
        Supplier<String> content = () -> livraria.stream()
                .map(livro -> {
                            String prefixCarrinho = carrinho.contains(livro) ? CART_MARK_ON : CART_MARK_OFF;
                            return getSelectedBook().filter(livro::equals)
                                    .map(selected -> {
                                        return prefixCarrinho + color(selected.toString(), BG_DARK_GRAY, BOLD);
                                    })
                                    .orElse(prefixCarrinho + livro.toString());
                        }
                )
                .collect(Collectors.joining("\n"));
        return new Menu(header, content, bookOptions, "");
    }

    public void repl() {
        String line = "";
        do {
            selectOption(line);
            printMenu();
            line = scan.nextLine().trim();
        } while (!EXIT_SEQUENCES.contains(line) && !menus.isEmpty());
    }

    private String readInputDouble() {
        String line = "";
        do {
            printMenu();
            line = scan.nextLine().trim();

            if (line.isBlank()) getCurrentMenu().setMessage("A entrada não deve ser vazia");
            if (!isDouble(line)) getCurrentMenu().setMessage("A entrada deve ser um numero");
        } while (!line.equals(GO_BACK_OPTION) && line.isBlank() || !isDouble(line));

        if (line.equals(GO_BACK_OPTION))
            return "";

        return line;
    }

    private String readInputText() {
        String line = "";
        do {
            printMenu();
            line = scan.nextLine().trim();

            if (line.isBlank()) getCurrentMenu().setMessage("A entrada não deve ser vazia");
        } while (!line.equals(GO_BACK_OPTION) && line.isBlank());

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
        this.menus.push(new Menu(header, menuOptions, ""));

        String line = "";
        do {
            printMenu();
            line = scan.nextLine().trim();
        } while (!optionInputs.contains(line));

        this.menus.pop();
        if (line.equals(GO_BACK_OPTION))
            return "";

        return options.get(optionInputs.indexOf(line));
    }

    private List<String> readInputSequence(List<String> menuHeaders) {
        int step = 0;
        Menu menuBeforeInput = this.menus.peek();
        List<String> userInput = new ArrayList<>(menuHeaders.size());
        while (step >= 0 && step < menuHeaders.size()) {
            String header = Text.banner(menuHeaders.get(step), BANNER_SIZE, BANNER_OUTLINE);
            this.menus.push(new Menu(header, List.of(GO_BACK), ""));

            String input = menuHeaders.get(step).contains(DOUBLE_INPUT_REQUISITE) ? readInputDouble() : readInputText();
            userInput.add(step, input);

            if (userInput.get(step).isBlank()) {
                step--;
                this.menus.pop();
                continue;
            }
            step++;
        }

        while (!this.menus.peek().equals(menuBeforeInput))
            this.menus.pop();

        if (step < 0) {
            userInput.clear();
            return userInput;
        }

        return userInput;
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
            getCurrentMenu().setMessage("");
            return;
        }

        this.menus.peek().getOptions().stream()
                .filter(option -> {
                    final int open = option.indexOf('[') + 1;
                    final int close = option.indexOf(']');
                    return option.substring(open, close).equals(line);
                })
                .findFirst()
                .ifPresent(selected -> {
                    getCurrentMenu().setMessage("");

                    Boolean success = false;
                    if (bookOptions.containsKey(selected)) {
                        success = bookOptions.get(selected).get();
                    } else if (cartOptions.containsKey(selected)) {
                        success = cartOptions.get(selected).get();
                    }

                    String message = success ? "Operação realizada com sucesso!" : "Operação abortada.";
                    if (getCurrentMenu().getMessage().isBlank())
                        getCurrentMenu().setMessage(message);
                });
    }

    private Menu getCurrentMenu() {
        return menus.peek();
    }

    private Boolean goBack() {
        if (menus.size() > 1) {
            menus.pop();
            return true;
        }
        return false;
    }

    private void printMenu() {
        if (this.menus.isEmpty()) return;
        if (ansi) out.println(ANSI.CLEAR);
        out.print(this.menus.peek().refreshMenu());
    }

    private int select(int index, List<?> list) {
        if (isOutOfBounds(list, index)) {
            return NO_SELECTION;
        }

        return index;
    }

    private boolean isOutOfBounds(Collection<?> collection, int index) {
        return index < 0 || index >= collection.size();
    }

    private Optional<Livro> getSelectedCartItem() {
        if (isOutOfBounds(carrinho.getBooks(), this.cartSelectionIndex))
            return Optional.empty();
        return Optional.of(this.carrinho.getBooks().get(this.cartSelectionIndex));
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