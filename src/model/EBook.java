package model;

public class EBook extends Livro{

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
        return "EBook [" +
                " title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", isbn='" + isbn + '\'' +
                ", sizeInMb=" + sizeInMb +
                ']';
    }
}
