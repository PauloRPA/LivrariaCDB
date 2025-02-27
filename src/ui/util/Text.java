package ui.util;

public class Text {

    private static final String SPACE = " ";

    public static String banner(String title, int prefSize, String lineChar) {
        StringBuilder banner = new StringBuilder();
        final int biggerLength = Math.max(prefSize, title.length());
        int padding = (biggerLength - title.length()) / 2;
        padding += padding % 2 == 0 ? 1 : 0;

        final int size = Math.max(biggerLength, padding * 2 + title.length());
        banner.append(lineln(lineChar, size));
        banner.append(line(SPACE, padding));
        banner.append(title);
        banner.append(lineln(SPACE, padding));
        banner.append(lineln(lineChar, size));
        return banner.toString();
    }

    public static String lineln(String lineChar, int size) {
        return line(lineChar, size) + "\n";
    }

    public static String line(String lineChar, int size) {
        StringBuilder line = new StringBuilder();
        while (line.length() < size) {
            for (int i = 0; line.length() < size; i++) {
                i = i >= lineChar.length() ? 0 : i;
                line.append(lineChar.charAt(i));
            }
        }
        return line.toString();
    }

    public static String msg(String msg, String lineChar) {
        return msg + "\n" + lineln(lineChar, msg.length());
    }

}
