package com.bertilware.vault.controllers;

import com.bertilware.vault.VaultCommonHandlers;
import com.bertilware.vault.VaultManager;
import com.bertilware.vault.SyncClient;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncDialogController {
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Button button;

    @FXML
    public void initialize() {
        // Run sync asynchronously
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // Set the theme
        rootVBox.setStyle(VaultManager.getTheme());

        // Make the window movable.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // Make the two title bar buttons work
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> {
            executor.shutdown();
            getStage().close();
        });

        button.setOnAction(mouseEvent -> {
            Task<Void> sync = new SyncClient();
            // Disable button while syncing
            sync.setOnRunning(workerStateEvent -> button.setDisable(true));
            sync.setOnSucceeded(workerStateEvent -> button.setDisable(false));

            // Display progress
            messageLabel.textProperty().bind(sync.messageProperty());

            executor.execute(sync);
        });
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }
}
