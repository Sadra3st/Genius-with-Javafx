package genius;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;

public class AlbumViewScreen {
    public static void show(Album album) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        HBox header = new HBox(10);
        Button backBtn = new Button("\u2190 Back");
        backBtn.setOnAction(e -> HomeScreen.show());

        Label titleLabel = new Label(album.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, spacer, titleLabel);
        layout.setTop(header);

        VBox albumInfo = new VBox(10);
        albumInfo.setPadding(new Insets(10));

        Label artistLabel = new Label("Artist: " + album.getArtistUsername());
        Label releaseDateLabel = new Label("Release Date: " + album.getReleaseDate());
        Label tracksLabel = new Label("Tracks: " + album.getSongIds().size());

        albumInfo.getChildren().addAll(artistLabel, releaseDateLabel, tracksLabel);

        ListView<Song> songList = new ListView<>();
        try {
            List<Song> songs = SongController.getSongsByAlbum(album.getId());

            songList.setItems(FXCollections.observableArrayList(songs));
            songList.setCellFactory(lv -> new ListCell<Song>() {
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    if (empty || song == null) {
                        setText(null);
                    } else {
                        setText((getIndex() + 1) + ". " + song.getTitle());
                    }
                }
            });

            songList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Song selected = songList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        SongViewScreen.show(selected);
                    }
                }
            });
        } catch (Exception e) {
            albumInfo.getChildren().add(new Text("Failed to load songs: " + e.getMessage()));
        }

        VBox contentBox = new VBox(20, albumInfo, new Label("Tracklist:"), songList);
        contentBox.setPadding(new Insets(15));
        layout.setCenter(contentBox);

        Scene scene = new Scene(layout, 700, 500);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Album - " + album.getTitle());
    }
}
