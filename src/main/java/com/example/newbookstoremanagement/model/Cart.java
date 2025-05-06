package com.example.newbookstoremanagement.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;

    public Cart() {
        items = new ArrayList<>();
    }

    public void addItem(Book book, int quantity) {
        for (CartItem item : items) {
            if (item.getBook().getIsbn().equals(book.getIsbn())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(book, quantity));
    }

    public void removeItem(String isbn) {
        items.removeIf(item -> item.getBook().getIsbn().equals(isbn));
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getTotalPrice() {
        return items.stream().mapToInt(item -> (int) (item.getBook().getPrice() * item.getQuantity())).sum();
    }

    public static class CartItem {
        private Book book;
        private int quantity;

        public CartItem(Book book, int quantity) {
            this.book = book;
            this.quantity = quantity;
        }

        public Book getBook() {
            return book;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}