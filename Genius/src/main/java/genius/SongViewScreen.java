package genius;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SongViewScreen {
    private static final Map<String, String> userRatings = new HashMap<>(); // userId+songId -> "like"/"dislike"

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

        header.getChildren().addAll(backBtn, titleLabel, artistLabel);
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
        layout.setBottom(ratingBox);

        Scene scene = new Scene(layout, 800, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle(song.getTitle() + " - Genius");
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
    private static void setupCommentsSection(Song song, BorderPane layout) {
        VBox commentsBox = new VBox(10);
        commentsBox.setPadding(new Insets(15));

        Label commentsLabel = new Label("Comments");
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
                    setText((user != null ? user.getUsername() : "Unknown") + ": " +
                            comment.getText() + "\n" + comment.getTimestamp());
                }
            }
        });

        // Add comment form
        HBox commentForm = new HBox(10);
        TextField commentField = new TextField();
        commentField.setPromptText("Add a comment...");
        Button postCommentBtn = new Button("Post");

        postCommentBtn.setOnAction(e -> {
            if (!commentField.getText().isEmpty() && Main.currentUser != null) {
                Comment newComment = new Comment(
                        UUID.randomUUID().toString(),
                        Main.currentUser.getUsername(),
                        song.getId(),
                        commentField.getText()
                );
                song.addComment(newComment);
                commentsList.getItems().add(newComment);
                SongStorage.saveSong(song);
                commentField.clear();
            }
        });

        commentForm.getChildren().addAll(commentField, postCommentBtn);
        commentsBox.getChildren().addAll(commentsLabel, commentsList, commentForm);
        layout.setBottom(commentsBox);
    }
}