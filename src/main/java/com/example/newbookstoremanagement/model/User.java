package com.example.newbookstoremanagement.model;

public class User {
    private String username;
    private String password;
    private String role; // New field for user role: "customer", "staff", or "admin"

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}