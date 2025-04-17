package genius;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.*;
import java.util.stream.Collectors;

public class HomeScreen {
    private static TabPane tabPane;
    private static ListView<Song> songListView;
    private static ListView<Album> albumListView;

    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Initialize list views
        songListView = new ListView<>();
        albumListView = new ListView<>();

        // Create tabs
        Tab songsTab = new Tab("Songs", songListView);
        songsTab.setClosable(false);

        Tab albumsTab = new Tab("Albums", albumListView);
        albumsTab.setClosable(false);


        tabPane = new TabPane(songsTab, albumsTab);


        configureSongListView();
        configureAlbumListView();

        HBox topBar = createTopBar();
        layout.setTop(topBar);

        layout.setCenter(tabPane);
        refreshContent(); // Initial load

        Scene scene = new Scene(layout, 900, 600);
        Main.primaryStage.setScene(scene);
    }

    private static void configureSongListView() {
        songListView.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s (%d views)",
                            song.getTitle(),
                            song.getArtistId() != null ? song.getArtistId() : "Unknown",
                            song.getViews()));
                }
            }
        });
        songListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selected = songListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SongViewScreen.show(selected);
                }
            }
        });
    }

    private static void configureAlbumListView() {
        albumListView.setCellFactory(lv -> new ListCell<Album>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                if (empty || album == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s (%d tracks)",
                            album.getTitle(),
                            album.getArtistUsername() != null ? album.getArtistUsername() : "Unknown",
                            album.getSongIds().size()));
                }
            }
        });
        albumListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Album selected = albumListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    AlbumViewScreen.show(selected);
                }
            }
        });
    }

    private static HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        // Logo
        ImageView logoView = new ImageView(new Image("file:genius_logo.png")); // Ensure the path is correct
        logoView.setFitHeight(40); // Set desired height
        logoView.setPreserveRatio(true);

        // Back button
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToPreviousScreen());

        // Refresh button
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshContent());

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search for artist, song, or album");

        // Search button
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchEntities(searchField.getText()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add logo and other components to top bar
        topBar.getChildren().addAll(logoView, backBtn, refreshBtn, spacer, searchField, searchBtn);
        return topBar;
    }


    public static void refreshContent() {
        try {
            // Reload all songs and albums from storage
            List<Song> allSongs = SongStorage.getAllSongs();
            List<Album> allAlbums = AlbumStorage.getAllAlbums();

            // Update list views
            songListView.setItems(FXCollections.observableArrayList(allSongs));
            albumListView.setItems(FXCollections.observableArrayList(allAlbums));

            // Sort by popularity (views for songs, track count for albums)
            songListView.getItems().sort(Comparator.comparingInt(Song::getViews).reversed());

        } catch (Exception e) {
            System.err.println("Error refreshing content: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Failed to refresh content").show();
        }
    }

    public static void searchEntities(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshContent();
            return;
        }

        String searchQuery = query.toLowerCase();
        List<Song> songResults = new ArrayList<>();
        List<Album> albumResults = new ArrayList<>();

        // Search songs
        for (Song song : SongStorage.getAllSongs()) {
            try {
                boolean matches = (song.getTitle() != null && song.getTitle().toLowerCase().contains(searchQuery)) ||
                        (song.getArtistId() != null && song.getArtistId().toLowerCase().contains(searchQuery)) ||
                        (song.getLyrics() != null && song.getLyrics().toLowerCase().contains(searchQuery));

                if (matches) songResults.add(song);
            } catch (Exception e) {
                System.err.println("Error processing song: " + e.getMessage());
            }
        }

        // Search albums
        for (Album album : AlbumStorage.getAllAlbums()) {
            try {
                boolean matches = (album.getTitle() != null && album.getTitle().toLowerCase().contains(searchQuery)) ||
                        (album.getArtistUsername() != null && album.getArtistUsername().toLowerCase().contains(searchQuery));

                if (matches) albumResults.add(album);
            } catch (Exception e) {
                System.err.println("Error processing album: " + e.getMessage());
            }
        }

        // Update UI
        songListView.setItems(FXCollections.observableArrayList(songResults));
        albumListView.setItems(FXCollections.observableArrayList(albumResults));

        // Select tab with results
        if (!songResults.isEmpty()) {
            tabPane.getSelectionModel().select(0);
        } else if (!albumResults.isEmpty()) {
            tabPane.getSelectionModel().select(1);
        } else {
            new Alert(Alert.AlertType.INFORMATION, "No results found for: " + query).show();
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