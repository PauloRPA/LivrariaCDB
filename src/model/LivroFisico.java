package model;

import ui.util.ANSI;

import static ui.util.ANSI.*;

public class LivroFisico extends Livro {

    protected final Double weight;
    protected final Double freight;

    public LivroFisico(String title, String author, String isbn, Double price, Double weight, Double freight) {
        super(title, author, isbn, price);
        this.weight = weight;
        this.freight = freight;
    }

    @Override
    public double totalPrice() {
        return this.price + this.freight;
    }

    public Double getWeight() {
        return weight;
    }

    public Double getFreight() {
        return freight;
    }

    @Override
    public String toString() {
        String priceStr = color(String.format("R$%.2f", price), GREEN),
        freightStr = color(String.format("R$%.2f", freight), GREEN),
        weightStr = color(String.format("%.2f", weight), BRIGHT_RED);
        return String.format("Fisico #%s - %s [Por: %s] [Preco: %s] [Frete: %s] [Peso: %s]",
                isbn, color(title, ANSI.UNDERLINE), color(author, ANSI.YELLOW), priceStr, freightStr, weightStr);
    }

}