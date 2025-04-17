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
    private static HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToPreviousScreen());

        TextField searchField = new TextField();
        searchField.setPromptText("Search for artist, song, or album");
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchEntities(searchField.getText()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button authBtn = new Button();
        if (Main.currentUser == null) {
            authBtn.setText("Login");
            authBtn.setOnAction(e -> LoginScreen.show());
        } else {
            authBtn.setText("Logout");
            authBtn.setOnAction(e -> {
                Main.currentUser = null;
                MainMenuScreen.show();
            });
        }

        topBar.getChildren().addAll(backBtn, spacer, searchField, searchBtn, authBtn);
        return topBar;
    }

    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        HBox topBar = createTopBar();
        layout.setTop(topBar);
        TabPane tabPane = new TabPane();
        
        Tab songsTab = new Tab("Songs");
        songsTab.setContent(createSongsList());
        songsTab.setClosable(false);
        
        Tab albumsTab = new Tab("Albums");
        albumsTab.setContent(createAlbumsList());
        albumsTab.setClosable(false);

        tabPane.getTabs().addAll(songsTab, albumsTab);
        layout.setCenter(tabPane);

        Scene scene = new Scene(layout, 900, 600);
        Main.primaryStage.setScene(scene);
    }

    private static ListView<Song> createSongsList() {
        ListView<Song> songList = new ListView<>();
        songList.setItems(FXCollections.observableArrayList(SongStorage.getAllSongs()));
        songList.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    setText(song.getTitle() + " - " + song.getArtistId());
                }
            }
        });
        songList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selected = songList.getSelectionModel().getSelectedItem();
                if (selected != null) SongViewScreen.show(selected);
            }
        });
        return songList;
    }

    private static ListView<Album> createAlbumsList() {
        ListView<Album> albumList = new ListView<>();
        albumList.setItems(FXCollections.observableArrayList(AlbumStorage.getAllAlbums()));
        albumList.setCellFactory(lv -> new ListCell<Album>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                if (empty || album == null) {
                    setText(null);
                } else {
                    setText(album.getTitle() + " - " + album.getArtistUsername());
                }
            }
        });
        albumList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Album selected = albumList.getSelectionModel().getSelectedItem();
                if (selected != null) AlbumViewScreen.show(selected);
            }
        });
        return albumList;
    }

    public static void refreshTopSongs() {
        try {

            List<Song> allSongs = new ArrayList<>();
            allSongs.addAll(SongStorage.getAllSongs());
            allSongs.addAll(DataStorage.loadAllSongs());

            Map<String, Integer> songViewMap = new HashMap<>();
            for (Song song : allSongs) {
                int views = DataStorage.loadSongViews(song.getId());
                songViewMap.put(song.getId(), views);
            }

            List<Album> allAlbums = AlbumStorage.getAllAlbums();
            Map<String, Integer> albumPopularityMap = new HashMap<>();

            for (Album album : allAlbums) {
                int totalViews = album.getSongIds().stream()
                        .mapToInt(songId -> songViewMap.getOrDefault(songId, 0))
                        .sum();
                albumPopularityMap.put(album.getId(), totalViews);
            }


            allSongs.sort(Comparator.comparingInt(s -> -songViewMap.getOrDefault(s.getId(), 0)));
            allAlbums.sort(Comparator.comparingInt(a -> -albumPopularityMap.getOrDefault(a.getId(), 0)));

            List<Object> combined = new ArrayList<>();
            int maxItems = Math.max(allSongs.size(), allAlbums.size());

            for (int i = 0; i < maxItems; i++) {
                if (i < allSongs.size()) {
                    combined.add(allSongs.get(i));
                }
                if (i < allAlbums.size()) {
                    combined.add(allAlbums.get(i));
                }
            }

            displayList.setItems(FXCollections.observableArrayList(combined));
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load content: " + e.getMessage()).show();
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
    public static void refreshContent() {
        try {
            // Get all songs with view counts
            List<Song> allSongs = SongStorage.getAllSongs();
            Map<String, Integer> songViews = new HashMap<>();
            for (Song song : allSongs) {
                songViews.put(song.getId(), DataStorage.loadSongViews(song.getId()));
            }

            // Get all albums with popularity scores (sum of song views)
            List<Album> allAlbums = AlbumStorage.getAllAlbums();
            Map<String, Integer> albumPopularity = new HashMap<>();
            for (Album album : allAlbums) {
                int popularity = album.getSongIds().stream()
                        .mapToInt(songId -> songViews.getOrDefault(songId, 0))
                        .sum();
                albumPopularity.put(album.getId(), popularity);
            }

            // Sort songs by views (descending)
            List<Song> sortedSongs = new ArrayList<>(allSongs);
            sortedSongs.sort((s1, s2) ->
                    Integer.compare(songViews.getOrDefault(s2.getId(), 0),
                            songViews.getOrDefault(s1.getId(), 0)));

            // Sort albums by popularity (descending)
            List<Album> sortedAlbums = new ArrayList<>(allAlbums);
            sortedAlbums.sort((a1, a2) ->
                    Integer.compare(albumPopularity.getOrDefault(a2.getId(), 0),
                            albumPopularity.getOrDefault(a1.getId(), 0)));

            // Combine and display
            List<Object> content = new ArrayList<>();

            // Add popular albums section
            if (!sortedAlbums.isEmpty()) {
                content.add(new Label("Popular Albums:"));
                content.addAll(sortedAlbums.stream().limit(5).collect(Collectors.toList()));
                content.add(new Label(""));
            }

            // Add popular songs section
            if (!sortedSongs.isEmpty()) {
                content.add(new Label("Popular Songs:"));
                content.addAll(sortedSongs.stream().limit(10).collect(Collectors.toList()));
            }

            displayList.setItems(FXCollections.observableArrayList(content));
            displayList.setCellFactory(lv -> new ListCell<Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else if (item instanceof Label) {
                        setGraphic((Label) item);
                        setText(null);
                    } else if (item instanceof Album) {
                        Album album = (Album) item;
                        setText(album.getTitle() + " - " + album.getArtistUsername());
                    } else if (item instanceof Song) {
                        Song song = (Song) item;
                        setText(song.getTitle() + " - " + song.getArtistId());
                    }
                }
            });
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error loading content: " + e.getMessage()).show();
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
