package genius;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ChangePasswordScreen {
    public static void show() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Change Password");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");

        Label message = new Label();
        message.setTextFill(Color.RED);

        Button changeBtn = new Button("Change Password");
        changeBtn.setOnAction(e -> {
            String current = currentPassword.getText();
            String newPass = newPassword.getText();
            String confirm = confirmPassword.getText();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                message.setText("Please fill all fields.");
                return;
            }

            if (!UserStorage.validateLogin(Main.currentUser.getUsername(), current)) {
                message.setText("Current password is incorrect.");
                return;
            }

            if (!newPass.equals(confirm)) {
                message.setText("New passwords don't match.");
                return;
            }

            if (newPass.length() < 8) {
                message.setText("Password must be at least 8 characters.");
                return;
            }

            if (RegisterScreen.calculatePasswordStrength(newPass) < 3) {
                message.setText("Password is too weak. Please choose a stronger password.");
                return;
            }

            Main.currentUser.setPassword(newPass);
            UserStorage.updateUser(Main.currentUser);
            message.setText("Password changed successfully!");
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            if (Main.currentUser.isAdmin()) {
                AdminDashboard.show();
            } else {
                UserDashboard.show();
            }
        });

        layout.getChildren().addAll(title, currentPassword, newPassword,
                confirmPassword, changeBtn, backBtn, message);
        Scene scene = new Scene(layout, 350, 300);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Change Password");
        Main.primaryStage.show();
    }
}