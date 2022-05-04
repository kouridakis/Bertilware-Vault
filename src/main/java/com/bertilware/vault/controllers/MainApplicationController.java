package com.bertilware.vault.controllers;

import com.bertilware.vault.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    private MenuItem editUserItem;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button maximizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button sortButton;
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
    private SortedList<Account> sortedAccounts;

    // Initialize() has access to FXML loaded variables, the constructor does not.
    @FXML
    public void initialize() {
        // Set the theme
        rootVBox.setStyle(VaultManager.getTheme());

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
                        VaultManager.getAccounts().remove(cell.getItem());
                        VaultManager.encrypt();
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
                        VaultManager.getAccounts().removeAll(accountListView.getSelectionModel().getSelectedItems());
                        VaultManager.encrypt();
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
                            VaultManager.getAccounts().remove(cell.getItem());
                            VaultManager.encrypt();
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

        // Populate the ListView
        observableAccounts = FXCollections.observableArrayList(VaultManager.getAccounts());
        sortedAccounts = new SortedList<>(observableAccounts);
        accountsListView.setItems(sortedAccounts);
        accountsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateList();
        updateCount();

        // Make the window movable if it is not maximized.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // This minimizes the window to the task bar
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        // This allows to enter and exit fullscreen mode
        maximizeButton.setOnAction(actionEvent -> {
            // Set the appropriate SVGPath icon
            maximizeButtonSVG.setContent(getStage().isMaximized() ?
                    "M3.646 9.146a.5.5 0 0 1 .708 0L8 12.793l3.646-3.647a.5.5 0 0 1 .708.708l-4 4a.5.5 0 0 1-.708 0l-4-4a.5.5 0 0 1 0-.708zm0-2.292a.5.5 0 0 0 .708 0L8 3.207l3.646 3.647a.5.5 0 0 0 .708-.708l-4-4a.5.5 0 0 0-.708 0l-4 4a.5.5 0 0 0 0 .708z" :
                    "M3.646 13.854a.5.5 0 0 0 .708 0L8 10.207l3.646 3.647a.5.5 0 0 0 .708-.708l-4-4a.5.5 0 0 0-.708 0l-4 4a.5.5 0 0 0 0 .708zm0-11.708a.5.5 0 0 1 .708 0L8 5.793l3.646-3.647a.5.5 0 0 1 .708.708l-4 4a.5.5 0 0 1-.708 0l-4-4a.5.5 0 0 1 0-.708z"
            );


            if (getStage().isMaximized()) {
                getStage().setMaximized(false);
            }
            else {
                getStage().setMaximized(true);
                // Setting the height and width manually is needed here in order to prevent
                // the window from covering the taskbar
                Rectangle2D currentScreenVisualBounds = Screen.getScreensForRectangle(
                        getStage().getX(), getStage().getY(), 1, 1
                ).get(0).getVisualBounds();
                getStage().setHeight(currentScreenVisualBounds.getHeight());
                getStage().setWidth(currentScreenVisualBounds.getWidth());
            }
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

            // Read the file
            try (Reader in = new FileReader(importFile, StandardCharsets.UTF_8)) {
                int newCount = 0;
                int duplicateCount = 0;
                // Headers of CSV files: name, url, username, password
                Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
                for (CSVRecord record : records) {
                    Account account = new Account(
                            record.get(0),
                            // Index 1 is ignored because it is the URL
                            record.get(2),
                            record.get(3)
                    );

                    if (VaultManager.getAccounts().contains(account)) {
                        duplicateCount++;
                    } else {
                        VaultManager.getAccounts().add(account);
                        newCount++;
                    }
                }

                // Save and show changes
                VaultManager.encrypt();
                updateList();
                updateCount();
                VaultDialog.createOkDialog("Imported " +
                        newCount + " new accounts and found " +
                        duplicateCount + " duplicate accounts that were ignored.");
            } catch (IOException e) {
                VaultDialog.createOkDialog("Something went wrong while reading the file.");
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

            // Write the file
            try (CSVPrinter printer = new CSVPrinter(
                    new FileWriter(exportFile, StandardCharsets.UTF_8),
                    CSVFormat.RFC4180)
            ) {
                printer.printRecord("name", "url", "username", "password");

                for (Account account : VaultManager.getAccounts()) {
                    printer.printRecord(account.getService(), "", account.getUsername(), account.getPassword());
                }
            } catch (IOException e) {
                VaultDialog.createOkDialog("Something went wrong while writing the file.");
                e.printStackTrace();
            }
        });

        syncItem.setOnAction(actionEvent -> {
            try {
                VaultDialog.createSyncDialog();
                VaultManager.encrypt();
                updateList();
                updateCount();
            } catch (Exception e) {
                VaultDialog.createErrorDialog(e);
            }
        });

        bertilwareItem.setOnAction(actionEvent -> setTheme(VaultThemes.BERTILWARE));
        swedenItem.setOnAction(actionEvent -> setTheme(VaultThemes.SWEDEN));
        greeceItem.setOnAction(actionEvent -> setTheme(VaultThemes.GREECE));
        GunmetalItem.setOnAction(actionEvent -> setTheme(VaultThemes.GUNMETAL));
        darkItem.setOnAction(actionEvent -> setTheme(VaultThemes.DARK));
        lightItem.setOnAction(actionEvent -> setTheme(VaultThemes.LIGHT));

        notesItem.setOnAction(actionEvent -> VaultDialog.createNotesDialog());
        aboutItem.setOnAction(actionEvent -> VaultDialog.createAboutDialog());

        editUserItem.setOnAction(actionEvent -> VaultDialog.createUserDialog());

        addButton.setOnAction(actionEvent -> {
            VaultDialog.createAccountDialog("","","");
            if (VaultDialog.getResult() == 0) {
                try {
                    VaultManager.encrypt();
                    updateList();
                    updateCount();
                } catch (Exception e) {
                    VaultDialog.createErrorDialog(e);
                }
            }
        });

        sortButton.setOnAction(actionEvent -> {
            List<String> sortOptions = Arrays.asList("Newest", "Oldest", "A to Z", "Z to A");

            // Get the index of the button's current text,
            // add 1 to change it, and % size() to wraparound if it reached the end of the list.
            // Finally, set the button's text as the string contained in the new index.
            sortButton.setText(
                    sortOptions.get(
                            (sortOptions.indexOf(sortButton.getText()) + 1) % sortOptions.size()
                    )
            );

            updateList();
        });

        // Update the ListView as you type in the search field
        searchField.textProperty().addListener((observableValue, s, t1) -> updateList());
        // Update the ListView every time a toggle button is used
        servicesToggle.setOnAction(actionEvent -> updateList());
        usernamesToggle.setOnAction(actionEvent -> updateList());
        passwordsToggle.setOnAction(actionEvent -> updateList());

        // Make the window resizable if it is not maximized
        resizeButton.setOnMousePressed(VaultCommonHandlers.grabWindow());
        resizeButton.setOnMouseDragged(VaultCommonHandlers.resizeWindow());
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

        // Filter accounts
        for (Account account: VaultManager.getAccounts()) {
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

        // Sort accounts
        Comparator<Account> comparator;
        if (sortButton.getText().equals("Newest")) {
            comparator = (o1, o2) -> (int)(o2.getCreationTime() - o1.getCreationTime());
        }
        else if (sortButton.getText().equals("Oldest")) {
            comparator = (o1, o2) -> (int)(o1.getCreationTime() - o2.getCreationTime());
        }
        else if (sortButton.getText().equals("A to Z")) {
            comparator = (o1, o2) -> o1.getService().compareTo(o2.getService());
        }
        else {
            comparator = (o1, o2) -> o2.getService().compareTo(o1.getService());
        }
        sortedAccounts.setComparator(comparator);
    }

    private void updateCount() {
        countLabel.setText("Accounts: " + VaultManager.getAccounts().size());
    }

    private void setTheme(String theme) {
        try {
            VaultManager.setTheme(theme);
            rootVBox.setStyle(theme);
            VaultManager.encrypt();
        } catch (Exception e) {
            VaultDialog.createErrorDialog(e);
        }
    }
}
