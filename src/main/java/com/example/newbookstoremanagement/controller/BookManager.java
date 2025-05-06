package com.example.newbookstoremanagement.controller;

import com.example.newbookstoremanagement.model.Book;
import com.example.newbookstoremanagement.model.Cart;

import java.io.*;
import java.util.ArrayList;

public class BookManager {
    private ArrayList<Book> books;
    private final String csvFilePath = "books.csv";
    private final String coverImageDir = "book_covers"; // Folder for cover images

    public BookManager() {
        books = new ArrayList<>();
        // Create book_covers directory if it doesn't exist
        File coverDir = new File(coverImageDir);
        if (!coverDir.exists()) {
            coverDir.mkdirs();
            System.out.println("Created book_covers directory at: " + coverDir.getAbsolutePath());
        }
        loadBooksFromCSV();
    }

    public void addStock(Book b) throws IOException {
        if (b == null || b.getIsbn() == null || b.getTitle() == null || b.getAuthor() == null) {
            throw new IllegalArgumentException("Invalid book data");
        }
        for (Book book : books) {
            if (book.getIsbn().equals(b.getIsbn())) {
                book.setQty(book.getQty() + b.getQty());
                // Preserve coverImage if not updating
                if (b.getCoverImage() != null && !b.getCoverImage().isEmpty()) {
                    book.setCoverImage(b.getCoverImage());
                }
                updateBooks();
                return;
            }
        }
        books.add(b);
        updateBooks();
    }

    public void removeStock(Book b) throws IOException {
        if (b == null || b.getIsbn() == null) {
            throw new IllegalArgumentException("Invalid book data");
        }
        for (Book book : books) {
            if (book.getIsbn().equals(b.getIsbn())) {
                if (book.getQty() <= b.getQty()) {
                    books.remove(book);
                } else {
                    book.setQty(book.getQty() - b.getQty());
                }
                updateBooks();
                return;
            }
        }
        throw new IllegalArgumentException("Book not found or insufficient quantity.");
    }

    public void checkoutCart(Cart cart) throws IOException {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        for (Cart.CartItem item : cart.getItems()) {
            Book book = item.getBook();
            int quantity = item.getQuantity();
            boolean found = false;
            for (Book b : books) {
                if (b.getIsbn().equals(book.getIsbn())) {
                    if (b.getQty() < quantity) {
                        throw new IllegalArgumentException("Not enough copies of " + b.getTitle());
                    }
                    b.setQty(b.getQty() - quantity);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Book not found: " + book.getTitle());
            }
        }
        updateBooks();
        cart.clear();
    }

    public ArrayList<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public ArrayList<Book> searchBooks(String keyword) {
        ArrayList<Book> result = new ArrayList<>();
        if (keyword == null) return result;
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    b.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                    b.getIsbn().equals(keyword)) {
                result.add(b);
            }
        }
        return result;
    }

    private void loadBooksFromCSV() {
        books.clear();
        File file = new File(csvFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Created new books.csv at: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating books.csv: " + e.getMessage());
            }
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] fields = parseCSVLine(line);
                if (fields.length >= 5) { // Expect at least 5 fields, coverImage is optional
                    try {
                        String title = fields[0].trim();
                        String author = fields[1].trim();
                        String isbn = fields[2].trim();
                        int qty = Integer.parseInt(fields[3].trim());
                        int price = Integer.parseInt(fields[4].trim());
                        String coverImage = fields.length > 5 ? fields[5].trim() : "";
                        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                            System.err.println("Skipping line " + lineNumber + ": Empty title, author, or ISBN");
                            continue;
                        }
                        if (qty < 0 || price < 0) {
                            System.err.println("Skipping line " + lineNumber + ": Negative qty or price");
                            continue;
                        }
                        Book book = new Book();
                        book.setTitle(title);
                        book.setAuthor(author);
                        book.setIsbn(isbn);
                        book.setQty(qty);
                        book.setPrice(price);
                        // Set cover image filename based on ISBN if not provided in CSV
                        if (coverImage.isEmpty()) {
                            book.setCoverImage(isbn + "_cover.jpg"); // Changed to .jpg
                        } else {
                            book.setCoverImage(coverImage);
                        }
                        books.add(book);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line " + lineNumber + ": Invalid number format in CSV - " + line);
                    }
                } else {
                    System.err.println("Skipping line " + lineNumber + ": Malformed CSV line - " + line);
                }
            }
            System.out.println("Loaded " + books.size() + " books from books.csv");
        } catch (IOException e) {
            System.err.println("Error reading books.csv: " + e.getMessage());
        }
    }

    private void updateBooks() throws IOException {
        File file = new File(csvFilePath);
        File tempFile = new File(csvFilePath + ".tmp");

        // Write to temporary file
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("title,author,isbn,qty,price,coverImage\n"); // Updated header
            for (Book b : books) {
                if (b.getTitle() == null || b.getAuthor() == null || b.getIsbn() == null) {
                    System.err.println("Skipping invalid book during export: " + b);
                    continue;
                }
                writer.write(String.format("%s,%s,%s,%d,%d,%s\n",
                        escapeCSV(b.getTitle()),
                        escapeCSV(b.getAuthor()),
                        escapeCSV(b.getIsbn()),
                        b.getQty(),
                        b.getPrice(),
                        escapeCSV(b.getCoverImage() == null ? "" : b.getCoverImage())));
            }
            writer.flush();
        } catch (IOException e) {
            tempFile.delete();
            throw new IOException("Failed to write temporary books.csv: " + e.getMessage());
        }

        // Replace original file only if write succeeds
        if (!file.exists() || file.delete()) {
            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file to books.csv");
            }
        } else {
            tempFile.delete();
            throw new IOException("Failed to delete original books.csv");
        }
        System.out.println("Updated books.csv with " + books.size() + " books");
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCSVLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    // Utility to get the absolute path of a cover image
    public String getCoverImagePath(String coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            return null;
        }
        return new File(coverImageDir, coverImage).getAbsolutePath();
    }
}