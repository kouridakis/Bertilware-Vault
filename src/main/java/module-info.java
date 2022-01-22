module com.bertilware.vault {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.bertilware.vault to javafx.fxml;
    exports com.bertilware.vault;
}