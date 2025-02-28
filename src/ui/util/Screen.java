package ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Screen {

    private String header;
    private Supplier<String> content;
    private final List<String> options;
    private String message;

    private final StringBuilder screen;
    private final StringBuilder screenCache;
    private int headerOffset;

    public Screen(String header, List<String> options, String message) {
        this(header, null, options, message);
    }

    public Screen(String header, Supplier<String> content, Iterable<String> options, String message) {
        this.header = Objects.requireNonNullElse(header, "");
        this.message = Objects.requireNonNullElse(message, "");
        this.content = Objects.requireNonNullElse(content, () -> "");
        this.screen = new StringBuilder();
        this.screenCache = new StringBuilder();
        this.options = new ArrayList<>();
        if (options != null)
            options.forEach(this.options::add);

        buildScreen();
    }

    private void buildScreen() {
        screen.setLength(0);
        screen.append(header);
        screen.append(content.get()).append("\n\n");
        screen.append(listOptions(options));
        screen.append(message).append("\n");

        screenCache.setLength(0);
        screenCache.append(header);
        this.headerOffset = screenCache.length();

        screenCache.append("\n\n");
        screenCache.append(listOptions(options));
    }

    private String listOptions(List<String> options) {
        StringBuilder optionList = new StringBuilder();
        options.forEach(op -> optionList.append(op).append("\n"));
        return optionList.toString();
    }

    public Screen refreshScreen() {
        screen.setLength(0);
        screen.append(screenCache.toString());
        screen.insert(headerOffset, content.get()).append("\n");
        screen.append(message).append("\n");
        return this;
    }

    public String getHeader() {
        return header;
    }

    public Supplier<String> getContent() {
        return content;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getMessage() {
        return message;
    }

    public void setHeader(String header) {
        this.header = header;
        buildScreen();
    }

    public void setContent(Supplier<String> content) {
        this.content = content;
        refreshScreen();
    }

    public void setMessage(String message) {
        this.message = message;
        refreshScreen();
    }

    @Override
    public String toString() {
        return screen.toString();
    }
}
