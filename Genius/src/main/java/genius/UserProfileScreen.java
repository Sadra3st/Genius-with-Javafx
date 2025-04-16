package genius;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class UserProfileScreen {
    public static void show(User user) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);


        HBox header = new HBox();
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToDashboard(user));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label titleLabel = new Label(user.getUsername() + "'s Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, spacer, titleLabel);
        layout.getChildren().add(header);

        // User info section
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 5;");

        Label emailLabel = new Label("Email: " + user.getEmail());

        Label activityLabel = new Label("Activity");
        activityLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> activityList = new ListView<>();
        activityList.getItems().addAll(
                "Comments posted: " + countUserComments(user.getUsername()),
                "Songs liked: " + countUserLikes(user.getUsername()),
                "Following: " + countFollowing(user.getUsername())
        );


        Label favoritesLabel = new Label("Favorite Songs");
        favoritesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<Song> favoritesList = new ListView<>();
        favoritesList.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                setText(empty || song == null ? null : song.getTitle() + " by " +
                        UserStorage.getUser(song.getArtistId()).getUsername());
            }
        });

        layout.getChildren().addAll(
                infoBox, activityLabel, activityList,
                favoritesLabel, favoritesList
        );


        Scene scene = new Scene(layout, 600, 700);
        Main.primaryStage.setScene(scene);
    }

    private static void returnToDashboard(User user) {
        if (user.isAdmin()) {
            AdminDashboard.show();
        } else if (user.isArtist()) {
            ArtistDashboard.show();
        } else {
            UserDashboard.show();
        }
    }

    private static int countUserComments(String username) {
        try {
            return (int) DataStorage.loadAllSongs().stream()
                    .flatMap(song -> song.getComments().stream())
                    .filter(comment -> comment.getUserId().equals(username))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private static int countUserLikes(String username) {
        return 0;
    }

    private static int countFollowing(String username) {
        return 0;
    }

}