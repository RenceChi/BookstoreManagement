module com.example.bookstoremanagement {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.newbookstoremanagement;
    opens com.example.newbookstoremanagement to javafx.fxml;
    exports com.example.newbookstoremanagement.model;
    opens com.example.newbookstoremanagement.model to javafx.fxml;
    exports com.example.newbookstoremanagement.controller;
    opens com.example.newbookstoremanagement.controller to javafx.fxml;
    exports com.example.newbookstoremanagement.view;
    opens com.example.newbookstoremanagement.view to javafx.fxml;
}