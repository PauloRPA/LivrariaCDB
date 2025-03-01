package ui;

import ui.util.ANSI;
import ui.util.Text;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Screen {

    private static final int BANNER_SIZE = 70;
    private static final String BANNER_OUTLINE = "=";
    private static final String DEFAULT_GO_BACK_OPTION = ".";
    private static final String DEFAULT_GO_BACK = "[.] Voltar";
    private static final String DEFAULT_DOUBLE_INPUT_FIELD_FLAG = "[Double]";

    private final String goBackOptionKey;
    private final String goBackOption;
    private final String doubleInputFlag;
    public static final Supplier<String> EMPTY_CONTENT = () -> "";

    private final Menu menu;
    private final String header;
    private final Supplier<String> content;
    private final Map<String, Supplier<Boolean>> options;

    private final Supplier<String> dataInput;
    private final Consumer<Screen> updateListener;
    private boolean ansi;

    public Screen(Menu menu, Supplier<String> dataInput, Consumer<Screen> updateListener) {
        this(menu, dataInput, updateListener, DEFAULT_GO_BACK_OPTION, DEFAULT_GO_BACK, DEFAULT_DOUBLE_INPUT_FIELD_FLAG);
    }

    public Screen(Menu menu, Supplier<String> dataInput, Consumer<Screen> updateListener,
                  String goBackOptionKey, String goBackOption, String doubleInputFlag) {
        this.goBackOptionKey = goBackOptionKey;
        this.goBackOption = goBackOption;
        this.doubleInputFlag = doubleInputFlag;
        this.menu = menu;
        this.header = menu.getHeader();
        this.options = new LinkedHashMap<>();
        this.content = menu.getContent();
        this.ansi = true;
        this.dataInput = dataInput;
        this.updateListener = updateListener;
        updateMenuOptions();
    }

    public void addOption(String option, Supplier<Boolean> action) {
        this.options.put(option, action);
        updateMenuOptions();
    }

    public void runOption(String line) {
        if (line.isBlank()) {
            setMessage("");
            return;
        }

        getMenu().getOptions().stream()
                .filter(option -> {
                    final int open = option.indexOf('[') + 1;
                    final int close = option.indexOf(']');
                    return option.substring(open, close).equals(line);
                })
                .findFirst()
                .ifPresent(selected -> {
                    setMessage("");

                    Boolean success = false;
                    if (options.containsKey(selected)) {
                        success = options.get(selected).get();
                    }

                    String message = success ? "Operacao realizada com sucesso!" : "Operação abortada.";
                    if (getMenu().getMessage().isBlank())
                        setMessage(message);
                });
    }

    private String readInputDouble(String header) {
        String line = "";
        setSubmenu(header, EMPTY_CONTENT, List.of(goBackOption));
        do {
            line = dataInput.get().trim();

            if (line.isBlank()) setMessage("A entrada não deve ser vazia");
            if (isNotDouble(line)) setMessage("A entrada deve ser um numero");
        } while (!line.equals(goBackOptionKey) && line.isBlank() || isNotDouble(line));
        restoreMenu("");

        if (line.equals(goBackOptionKey))
            return "";

        return line;
    }

    private String readInputText(String header) {
        String line;
        setSubmenu(header, EMPTY_CONTENT, List.of(goBackOption));
        do {
            line = dataInput.get().trim();
            if (line.isBlank()) setMessage("A entrada não deve ser vazia");
        } while (!line.equals(goBackOptionKey) && line.isBlank());
        restoreMenu("");

        if (line.equals(goBackOptionKey))
            return "";

        return line;
    }

    public String readInputOptions(String menuHeader, List<String> options) {
        List<String> menuOptions = new ArrayList<>(options);
        for (int i = 0; i < menuOptions.size(); i++) {
            menuOptions.set(i, String.format("[%d] %s", i, menuOptions.get(i)));
        }
        menuOptions.add(goBackOption);

        List<String> optionKeys = extractOptionKeys(menuOptions);
        String header = Text.banner(menuHeader, BANNER_SIZE, BANNER_OUTLINE);
        setSubmenu(header, EMPTY_CONTENT, menuOptions);

        String line = "";
        do {
            line = dataInput.get().trim();
            setMessage("Opção invalida");
            if (line.isBlank())
                setMessage("A entrada não pode ser vazia");
        } while (!optionKeys.contains(line));

        restoreMenu("");
        if (line.equals(goBackOptionKey)) {
            return "";
        }
        return options.get(optionKeys.indexOf(line));
    }

    public List<String> readInputSequence(List<String> menuHeaders) {
        int step = 0;
        List<String> userInput = new ArrayList<>(menuHeaders.size());
        while (step >= 0 && step < menuHeaders.size()) {
            String header = Text.banner(menuHeaders.get(step), BANNER_SIZE, BANNER_OUTLINE);
            String input = menuHeaders.get(step).contains(doubleInputFlag) ?
                    readInputDouble(header) : readInputText(header);

            userInput.add(step, input);
            if (userInput.get(step).isBlank()) {
                step--;
                continue;
            }
            step++;
        }

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

    private boolean isNotDouble(String valor) {
        return !valor.replaceFirst("\\.", "").chars()
                .mapToObj(ch -> (char) ch)
                .allMatch(Character::isDigit);
    }

    private void updateMenuOptions() {
        getMenu().setOptions(this.options.keySet());
    }

    private Menu getMenu() {
        return this.menu;
    }

    private void restoreMenu() {
        restoreMenu(null);
    }

    private void restoreMenu(String message) {
        getMenu().setHeader(this.header);
        getMenu().setContent(this.content);
        getMenu().setOptions(this.options.keySet());
        if (message == null)
            updateListener.accept(this);
        else
            setMessage(message);
    }

    private void setSubmenu(String header, Supplier<String> content, List<String> options) {
        getMenu().setHeader(header);
        getMenu().setContent(content);
        getMenu().setOptions(options);
        updateListener.accept(this);
    }

    public void setMessage(String message) {
        this.menu.setMessage(message);
        updateListener.accept(this);
    }

    @Override
    public String toString() {
        return ansi ? ANSI.CLEAR + menu.refreshMenu().toString() : menu.refreshMenu().toString();
    }
}