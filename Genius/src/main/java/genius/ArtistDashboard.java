package genius;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ArtistDashboard {
    private static ObservableList<Song> songs = FXCollections.observableArrayList();
    private static ObservableList<Album> albums = FXCollections.observableArrayList();
    private static TableView<Song> songsTable;
    private static TableView<Album> albumsTable;

    public static void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Top Bar
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);
// Bio Section
        VBox bioBox = new VBox(10);
        bioBox.setPadding(new Insets(10));

        Artist artist = ArtistStorage.getArtistByUsername(Main.currentUser.getUsername());
        Label bioLabel = new Label("Bio: " + (artist != null ? artist.getBio() : ""));
        TextField bioField = new TextField();
        bioField.setPromptText("Update your artist bio");

        Button updateBioBtn = new Button("Update Bio");
        updateBioBtn.setOnAction(e -> {
            if (artist != null) {
                artist.setBio(bioField.getText().trim());
                ArtistStorage.saveArtist(artist);
                bioLabel.setText("Bio: " + artist.getBio());
            }
        });

        bioBox.getChildren().addAll(bioLabel, bioField, updateBioBtn);
        mainLayout.setTop(new VBox(topBar, bioBox));

        // Main Content
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createSongsTab(),
                createAlbumsTab(),
                createAnalyticsTab()
        );
        mainLayout.setCenter(tabPane);

        loadArtistData();

        Scene scene = new Scene(mainLayout, 900, 650);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Artist Dashboard - " + Main.currentUser.getUsername());
    }

    private static HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Button homeBtn = new Button("Home");
        homeBtn.setOnAction(e -> HomeScreen.show());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadArtistData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> ChangePasswordScreen.show());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });

        topBar.getChildren().addAll(homeBtn, refreshBtn, spacer, changePasswordBtn, logoutBtn);
        return topBar;
    }

    private static void loadArtistData() {
        songs.clear();
        albums.clear();

        try {
            // Load songs
            List<Song> loadedSongs = DataStorage.loadArtistSongs(Main.currentUser.getUsername());
            songs.addAll(loadedSongs);

            // Load albums
            List<Album> loadedAlbums = AlbumStorage.getAlbumsByArtist(Main.currentUser.getUsername());
            albums.addAll(loadedAlbums);

            // Refresh tables
            if (songsTable != null) songsTable.refresh();
            if (albumsTable != null) albumsTable.refresh();
        } catch (Exception e) {
            showAlert("Error", "Could not load artist data: " + e.getMessage());
        }
    }

    private static Tab createSongsTab() {
        Tab tab = new Tab("My Songs");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Songs Table
        songsTable = new TableView<>();
        setupSongsTable();

        // Song Actions
        HBox actions = new HBox(10);
        Button addBtn = new Button("Add Song");
        addBtn.setOnAction(e -> showAddSongDialog());

        Button editBtn = new Button("Edit Selected");
        editBtn.setOnAction(e -> {
            Song selected = songsTable.getSelectionModel().getSelectedItem();
            if (selected != null) showEditSongDialog(selected);
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setOnAction(e -> {
            Song selected = songsTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteSong(selected);
        });

        actions.getChildren().addAll(addBtn, editBtn, deleteBtn);
        content.getChildren().addAll(songsTable, actions);
        tab.setContent(content);

        return tab;
    }

    private static void setupSongsTable() {
        songsTable.setItems(songs);
        songsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Song, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArtistId()));

        TableColumn<Song, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getViews()).asObject());

        TableColumn<Song, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(cell -> {
            String albumId = cell.getValue().getAlbumId();
            Album album = AlbumStorage.getAlbumById(albumId);
            return new SimpleStringProperty(album != null ? album.getTitle() : "Single");
        });

        songsTable.getColumns().setAll(titleCol, artistCol, viewsCol, albumCol);
    }

    private static Tab createAlbumsTab() {
        Tab tab = new Tab("My Albums");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Albums Table
        albumsTable = new TableView<>();
        setupAlbumsTable();

        // Album Actions
        HBox actions = new HBox(10);
        Button addBtn = new Button("Add Album");
        addBtn.setOnAction(e -> showAddAlbumDialog());


        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setOnAction(e -> {
            Album selected = albumsTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteAlbum(selected);
        });

        actions.getChildren().addAll(addBtn, deleteBtn);
        content.getChildren().addAll(albumsTable, actions);
        tab.setContent(content);

        return tab;
    }

    private static void setupAlbumsTable() {
        albumsTable.setItems(albums);
        albumsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Album, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Album, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArtistUsername()));

        TableColumn<Album, Integer> tracksCol = new TableColumn<>("Tracks");
        tracksCol.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getSongIds().size()).asObject());

        TableColumn<Album, String> dateCol = new TableColumn<>("Release Date");
        dateCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getReleaseDate().toString()));

        albumsTable.getColumns().setAll(titleCol, artistCol, tracksCol, dateCol);
    }

    private static Tab createAnalyticsTab() {
        Tab tab = new Tab("Analytics");
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label statsLabel = new Label("Your Statistics");
        statsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Calculate statistics
        int totalSongs = songs.size();
        int totalAlbums = albums.size();
        int totalViews = songs.stream().mapToInt(Song::getViews).sum();
        int avgViews = totalSongs > 0 ? totalViews / totalSongs : 0;

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.addRow(0, new Label("Total Songs:"), new Label(String.valueOf(totalSongs)));
        statsGrid.addRow(1, new Label("Total Albums:"), new Label(String.valueOf(totalAlbums)));
        statsGrid.addRow(2, new Label("Total Views:"), new Label(String.valueOf(totalViews)));
        statsGrid.addRow(3, new Label("Average Views:"), new Label(String.valueOf(avgViews)));

        content.getChildren().addAll(statsLabel, statsGrid);
        tab.setContent(content);

        return tab;
    }

    private static void showAddSongDialog() {
        Dialog<Song> dialog = new Dialog<>();
        dialog.setTitle("Add New Song");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Song Title");

        TextArea lyricsArea = new TextArea();
        lyricsArea.setPromptText("Lyrics");
        lyricsArea.setPrefRowCount(5);

        ComboBox<Album> albumCombo = new ComboBox<>();
        albumCombo.setItems(albums);
        albumCombo.setPromptText("Select Album");
        albumCombo.setConverter(new StringConverter<Album>() {
            @Override
            public String toString(Album album) {
                return album != null ? album.getTitle() : "";
            }

            @Override
            public Album fromString(String string) {
                return null;
            }
        });

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lyrics:"), 0, 1);
        grid.add(lyricsArea, 1, 1);
        grid.add(new Label("Album:"), 0, 2);
        grid.add(albumCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String title = titleField.getText().trim();
                String lyrics = lyricsArea.getText().trim();
                Album album = albumCombo.getValue();

                if (title.isEmpty() || lyrics.isEmpty()) {
                    showAlert("Error", "Title and lyrics are required");
                    return null;
                }

                Song song = new Song(UUID.randomUUID().toString(), title, lyrics, Main.currentUser.getUsername());
                if (album != null) {
                    song.setAlbumId(album.getId());
                    album.addSong(song.getId());
                    AlbumStorage.saveAlbum(album);
                }

                try {
                    DataStorage.saveSong(song);
                    return song;
                } catch (IOException e) {
                    showAlert("Error", "Failed to save song: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(song -> {
            songs.add(song);
            HomeScreen.show();
        });
    }

    private static void showEditSongDialog(Song song) {
        Dialog<Song> dialog = new Dialog<>();
        dialog.setTitle("Edit Song");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(song.getTitle());
        TextArea lyricsArea = new TextArea(song.getLyrics());
        lyricsArea.setPrefRowCount(5);

        ComboBox<Album> albumCombo = new ComboBox<>();
        albumCombo.setItems(albums);
        albumCombo.setConverter(new StringConverter<Album>() {
            @Override
            public String toString(Album album) {
                return album != null ? album.getTitle() : "";
            }

            @Override
            public Album fromString(String string) {
                return null;
            }
        });


        Album currentAlbum = AlbumStorage.getAlbumById(song.getAlbumId());
        if (currentAlbum != null) {
            albumCombo.getSelectionModel().select(currentAlbum);
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lyrics:"), 0, 1);
        grid.add(lyricsArea, 1, 1);
        grid.add(new Label("Album:"), 0, 2);
        grid.add(albumCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                song.setTitle(titleField.getText().trim());
                song.setLyrics(lyricsArea.getText().trim());

                Album selectedAlbum = albumCombo.getValue();
                song.setAlbumId(selectedAlbum != null ? selectedAlbum.getId() : Song.SINGLE_ALBUM_ID);

                try {
                    DataStorage.saveSong(song);
                    return song;
                } catch (IOException e) {
                    showAlert("Error", "Failed to save song: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedSong -> {
            songsTable.refresh();
            HomeScreen.show();
        });
    }

    private static void deleteSong(Song song) {
        try {
            DataStorage.deleteSong(song.getId());
            songs.remove(song);

            // Remove from album if exists
            if (!song.getAlbumId().equals(Song.SINGLE_ALBUM_ID)) {
                Album album = AlbumStorage.getAlbumById(song.getAlbumId());
                if (album != null) {
                    album.removeSong(song.getId());
                    AlbumStorage.saveAlbum(album);
                }
            }

            HomeScreen.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to delete song: " + e.getMessage());
        }
    }

    private static void showAddAlbumDialog() {
        Dialog<Album> dialog = new Dialog<>();
        dialog.setTitle("Add New Album");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Album Title");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Release Date:"), 0, 1);
        grid.add(datePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String title = titleField.getText().trim();
                LocalDate releaseDate = datePicker.getValue();

                if (title.isEmpty()) {
                    showAlert("Error", "Title is required");
                    return null;
                }

                Album album = new Album(
                        UUID.randomUUID().toString(),
                        title,
                        Main.currentUser.getUsername(),
                        releaseDate
                );

                AlbumStorage.saveAlbum(album);
                return album;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(album -> {
            albums.add(album);
            showAlert("Success", "Album created successfully!");
        });
    }

    private static void deleteAlbum(Album album) {
        try {
            for (String songId : album.getSongIds()) {
                Song song = SongStorage.getSong(songId);
                if (song != null) {
                    song.setAlbumId(Song.SINGLE_ALBUM_ID);
                    DataStorage.saveSong(song);
                }
            }

            AlbumStorage.deleteAlbum(album.getId());
            albums.remove(album);
            showAlert("Success", "Album deleted successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to delete album: " + e.getMessage());
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}