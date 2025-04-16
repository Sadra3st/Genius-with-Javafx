package genius;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SongViewScreen {
    private static final Map<String, String> userRatings = new HashMap<>(); // userId+songId -> "like"/"dislike"
    private static final Map<String, Boolean> followStatus = new HashMap<>(); // userId+artistId -> true/false

    public static void show(Song song) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Header with back button and song info
        HBox header = new HBox(10);
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> HomeScreen.show());

        Label titleLabel = new Label(song.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        User artist = UserStorage.getUser(song.getArtistId());
        Label artistLabel = new Label("By " + (artist != null ? artist.getUsername() : "Unknown Artist"));

        // Follow button
        Button followBtn = new Button();
        updateFollowButton(followBtn, song.getArtistId());
        followBtn.setOnAction(e -> toggleFollowArtist(song.getArtistId(), followBtn));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, titleLabel, artistLabel, spacer, followBtn);
        layout.setTop(header);

        // Lyrics display with proper formatting
        TextArea lyricsArea = new TextArea(song.getLyrics());
        lyricsArea.setEditable(false);
        lyricsArea.setWrapText(true);
        lyricsArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");
        layout.setCenter(lyricsArea);

        // Rating system
        HBox ratingBox = new HBox(10);
        Button likeBtn = new Button("👍 " + song.getLikeCount());
        Button dislikeBtn = new Button("👎 " + song.getDislikeCount());

        // Initialize button states
        String currentUserId = Main.currentUser != null ? Main.currentUser.getUsername() : null;
        String ratingKey = currentUserId + "|" + song.getId();

        if (currentUserId != null) {
            String currentRating = userRatings.get(ratingKey);
            if ("like".equals(currentRating)) {
                likeBtn.setStyle("-fx-background-color: #aaffaa;");
            } else if ("dislike".equals(currentRating)) {
                dislikeBtn.setStyle("-fx-background-color: #ffaaaa;");
            }
        }

        // Rating handlers
        likeBtn.setOnAction(e -> handleRating(song, likeBtn, dislikeBtn, true, ratingKey));
        dislikeBtn.setOnAction(e -> handleRating(song, likeBtn, dislikeBtn, false, ratingKey));

        // Double-click to remove rating
        likeBtn.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && "like".equals(userRatings.get(ratingKey))) {
                removeRating(song, likeBtn, dislikeBtn, ratingKey);
            }
        });

        dislikeBtn.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && "dislike".equals(userRatings.get(ratingKey))) {
                removeRating(song, likeBtn, dislikeBtn, ratingKey);
            }
        });

        ratingBox.getChildren().addAll(likeBtn, dislikeBtn);

        // Combine rating and comment sections
        VBox bottomSection = new VBox(10);
        bottomSection.getChildren().add(ratingBox);
        setupCommentsSection(song, bottomSection);
        layout.setBottom(bottomSection);

        Scene scene = new Scene(layout, 800, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle(song.getTitle() + " - Genius");
    }

    private static void toggleFollowArtist(String artistId, Button followBtn) {
        if (Main.currentUser == null || artistId.equals(Main.currentUser.getUsername())) return;

        String currentUser = Main.currentUser.getUsername();
        String followKey = currentUser + "|" + artistId;

        if (UserStorage.isFollowing(currentUser, artistId)) {
            UserStorage.unfollowArtist(currentUser, artistId);
            followStatus.put(followKey, false);
        } else {
            UserStorage.followArtist(currentUser, artistId);
            followStatus.put(followKey, true);
        }
        updateFollowButton(followBtn, artistId);
    }

    private static void updateFollowButton(Button followBtn, String artistId) {
        if (Main.currentUser == null || artistId.equals(Main.currentUser.getUsername())) {
            followBtn.setVisible(false);
            return;
        }

        String followKey = Main.currentUser.getUsername() + "|" + artistId;
        boolean isFollowing = followStatus.computeIfAbsent(followKey,
                k -> UserStorage.isFollowing(Main.currentUser.getUsername(), artistId));

        followBtn.setText(isFollowing ? "Following ✓" : "+ Follow");
        followBtn.setStyle(isFollowing ?
                "-fx-background-color: #4CAF50; -fx-text-fill: white;" :
                "-fx-background-color: #2196F3; -fx-text-fill: white;");
    }

    private static void handleRating(Song song, Button likeBtn, Button dislikeBtn, boolean isLike, String ratingKey) {
        if (Main.currentUser == null) return;

        String currentRating = userRatings.get(ratingKey);

        if (isLike) {
            if (!"like".equals(currentRating)) {
                // Add like
                song.incrementLikes();
                userRatings.put(ratingKey, "like");

                // Remove dislike if exists
                if ("dislike".equals(currentRating)) {
                    song.incrementDislikes(); // Decrement dislike count
                }
            }
        } else {
            if (!"dislike".equals(currentRating)) {
                // Add dislike
                song.incrementDislikes();
                userRatings.put(ratingKey, "dislike");

                // Remove like if exists
                if ("like".equals(currentRating)) {
                    song.incrementLikes(); // Decrement like count
                }
            }
        }

        updateButtons(song, likeBtn, dislikeBtn, ratingKey);
        SongStorage.saveSong(song);
    }

    private static void removeRating(Song song, Button likeBtn, Button dislikeBtn, String ratingKey) {
        String currentRating = userRatings.get(ratingKey);

        if ("like".equals(currentRating)) {
            song.incrementLikes(); // Decrement like count
        } else if ("dislike".equals(currentRating)) {
            song.incrementDislikes(); // Decrement dislike count
        }

        userRatings.remove(ratingKey);
        updateButtons(song, likeBtn, dislikeBtn, ratingKey);
        SongStorage.saveSong(song);
    }

    private static void updateButtons(Song song, Button likeBtn, Button dislikeBtn, String ratingKey) {
        likeBtn.setText("👍 " + song.getLikeCount());
        dislikeBtn.setText("👎 " + song.getDislikeCount());

        String currentRating = userRatings.get(ratingKey);
        likeBtn.setStyle("".equals(currentRating) || !"like".equals(currentRating) ? "" : "-fx-background-color: #aaffaa;");
        dislikeBtn.setStyle("".equals(currentRating) || !"dislike".equals(currentRating) ? "" : "-fx-background-color: #ffaaaa;");
    }

    private static void setupCommentsSection(Song song, VBox container) {
        VBox commentsBox = new VBox(10);
        commentsBox.setPadding(new Insets(15));

        Label commentsLabel = new Label("Comments (" + song.getComments().size() + ")");
        commentsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<Comment> commentsList = new ListView<>();
        commentsList.setItems(FXCollections.observableArrayList(song.getComments()));
        commentsList.setCellFactory(lv -> new ListCell<Comment>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                } else {
                    User user = UserStorage.getUser(comment.getUserId());
                    setText((user != null ? user.getUsername() : "Unknown") +
                            " (" + comment.getTimestamp().toLocalDate() + "):\n" +
                            comment.getText());
                }
            }
        });

        // Add comment form
        HBox commentForm = new HBox(10);
        TextArea commentField = new TextArea();
        commentField.setPromptText("Write your comment...");
        commentField.setPrefRowCount(2);
        commentField.setWrapText(true);

        Button postCommentBtn = new Button("Post");
        postCommentBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        postCommentBtn.setOnAction(e -> {
            String commentText = commentField.getText().trim();
            if (!commentText.isEmpty() && Main.currentUser != null) {
                Comment newComment = new Comment(
                        UUID.randomUUID().toString(),
                        Main.currentUser.getUsername(),
                        song.getId(),
                        commentText
                );
                song.addComment(newComment);
                try {
                    DataStorage.saveComment(newComment);
                    commentsList.getItems().add(newComment);
                    commentsLabel.setText("Comments (" + song.getComments().size() + ")");
                    commentField.clear();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save comment").show();
                }
            }
        });

        commentForm.getChildren().addAll(commentField, postCommentBtn);
        HBox.setHgrow(commentField, Priority.ALWAYS);
        commentsBox.getChildren().addAll(commentsLabel, commentsList, commentForm);
        container.getChildren().add(commentsBox);
    }
}