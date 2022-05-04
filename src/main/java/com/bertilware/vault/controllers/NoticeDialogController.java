package com.bertilware.vault.controllers;

import com.bertilware.vault.VaultCommonHandlers;
import com.bertilware.vault.VaultManager;
import com.bertilware.vault.VaultDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NoticeDialogController {
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private HBox buttonHBox;
    @FXML
    private Label messageLabel;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    public void initialize() {
        rootVBox.setStyle(VaultManager.getTheme());

        // Make the window movable.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // Make the two title bar buttons work.
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });

        messageLabel.setText(VaultDialog.getMessage());

        // Set the OK button's text and EventHandler.
        okButton.setText(VaultDialog.getOkButtonText());
        okButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(0);
            getStage().close();
        });

        // Remove the CANCEL button if its text is set to null.
        if (VaultDialog.getCancelButtonText() == null) {
            buttonHBox.getChildren().remove(cancelButton);
        }
        else {
            cancelButton.setText(VaultDialog.getCancelButtonText());
            cancelButton.setOnAction(actionEvent -> {
                VaultDialog.setResult(1);
                getStage().close();
            });
        }
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }
}
