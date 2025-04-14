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

import java.util.Map;

public class AdminDashboard {
    public static void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome, " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("You are logged in as an administrator.");
        infoLabel.setTextFill(Color.BLUE);

        // Tab pane for different admin sections
        TabPane tabPane = new TabPane();

        // User Management Tab
        Tab userTab = new Tab("User Management");
        userTab.setContent(createUserManagementContent());
        userTab.setClosable(false);

        // Artist Verification Tab
        Tab artistTab = new Tab("Artist Verification");
        artistTab.setContent(createArtistVerificationContent());
        artistTab.setClosable(false);

        tabPane.getTabs().addAll(userTab, artistTab);
        layout.setCenter(tabPane);

        // Footer with logout button
        HBox footer = new HBox(10);
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });
        footer.getChildren().add(logoutBtn);
        layout.setBottom(footer);

        Scene scene = new Scene(layout, 800, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Admin Dashboard");
        Main.primaryStage.show();
    }

    private static VBox createUserManagementContent() {
        VBox content = new VBox(10);

        // Table for user management
        TableView<User> userTable = new TableView<>();
        ObservableList<User> users = FXCollections.observableArrayList(UserStorage.getAllUsers().values());

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
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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

        Button banBtn = new Button("Ban User");
        banBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.getUsername().equals("admin")) {
                UserStorage.deleteUser(selected.getUsername());
                users.remove(selected);
            }
        });

        adminControls.getChildren().addAll(promoteBtn, banBtn);
        content.getChildren().addAll(userTable, adminControls);

        return content;
    }

    private static VBox createArtistVerificationContent() {
        VBox content = new VBox(10);

        // Table for artist verification
        TableView<Map.Entry<String, String>> requestsTable = new TableView<>();
        ObservableList<Map.Entry<String, String>> requests = FXCollections.observableArrayList(
                ArtistVerification.getPendingRequests().entrySet()
        );

        TableColumn<Map.Entry<String, String>, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));

        TableColumn<Map.Entry<String, String>, String> bioCol = new TableColumn<>("Artist Bio");
        bioCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));

        requestsTable.getColumns().addAll(userCol, bioCol);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Verification controls
        HBox verifyControls = new HBox(10);
        Button verifyBtn = new Button("Verify Artist");
        verifyBtn.setOnAction(e -> {
            Map.Entry<String, String> selected = requestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArtistVerification.approveRequest(selected.getKey());
                requests.remove(selected);
            }
        });

        Button rejectBtn = new Button("Reject");
        rejectBtn.setOnAction(e -> {
            Map.Entry<String, String> selected = requestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArtistVerification.rejectRequest(selected.getKey());
                requests.remove(selected);
            }
        });

        verifyControls.getChildren().addAll(verifyBtn, rejectBtn);
        content.getChildren().addAll(requestsTable, verifyControls);

        return content;
    }
}