package genius;

import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import java.util.*;
import java.io.IOException;

public class SongViewScreen {
    private static final int COMMENT_HEIGHT = 80;
    private static final int COMMENT_FONT_SIZE = 11;

    public static void show(Song song) {
        DataStorage.incrementSongViews(song.getId());

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(8));

        HBox header = new HBox(5);
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> HomeScreen.show());

        Label titleLabel = new Label(song.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, titleLabel);
        layout.setTop(header);

        VBox songInfo = new VBox(5);
        songInfo.setPadding(new Insets(5));
        songInfo.getChildren().addAll(
                new Label("Artist: " + song.getArtistId()),
                new Label("Genre: " + Optional.ofNullable(song.getGenre()).orElse("Unknown")),
                new Label("Tags: " + String.join(", ", song.getTags())),
                new Label("Views: " + DataStorage.loadSongViews(song.getId())),
                new Label("Release Date: " + song.getReleaseDate())
        );

        TextArea lyricsArea = new TextArea(song.getLyrics());
        lyricsArea.setEditable(false);
        lyricsArea.setWrapText(true);
        lyricsArea.setPrefHeight(200);
        lyricsArea.setStyle("-fx-font-size: 12px;");

        VBox centerBox = new VBox(10, songInfo, new Label("Lyrics:"), lyricsArea);
        layout.setCenter(centerBox);

        VBox commentContainer = new VBox(5);
        commentContainer.setPadding(new Insets(5));

        ListView<Comment> commentList = new ListView<>();
        commentList.setPrefHeight(COMMENT_HEIGHT);
        commentList.setStyle("-fx-font-size: " + COMMENT_FONT_SIZE + "px;");

        try {
            commentList.setItems(FXCollections.observableArrayList(song.getComments()));
        } catch (Exception e) {
            commentList.setPlaceholder(new Label("No comments yet"));
        }

        commentList.setCellFactory(lv -> new ListCell<Comment>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(2);
                    Label userLabel = new Label(comment.getUserId() + ":");
                    userLabel.setStyle("-fx-font-weight: bold;");

                    Text commentText = new Text(comment.getText());
                    commentText.setWrappingWidth(400);

                    box.getChildren().addAll(userLabel, commentText);
                    setGraphic(box);
                }
            }
        });

        HBox inputBox = new HBox(5);
        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.setPrefWidth(300);

        Button postBtn = new Button("Post");
        postBtn.setPrefWidth(60);
        postBtn.setOnAction(e -> postComment(song, commentField, commentList));

        commentField.setOnAction(e -> postComment(song, commentField, commentList));

        inputBox.getChildren().addAll(commentField, postBtn);
        commentContainer.getChildren().addAll(commentList, inputBox);
        layout.setBottom(commentContainer);

        Scene scene = new Scene(layout, 600, 500);
        Main.primaryStage.setScene(scene);
    }

    private static void postComment(Song song, TextField field, ListView<Comment> list) {
        String text = field.getText().trim();
        if (text.isEmpty() || Main.currentUser == null) return;

        try {
            Comment comment = new Comment(
                    UUID.randomUUID().toString(),
                    Main.currentUser.getUsername(),
                    song.getId(),
                    text
            );

            DataStorage.saveComment(comment);
            song.addComment(comment);
            list.getItems().add(comment);
            field.clear();

        } catch (IOException e) {
            System.err.println("Failed to save comment: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Failed to save comment").show();
        }
    }
}
