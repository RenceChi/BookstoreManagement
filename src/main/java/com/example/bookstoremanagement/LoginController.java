package com.example.bookstoremanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    private UserManager userManager;
    private Stage primaryStage;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label signUpLabel;

    public LoginController(UserManager userManager, Stage primaryStage) {
        this.userManager = userManager;
        this.primaryStage = primaryStage;
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bookstoremanagement/loginView.fxml"));
            loader.setController(this);
            Scene scene = new Scene(loader.load(), 400, 300);
            scene.getStylesheets().add(getClass().getResource("/com/example/bookstoremanagement/loginView.css").toExternalForm());
            primaryStage.setTitle("Bookstore Login");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Add click handler for Sign up link
            signUpLabel.setOnMouseClicked(this::handleSignUp);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading login screen: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
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

    private void handleSignUp(MouseEvent event) {
        Stage signUpStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Username");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Password");
        Button createButton = new Button("Create Account");
        Label signUpErrorLabel = new Label("");
        signUpErrorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(
                new Label("Create New Account"),
                new Label("Username:"), newUsernameField,
                new Label("Password:"), newPasswordField,
                createButton, signUpErrorLabel);

        createButton.setOnAction(e -> {
            try {
                User newUser = new User();
                newUser.setUsername(newUsernameField.getText());
                newUser.setPassword(newPasswordField.getText());
                userManager.addUser(newUser);
                signUpStage.close();
                showError("Account created successfully. Please log in.");
            } catch (IllegalArgumentException | IOException ex) {
                signUpErrorLabel.setText(ex.getMessage());
            }
        });

        Scene scene = new Scene(root, 300, 200);
        signUpStage.setTitle("Sign Up");
        signUpStage.setScene(scene);
        signUpStage.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}