package genius;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterScreen {

    public static void show() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Register");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField visiblePassword = new TextField();
        visiblePassword.setManaged(false);
        visiblePassword.setVisible(false);
        visiblePassword.managedProperty().bind(passwordField.visibleProperty().not());
        visiblePassword.visibleProperty().bind(passwordField.visibleProperty().not());
        visiblePassword.textProperty().bindBidirectional(passwordField.textProperty());

        CheckBox showPassword = new CheckBox("Show Password");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
            visiblePassword.setVisible(show);
            visiblePassword.setManaged(show);
        });

        Label message = new Label();

        Button registerBtn = new Button("Register");
        registerBtn.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = passwordField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                message.setText("Please fill all fields.");
                return;
            }

            if (UserStorage.userExists(user)) {
                message.setText("Username already exists.");
            } else {
                UserStorage.registerUser(user, pass);
                message.setText("Registration successful!");
            }
        });

        Button backBtn = new Button("Back to Login");
        backBtn.setOnAction(e -> LoginScreen.show());

        layout.getChildren().addAll(title, username, passwordField, visiblePassword, showPassword, registerBtn, backBtn, message);
        Main.primaryStage.setScene(new Scene(layout, 300, 300));
        Main.primaryStage.setTitle("Register");
        Main.primaryStage.show();
    }
}
