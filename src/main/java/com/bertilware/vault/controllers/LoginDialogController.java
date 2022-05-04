package com.bertilware.vault.controllers;

import com.bertilware.vault.VaultCommonHandlers;
import com.bertilware.vault.VaultDialog;
import com.bertilware.vault.VaultManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
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

    @FXML
    public void initialize() {
        // Read last session's info
        try {
            VaultManager.readSession();
            usernameField.setText(VaultManager.getUser());
        } catch (FileNotFoundException e) {
            VaultDialog.createErrorDialog(e);
        }

        // Set the theme
        if (VaultManager.getTheme() != null)
            rootVBox.setStyle(VaultManager.getTheme());

        // Make the window movable.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // Make the two title bar buttons work
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });

        loginButton.setOnAction(actionEvent -> {
            try {
                VaultManager.initialize(
                        usernameField.getText().strip(),
                        passwordField.getText().strip()
                );

                // If the file does not exist, the user does not exist.
                if (!VaultManager.getUserFile().exists()) {
                    VaultDialog.createOkDialog("No such user exists");
                    return;
                }

                VaultManager.decrypt();
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
            if (usernameField.getText().isBlank()) {
                VaultDialog.createOkDialog("The username cannot be empty.");
                return;
            }
            if (passwordField.getText().length() < 4 || passwordField.getText().length() > 32) {
                VaultDialog.createOkDialog("Password must be between 4 and 32 characters long.");
                return;
            }

            try {
                Files.createDirectories(VaultManager.getVaultDirectory().toPath());
                VaultManager.initialize(
                        usernameField.getText().strip(),
                        passwordField.getText().strip()
                );

                // Prevent accidentally overwriting existing user files.
                if (VaultManager.getUserFile().exists()) {
                    VaultDialog.createOkDialog("User already exists.");
                    return;
                }

                VaultManager.encrypt();
                VaultDialog.createOkDialog("User created successfully!");
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        deleteButton.setOnAction(actionEvent -> {
            try {
                VaultManager.initialize(
                        usernameField.getText().strip(),
                        passwordField.getText().strip()
                );

                if (!VaultManager.getUserFile().exists()) {
                    VaultDialog.createOkDialog("No such user exists");
                    return;
                }

                // Check if the person trying to delete the account knows the password
                VaultManager.decrypt();

                VaultDialog.createYesNoDialog(
                        "Are you sure you want to delete the user: " +
                        usernameField.getText().strip() + "?"
                );
                // Delete only if the user clicked "OK" in the confirmation dialog
                if (VaultDialog.getResult() == 0) {
                    if (VaultManager.getUserFile().delete()) {
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
}
