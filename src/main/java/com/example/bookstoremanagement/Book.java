package com.example.bookstoremanagement;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private int qty;
    private int price;

    // Getters
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getIsbn() {
        return isbn;
    }
    public int getQty() {
        return qty;
    }
    public int getPrice() {
        return price;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    public void setPrice(int price) {
        this.price = price;
    }
}
