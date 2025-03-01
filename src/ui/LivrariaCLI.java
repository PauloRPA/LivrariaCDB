package ui;

import model.Carrinho;
import model.EBook;
import model.Livro;
import model.LivroFisico;
import ui.util.Text;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;

import static ui.util.ANSI.*;

public class LivrariaCLI {

    private static final Set<String> EXIT_SEQUENCES = Set.of("q", "quit", "exit");
    private static final String GO_BACK = "[.] Voltar";
    private static final String DOUBLE_INPUT_FIELD_FLAG = "[Double]";
    private static final String CART_MARK_ON = "[X] ";
    private static final String CART_MARK_OFF = "[ ] ";

    private static final int BANNER_SIZE = 70;
    private static final String BANNER_OUTLINE = "=";

    private final Carrinho carrinho;
    private final List<Livro> livraria;
    private final Stack<Screen> screens;
    private final Screen cartScreen;

    private final Scanner scan;
    private final PrintStream out;

    private final Integer NO_SELECTION = -1;
    private Integer bookSelectionIndex;
    private Integer cartSelectionIndex;

    public LivrariaCLI(InputStream in, PrintStream out) {
        this(in, out, new Carrinho(), new ArrayList<>());
    }

    public LivrariaCLI(InputStream in, PrintStream out, Carrinho carrinho, List<Livro> livraria) {
        this.out = out;
        this.scan = new Scanner(in);
        this.screens = new Stack<>();
        this.livraria = livraria;
        this.carrinho = carrinho;
        this.bookSelectionIndex = NO_SELECTION;
        this.cartSelectionIndex = NO_SELECTION;

        this.screens.push(setupBookScreen());
        this.cartScreen = setupCartScreen();
    }

    private Screen setupBookScreen() {
        String header = Text.banner("Books", BANNER_SIZE, BANNER_OUTLINE);
        Supplier<String> content = () -> {
            StringBuilder builder = new StringBuilder();
            Optional<Livro> selected = getSelectedBook();
            for (Livro livro : livraria) {
                String prefixCarrinho = carrinho.contains(livro) ? CART_MARK_ON : CART_MARK_OFF;
                String menuLine = selected.filter(livro::equals)
                        .map(book -> color(book.toString(), BG_DARK_GRAY, BOLD))
                        .orElse(livro.toString());
                builder.append(prefixCarrinho).append(menuLine).append("\n");
            }
            return builder.toString();
        };

        Menu bookMenu = new Menu(header, content, List.of(), "");
        Screen bookScreen = new Screen(bookMenu, scan::nextLine, out::println);
        bookScreen.addOption("[a] Novo livro", this::addBook);
        bookScreen.addOption("[r] Remove livro", this::removeBook);
        bookScreen.addOption("[j] Seleciona próximo livro", this::nextBook);
        bookScreen.addOption("[k] Seleciona livro anterior", this::prevBook);
        bookScreen.addOption("[c] Adiciona livro ao carrinho", this::addToCart);
        bookScreen.addOption("[d] Remove livro do carrinho", this::removeFromCart);
        bookScreen.addOption("[>] Ver carrinho", this::switchToCart);
        return bookScreen;
    }

    private Screen setupCartScreen() {
        String header = Text.banner("Carrinho", BANNER_SIZE, BANNER_OUTLINE);

        Supplier<String> content = () -> {
            StringBuilder text = new StringBuilder();
            for (Livro livro : carrinho.getBooks()) {
                String menuLine = getSelectedCartItem().filter(livro::equals)
                        .map(selected -> color(selected.toString(), BG_DARK_GRAY, BOLD))
                        .orElse(livro.toString());
                text.append(menuLine).append("\n");
            }
            text.append(String.format("\nFrete total: R$%.2f\n", carrinho.calcularFreteTotal()));
            text.append(String.format("Subtotal: R$%.2f\n", carrinho.calcularSubTotal()));
            text.append(String.format("Total: R$%.2f\n", carrinho.calcularTotal()));
            return text.toString();
        };

        Menu screenMenu = new Menu(header, content, List.of(), "");
        Screen cartScreen = new Screen(screenMenu, scan::nextLine, out::println);
        cartScreen.addOption("[r] Remove livro do carrinho", this::removeCartItem);
        cartScreen.addOption("[j] Seleciona próximo item", this::nextCartItem);
        cartScreen.addOption("[k] Seleciona item anterior", this::prevCartItem);
        cartScreen.addOption("[l] Limpar carrinho", this::clearCart);
        cartScreen.addOption(GO_BACK, this::goBack);
        return cartScreen;
    }

    private boolean addBook() {
        String LIVRO_FISICO = "LIVRO FISICO", EBOOK = "EBOOK";
        out.println(getCurrentScreen());
        String selected = getCurrentScreen().readInputOptions("Selecione uma das opcoes", List.of(LIVRO_FISICO, EBOOK));
        if (selected.isBlank())
            return false;

        List<String> inputMessages = new ArrayList<>();
        inputMessages.add("Insira o titulo para o novo livro");
        inputMessages.add("Insira o nome do autor");
        inputMessages.add("Insira o isbn do livro");
        inputMessages.add("Insira o valor do livro " + DOUBLE_INPUT_FIELD_FLAG);

        if (selected.equals(LIVRO_FISICO)) {
            inputMessages.add("Insira o peso do livro " + DOUBLE_INPUT_FIELD_FLAG);
            inputMessages.add("Insira o frete do livro " + DOUBLE_INPUT_FIELD_FLAG);
        } else if (selected.equals(EBOOK)) {
            inputMessages.add("Insira o tamanho em MB do livro " + DOUBLE_INPUT_FIELD_FLAG);
        }

        List<String> userInput = getCurrentScreen().readInputSequence(inputMessages);
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

        this.bookSelectionIndex = selectLast(livraria);
        getCurrentScreen().setMessage("Livro adicionado com sucesso!");
        return true;
    }

