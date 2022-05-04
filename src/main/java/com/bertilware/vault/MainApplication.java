package com.bertilware.vault;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Show the login dialog first and exit if the user cancelled.
        VaultDialog.createLoginDialog();
        if (VaultDialog.getResult() == 1) {
            System.exit(0);
        }

        // Load the FXML file and create the scene.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-form.fxml"));
        Scene scene = new Scene(loader.load());
        scene.setFill(Color.TRANSPARENT);
        // Make the window borderless,
        // set the scene and title,
        // and resize the stage to exactly what it needs.
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Vault");
        stage.sizeToScene();

        // Add the icon while making sure it's not null.
        // This is needed even with the title bar disabled,
        // so the icon in the taskbar is set.
        InputStream iconStream = getClass().getResourceAsStream("images/vault-icon.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }

        // Finally, show the main window and set the minimum dimensions.
        stage.show();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
    }

    public static void launchApplication() {
        launch();
    }
}
