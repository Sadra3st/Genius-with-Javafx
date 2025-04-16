package genius;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {
    public static Stage primaryStage;
    public static User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize components
            UserStorage.initialize();
            ArtistVerification.loadRequests();

            // Set up primary stage
            Main.primaryStage = primaryStage;
            Main.primaryStage.setTitle("Genius App");

            // Show main menu
            MainMenuScreen.show();

            // CRUCIAL: Make sure to show the stage
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Application failed to start");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}