module com.example.bookstoremanagement {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bookstoremanagement to javafx.fxml;
    exports com.example.bookstoremanagement;
}