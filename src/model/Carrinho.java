package model;

import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private final List<Livro> carrinho;

    public Carrinho() {
        this.carrinho = new ArrayList<>();
    }

    public boolean addBook(Livro book) {
        if (carrinho.contains(book)) {
            return false;
        }
        return carrinho.add(book);
    }

    public boolean removeBook(Livro book) {
        if (book == null) return false;
        return carrinho.remove(book);
    }

    public double calcularSubTotal() {
        return carrinho.stream()
                .mapToDouble(Livro::getPrice)
                .sum();
    }

    public double calcularFreteTotal() {
        return carrinho.stream()
                .filter(LivroFisico.class::isInstance)
                .map(LivroFisico.class::cast)
                .mapToDouble(LivroFisico::getFreight)
                .sum();
    }

    public double calcularTotal() {
        return carrinho.stream().mapToDouble(Livro::totalPrice).sum();
    }

    public List<Livro> getBooks() {
        return new ArrayList<>(carrinho);
    }

    public boolean contains(Livro livro) {
        return carrinho.contains(livro);
    }

    public Boolean clear() {
        carrinho.clear();
        return true;
    }
}
