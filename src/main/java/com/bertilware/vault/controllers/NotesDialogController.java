package com.bertilware.vault.controllers;

import com.bertilware.vault.VaultCommonHandlers;
import com.bertilware.vault.VaultManager;
import com.bertilware.vault.VaultDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NotesDialogController {
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private TextArea notesTextArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button resizeButton;

    public void initialize() {
        rootVBox.setStyle(VaultManager.getTheme());

        // Load saved notes.
        notesTextArea.setText(VaultManager.getNotes());

        // Make the window movable.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // Make the window resizable.
        resizeButton.setOnMousePressed(VaultCommonHandlers.grabWindow());
        resizeButton.setOnMouseDragged(VaultCommonHandlers.resizeWindow());

        // Make the two title bar buttons work.
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> getStage().close());

        saveButton.setOnAction(actionEvent -> {
            try {
                VaultManager.setNotes(notesTextArea.getText().strip());
                VaultManager.encrypt();
                VaultDialog.createOkDialog("Notes saved!");
                getStage().close();
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }
}
