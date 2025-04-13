package genius;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ArtistDashboard {
    public static void show() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome Artist " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("You are logged in as a verified artist.");
        infoLabel.setTextFill(Color.BLUE);

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> ChangePasswordScreen.show());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });

        layout.getChildren().addAll(welcomeLabel, infoLabel, changePasswordBtn, logoutBtn);
        Scene scene = new Scene(layout, 350, 250);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Artist Dashboard");
        Main.primaryStage.show();
    }
}