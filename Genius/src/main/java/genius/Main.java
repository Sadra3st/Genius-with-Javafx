package genius;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        LoginScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
