package genius;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class MainMenuScreen {
    public static void show() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Welcome to Genius");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button browseBtn = new Button("Browse Songs");
        browseBtn.setPrefWidth(200);
        browseBtn.setOnAction(e -> HomeScreen.show());

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(200);
        loginBtn.setOnAction(e -> LoginScreen.show());

        Button registerBtn = new Button("Register");
        registerBtn.setPrefWidth(200);
        registerBtn.setOnAction(e -> RegisterScreen.show());

        layout.getChildren().addAll(titleLabel, browseBtn, loginBtn, registerBtn);

        Scene scene = new Scene(layout, 400, 400);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Genius - Main Menu");
    }
}