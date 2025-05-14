package com.example.newbookstoremanagement.controller;

import com.example.newbookstoremanagement.model.User;

import java.io.*;
import java.util.ArrayList;

public class UserManager {
    private final ArrayList<User> users;
    private final String csvFilePath = "users.csv";

    public UserManager() {
        users = new ArrayList<>();
        try {
            loadUsers();
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void loadUsers() throws IOException {
        users.clear();
        File file = new File(csvFilePath);
        if (!file.exists()) {
            // Create file with default admin user
            User admin = new User();
            admin.setUsername(System.getenv().getOrDefault("ADMIN_USERNAME", "administrator"));
            admin.setPassword(System.getenv().getOrDefault("ADMIN_PASSWORD", "SecurePass#2025"));
            admin.setRole("admin");
            users.add(admin);
            updateUsers();
            System.out.println("Created new users.csv at: " + file.getAbsolutePath());
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
                String[] userInfo = parseCSVLine(line);
                if (userInfo.length >= 3) { // Now expecting 3 fields: username, password, role
                    String username = userInfo[0].trim();
                    String password = userInfo[1].trim();
                    String role = userInfo[2].trim();
                    if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                        System.err.println("Skipping line " + lineNumber + ": Empty username, password, or role");
                        continue;
                    }
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setRole(role);
                    users.add(user);
                } else {
                    System.err.println("Skipping line " + lineNumber + ": Malformed CSV line - " + line);
                }
            }
            System.out.println("Loaded " + users.size() + " users from users.csv");
        }
    }

    public void addUser(User user) throws IOException {
        if (user == null || user.getUsername() == null || user.getPassword() == null ||
                user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Invalid user data");
        }
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("User already exists.");
            }
        }
        // Set default role to "customer" if not specified
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("customer");
        }
        users.add(user);
        updateUsers();
    }

    public void removeUser(User user) throws IOException {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }
        users.remove(user);
        updateUsers();
    }

    public ArrayList<User> getUsers() {
        return new ArrayList<>(users);
    }

    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public void updateUsers() throws IOException {
        File file = new File(csvFilePath);
        File tempFile = new File(csvFilePath + ".tmp");

        // Write to temporary file
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("username,password,role\n"); // Updated header
            for (User user : users) {
                if (user.getUsername() == null || user.getPassword() == null || user.getRole() == null) {
                    System.err.println("Skipping invalid user during export: " + user);
                    continue;
                }
                writer.write(String.format("%s,%s,%s\n",
                        escapeCSV(user.getUsername()),
                        escapeCSV(user.getPassword()),
                        escapeCSV(user.getRole())));
            }
            writer.flush();
        } catch (IOException e) {
            tempFile.delete();
            throw new IOException("Failed to write temporary users.csv: " + e.getMessage());
        }

        // Replace original file only if write succeeds
        if (!file.exists() || file.delete()) {
            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file to users.csv");
            }
        } else {
            tempFile.delete();
            throw new IOException("Failed to delete original users.csv");
        }
        System.out.println("Updated users.csv with " + users.size() + " users");
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
}