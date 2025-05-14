package com.example.newbookstoremanagement.view;

import com.example.newbookstoremanagement.controller.BookManager;
import com.example.newbookstoremanagement.controller.UserManager;
import com.example.newbookstoremanagement.model.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class MainController {
    private BookManager bookManager;
    private UserManager userManager;
    private User currentUser;
    private Stage primaryStage;
    private ObservableList<Book> bookList;
    private Label statusLabel;
    private Cart cart;
    private boolean isDarkMode = false;
    private BorderPane root;
    private Button manageUsersButton;
    private Label appTitleLabel;
    private Label endUserLabel;
    private Label staffLabel;
    private Label adminLabel;
    private Label detailsTitle;
    private Label titleLabel;
    private Label authorLabel;
    private Label isbnLabel;
    private Label priceLabel;
    private Label qtyLabel;
    private TextField searchField;
    private TableView<Book> bookTable;
    private final String coverImageDir = "book_covers"; // Directory for book cover images

    public MainController(BookManager bookManager, UserManager userManager, User currentUser, Stage primaryStage) {
        this.bookManager = bookManager;
        this.userManager = userManager;
        this.currentUser = currentUser;
        this.primaryStage = primaryStage;
        this.bookList = FXCollections.observableArrayList(bookManager.getBooks());
        this.cart = new Cart();
    }

    public void showMainScreen() {
        root = new BorderPane();
        root.setPadding(new Insets(10));

        // Initialize bookTable early to avoid NullPointerException
        bookTable = new TableView<>(); // Moved up

        // Top: Title and theme toggle
        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(5));
        appTitleLabel = new Label("Bookstore Management System");
        appTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.setOnAction(e -> toggleTheme());
        styleButton(themeToggle);
        topBox.getChildren().addAll(appTitleLabel, new Region(), themeToggle);
        HBox.setHgrow(new Region(), Priority.ALWAYS); // Push toggle to the right
        root.setTop(topBox);

        // Left: Sidebar with grouped buttons
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #f0f0f0;");
        sidebar.setPrefWidth(180);

        // End-user buttons (visible to all)
        endUserLabel = new Label("Customer Actions");
        endUserLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Button addToCartButton = new Button("Add to Cart");
        Button viewCartButton = new Button("View Cart");
        Button checkoutButton = new Button("Checkout");
        styleButton(addToCartButton);
        styleButton(viewCartButton);
        styleButton(checkoutButton);
        VBox endUserBox = new VBox(5, endUserLabel, addToCartButton, viewCartButton, checkoutButton);

        // Determine user role
        boolean isAdmin = "admin".equals(currentUser.getRole());
        boolean isStaff = "staff".equals(currentUser.getRole());

        // Staff buttons (visible to staff and admin only)
        VBox staffBox = new VBox(5);
        if (isStaff || isAdmin) {
            staffLabel = new Label("Staff Actions");
            staffLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Button addButton = new Button("Add Book");
            Button editButton = new Button("Edit Book");
            Button deleteButton = new Button("Delete Book");
            Button inventoryButton = new Button("Check Inventory");
            styleButton(addButton);
            styleButton(editButton);
            styleButton(deleteButton);
            styleButton(inventoryButton);
            staffBox.getChildren().addAll(staffLabel, addButton, editButton, deleteButton, inventoryButton);

            // Event handlers for staff buttons
            addButton.setOnAction(e -> handleAddBook());
            editButton.setOnAction(e -> handleEditBook(bookTable.getSelectionModel().getSelectedItem()));
            deleteButton.setOnAction(e -> handleDeleteBook(bookTable.getSelectionModel().getSelectedItem()));
            inventoryButton.setOnAction(e -> handleCheckInventory());

            // Bindings
            editButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));
            deleteButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));
        } else {
            staffLabel = null; // Ensure reference is null if not used
        }

        // Admin buttons (visible to admin only)
        VBox adminBox = new VBox(5);
        if (isAdmin) {
            adminLabel = new Label("Admin Actions");
            adminLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            manageUsersButton = new Button("Manage Users");
            styleButton(manageUsersButton);
            adminBox.getChildren().addAll(adminLabel, manageUsersButton);
            manageUsersButton.setOnAction(e -> handleManageUsers());
        } else {
            adminLabel = null; // Ensure reference is null if not used
            manageUsersButton = null;
        }

        // Logout button at the bottom
        Button logoutButton = new Button("Logout");
        styleButton(logoutButton, true);
        sidebar.getChildren().addAll(endUserBox, staffBox, adminBox, new Region(), logoutButton);
        VBox.setVgrow(new Region(), Priority.ALWAYS); // Push logout to bottom
        root.setLeft(sidebar);
        BorderPane.setMargin(sidebar, new Insets(0, 15, 0, 0)); // 15px padding to the right

        // Center: Search bar and book table
        VBox centerBox = new VBox(5);
        searchField = new TextField();
        searchField.setPromptText("Search by title, author, or ISBN");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 15; -fx-padding: 5 10 5 10;");

        // Add text change listener to search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            ArrayList<Book> searchResults = bookManager.searchBooks(newValue);
            bookList.setAll(searchResults);
        });

        TableColumn<Book, String> titleColumn = new TableColumn<>();
        TableColumn<Book, String> authorColumn = new TableColumn<>();
        TableColumn<Book, String> isbnColumn = new TableColumn<>();
        TableColumn<Book, Integer> priceColumn = new TableColumn<>();
        TableColumn<Book, Integer> qtyColumn = new TableColumn<>();

        // Set custom headers with styled labels
        Label titleHeaderLabel = new Label("Title");
        titleHeaderLabel.setStyle("-fx-font-weight: bold;");
        titleColumn.setGraphic(titleHeaderLabel);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        Label authorHeaderLabel = new Label("Author");
        authorHeaderLabel.setStyle("-fx-font-weight: bold;");
        authorColumn.setGraphic(authorHeaderLabel);
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

        Label isbnHeaderLabel = new Label("ISBN");
        isbnHeaderLabel.setStyle("-fx-font-weight: bold;");
        isbnColumn.setGraphic(isbnHeaderLabel);
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        Label priceHeaderLabel = new Label("Price");
        priceHeaderLabel.setStyle("-fx-font-weight: bold;");
        priceColumn.setGraphic(priceHeaderLabel);
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        Label qtyHeaderLabel = new Label("Quantity");
        qtyHeaderLabel.setStyle("-fx-font-weight: bold;");
        qtyColumn.setGraphic(qtyHeaderLabel);
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));
        qtyColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Store header labels for access in updateTheme
        titleColumn.setUserData(titleHeaderLabel);
        authorColumn.setUserData(authorHeaderLabel);
        isbnColumn.setUserData(isbnHeaderLabel);
        priceColumn.setUserData(priceHeaderLabel);
        qtyColumn.setUserData(qtyHeaderLabel);

        // Adjust column widths to fill the table horizontally
        titleColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.25));
        authorColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.20));
        isbnColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.20));
        priceColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.10));
        priceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        qtyColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.15));

        bookTable.getColumns().addAll(titleColumn, authorColumn, isbnColumn, priceColumn, qtyColumn);
        bookTable.setItems(bookList);
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Ensures columns fill the table

        // Set adjustable height (50% of window height)
        bookTable.prefHeightProperty().bind(primaryStage.heightProperty().multiply(0.8));
        bookTable.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.65)); // 65% of window width

        centerBox.getChildren().addAll(searchField, bookTable);
        root.setCenter(centerBox);
        BorderPane.setMargin(centerBox, new Insets(0, 15, 0, 15)); // 15px padding on both sides

        // Right: Book details card
        VBox bookDetails = new VBox(10);
        bookDetails.setPadding(new Insets(10));
        bookDetails.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-width: 1;");
        bookDetails.setPrefWidth(250);
        detailsTitle = new Label("Book Details");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(150);
        coverImage.setFitHeight(200);
        coverImage.setPreserveRatio(true); // Maintain aspect ratio
        titleLabel = new Label();
        authorLabel = new Label();
        isbnLabel = new Label();
        priceLabel = new Label();
        qtyLabel = new Label();
        bookDetails.getChildren().addAll(detailsTitle, coverImage, titleLabel, authorLabel, isbnLabel, priceLabel, qtyLabel);
        root.setRight(bookDetails);
        BorderPane.setMargin(bookDetails, new Insets(0, 0, 0, 15)); // 15px padding to the left

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Load cover image from file system
                String imagePath = bookManager.getCoverImagePath(newSelection.getCoverImage());
                Image image = null;
                if (imagePath != null && new File(imagePath).exists()) {
                    try {
                        image = new Image(new File(imagePath).toURI().toString());
                    } catch (Exception e) {
                        System.err.println("Error loading image " + imagePath + ": " + e.getMessage());
                    }
                }
                // If cover image failed to load or doesn't exist, try loading the placeholder
                if (image == null || image.isError()) {
                    InputStream placeholderStream = getClass().getResourceAsStream("/images/placeholder.png");
                    if (placeholderStream != null) {
                        image = new Image(placeholderStream);
                        if (image.isError()) {
                            System.err.println("Error loading placeholder image: " + image.getException());
                            image = null; // Set to null to avoid displaying a broken image
                        }
                    } else {
                        System.err.println("Placeholder image not found at /images/placeholder.jpg");
                        image = null; // Set to null to avoid NullPointerException
                    }
                }
                coverImage.setImage(image);
                titleLabel.setText("Title: " + newSelection.getTitle());
                authorLabel.setText("Author: " + newSelection.getAuthor());
                isbnLabel.setText("ISBN: " + newSelection.getIsbn());
                priceLabel.setText("Price: $" + newSelection.getPrice());
                qtyLabel.setText("Quantity: " + newSelection.getQty());
            } else {
                coverImage.setImage(null);
                titleLabel.setText("");
                authorLabel.setText("");
                isbnLabel.setText("");
                priceLabel.setText("");
                qtyLabel.setText("");
            }
        });

        // Bottom: Status label
        statusLabel = new Label("");
        statusLabel.setPadding(new Insets(5));
        root.setBottom(statusLabel);

        // Event handlers for customer actions
        addToCartButton.setOnAction(e -> handleAddToCart(bookTable.getSelectionModel().getSelectedItem()));
        viewCartButton.setOnAction(e -> handleViewCart());
        checkoutButton.setOnAction(e -> handleCheckout());
        logoutButton.setOnAction(e -> handleLogout());

        addToCartButton.disableProperty().bind(Bindings.isNull(bookTable.getSelectionModel().selectedItemProperty()));

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Bookstore Management System");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Apply initial theme
        updateTheme();
    }

    private void styleButton(Button button) {
        styleButton(button, false);
    }

    private void styleButton(Button button, boolean isLogout) {
        button.setPrefWidth(160);
        String buttonColor = isDarkMode ? "#3c4e6e" : (isLogout ? "#ff4444" : "#3c4e6e");
        String hoverColor = isDarkMode ? "#3c4e6e" : (isLogout ? "#ff6666" : "#3c4e6e");
        button.setStyle(
                "-fx-background-color: " + buttonColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + buttonColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-cursor: hand;"
        ));
    }

    private void styleButton(ToggleButton toggleButton) {
        toggleButton.setPrefWidth(160);
        String buttonColor = isDarkMode ? "#2b3a55" : "#2b3a55";
        String hoverColor = isDarkMode ? "#2b3a55" : "#3c4e6e";
        toggleButton.setStyle(
                "-fx-background-color: " + buttonColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-cursor: hand;"
        );
        toggleButton.setOnMouseEntered(e -> toggleButton.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-cursor: hand;"
        ));
        toggleButton.setOnMouseExited(e -> toggleButton.setStyle(
                "-fx-background-color: " + buttonColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-cursor: hand;"
        ));
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        updateTheme();
    }

    private void updateTheme() {
        if (isDarkMode) {
            root.setStyle("-fx-background-color: #2b2b2b;");
            root.getLeft().setStyle("-fx-background-color: #555;");
            root.getRight().setStyle("-fx-background-color: #555; -fx-border-color: #555; -fx-border-width: 1;");
            statusLabel.setTextFill(Color.WHITE);
            appTitleLabel.setTextFill(Color.WHITE);
            endUserLabel.setTextFill(Color.WHITE);
            if (staffLabel != null) staffLabel.setTextFill(Color.WHITE);
            if (adminLabel != null) adminLabel.setTextFill(Color.WHITE);
            detailsTitle.setTextFill(Color.WHITE);
            titleLabel.setTextFill(Color.WHITE);
            authorLabel.setTextFill(Color.WHITE);
            isbnLabel.setTextFill(Color.WHITE);
            priceLabel.setTextFill(Color.WHITE);
            qtyLabel.setTextFill(Color.WHITE);
            searchField.setStyle("-fx-background-radius: 15; -fx-padding: 5 10 5 10; -fx-text-fill: black; -fx-prompt-text-fill: #aaaaaa;");
            bookTable.setStyle("-fx-control-inner-background: #3c3f41; -fx-table-cell-border-color: transparent; -fx-background-color: #3c3f41;");
            // Style column headers
            for (TableColumn<Book, ?> column : bookTable.getColumns()) {
                Label headerLabel = (Label) column.getUserData();
                if (headerLabel != null) {
                    headerLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #e3e3e3; -fx-text-fill: #3c4e6e; -fx-padding: 5;");
                }
            }
        } else {
            root.setStyle("-fx-background-color: #faf2e8;");
            root.getLeft().setStyle("-fx-background-color: #faf2e8;");
            root.getRight().setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-width: 1;");
            statusLabel.setTextFill(Color.BLACK);
            appTitleLabel.setTextFill(Color.BLACK);
            endUserLabel.setTextFill(Color.BLACK);
            if (staffLabel != null) staffLabel.setTextFill(Color.BLACK);
            if (adminLabel != null) adminLabel.setTextFill(Color.BLACK);
            detailsTitle.setTextFill(Color.BLACK);
            titleLabel.setTextFill(Color.BLACK);
            authorLabel.setTextFill(Color.BLACK);
            isbnLabel.setTextFill(Color.BLACK);
            priceLabel.setTextFill(Color.BLACK);
            qtyLabel.setTextFill(Color.BLACK);
            searchField.setStyle("-fx-background-radius: 15; -fx-padding: 5 10 5 10; -fx-text-fill: black; -fx-prompt-text-fill: #666666;");
            bookTable.setStyle("-fx-control-inner-background: #ffffff; -fx-table-cell-border-color: transparent; -fx-background-color: #ffffff;");
            // Style column headers
            for (TableColumn<Book, ?> column : bookTable.getColumns()) {
                Label headerLabel = (Label) column.getUserData();
                if (headerLabel != null) {
                    headerLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #e3e3e3; -fx-text-fill: #2b3a55; -fx-padding: 5;");
                }
            }
        }
        // Refresh the table to apply cell factory styles
        bookTable.refresh();
    }

    private void handleAddToCart(Book book) {
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
        totalLabel.setTextFill(isDarkMode ? Color.WHITE : Color.BLACK);
        Button removeButton = new Button("Remove Selected Item");
        Button closeButton = new Button("Close");
        styleButton(removeButton);
        styleButton(closeButton);

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

    private void handleAddBook() {
        Stage addStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField isbnField = new TextField();
        TextField priceField = new TextField();
        TextField qtyField = new TextField();
        Button selectImageButton = new Button("Select Cover Image");
        Label imageLabel = new Label("No image selected");
        File[] selectedImage = {null}; // Store selected image file
        Button saveButton = new Button("Save");
        styleButton(saveButton);
        styleButton(selectImageButton);
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        selectImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cover Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(addStage);
            if (file != null) {
                selectedImage[0] = file;
                imageLabel.setText(file.getName());
            }
        });

        root.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("ISBN:"), isbnField,
                new Label("Price:"), priceField,
                new Label("Quantity:"), qtyField,
                selectImageButton, imageLabel,
                saveButton, errorLabel);

        saveButton.setOnAction(e -> {
            try {
                Book book = new Book();
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setIsbn(isbnField.getText());
                book.setPrice(Integer.parseInt(priceField.getText()));
                book.setQty(Integer.parseInt(qtyField.getText()));
                if (book.getTitle().isEmpty() || book.getAuthor().isEmpty() || book.getIsbn().isEmpty()) {
                    errorLabel.setText("Title, author, and ISBN are required.");
                    return;
                }
                if (book.getPrice() < 0 || book.getQty() < 0) {
                    errorLabel.setText("Price and quantity cannot be negative.");
                    return;
                }

                // Handle image
                if (selectedImage[0] != null) {
                    String imageName = book.getIsbn() + "_" + selectedImage[0].getName();
                    Path destPath = Paths.get(coverImageDir, imageName);
                    Files.copy(selectedImage[0].toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    book.setCoverImage(imageName);
                }

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

        Scene scene = new Scene(root, 300, 350); // Increased height for image fields
        addStage.setScene(scene);
        addStage.setTitle("Add Book");

        addStage.setWidth(400);  // Set preferred width
        addStage.setHeight(600);
        addStage.setResizable(false);
        addStage.centerOnScreen();

        // Handle stage close request to prevent error logs
        addStage.setOnCloseRequest(event -> {
            try {
                addStage.close();
                System.out.println("Terminated");
            } catch (Exception ex) {
                System.out.println("Terminated");
            }
        });

        addStage.show();
    }

    private void handleEditBook(Book book) {
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
        Button selectImageButton = new Button("Select Cover Image");
        Label imageLabel = new Label(book.getCoverImage() != null ? book.getCoverImage() : "No image selected");
        File[] selectedImage = {null}; // Store selected image file
        Button saveButton = new Button("Save");
        styleButton(saveButton);
        styleButton(selectImageButton);
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        selectImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cover Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(editStage);
            if (file != null) {
                selectedImage[0] = file;
                imageLabel.setText(file.getName());
            }
        });

        root.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("ISBN:"), isbnField,
                new Label("Price:"), priceField,
                new Label("Quantity:"), qtyField,
                selectImageButton, imageLabel,
                saveButton, errorLabel);

        saveButton.setOnAction(e -> {
            try {
                Book updatedBook = new Book();
                updatedBook.setTitle(titleField.getText());
                updatedBook.setAuthor(authorField.getText());
                updatedBook.setIsbn(book.getIsbn());
                updatedBook.setPrice(Integer.parseInt(priceField.getText()));
                updatedBook.setQty(Integer.parseInt(qtyField.getText()));
                if (updatedBook.getTitle().isEmpty() || updatedBook.getAuthor().isEmpty()) {
                    errorLabel.setText("Title and author are required.");
                    return;
                }
                if (updatedBook.getPrice() < 0 || updatedBook.getQty() < 0) {
                    errorLabel.setText("Price and quantity cannot be negative.");
                    return;
                }

                // Handle image
                if (selectedImage[0] != null) {
                    String imageName = updatedBook.getIsbn() + "_" + selectedImage[0].getName();
                    Path destPath = Paths.get(coverImageDir, imageName);
                    Files.copy(selectedImage[0].toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    updatedBook.setCoverImage(imageName);
                } else {
                    updatedBook.setCoverImage(book.getCoverImage()); // Retain existing image
                }

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

        Scene scene = new Scene(root, 300, 350);
        editStage.setTitle("Edit Book");
        editStage.setScene(scene);
        editStage.setWidth(400);
        editStage.setHeight(600);
        editStage.setResizable(false);
        editStage.centerOnScreen();
        editStage.show();
    }

    private void handleDeleteBook(Book book) {
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

    private void handleLogout() {
        LoginController loginController = new LoginController(userManager, primaryStage);
        loginController.showLoginScreen();
    }

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
        styleButton(addUserButton);
        styleButton(removeUserButton);
        styleButton(closeButton);
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
                user.setRole("customer");
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