package com.bertilware.vault;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.security.InvalidKeyException;

public class LoginDialogController {
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private double xOffset;
    private double yOffset;

    @FXML
    public void initialize() {
        // Read last session's info
        try {
            EncryptionManager.readSession();
            usernameField.setText(EncryptionManager.getUser());
        } catch (FileNotFoundException e) {
            VaultDialog.createErrorDialog(e);
        }

        // Set the theme
        if (EncryptionManager.getTheme() != null)
            rootVBox.setStyle(EncryptionManager.getTheme());

        // Make the window movable
        titleHBox.setOnMousePressed(mouseEvent -> {
            xOffset = mouseEvent.getSceneX();
            yOffset = mouseEvent.getSceneY();
        });
        titleHBox.setOnMouseDragged(mouseEvent -> {
            getStage().setX(mouseEvent.getScreenX() - xOffset);
            getStage().setY(mouseEvent.getScreenY() - yOffset);
        });

        // Make the two titlebar buttons work
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });

        loginButton.setOnAction(actionEvent -> {
            File userFile = new File(
                    EncryptionManager.getVaultDirectory() + File.separator +
                            usernameField.getText().strip() + ".vault"
            );

            // If the file does not exist, the user does not exist.
            if (!userFile.exists()) {
                VaultDialog.createOkDialog("No such user exists");
                return;
            }

            try {
                EncryptionManager.setUser(usernameField.getText().strip());
                EncryptionManager.initialize(userFile, passwordField.getText());
                EncryptionManager.decrypt();
                VaultDialog.setResult(0);
                getStage().close();
            // BadPadding or InvalidKey both mean wrong password
            } catch (BadPaddingException | InvalidKeyException e) {
                VaultDialog.createOkDialog("Wrong password.");
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        registerButton.setOnAction(actionEvent -> {
            File userFile = new File(
                    EncryptionManager.getVaultDirectory() + File.separator +
                            usernameField.getText().strip() + ".vault"
            );

            if (usernameField.getText().isBlank()) {
                VaultDialog.createOkDialog("The username cannot be empty.");
                return;
            }
            if (passwordField.getText().length() < 4 || passwordField.getText().length() > 16) {
                VaultDialog.createOkDialog("Password must be between 4 and 16 characters long.");
                return;
            }

            // Prevent accidentally overwriting existing user files.
            if (userFile.exists()) {
                VaultDialog.createOkDialog("User already exists.");
                return;
            }

            try {
                Files.createDirectories(EncryptionManager.getVaultDirectory().toPath());
                EncryptionManager.setUser(usernameField.getText().strip());
                EncryptionManager.initialize(userFile, passwordField.getText());
                EncryptionManager.encrypt();
                VaultDialog.createOkDialog("User created successfully!");
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        deleteButton.setOnAction(actionEvent -> {
            File userFile = new File(
                    EncryptionManager.getVaultDirectory() + File.separator +
                            usernameField.getText().strip() + ".vault"
            );

            if (!userFile.exists()) {
                VaultDialog.createOkDialog("No such user exists");
                return;
            }

            try {
                // Check if the person trying to delete the account knows the password
                EncryptionManager.initialize(userFile, passwordField.getText());
                EncryptionManager.decrypt();
                VaultDialog.createYesNoDialog(
                        "Are you sure you want to delete the user: " +
                        usernameField.getText().strip() + "?"
                );
                // Delete only if the user clicked "OK" in the confirmation dialog
                if (VaultDialog.getResult() == 0) {
                    if (userFile.delete()) {
                        VaultDialog.createOkDialog("User deleted successfully.");
                    }
                    else {
                        VaultDialog.createOkDialog("Failed to delete the user.");
                    }
                }
            } catch (BadPaddingException | InvalidKeyException e) {
                VaultDialog.createOkDialog("Wrong password.");
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }

    public LoginDialogController() {
        yOffset = 0;
        xOffset = 0;
    }
}
