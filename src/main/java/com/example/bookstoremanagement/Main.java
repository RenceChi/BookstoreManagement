package com.example.bookstoremanagement;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        UserManager userManager = new UserManager();
        LoginController loginController = new LoginController(userManager, primaryStage);
        loginController.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}