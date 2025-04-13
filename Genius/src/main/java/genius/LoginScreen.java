package genius;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LoginScreen {
    public static void show() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

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
        message.setTextFill(Color.RED);

        Button loginBtn = new Button("Login");
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = passwordField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                message.setText("Please fill all fields.");
                return;
            }

            if (UserStorage.validateLogin(user, pass)) {
                Main.currentUser = UserStorage.getUser(user);
                message.setText("Login successful!");
                if (Main.currentUser.isAdmin()) {
                    AdminDashboard.show();
                } else if (Main.currentUser.isArtist()) {
                    ArtistDashboard.show();
                } else {
                    UserDashboard.show();
                }
            } else {
                User userObj = UserStorage.getUser(user);
                if (userObj != null && userObj.isArtist() && !userObj.isVerified()) {
                    message.setText("Your artist account is pending verification.");
                } else {
                    message.setText("Invalid credentials.");
                }
            }
        });

        Button registerBtn = new Button("Go to Register");
        registerBtn.setOnAction(e -> RegisterScreen.show());

        layout.getChildren().addAll(title, username, passwordField, visiblePassword,
                showPassword, loginBtn, registerBtn, message);
        Scene scene = new Scene(layout, 350, 350);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Login");
        Main.primaryStage.show();
    }
}