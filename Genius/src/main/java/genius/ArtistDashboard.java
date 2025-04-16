package genius;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.util.*;

public class ArtistDashboard {
    private static ObservableList<Song> songs = FXCollections.observableArrayList();
    private static Map<String, Integer> songViews = new HashMap<>();

    public static void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Top Navigation Bar
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

        // Center Content (Tabbed Interface)
        TabPane tabPane = new TabPane();

        // Songs Tab
        Tab songsTab = new Tab("My Songs");
        songsTab.setContent(createSongsTab());
        songsTab.setClosable(false);

        // Analytics Tab
        Tab analyticsTab = new Tab("Analytics");
        analyticsTab.setContent(createAnalyticsTab());
        analyticsTab.setClosable(false);

        tabPane.getTabs().addAll(songsTab, analyticsTab);
        mainLayout.setCenter(tabPane);

        // Load artist data
        loadArtistData();

        Scene scene = new Scene(mainLayout, 800, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Artist Dashboard - " + Main.currentUser.getUsername());
        Main.primaryStage.show();
    }

    private static void loadArtistData() {
        songs.clear();
        songViews.clear();

        try {
            List<Song> loadedSongs = DataStorage.loadArtistSongs(Main.currentUser.getUsername());
            songs.addAll(loadedSongs);

            // Initialize views
            for (Song song : songs) {
                songViews.put(song.getId(), loadSongViews(song.getId()));
            }
        } catch (Exception e) {
            System.err.println("Error loading artist data: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Could not load artist data: " + e.getMessage()).show();
        }
    }

    private static int loadSongViews(String songId) {
        return 0; // Default to 0 views
    }

    private static String getQuickStats() {
        int totalSongs = songs.size();
        int totalViews = songViews.values().stream().mapToInt(Integer::intValue).sum();

        return String.format("Songs: %d | Total Views: %,d", totalSongs, totalViews);
    }

    private static VBox createSongsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(15));

        // Songs Table
        TableView<Song> songsTable = new TableView<>();
        songsTable.setItems(songs);

        TableColumn<Song, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Song, String> lyricsCol = new TableColumn<>("Preview");
        lyricsCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getLyrics().length() > 50 ?
                        param.getValue().getLyrics().substring(0, 50) + "..." :
                        param.getValue().getLyrics()));

        TableColumn<Song, Integer> lengthCol = new TableColumn<>("Length (sec)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        songsTable.getColumns().addAll(titleCol, lyricsCol, lengthCol);
        songsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Song Actions
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

        // Real statistics
        Label statsLabel = new Label("Your Statistics");
        statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Calculate real stats
        int totalSongs = songs.size();
        int totalViews = songViews.values().stream().mapToInt(Integer::intValue).sum();
        int totalLikes = songs.stream().mapToInt(Song::getLikeCount).sum();

        Label statsText = new Label(
                String.format("Total Songs: %d\nTotal Views: %d\nTotal Likes: %d",
                        totalSongs, totalViews, totalLikes)
        );

        // Most popular song
        Optional<Map.Entry<String, Integer>> mostPopular = songViews.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (mostPopular.isPresent()) {
            String songId = mostPopular.get().getKey();
            String songTitle = songs.stream()
                    .filter(s -> s.getId().equals(songId))
                    .findFirst()
                    .map(Song::getTitle)
                    .orElse("Unknown");
            statsText.setText(statsText.getText() +
                    String.format("\n\nMost Popular Song: %s (%d views)",
                            songTitle, mostPopular.get().getValue()));
        }

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
        TextField lengthField = new TextField();

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lyrics:"), 0, 1);
        grid.add(lyricsArea, 1, 1);
        grid.add(new Label("Duration (seconds):"), 0, 2);
        grid.add(lengthField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Song newSong = new Song(
                            UUID.randomUUID().toString(),
                            titleField.getText(),
                            lyricsArea.getText(),
                            Integer.parseInt(lengthField.getText()),
                            Main.currentUser.getUsername()
                    );

                    DataStorage.saveSong(newSong);
                    SongStorage.saveSong(newSong);
                    return newSong;
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Please enter a valid number for duration").show();
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save song: " + e.getMessage()).show();
                }
            }
            return null;
        });

        Optional<Song> result = dialog.showAndWait();
        result.ifPresent(song -> {
            songs.add(song);
            songViews.put(song.getId(), 0);
            HomeScreen.refreshSongList();
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
        TextField lengthField = new TextField(String.valueOf(song.getDuration()));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lyrics:"), 0, 1);
        grid.add(lyricsArea, 1, 1);
        grid.add(new Label("Duration (seconds):"), 0, 2);
        grid.add(lengthField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    song.setTitle(titleField.getText());
                    song.setLyrics(lyricsArea.getText());
                    song.setDuration(Integer.parseInt(lengthField.getText()));

                    DataStorage.saveSong(song);
                    SongStorage.saveSong(song);
                    return song;
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Please enter a valid number for duration").show();
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to update song: " + e.getMessage()).show();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private static void deleteSong(Song song) {
        try {
            DataStorage.deleteSong(song.getId());
            SongStorage.deleteSong(song.getId());
            songs.remove(song);
            songViews.remove(song.getId());
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete song: " + e.getMessage()).show();
        }
    }
}