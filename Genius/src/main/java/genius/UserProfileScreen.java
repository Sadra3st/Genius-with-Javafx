package genius;

import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.*;

public class UserProfileScreen {
    public static void show(User user) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        HBox header = new HBox(10);
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToDashboard(user));

        Label titleLabel = new Label(user.getUsername() + "'s Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, spacer, titleLabel);
        layout.getChildren().add(header);


        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 5;");

        Label emailLabel = new Label("Email: " + user.getEmail());
        Label memberSinceLabel = new Label("Member since: " + getRegistrationDate(user));
        Label artistStatusLabel = new Label("Artist: " + (user.isArtist() ? "✓ Verified" : "Regular user"));
        if (user.isArtist() && user.isVerified() && !user.getUsername().equals(Main.currentUser.getUsername())) {
            Button followBtn = new Button();

            boolean alreadyFollowing = UserStorage.isFollowing(Main.currentUser.getUsername(), user.getUsername());
            followBtn.setText(alreadyFollowing ? "Unfollow" : "Follow");

            followBtn.setOnAction(e -> {
                if (UserStorage.isFollowing(Main.currentUser.getUsername(), user.getUsername())) {
                    UserStorage.unfollowArtist(Main.currentUser.getUsername(), user.getUsername());
                    followBtn.setText("Follow");
                } else {
                    UserStorage.followArtist(Main.currentUser.getUsername(), user.getUsername());
                    followBtn.setText("Unfollow");
                }
            });

            infoBox.getChildren().add(followBtn);
        }

        infoBox.getChildren().addAll(emailLabel, memberSinceLabel, artistStatusLabel);


        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));

        Label statsLabel = new Label("Activity Statistics");
        statsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        int commentCount = DataStorage.countUserComments(user.getUsername());
        int likeCount = countUserLikes(user.getUsername());
        int followingCount = UserStorage.countFollowing(user.getUsername());
        int followerCount = countFollowers(user.getUsername());

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);

        statsGrid.add(new Label("Comments:"), 0, 0);
        statsGrid.add(new Label(String.valueOf(commentCount)), 1, 0);
        statsGrid.add(new Label("Likes Given:"), 0, 1);
        statsGrid.add(new Label(String.valueOf(likeCount)), 1, 1);
        statsGrid.add(new Label("Following:"), 0, 2);
        statsGrid.add(new Label(followingCount + " artists"), 1, 2);
        statsGrid.add(new Label("Followers:"), 0, 3);
        statsGrid.add(new Label(followerCount + " users"), 1, 3);

        statsBox.getChildren().addAll(statsLabel, statsGrid);

        if (followingCount > 0) {
            VBox followingBox = new VBox(10);
            followingBox.setPadding(new Insets(15));

            Label followingLabel = new Label("Followed Artists");
            followingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            ListView<String> followingList = new ListView<>();
            try {
                List<String> artists = Files.lines(Paths.get(UserStorage.FOLLOWING_FILE))
                        .filter(line -> line.startsWith(user.getUsername() + "|"))
                        .map(line -> line.split("\\|")[1])
                        .collect(Collectors.toList());
                followingList.getItems().addAll(artists);
            } catch (IOException e) {
                followingList.getItems().add("Error loading followed artists");
            }

            followingBox.getChildren().addAll(followingLabel, followingList);
            layout.getChildren().add(followingBox);
        }


        if (commentCount > 0) {
            VBox commentsBox = new VBox(10);
            commentsBox.setPadding(new Insets(15));

            Label commentsLabel = new Label("Recent Comments");
            commentsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            ListView<Comment> commentsList = new ListView<>();
            try {
                commentsList.setItems(FXCollections.observableArrayList(
                        DataStorage.loadUserComments(user.getUsername()).stream()
                                .sorted(Comparator.comparing(Comment::getTimestamp).reversed())
                                .limit(5)
                                .collect(Collectors.toList())
                ));
            } catch (Exception e) {

            }

            commentsList.setCellFactory(lv -> new ListCell<Comment>() {
                @Override
                protected void updateItem(Comment comment, boolean empty) {
                    super.updateItem(comment, empty);
                    if (empty || comment == null) {
                        setText(null);
                    } else {
                        try {
                            Song song = DataStorage.loadSong(comment.getSongId());
                            setText("On '" + song.getTitle() + "':\n" +
                                    comment.getText() + "\n" +
                                    comment.getTimestamp().toLocalDate());
                        } catch (Exception e) {
                            setText("Comment on deleted song");
                        }
                    }
                }
            });

            commentsBox.getChildren().addAll(commentsLabel, commentsList);
            layout.getChildren().add(commentsBox);
        }

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 800, 600);
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

    private static int countUserLikes(String username) {
        try {
            return (int) DataStorage.loadAllSongs().stream()
                    .flatMap(song -> song.getComments().stream())
                    .filter(comment -> comment.getUserId().equals(username))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private static int countFollowers(String username) {
        try {
            return (int) Files.lines(Paths.get(UserStorage.FOLLOWING_FILE))
                    .filter(line -> line.endsWith("|" + username))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }

    private static String getRegistrationDate(User user) {
        // This should be replaced with actual registration date
        return "April 2025"; //
    }

}