package com.bertilware.vault;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public final class VaultCommonHandlers {
    private static double xOffsetMove;
    private static double yOffsetMove;
    private static double xOffsetResize;
    private static double yOffsetResize;
    private static double initialWidth;
    private static double initialHeight;


    public static EventHandler<MouseEvent> grabWindow() {
        return mouseEvent -> {
            Node source = (Node) mouseEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            if (stage.isMaximized())
                return;

            // Get the initial position of the mouse pointer, both for move and resize
            xOffsetMove = mouseEvent.getSceneX();
            yOffsetMove = mouseEvent.getSceneY();

            xOffsetResize = mouseEvent.getScreenX();
            yOffsetResize = mouseEvent.getScreenY();

            // Get starting dimensions of the window.
            initialWidth = stage.getWidth();
            initialHeight = stage.getHeight();
        };

    }

    public static EventHandler<MouseEvent> moveWindow() {
        return mouseEvent -> {
            Node source = (Node) mouseEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            if (stage.isMaximized())
                return;

            stage.setX(mouseEvent.getScreenX() - xOffsetMove);
            stage.setY(mouseEvent.getScreenY() - yOffsetMove);
        };

    }

    public static EventHandler<MouseEvent> resizeWindow() {
        return mouseEvent -> {
            Node source = (Node) mouseEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            if (stage.isMaximized())
                return;

            double widthChange = mouseEvent.getScreenX() - xOffsetResize;
            double heightChange = mouseEvent.getScreenY() - yOffsetResize;

            // For both width and height:
            // If the change is 0 keep the initial width,
            // otherwise if the value is less than the minimum allowed,
            // set as the minimum allowed,
            // otherwise set as the change + the initial width
            stage.setWidth(
                    widthChange == 0 ?
                            initialWidth :
                            Math.max(widthChange + initialWidth, stage.getMinWidth())
            );

            stage.setHeight(
                    heightChange == 0 ?
                            initialHeight :
                            Math.max(heightChange + initialHeight, stage.getMinHeight())
            );
        };
    }
}
