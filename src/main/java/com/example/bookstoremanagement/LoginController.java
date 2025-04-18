package com.example.bookstoremanagement;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    private UserManager userManager;
    private Stage primaryStage;
    private Label errorLabel;

    public LoginController(UserManager userManager, Stage primaryStage) {
        this.userManager = userManager;
        this.primaryStage = primaryStage;
    }

    public void showLoginScreen() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, errorLabel);

        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        User user = userManager.authenticate(username, password);
        if (user != null) {
            try {
                BookManager bookManager = new BookManager();
                MainController mainController = new MainController(bookManager, userManager, user, primaryStage);
                mainController.showMainScreen();
            } catch (Exception e) {
                showError("Error initializing main screen: " + e.getMessage());
            }
        } else {
            showError("Invalid username or password.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}