package model;

import ui.util.ANSI;

import static ui.util.ANSI.*;

public class EBook extends Livro {

    private final Double sizeInMb;

    public EBook(String title, String author, String isbn, Double price, Double sizeInMb) {
        super(title, author, isbn, price);
        this.sizeInMb = sizeInMb;
    }

    public Double getSizeInMb() {
        return sizeInMb;
    }

    @Override
    public String toString() {
        String priceStr = color(String.format("R$%.2f", price), ANSI.GREEN);
        String sizeInMbStr = color(String.format("%.2fMB", sizeInMb), ANSI.BRIGHT_RED);
        return String.format("EBook  #%s - %s [Por: %s] [Preço: %s] [Size: %s]",
                isbn, color(title, UNDERLINE), color(author, YELLOW), priceStr, sizeInMbStr);
    }
}
