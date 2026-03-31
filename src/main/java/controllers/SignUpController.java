package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.sql.SQLException;

import dao.userDao;
import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class SignUpController {

    @FXML
    private TextField nameField, emailField, usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private ComboBox<String> sex;
    @FXML
    private Label messageLabel;

    @FXML
    private void handleButtonPress(MouseEvent e) {
        Button btn = (Button) e.getSource();
        btn.setStyle(btn.getStyle() + "-fx-opacity: 0.7;");
    }

    @FXML
    private void handleButtonRelease(MouseEvent e) {
        Button btn = (Button) e.getSource();
        btn.setStyle(btn.getStyle().replace("-fx-opacity: 0.7;", ""));
    }

    @FXML
    public void initialize() {
        sex.getItems().addAll("Male", "Female", "Other");
        dobPicker.setEditable(false);
    }

    @FXML
    private void handleBack() {
        try {
            BorderPane root = (BorderPane) nameField.getScene().getRoot();
            BorderPane loginView = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            root.setRight(loginView.getRight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateAccount() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()
                || dobPicker.getValue() == null || sex.getValue() == null) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        userDao user = new userDao();
        try {
            boolean success = user.createAccount(name, sex.getValue(), dobPicker.getValue(), username, password, email);
            if (success) {
                try {
                    BorderPane root = (BorderPane) nameField.getScene().getRoot();
                    BorderPane loginView = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                    root.setRight(loginView.getRight());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            // Show user-friendly messages based on constraint violation
            if (e.getMessage().contains("Email already exists.")) messageLabel.setText("Email is already registered."); 
            else if (e.getMessage().contains("Username already exists.")) messageLabel.setText("Username is already taken.");
            else if (e.getMessage().contains("Gender must be")) messageLabel.setText("Invalid gender selected.");
            else if (e.getMessage().contains("Username format invalid.")) messageLabel.setText("Username format is invalid.");
            else if (e.getMessage().contains("All fields are required.")) messageLabel.setText("Please fill all fields.");
            else messageLabel.setText("Account creation failed: " + e.getMessage());
        }
    }
}
