package com.example.newbookstoremanagement;

import com.example.newbookstoremanagement.controller.UserManager;
import com.example.newbookstoremanagement.view.LoginController;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            UserManager userManager = new UserManager();
            LoginController loginController = new LoginController(userManager, primaryStage);
            loginController.showLoginScreen();
        } catch (Exception e) {
            System.err.println("Error starting applicaztion: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to start the application: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}