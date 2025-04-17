package genius;

import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.util.*;
import java.util.stream.Collectors;

public class HomeScreen {
    private static ListView<Object> displayList;

    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Search for artist, song, or album");
        Button searchBtn = new Button("Search");

        searchBtn.setOnAction(e -> searchEntities(searchField.getText()));

        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToPreviousScreen());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, spacer, searchField, searchBtn);
        layout.setTop(topBar);

        displayList = new ListView<>();
        refreshTopSongs();

        displayList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Object selected = displayList.getSelectionModel().getSelectedItem();
                if (selected instanceof Song) {
                    SongViewScreen.show((Song) selected);
                } else if (selected instanceof Album) {
                    AlbumViewScreen.show((Album) selected);
                } else if (selected instanceof Artist) {
                    ArtistProfileScreen.show((Artist) selected);
                }
            }
        });

        layout.setCenter(displayList);

        Scene scene = new Scene(layout, 900, 600);
        Main.primaryStage.setScene(scene);
    }

    public static void refreshTopSongs() {
        try {
            List<Song> allSongs = new ArrayList<>();
            allSongs.addAll(SongStorage.getAllSongs());
            allSongs.addAll(DataStorage.loadAllSongs());

            Map<String, Integer> viewMap = new HashMap<>();
            for (Song song : allSongs) {
                int views = DataStorage.loadSongViews(song.getId());
                viewMap.put(song.getId(), views);
            }

            allSongs.sort(Comparator.comparingInt(s -> -viewMap.getOrDefault(s.getId(), 0)));

            displayList.setItems(FXCollections.observableArrayList(allSongs));
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load songs: " + e.getMessage()).show();
        }
    }

    public static void searchEntities(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshTopSongs();
            return;
        }

        query = query.toLowerCase();
        List<Object> results = new ArrayList<>();

        for (Song song : SongStorage.getAllSongs()) {
            if (song.getTitle().toLowerCase().contains(query) ||
                    song.getArtistId().toLowerCase().contains(query)) {
                results.add(song);
            }
        }

        for (Artist artist : ArtistStorage.getAllArtists()) {
            if (artist.getUsername().toLowerCase().contains(query)) {
                results.add(artist);
            }
        }

        for (Album album : AlbumStorage.getAllAlbums()) {
            if (album.getTitle().toLowerCase().contains(query)) {
                results.add(album);
            }
        }

        displayList.setItems(FXCollections.observableArrayList(results));
    }

    public static void showFollowing() {
        try {
            List<String> following = UserStorage.getFollowedArtists(Main.currentUser.getUsername());
            List<Object> feed = new ArrayList<>();

            for (String artistName : following) {
                Artist artist = ArtistStorage.getArtistByUsername(artistName);
                if (artist != null) {
                    feed.add(artist);
                    feed.addAll(AlbumStorage.getAlbumsByArtist(artist.getUsername()));
                    for (Song song : SongStorage.getAllSongs()) {
                        if (song.getArtistId().equals(artist.getUsername())) {
                            feed.add(song);
                        }
                    }
                }
            }

            displayList.setItems(FXCollections.observableArrayList(feed));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error loading following feed: " + e.getMessage()).show();
        }
    }

    private static void returnToPreviousScreen() {
        if (Main.currentUser == null) {
            MainMenuScreen.show();
        } else if (Main.currentUser.isAdmin()) {
            AdminDashboard.show();
        } else if (Main.currentUser.isArtist()) {
            ArtistDashboard.show();
        } else {
            UserDashboard.show();
        }
    }
}
