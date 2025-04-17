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
        loginBtn.setOnAction(e -> handleLogin(username, passwordField, message));

        Button backBtn = new Button("Back to Menu");
        backBtn.setOnAction(e -> MainMenuScreen.show());

        Button registerBtn = new Button("Go to Register");
        registerBtn.setOnAction(e -> RegisterScreen.show());

        layout.getChildren().addAll(title, username, passwordField, visiblePassword,
                showPassword, loginBtn, registerBtn, backBtn, message);

        Scene scene = new Scene(layout, 350, 400);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Login");
        Main.primaryStage.show();
    }

    private static void handleLogin(TextField usernameField, PasswordField passwordField, Label messageLabel) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        System.out.println("Attempting login for: " + username);

        try {
            if (UserStorage.validateLogin(username, password)) {
                System.out.println("Login validated!");

                Main.currentUser = UserStorage.getUser(username.toLowerCase());
                if (Main.currentUser == null) {
                    System.out.println("User returned is NULL");
                    messageLabel.setText("User not found.");
                    return;
                }

                System.out.println("User loaded: " + Main.currentUser.getUsername());
                System.out.println("Admin: " + Main.currentUser.isAdmin());
                System.out.println("Artist: " + Main.currentUser.isArtist());

                if (Main.currentUser.isAdmin()) {
                    AdminDashboard.show();
                } else if (Main.currentUser.isArtist()) {
                    ArtistDashboard.show();
                } else {
                    UserDashboard.show();
                }
            } else {
                messageLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace(); // THIS is what will show your real error
            messageLabel.setText("System error during login");
        }
    }
    }