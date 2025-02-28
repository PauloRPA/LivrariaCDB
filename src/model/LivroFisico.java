package model;

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
        return "Livro Físico [" +
                " title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", isbn='" + isbn + '\'' +
                ", weight=" + weight +
                ", freight=" + freight +
                ']';
    }

}