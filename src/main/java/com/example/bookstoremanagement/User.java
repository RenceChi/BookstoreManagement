package com.example.bookstoremanagement;

public class User {
    private String username;
    private String password;

    // Note: Cart logic handled in MainController

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
