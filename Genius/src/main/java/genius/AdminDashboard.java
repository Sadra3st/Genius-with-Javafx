package genius;

import javafx.beans.property.SimpleBooleanProperty;
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
        // Initialize artist verification data
        ArtistVerification.loadRequests();

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Create top bar with welcome message and buttons
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Label welcomeLabel = new Label("Welcome, Admin " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> ChangePasswordScreen.show());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            Main.currentUser = null;
            LoginScreen.show();
        });

        // Add spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(welcomeLabel, spacer, changePasswordBtn, logoutBtn);
        layout.setTop(topBar);

        TabPane tabPane = new TabPane();

        // User Management Tab
        Tab userManagementTab = new Tab("User Management");
        userManagementTab.setContent(createUserManagementTab());
        userManagementTab.setClosable(false);

        // Artist Verification Tab
        Tab artistVerificationTab = new Tab("Artist Verification");
        artistVerificationTab.setContent(createArtistVerificationTab());
        artistVerificationTab.setClosable(false);

        tabPane.getTabs().addAll(userManagementTab, artistVerificationTab);
        layout.setCenter(tabPane);

        Scene scene = new Scene(layout, 900, 600);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Admin Dashboard");
        Main.primaryStage.show();
    }

    private static VBox createUserManagementTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // User Table
        TableView<User> userTable = new TableView<>();
        ObservableList<User> users = FXCollections.observableArrayList(UserStorage.getAllUsers().values());
        userTable.setItems(users);

        // Table Columns
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, Boolean> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().isAdmin()));

        TableColumn<User, Boolean> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().isArtist()));

        TableColumn<User, Boolean> verifiedCol = new TableColumn<>("Verified");
        verifiedCol.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().isVerified()));

        userTable.getColumns().addAll(usernameCol, emailCol, adminCol, artistCol, verifiedCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Admin Controls
        HBox controls = new HBox(10);
        Button promoteBtn = new Button("Promote/Demote");
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

        controls.getChildren().addAll(promoteBtn, banBtn);
        content.getChildren().addAll(userTable, controls);

        return content;
    }

    private static VBox createArtistVerificationTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Artist Verification Table
        TableView<Map.Entry<String, String>> verificationTable = new TableView<>();
        ObservableList<Map.Entry<String, String>> requests =
                FXCollections.observableArrayList(ArtistVerification.getPendingRequests().entrySet());
        verificationTable.setItems(requests);

        // Table Columns
        TableColumn<Map.Entry<String, String>, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getKey()));

        TableColumn<Map.Entry<String, String>, String> bioCol = new TableColumn<>("Artist Bio");
        bioCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getValue()));

        verificationTable.getColumns().addAll(usernameCol, bioCol);
        verificationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Verification Controls
        HBox controls = new HBox(10);
        Button verifyBtn = new Button("Verify Artist");
        verifyBtn.setOnAction(e -> {
            Map.Entry<String, String> selected = verificationTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArtistVerification.approveRequest(selected.getKey());
                requests.remove(selected);
                verificationTable.refresh();
            }
        });

        Button rejectBtn = new Button("Reject");
        rejectBtn.setOnAction(e -> {
            Map.Entry<String, String> selected = verificationTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArtistVerification.rejectRequest(selected.getKey());
                requests.remove(selected);
                verificationTable.refresh();
            }
        });

        controls.getChildren().addAll(verifyBtn, rejectBtn);
        content.getChildren().addAll(verificationTable, controls);

        return content;
    }
}