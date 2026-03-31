package controllers;

import dao.userDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class loginController {

    @FXML
    private AnchorPane rightPane;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
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
    private void handleSignin() throws Exception {
        AnchorPane signInView = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
        rightPane.getChildren().setAll(signInView);
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both fields.");
            return;
        }

        userDao user = new userDao();
        if (user.validateLogin(username, password)) {
            try {
                BorderPane dashboard = FXMLLoader.load(
                        getClass().getResource("/fxml/dashboard2.fxml"));

                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(dashboard);
                stage.setScene(scene);
                stage.sizeToScene();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            messageLabel.setText("Invalid credentials!");
            passwordField.clear();
            usernameField.setFocusTraversable(false);
        }
        return;
    }
}