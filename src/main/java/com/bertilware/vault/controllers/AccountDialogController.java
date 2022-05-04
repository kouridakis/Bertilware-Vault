package com.bertilware.vault.controllers;

import com.bertilware.vault.Account;
import com.bertilware.vault.VaultCommonHandlers;
import com.bertilware.vault.VaultManager;
import com.bertilware.vault.VaultDialog;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java.util.*;

public class AccountDialogController {
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox titleHBox;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private TextField serviceField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private CheckBox numbersCheckBox;
    @FXML
    private CheckBox uppercaseCheckBox;
    @FXML
    private CheckBox lowercaseCheckBox;
    @FXML
    private CheckBox specialCharactersCheckBox;
    @FXML
    private Label lengthLabel;
    @FXML
    private Slider lengthSlider;
    @FXML
    private Button generateButton;

    @FXML
    public void initialize() {
        rootVBox.setStyle(VaultManager.getTheme());

        // Load the selected account's details
        serviceField.setText(VaultDialog.getService());
        usernameField.setText(VaultDialog.getUsername());
        passwordField.setText(VaultDialog.getPassword());

        // Make the window movable.
        titleHBox.setOnMousePressed(VaultCommonHandlers.grabWindow());
        titleHBox.setOnMouseDragged(VaultCommonHandlers.moveWindow());

        // Make the two title bar buttons work.
        minimizeButton.setOnAction(actionEvent -> getStage().setIconified(true));
        exitButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });

        okButton.setOnAction(actionEvent -> {
            if (serviceField.getText().isBlank()
                    || usernameField.getText().isBlank()
                    || passwordField.getText().isBlank()) {
                VaultDialog.createOkDialog("You cannot leave any of the fields empty.");
                return;
            }

            Account account = new Account(
                    serviceField.getText().strip(),
                    usernameField.getText().strip(),
                    passwordField.getText()
            );

            for (Iterator<Account> iterator = VaultManager.getAccounts().iterator(); iterator.hasNext(); ) {
                Account savedAccount = iterator.next();
                if (account.isAlternativeOf(savedAccount)) {
                    iterator.remove();
                }
            }

            VaultManager.getAccounts().add(account);
            VaultDialog.setResult(0);
            getStage().close();
        });

        cancelButton.setOnAction(actionEvent -> {
            VaultDialog.setResult(1);
            getStage().close();
        });


        // Update the length label as the value is changed
        EventHandler<MouseEvent> lengthHandler = mouseEvent -> {
            String length = "Length: " + (int) lengthSlider.getValue();
            lengthLabel.setText(length);
        };
        lengthSlider.setOnMouseDragged(lengthHandler);
        lengthSlider.setOnMouseReleased(lengthHandler);

        generateButton.setOnAction(actionEvent -> {
            // The password generation is set up this was so that
            // there will be at least 1 character from every selected group
            SecureRandom generator = new SecureRandom();

            // Arraylist for the generated password
            ArrayList<Character> passwordArray = new ArrayList<>();

            // Create a hashmap and a set from the hashmap
            HashMap<CheckBox, String> boxesAndChars= new HashMap<>();
            boxesAndChars.put(numbersCheckBox, "0123456789");
            boxesAndChars.put(uppercaseCheckBox, "QWERTYUIOPASDFGHJKLZXCVBNM");
            boxesAndChars.put(specialCharactersCheckBox, "!@#$%^&*()-=_+[]{}<>/?;");
            boxesAndChars.put(lowercaseCheckBox, "qwertyuiopasdfghjklzxcvbnm");
            Set<Map.Entry<CheckBox, String>> checkBoxSet = boxesAndChars.entrySet();

            int count;
            int enabledCheckBoxes = 0;
            int length = (int) lengthSlider.getValue();
            // Check how many checkboxes are enabled
            for (Map.Entry<CheckBox, String> entry : checkBoxSet) {
                if (entry.getKey().isSelected())
                    enabledCheckBoxes++;
            }

            // Generate characters according to the enabled checkboxes
            for (Map.Entry<CheckBox, String> entry : checkBoxSet) {
                if (!entry.getKey().isSelected())
                    continue;
                else if (enabledCheckBoxes == 1)
                    count = length;
                else
                    // nextInt(max - min) + min
                    count = generator.nextInt(length / enabledCheckBoxes - 1) + 1;

                String characters = entry.getValue();
                for (int i=0; i<count; i++) {
                    passwordArray.add(characters.charAt(generator.nextInt(characters.length())));
                }
                length -= count;
                enabledCheckBoxes--;
            }

            // Shuffle the characters
            for (int i=0; i<passwordArray.size() * 3; i++) {
                int indexA = generator.nextInt(passwordArray.size());
                int indexB = generator.nextInt(passwordArray.size());

                char temp = passwordArray.get(indexA);
                passwordArray.set(indexA, passwordArray.get(indexB));
                passwordArray.set(indexB, temp);
            }

            StringBuilder finalPassword = new StringBuilder();
            for (Character character : passwordArray) {
                finalPassword.append(character);
            }
            passwordField.setText(finalPassword.toString());
        });
    }

    private Stage getStage() {
        return (Stage) titleHBox.getScene().getWindow();
    }
}
