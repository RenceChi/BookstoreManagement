package com.example.newbookstoremanagement.view;

import com.example.newbookstoremanagement.controller.BookManager;
import com.example.newbookstoremanagement.model.User;
import com.example.newbookstoremanagement.controller.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
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
        // Create the main pane with a background image
        StackPane root = new StackPane();
        root.setPadding(new Insets(20));

        // Set the background image from resources
        try {
            // Load the image from the resources folder
            String imagePath = getClass().getResource("/background.png").toExternalForm();
            Image backgroundImage = new Image(imagePath);
            BackgroundImage bgImage = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
            );
            root.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            // Fallback to a plain background if image fails to load
            root.setStyle("-fx-background-color: #ffffff;");
        }

        // VBox for login form (positioned on the left)
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER_LEFT);
        loginBox.setPadding(new Insets(10));
        loginBox.setMaxWidth(400); // Increased slightly to accommodate larger window

        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(
                "-fx-background-color: #2b3a55;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #cccccc;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10;" +
                        "-fx-font: bold 14px Arial;"
        );
        usernameField.setPrefHeight(40);
        usernameField.setPrefWidth(400); // Increased to fit larger window

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(
                "-fx-background-color: #2b3a55;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #cccccc;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10;" +
                        "-fx-font: bold 14px Arial;"
        );
        passwordField.setPrefHeight(40);
        passwordField.setPrefWidth(400); // Increased to fit larger window

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #2b3a55;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;"
        );
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2b3a55;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #2b3a55;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;"
        ));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
                "-fx-background-color:  #e0e0e0;" +
                        "-fx-text-fill: #2b3a55;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #2b3a55;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;"
        ));

        Button signupButton = new Button("Sign Up");
        signupButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2b3a55;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #2b3a55;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 6 18 6 18;" +
                        "-fx-cursor: hand;"
        );
        signupButton.setOnMouseEntered(e -> signupButton.setStyle(
                "-fx-background-color: #e0e0e0;" +
                        "-fx-text-fill: #2b3a55;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #2b3a55;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 6 18 6 18;" +
                        "-fx-cursor: hand;"
        ));
        signupButton.setOnMouseExited(e -> signupButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2b3a55;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #2b3a55;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 6 18 6 18;" +
                        "-fx-cursor: hand;"
        ));

        buttonBox.getChildren().addAll(loginButton, signupButton);

        // Error label
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setTextAlignment(TextAlignment.CENTER); // Center the text within the label

        loginBox.getChildren().addAll(usernameField, passwordField, buttonBox, errorLabel);

        // Add loginBox to the left side of the StackPane
        StackPane.setAlignment(loginBox, Pos.CENTER_LEFT);
        StackPane.setMargin(loginBox, new Insets(250, 0, 0, 0)); // Shift down by 250 pixels to be below the title
        root.getChildren().add(loginBox);

        // Event handlers
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        signupButton.setOnAction(e -> handleSignup());

        // Set up the scene with desired window size and lock it
        Scene scene = new Scene(root, 800, 500); // Window size set to 800x500
        primaryStage.setTitle("Login Screen");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Lock the window size
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

    private void handleSignup() {
        Stage signupStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Sign Up");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button createButton = new Button("Create Account");
        Label signupErrorLabel = new Label("");
        signupErrorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(titleLabel, usernameLabel, usernameField, passwordLabel, passwordField, createButton, signupErrorLabel);

        createButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                signupErrorLabel.setText("Username and password are required.");
                return;
            }
            try {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setRole("customer"); // Set role to customer
                userManager.addUser(newUser);
                userManager.updateUsers();
                signupStage.close();
                errorLabel.setText("Account created! Please log in.");
            } catch (Exception ex) {
                signupErrorLabel.setText(ex.getMessage());
            }
        });

        Scene scene = new Scene(root, 300, 200);
        signupStage.setTitle("Sign Up");
        signupStage.setScene(scene);
        signupStage.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}