package genius;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class RegisterScreen {
    public static void show() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Register User");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Username");

        TextField email = new TextField();
        email.setPromptText("Email");

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

        CheckBox artistCheckbox = new CheckBox("I am an artist");

        Text passwordStrength = new Text();
        passwordStrength.setFill(Color.GRAY);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            int strength = calculatePasswordStrength(newVal);
            updatePasswordStrengthText(passwordStrength, strength);
        });

        Label message = new Label();
        message.setTextFill(Color.RED);

        Button registerBtn = new Button("Register");
        registerBtn.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = passwordField.getText().trim();
            String userEmail = email.getText().trim();
            boolean isArtist = artistCheckbox.isSelected();

            if (user.isEmpty() || pass.isEmpty() || userEmail.isEmpty()) {
                message.setText("Please fill all fields.");
                return;
            }

            if (user.length() < 4) {
                message.setText("Username must be at least 4 characters.");
                return;
            }

            if (pass.length() < 8) {
                message.setText("Password must be at least 8 characters.");
                return;
            }

            if (!userEmail.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                message.setText("Please enter a valid email address.");
                return;
            }

            if (UserStorage.isEmailBanned(userEmail)) {
                message.setText("This email is banned and cannot be used.");
                return;
            }

            if (calculatePasswordStrength(pass) < 2) {
                message.setText("Password is too weak. Please choose a stronger password.");
                return;
            }

            if (UserStorage.userExists(user)) {
                message.setText("Username already exists.");
            } else if (UserStorage.emailExists(userEmail)) {
                message.setText("Email already in use.");
            } else {
                // Artists start unverified
                UserStorage.registerUser(user, pass, userEmail, false, isArtist, !isArtist);
                message.setText("Registration successful! " +
                        (isArtist ? "Your artist account will be reviewed by an admin." : "You can now login."));
            }
        });

        Button backBtn = new Button("Back to Login");
        backBtn.setOnAction(e -> LoginScreen.show());

        layout.getChildren().addAll(title, username, email, passwordField, visiblePassword,
                showPassword, artistCheckbox, passwordStrength, registerBtn, backBtn, message);
        Scene scene = new Scene(layout, 350, 450);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Register");
        Main.primaryStage.show();
    }

    // Rest of the methods remain the same
    public static int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()':><?/].*")) strength++;
        return strength;
    }

    private static void updatePasswordStrengthText(Text text, int strength) {
        String[] descriptions = {"Very Weak", "Weak", "Moderate", "Strong", "Very Strong"};
        Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.DARKGREEN};

        int index = Math.min(strength, descriptions.length - 1);
        text.setText("Password Strength: " + descriptions[index]);
        text.setFill(colors[index]);
    }
}