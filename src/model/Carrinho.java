package model;

import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private final List<Livro> carrinho;

    public Carrinho() {
        this.carrinho = new ArrayList<>();
    }

    public boolean addBook(Livro book) {
        return carrinho.add(book);
    }

    public boolean removeBook(Livro book) {
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

}
