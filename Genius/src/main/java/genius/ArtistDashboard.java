package genius;


import javafx.beans.property.SimpleObjectProperty;
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
import javafx.util.Pair;

import java.util.*;

public class ArtistDashboard {
    private static ObservableList<Song> songs = FXCollections.observableArrayList();
    private static Map<String, List<Annotation>> songAnnotations = new HashMap<>();
    private static Map<String, Integer> songViews = new HashMap<>();

    public static void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Header
        VBox header = new VBox(10);
        Label welcomeLabel = new Label("Welcome Artist " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

        Label statsLabel = new Label(getQuickStats());
        statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #636e72;");

        header.getChildren().addAll(welcomeLabel, statsLabel);
        mainLayout.setTop(header);

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

        // Annotations Tab
        Tab annotationsTab = new Tab("Lyric Annotations");
        annotationsTab.setContent(createAnnotationsTab());
        annotationsTab.setClosable(false);

        tabPane.getTabs().addAll(songsTab, analyticsTab, annotationsTab);
        mainLayout.setCenter(tabPane);

        // Footer
        HBox footer = new HBox(10);
        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> ChangePasswordScreen.show());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });

        footer.getChildren().addAll(changePasswordBtn, logoutBtn);
        mainLayout.setBottom(footer);

        // Load artist data
        loadArtistData();

        Scene scene = new Scene(mainLayout, 800, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Artist Dashboard - " + Main.currentUser.getUsername());
        Main.primaryStage.show();
    }

    private static void loadArtistData() {
        // In a real app, this would load from database
        songs.addAll(
                new Song("1", "Moonlight Sonata", "The classic piano piece...", 150),
                new Song("2", "Summer Vibes", "Feel the summer heat...", 320)
        );

        songViews.put("1", 1250);
        songViews.put("2", 875);

        // Sample annotations
        List<Annotation> annotations1 = new ArrayList<>();
        annotations1.add(new Annotation("1", 5, 10, "This part inspired by Beethoven's 5th"));
        annotations1.add(new Annotation("1", 15, 20, "Reference to Shakespeare's sonnets"));

        List<Annotation> annotations2 = new ArrayList<>();
        annotations2.add(new Annotation("2", 2, 8, "Summer of '69 reference"));

        songAnnotations.put("1", annotations1);
        songAnnotations.put("2", annotations2);
    }

    private static String getQuickStats() {
        int totalSongs = songs.size();
        int totalViews = songViews.values().stream().mapToInt(Integer::intValue).sum();
        int totalAnnotations = songAnnotations.values().stream().mapToInt(List::size).sum();

        return String.format("Songs: %d | Total Views: %d | Annotations: %d",
                totalSongs, totalViews, totalAnnotations);
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
        lyricsCol.setCellValueFactory(new PropertyValueFactory<>("lyricsPreview"));

        TableColumn<Song, Integer> lengthCol = new TableColumn<>("Length (sec)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("lengthSeconds"));

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
            if (selected != null) {
                songs.remove(selected);
                songViews.remove(selected.getId());
                songAnnotations.remove(selected.getId());
            }
        });

        songActions.getChildren().addAll(addSongBtn, editSongBtn, deleteSongBtn);
        tabContent.getChildren().addAll(songsTable, songActions);

        return tabContent;
    }

    private static VBox createAnalyticsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(15));

        // Most Popular Songs
        Label popularLabel = new Label("Most Popular Songs");
        popularLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> popularSongsList = new ListView<>();
        songViews.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String songTitle = songs.stream()
                            .filter(s -> s.getId().equals(entry.getKey()))
                            .findFirst()
                            .map(Song::getTitle)
                            .orElse("Unknown Song");
                    popularSongsList.getItems().add(String.format("%s - %,d views", songTitle, entry.getValue()));
                });

        // Recent Activity
        Label activityLabel = new Label("Recent Activity");
        activityLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextFlow activityFlow = new TextFlow();
        activityFlow.getChildren().addAll(
                new Text("Today: 42 new views\n"),
                new Text("Yesterday: 128 new views\n"),
                new Text("This week: 540 new views")
        );

        tabContent.getChildren().addAll(popularLabel, popularSongsList, activityLabel, activityFlow);
        return tabContent;
    }

    private static VBox createAnnotationsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(15));

        // Annotations Table
        TableView<Annotation> annotationsTable = new TableView<>();
        ObservableList<Annotation> allAnnotations = FXCollections.observableArrayList();
        songAnnotations.values().forEach(allAnnotations::addAll);
        annotationsTable.setItems(allAnnotations);

        TableColumn<Annotation, String> songCol = new TableColumn<>("Song");
        songCol.setCellValueFactory(param -> {
            String songId = param.getValue().getSongId();
            String title = songs.stream()
                    .filter(s -> s.getId().equals(songId))
                    .findFirst()
                    .map(Song::getTitle)
                    .orElse("Unknown");
            return new SimpleStringProperty(title);
        });

        TableColumn<Annotation, String> textCol = new TableColumn<>("Annotation");
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));

        TableColumn<Annotation, Integer> startCol = new TableColumn<>("Start Line");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startLine"));

        TableColumn<Annotation, Integer> endCol = new TableColumn<>("End Line");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endLine"));

        annotationsTable.getColumns().addAll(songCol, textCol, startCol, endCol);
        annotationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Annotation Actions
        HBox annotationActions = new HBox(10);
        Button addAnnotationBtn = new Button("Add Annotation");
        addAnnotationBtn.setOnAction(e -> showAddAnnotationDialog());

        Button editAnnotationBtn = new Button("Edit Selected");
        editAnnotationBtn.setDisable(true); // Implementation omitted for brevity

        annotationActions.getChildren().addAll(addAnnotationBtn, editAnnotationBtn);
        tabContent.getChildren().addAll(annotationsTable, annotationActions);

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
        grid.add(new Label("Length (seconds):"), 0, 2);
        grid.add(lengthField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return new Song(
                            UUID.randomUUID().toString(),
                            titleField.getText(),
                            lyricsArea.getText(),
                            Integer.parseInt(lengthField.getText())
                    );
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Please enter a valid number for length").show();
                    return null;
                }
            }
            return null;
        });

        Optional<Song> result = dialog.showAndWait();
        result.ifPresent(song -> {
            songs.add(song);
            songViews.put(song.getId(), 0);
            songAnnotations.put(song.getId(), new ArrayList<>());
        });
    }

    private static void showEditSongDialog(Song song) {
        // Similar to add dialog but with existing values
        // Implementation omitted for brevity
    }

    private static void showAddAnnotationDialog() {
        Dialog<Annotation> dialog = new Dialog<>();
        dialog.setTitle("Add Annotation");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Song> songCombo = new ComboBox<>(songs);
        songCombo.setPromptText("Select song");

        TextField startLineField = new TextField();
        TextField endLineField = new TextField();
        TextArea annotationArea = new TextArea();

        grid.add(new Label("Song:"), 0, 0);
        grid.add(songCombo, 1, 0);
        grid.add(new Label("Start Line:"), 0, 1);
        grid.add(startLineField, 1, 1);
        grid.add(new Label("End Line:"), 0, 2);
        grid.add(endLineField, 1, 2);
        grid.add(new Label("Annotation:"), 0, 3);
        grid.add(annotationArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && songCombo.getValue() != null) {
                try {
                    return new Annotation(
                            songCombo.getValue().getId(),
                            Integer.parseInt(startLineField.getText()),
                            Integer.parseInt(endLineField.getText()),
                            annotationArea.getText()
                    );
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Please enter valid line numbers").show();
                    return null;
                }
            }
            return null;
        });

        Optional<Annotation> result = dialog.showAndWait();
        result.ifPresent(annotation -> {
            songAnnotations.get(annotation.getSongId()).add(annotation);
        });
    }

    // Helper classes
    public static class Song {
        private String id;
        private String title;
        private String lyrics;
        private int lengthSeconds;

        public Song(String id, String title, String lyrics, int lengthSeconds) {
            this.id = id;
            this.title = title;
            this.lyrics = lyrics;
            this.lengthSeconds = lengthSeconds;
        }

        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getLyrics() { return lyrics; }
        public String getLyricsPreview() {
            return lyrics.length() > 50 ? lyrics.substring(0, 50) + "..." : lyrics;
        }
        public int getLengthSeconds() { return lengthSeconds; }
    }

    public static class Annotation {
        private String songId;
        private int startLine;
        private int endLine;
        private String text;

        public Annotation(String songId, int startLine, int endLine, String text) {
            this.songId = songId;
            this.startLine = startLine;
            this.endLine = endLine;
            this.text = text;
        }

        // Getters
        public String getSongId() { return songId; }
        public int getStartLine() { return startLine; }
        public int getEndLine() { return endLine; }
        public String getText() { return text; }
    }
}