package genius;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import java.util.*;

public class HomeScreen {
    private static ListView<Song> songList; // Declare as static field

    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Top bar with back button
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToPreviousScreen());

        Label titleLabel = new Label("Browse Songs");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, spacer, titleLabel);
        layout.setTop(topBar);

        // Song list
        songList = new ListView<>(); // Initialize the songList
        refreshSongList();

        // Set double-click handler
        songList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selected = songList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SongViewScreen.show(selected);
                }
            }
        });

        layout.setCenter(songList);

        Scene scene = new Scene(layout, 800, 600);
        Main.primaryStage.setScene(scene);
    }

    public static void refreshSongList() {
        List<Song> songs = new ArrayList<>();

        try {
            // Load from both storage systems
            songs.addAll(SongStorage.getAllSongs());
            songs.addAll(DataStorage.loadAllSongs());

            // Remove duplicates by ID
            Set<String> seenIds = new HashSet<>();
            List<Song> uniqueSongs = new ArrayList<>();
            for (Song song : songs) {
                if (seenIds.add(song.getId())) {
                    uniqueSongs.add(song);
                }
            }

            if (songList != null) {
                songList.setItems(FXCollections.observableArrayList(uniqueSongs));
            }
        } catch (Exception e) {
            System.err.println("Error loading songs: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Could not load songs: " + e.getMessage()).show();
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