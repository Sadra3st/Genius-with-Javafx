package genius;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

public class UserDashboard {
    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        HBox topBar = new HBox(10);
        Button homeBtn = new Button("Home");
        Button profileBtn = new Button("My Profile");
        Button logoutBtn = new Button("Logout");

        homeBtn.setOnAction(e -> HomeScreen.show());
        profileBtn.setOnAction(e -> UserProfileScreen.show(Main.currentUser));
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            MainMenuScreen.show();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(homeBtn, profileBtn, spacer, logoutBtn);
        layout.setTop(topBar);

        // Main content
        Label welcomeLabel = new Label("Welcome, " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        layout.setCenter(welcomeLabel);

        Main.primaryStage.setScene(new Scene(layout, 800, 600));
    }
}