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
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ArtistDashboard {
    private static ObservableList<Song> songs = FXCollections.observableArrayList();

    public static void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Button homeBtn = new Button("Browse Songs");
        homeBtn.setOnAction(e -> HomeScreen.show());

        Button mySongsBtn = new Button("My Songs");
        mySongsBtn.setOnAction(e -> loadArtistData());

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> ChangePasswordScreen.show());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(homeBtn, mySongsBtn, spacer, changePasswordBtn, logoutButton);
        mainLayout.setTop(topBar);

        TabPane tabPane = new TabPane();

        Tab songsTab = new Tab("My Songs");
        songsTab.setContent(createSongsTab());
        songsTab.setClosable(false);

        Tab analyticsTab = new Tab("Analytics");
        analyticsTab.setContent(createAnalyticsTab());
        analyticsTab.setClosable(false);

        tabPane.getTabs().addAll(songsTab, analyticsTab);
        mainLayout.setCenter(tabPane);

        loadArtistData();

        Scene scene = new Scene(mainLayout, 800, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Artist Dashboard - " + Main.currentUser.getUsername());
        Main.primaryStage.show();
    }

    private static void loadArtistData() {
        songs.clear();
        try {
            List<Song> loadedSongs = DataStorage.loadArtistSongs(Main.currentUser.getUsername());
            loadedSongs.removeIf(song -> song == null || song.getArtistId() == null);
            songs.addAll(loadedSongs);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load artist data").show();
        }
    }

    private static VBox createSongsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(15));

        TableView<Song> songsTable = new TableView<>();
        songsTable.setItems(songs);

        TableColumn<Song, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(cell -> {
            Song song = cell.getValue();
            return new SimpleStringProperty(song != null ? song.getArtistId() : "");
        });

        TableColumn<Song, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(cell -> {
            Song song = cell.getValue();
            return new SimpleIntegerProperty(song != null ? song.getViews() : 0).asObject();
        });

        songsTable.getColumns().addAll(titleCol, artistCol, viewsCol);

        HBox songActions = new HBox(10);
        Button addSongBtn = new Button("Add New Song");
        addSongBtn.setOnAction(e -> showAddSongDialog());

        Button editSongBtn = new Button("Edit Selected");
        editSongBtn.setOnAction(e -> {
            Song selected = songsTable.getSelectionModel().getSelectedItem();
            if (selected != null) showEditSongDialog(selected);
        });

        Button deleteSongBtn = new Button("Delete Selected");
        deleteSongBtn.setOnAction(e -> {
            Song selected = songsTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteSong(selected);
        });

        songActions.getChildren().addAll(addSongBtn, editSongBtn, deleteSongBtn);
        tabContent.getChildren().addAll(songsTable, songActions);

        return tabContent;
    }

    private static VBox createAnalyticsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(15));

        Label statsLabel = new Label("Your Statistics");
        statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        int totalSongs = songs.size();
        int totalViews = songs.stream()
                .filter(Objects::nonNull)
                .mapToInt(Song::getViews)
                .sum();

        Label statsText = new Label(String.format("Total Songs: %d\nTotal Views: %d",
                totalSongs, totalViews));

        tabContent.getChildren().addAll(statsLabel, statsText);
        return tabContent;
    }

    private static void showAddSongDialog() {
        Dialog<Song> dialog = new Dialog<>();
        dialog.setTitle("Add New Song");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titleField = new TextField();
        TextArea lyricsArea = new TextArea();
        TextField genreField = new TextField();

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lyrics:"), 0, 1);
        grid.add(lyricsArea, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String title = titleField.getText().trim();
                String lyrics = lyricsArea.getText().trim();
                String genre = genreField.getText().trim();

                if (title.isEmpty() || lyrics.isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Title and lyrics are required").show();
                    return null;
                }

                Song newSong = new Song(
                        UUID.randomUUID().toString(),
                        title,
                        lyrics,
                        Main.currentUser.getUsername());
                newSong.setGenre(genre.isEmpty() ? "Unknown" : genre);

                try {
                    DataStorage.saveSong(newSong);
                    return newSong;
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save song").show();
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

        TextField titleField = new TextField(song.getTitle());
        TextArea lyricsArea = new TextArea(song.getLyrics());
        TextField genreField = new TextField(song.getGenre());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lyrics:"), 0, 1);
        grid.add(lyricsArea, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                song.setTitle(titleField.getText().trim());
                song.setLyrics(lyricsArea.getText().trim());
                song.setGenre(genreField.getText().trim());

                try {
                    DataStorage.saveSong(song);
                    return song;
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to update song").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private static void deleteSong(Song song) {
        try {
            DataStorage.deleteSong(song.getId());
            songs.remove(song);
            HomeScreen.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete song").show();
        }
    }
}