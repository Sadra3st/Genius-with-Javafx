package genius;

import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class UserProfileScreen {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public static void show(User user) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        // Header
        HBox header = createHeader(user);
        layout.getChildren().add(header);

        // User Info Section
        VBox infoBox = createUserInfoBox(user);
        layout.getChildren().add(infoBox);

        // Content Section
        TabPane contentTabs = createContentTabs(user);
        layout.getChildren().add(contentTabs);

        // Stats Section
        VBox statsBox = createStatsBox(user);
        layout.getChildren().add(statsBox);

        // Comments Section (if any)
        if (DataStorage.countUserComments(user.getUsername()) > 0) {
            VBox commentsBox = createCommentsBox(user);
            layout.getChildren().add(commentsBox);
        }

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 800, 600);
        Main.primaryStage.setScene(scene);
    }

    private static HBox createHeader(User user) {
        HBox header = new HBox(10);
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> returnToDashboard(user));

        Label titleLabel = new Label(user.getUsername() + "'s Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Follow button for artists
        if (user.isArtist() && user.isVerified() &&
                Main.currentUser != null && !user.getUsername().equals(Main.currentUser.getUsername())) {
            Button followBtn = createFollowButton(user);
            header.getChildren().addAll(backBtn, spacer, titleLabel, followBtn);
        } else {
            header.getChildren().addAll(backBtn, spacer, titleLabel);
        }

        return header;
    }

    private static Button createFollowButton(User user) {
        Button followBtn = new Button();
        final boolean[] alreadyFollowing = {UserStorage.isFollowing(Main.currentUser.getUsername(), user.getUsername())};
        followBtn.setText(alreadyFollowing[0] ? "Unfollow" : "Follow");

        followBtn.setOnAction(e -> {
            if (alreadyFollowing[0]) {
                UserStorage.unfollowArtist(Main.currentUser.getUsername(), user.getUsername());
                followBtn.setText("Follow");
            } else {
                UserStorage.followArtist(Main.currentUser.getUsername(), user.getUsername());
                followBtn.setText("Unfollow");
            }
            alreadyFollowing[0] = !alreadyFollowing[0];
        });
        return followBtn;
    }

    private static VBox createUserInfoBox(User user) {
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 5;");

        Label emailLabel = new Label("Email: " + user.getEmail());
        Label memberSinceLabel = new Label("Member since: " + getRegistrationDate(user));
        Label artistStatusLabel = new Label("Artist: " + (user.isArtist() ? "✓ Verified" : "Regular user"));

        infoBox.getChildren().addAll(emailLabel, memberSinceLabel, artistStatusLabel);
        return infoBox;
    }

    private static TabPane createContentTabs(User user) {
        TabPane tabPane = new TabPane();


        if (user.isArtist()) {
            Tab songsTab = new Tab("Songs");
            songsTab.setContent(createSongsList(user));
            songsTab.setClosable(false);
            tabPane.getTabs().add(songsTab);
        }


        if (user.isArtist()) {
            Tab albumsTab = new Tab("Albums");
            albumsTab.setContent(createAlbumsList(user));
            albumsTab.setClosable(false);
            tabPane.getTabs().add(albumsTab);
        }


        if (UserStorage.countFollowing(user.getUsername()) > 0) {
            Tab followingTab = new Tab("Following");
            followingTab.setContent(createFollowingList(user));
            followingTab.setClosable(false);
            tabPane.getTabs().add(followingTab);
        }

        return tabPane;
    }

    private static ListView<Song> createSongsList(User user) {
        ListView<Song> songList = new ListView<>();
        try {
            List<Song> songs = DataStorage.loadArtistSongs(user.getUsername());
            songList.setItems(FXCollections.observableArrayList(songs));

            songList.setCellFactory(lv -> new ListCell<Song>() {
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    if (empty || song == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s (%d views)",
                                song.getTitle(), song.getViews()));
                    }
                }
            });

            songList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Song selected = songList.getSelectionModel().getSelectedItem();
                    if (selected != null) SongViewScreen.show(selected);
                }
            });
        } catch (Exception e) {
            songList.setPlaceholder(new Label("Error loading songs"));
        }
        return songList;
    }

    private static ListView<Album> createAlbumsList(User user) {
        ListView<Album> albumList = new ListView<>();
        try {
            List<Album> albums = AlbumStorage.getAlbumsByArtist(user.getUsername());
            albumList.setItems(FXCollections.observableArrayList(albums));

            albumList.setCellFactory(lv -> new ListCell<Album>() {
                @Override
                protected void updateItem(Album album, boolean empty) {
                    super.updateItem(album, empty);
                    if (empty || album == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s (%d tracks)",
                                album.getTitle(), album.getSongIds().size()));
                    }
                }
            });

            albumList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Album selected = albumList.getSelectionModel().getSelectedItem();
                    if (selected != null) AlbumViewScreen.show(selected);
                }
            });
        } catch (Exception e) {
            albumList.setPlaceholder(new Label("Error loading albums"));
        }
        return albumList;
    }

    private static ListView<String> createFollowingList(User user) {
        ListView<String> followingList = new ListView<>();
        try {
            List<String> artists = UserStorage.getFollowedArtists(user.getUsername());
            followingList.setItems(FXCollections.observableArrayList(artists));

            followingList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    String selected = followingList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        User artistUser = UserStorage.getUser(selected);
                        if (artistUser != null) {
                            UserProfileScreen.show(artistUser);
                        }
                    }
                }
            });
        } catch (Exception e) {
            followingList.setPlaceholder(new Label("Error loading followed artists"));
        }
        return followingList;
    }

    private static VBox createStatsBox(User user) {
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));

        Label statsLabel = new Label("Activity Statistics");
        statsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        int commentCount = DataStorage.countUserComments(user.getUsername());
        int likeCount = countUserLikes(user.getUsername());
        int followingCount = UserStorage.countFollowing(user.getUsername());
        int followerCount = UserStorage.countFollowers(user.getUsername());

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);

        statsGrid.add(new Label("Comments:"), 0, 0);
        statsGrid.add(new Label(String.valueOf(commentCount)), 1, 0);
        long followingCount2 = ArtistStorage.getAllArtists().stream()
                .filter(a -> a.getFollowers().contains(Main.currentUser.getUsername()))
                .count();

        Label followingLabel = new Label("Following Artists: " + followingCount);



        statsBox.getChildren().addAll(statsLabel, statsGrid);
        return statsBox;
    }

    private static VBox createCommentsBox(User user) {
        VBox commentsBox = new VBox(10);
        commentsBox.setPadding(new Insets(15));

        Label commentsLabel = new Label("Recent Comments");
        commentsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<Comment> commentsList = new ListView<>();
        try {
            commentsList.setItems(FXCollections.observableArrayList(
                    DataStorage.loadUserComments(user.getUsername()).stream()
                            .sorted(Comparator.comparing(Comment::getTimestamp).reversed())
                            .limit(5)
                            .collect(Collectors.toList())
            ));

            commentsList.setCellFactory(lv -> new ListCell<Comment>() {
                @Override
                protected void updateItem(Comment comment, boolean empty) {
                    super.updateItem(comment, empty);
                    if (empty || comment == null) {
                        setText(null);
                    } else {
                        try {
                            Song song = SongStorage.getSong(comment.getSongId());
                            if (song != null) {
                                setText(String.format("On '%s' (%s):\n%s",
                                        song.getTitle(),
                                        comment.getTimestamp().format(DATE_FORMAT),
                                        comment.getText()));
                            } else {
                                setText("Comment on deleted song");
                            }
                        } catch (Exception e) {
                            setText("Error loading comment");
                        }
                    }
                }
            });
        } catch (Exception e) {
            commentsList.setPlaceholder(new Label("Error loading comments"));
        }

        commentsBox.getChildren().addAll(commentsLabel, commentsList);
        return commentsBox;
    }

    private static void returnToDashboard(User user) {
        if (Main.currentUser == null) {
            MainMenuScreen.show();
        } else if (user.isAdmin()) {
            AdminDashboard.show();
        } else if (user.isArtist()) {
            ArtistDashboard.show();
        } else {
            UserDashboard.show();
        }
    }

    private static int countUserLikes(String username) {
        try {
            return (int) DataStorage.loadAllSongs().stream()
                    .flatMap(song -> song.getComments().stream())
                    .filter(comment -> comment.getUserId().equals(username))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getRegistrationDate(User user) {
        // In a real app, this would come from user's registration date
        // For now, we'll use a placeholder
        return "April 2025";
    }
}