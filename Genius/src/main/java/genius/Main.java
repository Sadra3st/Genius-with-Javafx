package genius;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static Stage primaryStage;
    public static User currentUser;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        UserStorage.initialize();
        ArtistVerification.loadRequests(); // Load verification requests
        LoginScreen.show();
    }
}