    private boolean removeBook() {
        Optional<Livro> selectedBook = getSelectedBook();
        final boolean bookFound = selectedBook.isPresent();

        String message = selectedBook.map((book) -> {
                    livraria.remove(book);
                    carrinho.removeBook(book);
                    this.bookSelectionIndex = select(Math.max(this.bookSelectionIndex - 1, 0), livraria);
                    return "Livro removido com sucesso!";
                })
                .orElse("Nenhum livro selecionado");
        getCurrentScreen().setMessage(message);
        return bookFound;
    }

    private Boolean nextBook() {
        this.bookSelectionIndex = nextIndex(livraria, bookSelectionIndex);
        if (bookSelectionIndex.equals(NO_SELECTION)) {
            getCurrentScreen().setMessage("Lista de livros vazia!");
            return false;
        }

        getCurrentScreen().setMessage("\\/");
        return true;
    }

    private Boolean prevBook() {
        this.bookSelectionIndex = prevIndex(livraria, bookSelectionIndex);
        if (bookSelectionIndex.equals(NO_SELECTION)) {
            getCurrentScreen().setMessage("Lista de livros vazia!");
            return false;
        }

        getCurrentScreen().setMessage("/\\");
        return true;
    }

    private Boolean addToCart() {
        boolean notPresent = getSelectedBook().filter(this.carrinho::addBook).isEmpty();
        if (notPresent) {
            getCurrentScreen().setMessage("O livro ja esta no carrinho");
        }
        if (this.carrinho.getBooks().size() == 1)
            this.cartSelectionIndex = select(0, this.carrinho.getBooks());
        getCurrentScreen().setMessage("Livro adicionado ao carrinho com sucesso");
        return true;
    }

    private Boolean removeFromCart() {
        boolean notPresent = !getSelectedBook().map(this.carrinho::removeBook).orElse(false);
        if (notPresent) {
            getCurrentScreen().setMessage("O livro ja não estava carrinho");
            return false;
        }
        getCurrentScreen().setMessage("Livro removido do carrinho com sucesso");
        this.cartSelectionIndex = selectLast(carrinho.getBooks());
        return true;
    }

    private Boolean switchToCart() {
        this.screens.push(cartScreen);
        return true;
    }

    private Boolean removeCartItem() {
        if (getSelectedCartItem().isEmpty()) {
            getCurrentScreen().setMessage("O carrinho esta vazio");
            return false;
        }

        getSelectedCartItem().ifPresent(carrinho::removeBook);
        this.cartSelectionIndex = select(Math.max(this.cartSelectionIndex - 1, 0), carrinho.getBooks());
        getCurrentScreen().setMessage("Item removido com sucesso");
        return true;
    }

    private Boolean nextCartItem() {
        this.cartSelectionIndex = nextIndex(carrinho.getBooks(), cartSelectionIndex);
        if (cartSelectionIndex.equals(NO_SELECTION)) {
            getCurrentScreen().setMessage("O carrinho esta vazio");
            return false;
        }

        getCurrentScreen().setMessage("\\/");
        return true;
    }

    private Boolean prevCartItem() {
        this.cartSelectionIndex = prevIndex(carrinho.getBooks(), cartSelectionIndex);
        if (cartSelectionIndex.equals(NO_SELECTION)) {
            getCurrentScreen().setMessage("O carrinho esta vazio");
            return false;
        }

        getCurrentScreen().setMessage("/\\");
        return true;
    }

    private Boolean clearCart() {
        this.cartSelectionIndex = NO_SELECTION;
        getCurrentScreen().setMessage("Carrinho esvaziado!");
        return carrinho.clear();
    }

    private Boolean goBack() {
        if (this.screens.size() > 1) {
            this.screens.pop();
            return true;
        }
        return false;
    }

    public void repl() {
        String line = "";
        do {
            getCurrentScreen().runOption(line);
            printScreen();
            line = scan.nextLine().trim();
        } while (!EXIT_SEQUENCES.contains(line) && !this.screens.isEmpty());
    }

    private void printScreen() {
        if (this.screens.isEmpty()) return;
        out.print(getCurrentScreen());
    }

    private Optional<Livro> getSelectedBook() {
        if (isOutOfBounds(livraria, this.bookSelectionIndex))
            return Optional.empty();
        return Optional.of(this.livraria.get(this.bookSelectionIndex));
    }

    private Optional<Livro> getSelectedCartItem() {
        if (isOutOfBounds(carrinho.getBooks(), this.cartSelectionIndex))
            return Optional.empty();
        return Optional.of(this.carrinho.getBooks().get(this.cartSelectionIndex));
    }

    private int select(int index, List<?> list) {
        if (isOutOfBounds(list, index))
            return NO_SELECTION;
        return index;
    }

    private int selectLast(List<?> list) {
        if (isOutOfBounds(list, list.size() - 1))
            return NO_SELECTION;
        return list.size() - 1;
    }

    private int nextIndex(List<?> list, Integer index) {
        if (list.isEmpty())
            return NO_SELECTION;
        return index + 1 >= list.size() ? 0 : index + 1;
    }

    private int prevIndex(List<?> list, Integer index) {
        if (list.isEmpty())
            return NO_SELECTION;
        return index - 1 < 0 ? list.size() - 1 : index - 1;
    }

    private Screen getCurrentScreen() {
        return this.screens.peek();
    }

    private boolean isOutOfBounds(Collection<?> collection, int index) {
        return index < 0 || index >= collection.size();
    }

    public void close() {
        this.scan.close();
    }

}