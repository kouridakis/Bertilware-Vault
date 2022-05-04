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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;

public class UserDialogController {
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        rootVBox.setStyle(VaultManager.getTheme());

        // Load the current username
        newUsernameField.setText(VaultManager.getUser());

        // Make the window movable.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // Make the two title bar buttons work.
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });

        saveButton.setOnAction(actionEvent -> {
            if (newUsernameField.getText().isBlank()
                    || newPasswordField.getText().isBlank()
                    || currentPasswordField.getText().isBlank()) {
                VaultDialog.createOkDialog("You cannot leave any of the fields empty.");
                return;
            }

            try {
                // Check if password is correct
                VaultManager.initialize(
                        VaultManager.getUser(),
                        currentPasswordField.getText()
                );
                VaultManager.decrypt();

                // Encrypt with the new password
                VaultManager.initialize(
                        VaultManager.getUser(),
                        newPasswordField.getText()
                );
                VaultManager.encrypt();

                // Rename file and change username
                Path current = VaultManager.getUserFile().toPath();
                Files.move(
                        current,
                        current.resolveSibling(newUsernameField.getText().strip() + ".vault")
                );
                VaultManager.initialize(
                        newUsernameField.getText().strip(),
                        newPasswordField.getText()
                );

                VaultManager.encrypt();
                VaultDialog.createOkDialog("User updated successfully.");
            } catch (InvalidKeyException | BadPaddingException e) {
                VaultDialog.createOkDialog("Wrong current password.");
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
            VaultDialog.setResult(0);
            getStage().close();
        });

        cancelButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }
}
