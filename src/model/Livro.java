package model;

import ui.util.ANSI;

import java.util.Objects;

import static ui.util.ANSI.YELLOW;
import static ui.util.ANSI.color;

public abstract class Livro {

    protected final String title;
    protected final String author;
    protected final String isbn;
    protected final Double price;

    public Livro(String title, String author, String isbn, Double price) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
    }

    public double totalPrice() {
        return this.price;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Livro livro = (Livro) o;
        return Objects.equals(isbn, livro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }

    @Override
    public String toString() {
        String priceStr = String.format("R$%.2f", price);
        return String.format("#%s - %s [Por: %s] [Preço: %s]",
                isbn, color(title, ANSI.UNDERLINE), color(author, YELLOW), priceStr);
    }
}