package com.bertilware.vault;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class MainApplicationController {
    // Custom cell in order to properly display each account.
    static class AccountCell extends ListCell<Account> {
        public  AccountCell() {}

        @Override
        protected void updateItem(Account account, boolean b) {
            super.updateItem(account, b);
            setText(account == null ? "" : account.toString());
        }
    }

    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private MenuItem syncItem;
    @FXML
    private MenuItem importItem;
    @FXML
    private MenuItem exportItem;
    @FXML
    private MenuItem notesItem;
    @FXML
    private MenuItem aboutItem;
    @FXML
    private MenuItem bertilwareItem;
    @FXML
    private MenuItem camoItem;
    @FXML
    private MenuItem GunmetalItem;
    @FXML
    private MenuItem swedenItem;
    @FXML
    private MenuItem greeceItem;
    @FXML
    private MenuItem darkItem;
    @FXML
    private  MenuItem lightItem;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button maximizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton servicesToggle;
    @FXML
    private ToggleButton usernamesToggle;
    @FXML
    private ToggleButton passwordsToggle;
    @FXML
    private ListView<Account> accountsListView;
    @FXML
    private Button addButton;
    @FXML
    private Label countLabel;
    @FXML
    private Button resizeButton;
    @FXML
    private SVGPath maximizeButtonSVG;

    private ObservableList<Account> observableAccounts;
    private double xOffset;
    private double yOffset;
    private double initialWidth;
    private double initialHeight;

    // Initialize() has access to FXML loaded variables, the constructor does not.
    @FXML
    public void initialize() {
        // Set the theme
        if (EncryptionManager.getTheme() != null)
            rootVBox.setStyle(EncryptionManager.getTheme());

        // Populate the ListView
        observableAccounts = FXCollections.observableArrayList(EncryptionManager.getAccounts());
        accountsListView.setItems(observableAccounts);
        accountsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateCount();

        // Set a custom cell factory.
        // This is done for 2 reasons:
        // 1. To be able to disable all context menu items if the selected cell is empty.
        // 2. To clear the selection every time an empty cell is clicked on.
        accountsListView.setCellFactory(accountListView -> {
            AccountCell cell = new AccountCell();

            // Create the right-click context menu for each cell.
            MenuItem editItem = new MenuItem("Edit");
            MenuItem deleteItem = new MenuItem("Delete");
            MenuItem copyUsernameItem = new MenuItem("Copy username");
            MenuItem copyPasswordItem = new MenuItem("Copy password");
            ContextMenu cellContextMenu = new ContextMenu(editItem, deleteItem, copyUsernameItem, copyPasswordItem);
            cell.setContextMenu(cellContextMenu);

            editItem.setOnAction(actionEvent -> {
                VaultDialog.createAccountDialog(
                        cell.getItem().getService(),
                        cell.getItem().getUsername(),
                        cell.getItem().getPassword()
                );

                if (VaultDialog.getResult() == 0) {
                    try {
                        EncryptionManager.getAccounts().remove(cell.getItem());
                        EncryptionManager.encrypt();
                        updateList();
                    } catch (Exception e) {
                        VaultDialog.createErrorDialog(e);
                    }
                }
            });

            deleteItem.setOnAction(actionEvent -> {
                int count = accountListView.getSelectionModel().getSelectedItems().size();
                VaultDialog.createYesNoDialog("Are you sure you want to delete the selected " + count + " account(s)?");
                if (VaultDialog.getResult() == 0) {
                    try {
                        EncryptionManager.getAccounts().removeAll(accountListView.getSelectionModel().getSelectedItems());
                        EncryptionManager.encrypt();
                        updateList();
                        updateCount();
                    } catch (Exception e) {
                        VaultDialog.createErrorDialog(e);
                    }
                }
            });

            // Copy username to clipboard
            copyUsernameItem.setOnAction(actionEvent -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(cell.getItem().getUsername());
                clipboard.setContent(content);
            });

            // Copy password to clipboard
            copyPasswordItem.setOnAction(actionEvent -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(cell.getItem().getPassword());
                clipboard.setContent(content);
            });

            cell.setOnMouseClicked(mouseEvent -> {
                accountListView.requestFocus();

                // Open an AccountDialog on double click
                if (!cell.isEmpty() && mouseEvent.getClickCount() == 2) {
                    VaultDialog.createAccountDialog(
                            cell.getItem().getService(),
                            cell.getItem().getUsername(),
                            cell.getItem().getPassword()
                    );

                    if (VaultDialog.getResult() == 0) {
                        try {
                            EncryptionManager.getAccounts().remove(cell.getItem());
                            EncryptionManager.encrypt();
                            updateList();
                        } catch (Exception e) {
                            VaultDialog.createErrorDialog(e);
                        }
                    }
                    return;
                }

                // If the cell is empty, clear the ListView's selection,
                // and disable all items in the context menu.
                if (cell.isEmpty()) {
                    accountsListView.getSelectionModel().clearSelection();
                    editItem.setDisable(true);
                    deleteItem.setDisable(true);
                    copyPasswordItem.setDisable(true);
                    copyUsernameItem.setDisable(true);
                }
                // If multiple items are selected, only enable the deleteItem
                else if (accountListView.getSelectionModel().getSelectedItems().size() > 1) {
                    editItem.setDisable(true);
                    deleteItem.setDisable(false);
                    copyPasswordItem.setDisable(true);
                    copyUsernameItem.setDisable(true);
                }
                else {
                    accountsListView.getSelectionModel().select(cell.getIndex());
                    editItem.setDisable(false);
                    deleteItem.setDisable(false);
                    copyPasswordItem.setDisable(false);
                    copyUsernameItem.setDisable(false);
                }
            });

            return cell;
        });


        // Make the window movable if it is not maximized.
        titleHBox.setOnMousePressed(mouseEvent -> {
            if (getStage().isMaximized())
                return;
            // Get the initial position of the mouse pointer.
            xOffset = mouseEvent.getSceneX();
            yOffset = mouseEvent.getSceneY();
        });
        titleHBox.setOnMouseDragged(mouseEvent -> {
            if (getStage().isMaximized())
                return;
            getStage().setX(mouseEvent.getScreenX() - xOffset);
            getStage().setY(mouseEvent.getScreenY() - yOffset);
        });

        // This minimizes the window to the task bar
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        // This allows to enter and exit fullscreen mode
        maximizeButton.setOnAction(actionEvent -> {
            // Set the appropriate SVGPath icon
            maximizeButtonSVG.setContent(getStage().isMaximized() ?
                    "M3.646 9.146a.5.5 0 0 1 .708 0L8 12.793l3.646-3.647a.5.5 0 0 1 .708.708l-4 4a.5.5 0 0 1-.708 0l-4-4a.5.5 0 0 1 0-.708zm0-2.292a.5.5 0 0 0 .708 0L8 3.207l3.646 3.647a.5.5 0 0 0 .708-.708l-4-4a.5.5 0 0 0-.708 0l-4 4a.5.5 0 0 0 0 .708z" :
                    "M3.646 13.854a.5.5 0 0 0 .708 0L8 10.207l3.646 3.647a.5.5 0 0 0 .708-.708l-4-4a.5.5 0 0 0-.708 0l-4 4a.5.5 0 0 0 0 .708zm0-11.708a.5.5 0 0 1 .708 0L8 5.793l3.646-3.647a.5.5 0 0 1 .708.708l-4 4a.5.5 0 0 1-.708 0l-4-4a.5.5 0 0 1 0-.708z"
            );

            // Do the opposite of what the current state is
            getStage().setMaximized(!getStage().isMaximized());
        });
        // This closes all windows and terminates the application.
        exitButton.setOnAction(actionEvent -> Platform.exit());

        importItem.setOnAction(actionEvent -> {
            // Create the FileChoose window
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open a .csv file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File importFile = fileChooser.showOpenDialog(getStage());

            // Cancel if a file was not selected
            if (importFile == null) {
                VaultDialog.createOkDialog("No file was selected.");
                return;
            }

            try {
                Scanner in = new Scanner(importFile, StandardCharsets.UTF_8);
                // Use the header row to check if the file has the correct format
                if (!in.nextLine().equals("name,url,username,password")) {
                    VaultDialog.createOkDialog("The selected file has an invalid format.");
                }

                int newCount = 0;
                int duplicateCount = 0;
                while (in.hasNextLine()) {
                    String line = in.nextLine();
                    String[] cells = line.split(",");
                    // Use the URL field if the service name is blank
                    String name = cells[0].isBlank() ? cells[1] : cells[0];
                    String username = cells[2];
                    // Using a StringBuilder here allows the password to contain commas
                    cells[0] = "";
                    cells[1] = "";
                    cells[2] = "";
                    StringBuilder password = new StringBuilder();
                    for (String str : cells) {
                        password.append(str);
                    }

                    Account account = new Account(name, username, password.toString());
                    if (EncryptionManager.getAccounts().contains(account)) {
                        duplicateCount++;
                    }
                    else {
                        EncryptionManager.getAccounts().add(account);
                        newCount++;
                    }
                }
                in.close();

                // Save and show changes
                EncryptionManager.encrypt();
                updateList();
                updateCount();
                VaultDialog.createOkDialog("Imported " +
                        newCount + " new accounts and found " +
                        duplicateCount + " duplicate accounts that were ignored.");
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        exportItem.setOnAction(actionEvent -> {
            // Create the FileChoose window
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export to a .csv file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            File exportFile = fileChooser.showSaveDialog(getStage());

            // Cancel if a file was not selected
            if (exportFile == null) {
                VaultDialog.createOkDialog("No file was selected.");
                return;
            }

            try {
                PrintWriter writer = new PrintWriter(exportFile, StandardCharsets.UTF_8);
                // Write the header row
                writer.println("name,url,username,password");

                for (Account account: EncryptionManager.getAccounts()) {
                    writer.println(
                            account.getService() + "," +
                            "," +
                            account.getUsername() + "," +
                            account.getPassword()
                    );
                }
                writer.close();
            } catch (IOException  e) {
                VaultDialog.createOkDialog("Something went wrong while writing the file.");
                e.printStackTrace();
            }
        });

        syncItem.setOnAction(actionEvent -> {
            try {
                VaultDialog.createSyncDialog();
                EncryptionManager.encrypt();
                updateList();
                updateCount();
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        // These are all the theme menu items, they call the setTheme() method with the appropriate css variables.
        bertilwareItem.setOnAction(actionEvent -> setTheme(VaultThemes.bertilware));
        swedenItem.setOnAction(actionEvent -> setTheme(VaultThemes.sweden));
        greeceItem.setOnAction(actionEvent -> setTheme(VaultThemes.greece));
        camoItem.setOnAction(actionEvent -> setTheme(VaultThemes.camo));
        GunmetalItem.setOnAction(actionEvent -> setTheme(VaultThemes.gunmetal));
        darkItem.setOnAction(actionEvent -> setTheme(VaultThemes.dark));
        lightItem.setOnAction(actionEvent -> setTheme(VaultThemes.light));

        notesItem.setOnAction(actionEvent -> VaultDialog.createNotesDialog());
        aboutItem.setOnAction(actionEvent -> VaultDialog.createAboutDialog());

        addButton.setOnAction(actionEvent -> {
            VaultDialog.createAccountDialog("","","");
            if (VaultDialog.getResult() == 0) {
                try {
                    EncryptionManager.encrypt();
                    updateList();
                    updateCount();
                } catch (Exception e) {
                    VaultDialog.createErrorDialog(e);
                }
            }
        });

        // Update the ListView as you type in the search field
        searchField.textProperty().addListener((observableValue, s, t1) -> updateList());
        // Update the ListView every time a toggle button is used
        servicesToggle.setOnAction(actionEvent -> updateList());
        usernamesToggle.setOnAction(actionEvent -> updateList());
        passwordsToggle.setOnAction(actionEvent -> updateList());

        // Make the window resizable if it is not maximized
        resizeButton.setOnMousePressed(mouseEvent -> {
            if (getStage().isMaximized())
                return;

            // Get the starting dimensions of the window,
            // along with the initial mouse position
            initialWidth = getStage().getWidth();
            initialHeight = getStage().getHeight();
            xOffset = mouseEvent.getScreenX();
            yOffset = mouseEvent.getScreenY();
        });
        resizeButton.setOnMouseDragged(mouseEvent -> {
            if (getStage().isMaximized())
                return;

            double widthChange = mouseEvent.getScreenX() - xOffset;
            double heightChange = mouseEvent.getScreenY() - yOffset;

            // For both width and height:
            // If the change is 0 keep the initial width,
            // otherwise if the value is less than the minimum allowed,
            // set as the minimum allowed,
            // otherwise set as the change + the initial width
            getStage().setWidth(
                    widthChange == 0 ?
                            initialWidth :
                            Math.max(widthChange + initialWidth, getStage().getMinWidth())
            );

            getStage().setHeight(
                    heightChange == 0 ?
                            initialHeight :
                            Math.max(heightChange + initialHeight, getStage().getMinHeight())
            );
        });
    }

    // This needs to be in a separate method,
    // instead of setting a stage variable in the initialize method,
    // because when the initialize method gets called,
    // the stage isn't available yet.
    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }

    private void updateList() {
        observableAccounts.clear();

        for (Account account: EncryptionManager.getAccounts()) {
            // Create a string to search according to the toggle button selection
            StringBuilder stringToSearch = new StringBuilder();
            if (servicesToggle.isSelected())
                stringToSearch.append(account.getService());
            if (usernamesToggle.isSelected())
                stringToSearch.append(account.getUsername());
            if (passwordsToggle.isSelected())
                stringToSearch.append(account.getPassword());

            // Turn both strings to lowercase
            String finalString = stringToSearch.toString().toLowerCase(Locale.ROOT);
            String searchTerm = searchField.getText().toLowerCase(Locale.ROOT);

            // Check if the keyword exists in the search string
            if (finalString.contains(searchTerm))
                observableAccounts.add(account);
        }
    }

    private void updateCount() {
        countLabel.setText("Accounts: " + EncryptionManager.getAccounts().size());
    }

    private void setTheme(String theme) {
        try {
            EncryptionManager.setTheme(theme);
            rootVBox.setStyle(theme);
            EncryptionManager.encrypt();
        } catch (Exception e) {
            VaultDialog.createErrorDialog(e);
        }
    }

    public MainApplicationController() {
        this.xOffset = 0;
        this.yOffset = 0;
        this.initialWidth = 0;
        this.initialHeight = 0;
    }
}
