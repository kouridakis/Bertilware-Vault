package com.bertilware.vault;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;

// Class with only static methods and variables,
// that is used for creating various different dialogs.
// Where needed, the result variable is set to 0 if the user pressed the OK or YES button,
// meanwhile it is set to 1 if the user cancelled, or if an error occurred.
public final class VaultDialog {
    private static int result = 1;
    private static String message;
    private static String okButtonText;
    private static String cancelButtonText;
    private static String service;
    private static String username;
    private static String password;

    // Getters
    public static int getResult() {
        return result;
    }
    public static String getMessage() {
        return message;
    }
    public static String getOkButtonText() {
        return okButtonText;
    }
    public static String getCancelButtonText() {
        return cancelButtonText;
    }
    public static String getService() {
        return service;
    }
    public static String getUsername() {
        return username;
    }
    public static String getPassword() {
        return password;
    }

    // Setter
    public static void setResult(int result) {
        VaultDialog.result = result;
    }

    // Various methods for creating different types of dialogs
    public static void createLoginDialog() {
        init("Login", "login-form.fxml",
                null, null, null,
                null, null, null);
    }

    public static void createUserDialog() {
        init("User", "user-form.fxml",
                null, null, null,
                null, null, null);
    }

    public static void createAboutDialog() {
        init("About", "about-form.fxml",
                null, null, null,
                null, null, null);
    }

    public static void createNotesDialog() {
        init("Notes", "notes-form.fxml",
                null,null,null,
                null,null,null);
    }

    public static void createSyncDialog() {
        init("Sync", "sync-form.fxml",
                null, null, null,
                null, null, null);
    }

    public static void createAccountDialog(String service, String username, String password) {
        init("Account", "account-form.fxml",
                null, null, null,
                service, username, password);
    }

    public static void createErrorDialog(Exception e) {
        e.printStackTrace();
        init("Notice", "notice-form.fxml",
                String.format("An exception occurred, exiting application:%n%nDetails: %s", e), "OK", null,
                null, null, null);
        System.exit(0);
    }

    public static void createYesNoDialog(String message) {
        init("Notice", "notice-form.fxml",
                message, "Yes", "No",
                null, null, null);
    }

    public static void createOkDialog(String message) {
        init("Notice", "notice-form.fxml",
                message, "OK", null,
                null, null, null);
    }

    private static void init(String title, String fxmlFile,
                             String message, String okButtonText, String cancelButtonText,
                             String service, String username, String password) {
        // Set all the variables.
        VaultDialog.message = message;
        VaultDialog.okButtonText = okButtonText;
        VaultDialog.cancelButtonText = cancelButtonText;
        VaultDialog.service = service;
        VaultDialog.username = username;
        VaultDialog.password = password;

        // From this point onwards it works the same as in the MainApplication class,
        // except for setting the modality and disabling resizing.
        Stage dialog = new Stage();
        FXMLLoader loader = new FXMLLoader(VaultDialog.class.getResource(fxmlFile));
        try {
            Scene scene = new Scene(loader.load());
            dialog.setScene(scene);
            dialog.setTitle(title);
            dialog.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            dialog.sizeToScene();

            InputStream iconStream = VaultDialog.class.getResourceAsStream("images/vault-icon.png");
            if (iconStream != null) {
                dialog.getIcons().add(new Image(iconStream));
            }

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Something went wrong, exiting application.");
            e.printStackTrace();
            System.exit(0);
        }
    }
}
