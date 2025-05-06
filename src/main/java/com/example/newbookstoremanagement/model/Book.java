package com.example.newbookstoremanagement.model;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private int qty;
    private int price;
    private String coverImage;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
}