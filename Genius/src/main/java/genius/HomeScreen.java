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
    private static ListView<Artist> followedArtistsListView;


    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // ✅ Initialize UI components BEFORE using them
        songListView = new ListView<>();
        albumListView = new ListView<>();
        followedArtistsListView = new ListView<>();

        // ✅ Set up tabPane first
        tabPane = new TabPane();

        // Create placeholder tabs
        Tab songsTab = new Tab("Songs");
        songsTab.setClosable(false);

        Tab albumsTab = new Tab("Albums");
        albumsTab.setClosable(false);

        Tab followedTab = new Tab("Followed Artists");
        followedTab.setClosable(false);

        tabPane.getTabs().addAll(songsTab, albumsTab, followedTab);


        configureSongListView();
        configureAlbumListView();
        configureFollowedArtistListView();

        layout.setTop(createTopBar());
        layout.setCenter(tabPane);
        refreshContent();

        Scene scene = new Scene(layout, 900, 600);
        Main.primaryStage.setScene(scene);
    }



    private static MenuItem createViewArtistMenuItem(ListView<Song> listView) {
        MenuItem item = new MenuItem("View Artist");
        item.setOnAction(e -> {
            Song selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Artist artist = ArtistStorage.getArtistByUsername(selected.getArtistId());
                if (artist != null) {
                    ArtistProfileScreen.show(artist);
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "This artist has no profile.").show();
                }
            }
        });
        return item;
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


        Button viewArtistBtn = new Button("View Artist");
        viewArtistBtn.setOnAction(e -> {
            Song selected = songListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Artist artist = ArtistStorage.getArtistByUsername(selected.getArtistId());
                if (artist != null) {
                    ArtistProfileScreen.show(artist);
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "This artist has no profile.").show();
                }
            }
        });

        VBox container = new VBox(10, songListView, viewArtistBtn);
        container.setPadding(new Insets(10));
        Tab songsTab = tabPane.getTabs().get(0);
        songsTab.setContent(container);
    }
    private static void configureFollowedArtistListView() {
        followedArtistsListView.setCellFactory(lv -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist artist, boolean empty) {
                super.updateItem(artist, empty);
                if (empty || artist == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (Followers: %d)", artist.getUsername(), artist.getFollowerCount()));
                }
            }
        });

        followedArtistsListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Artist selected = followedArtistsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ArtistProfileScreen.show(selected);
                }
            }
        });

        VBox container = new VBox(10, followedArtistsListView);
        container.setPadding(new Insets(10));
        Tab followedTab = tabPane.getTabs().get(2);
        followedTab.setContent(container);


        if (Main.currentUser != null) {
            List<Artist> followed = ArtistStorage.getAllArtists().stream()
                    .filter(a -> a.getFollowers().contains(Main.currentUser.getUsername()))
                    .collect(Collectors.toList());
            followedArtistsListView.setItems(FXCollections.observableArrayList(followed));
        }
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

        Button viewArtistBtn = new Button("View Artist");
        viewArtistBtn.setOnAction(e -> {
            Album selected = albumListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Artist artist = ArtistStorage.getArtistByUsername(selected.getArtistUsername());
                if (artist != null) {
                    ArtistProfileScreen.show(artist);
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "This artist has no profile.").show();
                }
            }
        });

        VBox container = new VBox(10, albumListView, viewArtistBtn);
        container.setPadding(new Insets(10));
        Tab albumsTab = tabPane.getTabs().get(1);
        albumsTab.setContent(container);
    }


    private static HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));


        ImageView logoView = new ImageView(new Image("file:genius_logo.png")); // Ensure the path is correct
        logoView.setFitHeight(40); // Set desired height
        logoView.setPreserveRatio(true);


        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToPreviousScreen());


        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshContent());


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

            List<Song> allSongs = SongStorage.getAllSongs();
            List<Album> allAlbums = AlbumStorage.getAllAlbums();


            songListView.setItems(FXCollections.observableArrayList(allSongs));
            albumListView.setItems(FXCollections.observableArrayList(allAlbums));


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