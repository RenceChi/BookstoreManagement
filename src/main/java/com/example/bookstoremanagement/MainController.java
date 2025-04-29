package com.example.bookstoremanagement;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    private BookManager bookManager;
    private UserManager userManager;
    private User currentUser;
    private Stage primaryStage;
    private ObservableList<Book> bookList;
    private Cart cart;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, Integer> priceColumn;
    @FXML private TableColumn<Book, Integer> qtyColumn;
    @FXML private TextField searchField;
    @FXML private Button addToCartButton;
    @FXML private Button viewCartButton;
    @FXML private Button checkoutButton;
    @FXML private Button sellButton;
    @FXML private Button inventoryButton;
    @FXML private Button logoutButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button manageUsersButton;
    @FXML private Label statusLabel;

    public MainController(BookManager bookManager, UserManager userManager, User currentUser, Stage primaryStage) {
        this.bookManager = bookManager;
        this.userManager = userManager;
        this.currentUser = currentUser;
        this.primaryStage = primaryStage;
        this.bookList = FXCollections.observableArrayList(bookManager.getBooks());
        this.cart = new Cart();
    }

    public void showMainScreen() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bookstoremanagement/mainView.fxml"));
            loader.setController(this); // Set this instance as the controller
            Scene scene = new Scene(loader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/com/example/bookstoremanagement/mainView.css").toExternalForm());

            // Set up the stage
            primaryStage.setTitle("Bookstore Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Set up table data
            bookTable.setItems(bookList);
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
            isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));

            // Set up search functionality
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.isEmpty()) {
                    bookList.setAll(bookManager.getBooks());
                } else {
                    bookList.setAll(bookManager.searchBooks(newVal));
                }
            });

            // Set up button disable bindings
            addToCartButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));
            editButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));
            deleteButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));
            sellButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));

            // Show admin buttons if user is admin
            boolean isAdmin = currentUser.getUsername().equals("admin");
            if (isAdmin) {
                addButton.setVisible(true);
                addButton.setManaged(true);
                editButton.setVisible(true);
                editButton.setManaged(true);
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
                manageUsersButton.setVisible(true);
                manageUsersButton.setManaged(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading main screen: " + e.getMessage());
            showError("Error loading main screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddToCart() {
        Book book = bookTable.getSelectionModel().getSelectedItem();
        if (book == null) {
            showError("Please select a book to add to cart.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + book.getTitle() + " to Cart");
        dialog.setContentText("Enter quantity:");
        dialog.showAndWait().ifPresent(quantityStr -> {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    showError("Quantity must be positive.");
                    return;
                }
                if (quantity > book.getQty()) {
                    showError("Not enough copies available.");
                    return;
                }
                cart.addItem(book, quantity);
                statusLabel.setText(quantity + " copies of " + book.getTitle() + " added to cart.");
            } catch (NumberFormatException e) {
                showError("Please enter a valid quantity.");
            }
        });
    }

    @FXML
    private void handleViewCart() {
        Stage cartStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TableView<Cart.CartItem> cartTable = new TableView<>();
        TableColumn<Cart.CartItem, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setPrefWidth(200);
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        TableColumn<Cart.CartItem, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setPrefWidth(100);
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Cart.CartItem, Integer> priceColumn = new TableColumn<>("Price");
        priceColumn.setPrefWidth(100);
        priceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getBook().getPrice() * cellData.getValue().getQuantity()).asObject());
        cartTable.getColumns().addAll(titleColumn, quantityColumn, priceColumn);
        cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));

        Label totalLabel = new Label("Total: $" + cart.getTotalPrice());
        Button removeButton = new Button("Remove Selected Item");
        Button closeButton = new Button("Close");

        root.getChildren().addAll(new Label("Shopping Cart"), cartTable, totalLabel, new HBox(10, removeButton, closeButton));

        removeButton.setOnAction(e -> {
            Cart.CartItem selected = cartTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cart.removeItem(selected.getBook().getIsbn());
                cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));
                totalLabel.setText("Total: $" + cart.getTotalPrice());
            } else {
                showError("Please select an item to remove.");
            }
        });

        closeButton.setOnAction(e -> cartStage.close());

        Scene scene = new Scene(root, 500, 400);
        cartStage.setTitle("View Cart");
        cartStage.setScene(scene);
        cartStage.show();
    }

    @FXML
    private void handleCheckout() {
        if (cart.getItems().isEmpty()) {
            showError("Cart is empty.");
            return;
        }
        try {
            bookManager.checkoutCart(cart);
            updateBookList();
            statusLabel.setText("Checkout successful. Total: $" + cart.getTotalPrice());
        } catch (IllegalArgumentException | IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleAddBook() {
        Stage addStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField isbnField = new TextField();
        TextField priceField = new TextField();
        TextField qtyField = new TextField();
        Button saveButton = new Button("Save");
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("ISBN:"), isbnField,
                new Label("Price:"), priceField,
                new Label("Quantity:"), qtyField,
                saveButton, errorLabel);

        saveButton.setOnAction(e -> {
            try {
                Book book = new Book();
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setIsbn(isbnField.getText());
                book.setPrice(Integer.parseInt(priceField.getText()));
                book.setQty(Integer.parseInt(qtyField.getText()));
                bookManager.addStock(book);
                updateBookList();
                addStage.close();
                statusLabel.setText("Book added successfully.");
            } catch (NumberFormatException ex) {
                errorLabel.setText("Price and quantity must be numbers.");
            } catch (IOException ex) {
                errorLabel.setText("Error saving book: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(root, 300, 300);
        addStage.setTitle("Add Book");
        addStage.setScene(scene);
        addStage.show();
    }

    @FXML
    private void handleEditBook() {
        Book book = bookTable.getSelectionModel().getSelectedItem();
        if (book == null) {
            showError("Please select a book to edit.");
            return;
        }
        Stage editStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField titleField = new TextField(book.getTitle());
        TextField authorField = new TextField(book.getAuthor());
        TextField isbnField = new TextField(book.getIsbn());
        isbnField.setDisable(true);
        TextField priceField = new TextField(String.valueOf(book.getPrice()));
        TextField qtyField = new TextField(String.valueOf(book.getQty()));
        Button saveButton = new Button("Save");
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("ISBN:"), isbnField,
                new Label("Price:"), priceField,
                new Label("Quantity:"), qtyField,
                saveButton, errorLabel);

        saveButton.setOnAction(e -> {
            try {
                Book updatedBook = new Book();
                updatedBook.setTitle(titleField.getText());
                updatedBook.setAuthor(authorField.getText());
                updatedBook.setIsbn(book.getIsbn());
                updatedBook.setPrice(Integer.parseInt(priceField.getText()));
                updatedBook.setQty(Integer.parseInt(qtyField.getText()));
                bookManager.removeStock(book);
                bookManager.addStock(updatedBook);
                updateBookList();
                editStage.close();
                statusLabel.setText("Book updated successfully.");
            } catch (NumberFormatException ex) {
                errorLabel.setText("Price and quantity must be numbers.");
            } catch (IOException ex) {
                errorLabel.setText("Error updating book: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(root, 300, 300);
        editStage.setTitle("Edit Book");
        editStage.setScene(scene);
        editStage.show();
    }

    @FXML
    private void handleDeleteBook() {
        Book book = bookTable.getSelectionModel().getSelectedItem();
        if (book == null) {
            showError("Please select a book to delete.");
            return;
        }
        try {
            Book toRemove = new Book();
            toRemove.setIsbn(book.getIsbn());
            toRemove.setQty(book.getQty());
            bookManager.removeStock(toRemove);
            updateBookList();
            statusLabel.setText("Book deleted successfully.");
        } catch (IOException e) {
            showError("Error deleting book: " + e.getMessage());
        }
    }

    @FXML
    private void handleSellBook() {
        Book book = bookTable.getSelectionModel().getSelectedItem();
        if (book == null) {
            showError("Please select a book to sell.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Sell Book");
        dialog.setHeaderText("Sell " + book.getTitle());
        dialog.setContentText("Enter quantity to sell:");
        dialog.showAndWait().ifPresent(quantityStr -> {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    showError("Quantity must be positive.");
                    return;
                }
                Book toSell = new Book();
                toSell.setIsbn(book.getIsbn());
                toSell.setQty(quantity);
                bookManager.removeStock(toSell);
                updateBookList();
                statusLabel.setText("Sold " + quantity + " copies of " + book.getTitle());
            } catch (NumberFormatException e) {
                showError("Please enter a valid quantity.");
            } catch (IOException e) {
                showError("Error selling book: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleCheckInventory() {
        int totalBooks = bookManager.getBooks().stream().mapToInt(Book::getQty).sum();
        StringBuilder inventorySummary = new StringBuilder("Inventory Summary:\n");
        inventorySummary.append("Total Books in Stock: ").append(totalBooks).append("\n");
        for (Book book : bookManager.getBooks()) {
            inventorySummary.append(book.getTitle()).append(": ").append(book.getQty()).append(" copies\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventory Check");
        alert.setHeaderText(null);
        alert.setContentText(inventorySummary.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        LoginController loginController = new LoginController(userManager, primaryStage);
        loginController.showLoginScreen();
    }

    @FXML
    private void handleManageUsers() {
        Stage userStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TableView<User> userTable = new TableView<>();
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setPrefWidth(150);
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userTable.getColumns().add(usernameColumn);
        userTable.setItems(FXCollections.observableArrayList(userManager.getUsers()));

        TextField usernameField = new TextField();
        TextField passwordField = new TextField();
        Button addUserButton = new Button("Add User");
        Button removeUserButton = new Button("Remove Selected User");
        Button closeButton = new Button("Close");
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(
                new Label("User Management"), userTable,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new HBox(10, addUserButton, removeUserButton, closeButton),
                errorLabel);

        addUserButton.setOnAction(e -> {
            try {
                User user = new User();
                user.setUsername(usernameField.getText());
                user.setPassword(passwordField.getText());
                if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
                    errorLabel.setText("Username and password are required.");
                    return;
                }
                userManager.addUser(user);
                userTable.setItems(FXCollections.observableArrayList(userManager.getUsers()));
                errorLabel.setText("User added successfully.");
                usernameField.clear();
                passwordField.clear();
            } catch (IOException ex) {
                errorLabel.setText("Error adding user: " + ex.getMessage());
            }
        });

        removeUserButton.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    userManager.removeUser(selected);
                    userTable.setItems(FXCollections.observableArrayList(userManager.getUsers()));
                    errorLabel.setText("User removed successfully.");
                } catch (IOException ex) {
                    errorLabel.setText("Error removing user: " + ex.getMessage());
                }
            } else {
                errorLabel.setText("Please select a user to remove.");
            }
        });

        closeButton.setOnAction(e -> userStage.close());

        Scene scene = new Scene(root, 400, 400);
        userStage.setTitle("Manage Users");
        userStage.setScene(scene);
        userStage.show();
    }

    public void updateBookList() {
        bookList.setAll(bookManager.getBooks());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}