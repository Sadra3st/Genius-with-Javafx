package genius;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.stream.Collectors;

public class ArtistProfileScreen {
    public static void show(Artist artist) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        
        HBox header = new HBox(10);
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> HomeScreen.show());

        Label titleLabel = new Label("Artist: " + artist.getUsername());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, spacer, titleLabel);
        layout.setTop(header);

        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(15));


        Label bioLabel = new Label("Bio: " + (artist.getBio() != null ? artist.getBio() : "No bio available."));
        Label followersLabel = new Label("Followers: " + artist.getFollowerCount());


        Button followBtn = new Button();
        String currentUsername = Main.currentUser != null ? Main.currentUser.getUsername() : null;
        boolean isFollowing = currentUsername != null && artist.getFollowers().contains(currentUsername);
        followBtn.setText(isFollowing ? "Unfollow" : "Follow");

        followBtn.setOnAction(e -> {
            if (Main.currentUser == null) {
                new Alert(Alert.AlertType.INFORMATION, "You need to be logged in to follow an artist.").show();
                return;
            }

            if (artist.getFollowers().contains(Main.currentUser.getUsername())) {
                artist.removeFollower(Main.currentUser.getUsername());
                followBtn.setText("Follow");
            } else {
                artist.addFollower(Main.currentUser.getUsername());
                followBtn.setText("Unfollow");
            }

            ArtistStorage.saveArtist(artist);
            followersLabel.setText("Followers: " + artist.getFollowerCount());
        });


        List<Song> artistSongs = SongStorage.getAllSongs().stream()
                .filter(s -> s.getArtistId().equals(artist.getUsername()))
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .collect(Collectors.toList());

        List<Album> artistAlbums = AlbumStorage.getAlbumsByArtist(artist.getUsername());


        ListView<Song> songsListView = new ListView<>(FXCollections.observableArrayList(artistSongs));
        songsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                setText(empty || song == null ? null : song.getTitle());
            }
        });
        songsListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selected = songsListView.getSelectionModel().getSelectedItem();
                if (selected != null) SongViewScreen.show(selected);
            }
        });


        ListView<Album> albumsListView = new ListView<>(FXCollections.observableArrayList(artistAlbums));
        albumsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                setText(empty || album == null ? null : album.getTitle());
            }
        });
        albumsListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Album selected = albumsListView.getSelectionModel().getSelectedItem();
                if (selected != null) AlbumViewScreen.show(selected);
            }
        });


        contentBox.getChildren().addAll(
                bioLabel,
                followersLabel,
                followBtn,
                new Label("Songs:"), songsListView,
                new Label("Albums:"), albumsListView
        );

        layout.setCenter(contentBox);

        Scene scene = new Scene(layout, 800, 600);
        Main.primaryStage.setScene(scene);
    }
}
