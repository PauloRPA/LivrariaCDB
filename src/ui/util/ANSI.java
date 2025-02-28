package ui.util;

public class ANSI {

    public final static String ESC = "\u001b";
    public final static String CLEAR = ESC + "[H" + ESC + "[J";
    public final static String RESET = ESC + "[0m";

    public static final String YELLOW = ESC + "[33m";
    public static final String BRIGHT_RED = ESC + "[91m";
    public static final String GREEN = ESC + "[32m";

    public final static String BG_DARK_GRAY = ESC + "[48;2;70;70;70m";

    public static final String UNDERLINE = ESC + "[4m";
    public static final String BOLD = ESC + "[1m";

    public static String color(String string, String style) {
        return style + string + RESET;
    }

    public static String color(String string, String ... styles) {
        for (String style : styles) {
            string = color(string, style);
        }
        return string;
    }
}
