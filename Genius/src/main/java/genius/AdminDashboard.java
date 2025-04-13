package genius;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Map;

public class AdminDashboard {
    public static void show() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome, Sir " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("You are logged in as an administrator.");
        infoLabel.setTextFill(Color.BLUE);

        // Tab pane for different admin sections
        TabPane tabPane = new TabPane();

        // User Management Tab
        Tab userTab = new Tab("User Management");
        userTab.setClosable(false);
        userTab.setContent(createUserManagementTab());

        // Artist Verification Tab
        Tab artistTab = new Tab("Artist Verification");
        artistTab.setClosable(false);
        artistTab.setContent(createArtistVerificationTab());

        tabPane.getTabs().addAll(userTab, artistTab);
        layout.getChildren().addAll(welcomeLabel, infoLabel, tabPane);

        Scene scene = new Scene(layout, 600, 500);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Admin Dashboard");
        Main.primaryStage.show();
    }

    private static VBox createUserManagementTab() {
        VBox tabContent = new VBox(10);

        // Table for user management
        TableView<User> userTable = new TableView<>();
        ObservableList<User> users = FXCollections.observableArrayList(UserStorage.getAllUsers().values());
        userTable.setItems(users);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, Boolean> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));

        TableColumn<User, Boolean> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn<User, Boolean> verifiedCol = new TableColumn<>("Verified");
        verifiedCol.setCellValueFactory(new PropertyValueFactory<>("verified"));

        userTable.getColumns().addAll(usernameCol, emailCol, adminCol, artistCol, verifiedCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Admin controls
        HBox adminControls = new HBox(10);

        Button promoteBtn = new Button("Promote/Demote Admin");
        promoteBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.getUsername().equals("admin")) {
                selected.setAdmin(!selected.isAdmin());
                UserStorage.updateUser(selected);
                userTable.refresh();
            }
        });

        Button deleteBtn = new Button("Ban User");
        deleteBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.getUsername().equals("admin")) {
                UserStorage.deleteUser(selected.getUsername());
                users.remove(selected);
            }
        });

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> ChangePasswordScreen.show());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });

        adminControls.getChildren().addAll(promoteBtn, deleteBtn, changePasswordBtn, logoutBtn);
        tabContent.getChildren().addAll(userTable, adminControls);

        return tabContent;
    }

    private static VBox createArtistVerificationTab() {
        VBox tabContent = new VBox(10);

        // Table for unverified artists
        TableView<User> artistTable = new TableView<>();
        ObservableList<User> artists = FXCollections.observableArrayList(
                UserStorage.getAllUsers().values().stream()
                        .filter(user -> user.isArtist() && !user.isVerified())
                        .toList()
        );
        artistTable.setItems(artists);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        artistTable.getColumns().addAll(usernameCol, emailCol);
        artistTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Verification controls
        HBox verifyControls = new HBox(10);

        Button verifyBtn = new Button("Verify Artist");
        verifyBtn.setOnAction(e -> {
            User selected = artistTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setVerified(true);
                UserStorage.updateUser(selected);
                artists.remove(selected);
            }
        });

        Button rejectBtn = new Button("Reject Artist");
        rejectBtn.setOnAction(e -> {
            User selected = artistTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                UserStorage.deleteUser(selected.getUsername());
                artists.remove(selected);
            }
        });

        verifyControls.getChildren().addAll(verifyBtn, rejectBtn);
        tabContent.getChildren().addAll(artistTable, verifyControls);

        return tabContent;
    }
}