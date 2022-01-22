package com.bertilware.vault;

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

    private double xOffset;
    private double yOffset;
    private double initialWidth;
    private double initialHeight;

    public void initialize() {
        if (EncryptionManager.getTheme() != null)
            rootVBox.setStyle(EncryptionManager.getTheme());

        // Load saved notes.
        notesTextArea.setText(EncryptionManager.getNotes());

        // Make the window movable.
        titleHBox.setOnMousePressed(mouseEvent -> {
            xOffset = mouseEvent.getSceneX();
            yOffset = mouseEvent.getSceneY();
        });
        titleHBox.setOnMouseDragged(mouseEvent -> {
            getStage().setX(mouseEvent.getScreenX() - xOffset);
            getStage().setY(mouseEvent.getScreenY() - yOffset);
        });
        // Make the two titlebar buttons work.
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> getStage().close());

        saveButton.setOnAction(actionEvent -> {
            try {
                EncryptionManager.setNotes(notesTextArea.getText().strip());
                EncryptionManager.encrypt();
                VaultDialog.createOkDialog("Notes saved!");
                getStage().close();
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        // Make the window resizable.
        resizeButton.setOnMousePressed(mouseEvent -> {
            initialWidth = getStage().getWidth();
            initialHeight = getStage().getHeight();
            xOffset = mouseEvent.getScreenX();
            yOffset = mouseEvent.getScreenY();
        });
        resizeButton.setOnMouseDragged(mouseEvent -> {
            double widthChange = mouseEvent.getScreenX() - xOffset;
            double heightChange = mouseEvent.getScreenY() - yOffset;
            getStage().setWidth(
                    widthChange == 0 ?
                            initialWidth :
                            Math.max(widthChange + initialWidth, 300)
            );
            getStage().setHeight(
                    heightChange == 0 ?
                            initialHeight :
                            Math.max(heightChange + initialHeight, 400)
            );
        });
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }

    public NotesDialogController() {
        this.xOffset = 0;
        this.yOffset = 0;
        this.initialWidth = 0;
        this.initialHeight = 0;
    }
}
