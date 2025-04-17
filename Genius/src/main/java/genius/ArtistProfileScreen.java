package genius;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

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

        Label bioLabel = new Label("Bio: " + artist.getBio());
        Label followersLabel = new Label("Followers: " + artist.getFollowerCount());

        ListView<Object> contentList = new ListView<>();
        List<Object> content = FXCollections.observableArrayList();

        content.addAll(AlbumStorage.getAlbumsByArtist(artist.getUsername()));
        for (Song song : SongStorage.getAllSongs()) {
            if (song.getArtistId().equals(artist.getUsername())) {
                content.add(song);
            }
        }

        contentList.setItems(FXCollections.observableArrayList(content));
        contentList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Object selected = contentList.getSelectionModel().getSelectedItem();
                if (selected instanceof Song) SongViewScreen.show((Song) selected);
                if (selected instanceof Album) AlbumViewScreen.show((Album) selected);
            }
        });

        contentBox.getChildren().addAll(bioLabel, followersLabel, new Label("Albums and Songs:"), contentList);
        layout.setCenter(contentBox);

        Scene scene = new Scene(layout, 800, 600);
        Main.primaryStage.setScene(scene);
    }
}