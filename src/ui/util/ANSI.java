package ui.util;

public class ANSI {

    public final static String ESC = "\u001b";
    public final static String CLEAR = ESC + "[H" + ESC + "[J";
    public final static String RESET = ESC + "[0m";
    public final static String BG_DARK_GRAY = ESC + "[48;2;50;50;50m";

    public static String colorize(String string, String style) {
        return style + string + RESET;
    }
}
