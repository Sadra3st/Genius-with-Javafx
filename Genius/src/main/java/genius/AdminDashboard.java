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


import java.util.Map;

public class AdminDashboard {
    private static ObservableList<User> userListRef;
    private static TableView<User> userTableRef;

    public static void show() {
        ArtistVerification.loadRequests();
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

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


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(welcomeLabel, spacer, changePasswordBtn, logoutBtn);
        layout.setTop(topBar);

        TabPane tabPane = new TabPane();


        Tab userManagementTab = new Tab("User Management");
        userManagementTab.setContent(createUserManagementTab());
        userManagementTab.setClosable(false);


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


        TableView<User> userTable = new TableView<>();
        ObservableList<User> users = FXCollections.observableArrayList(UserStorage.getAllUsers().values());
        userTable.setItems(users);
        userListRef = users;
        userTableRef = userTable;


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

        TableView<Map.Entry<String, String>> table = new TableView<>();
        ObservableList<Map.Entry<String, String>> requests =
                FXCollections.observableArrayList(ArtistVerification.getPendingRequests().entrySet());
        table.setItems(requests);

        TableColumn<Map.Entry<String, String>, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));

        TableColumn<Map.Entry<String, String>, String> bioCol = new TableColumn<>("Artist Bio");
        bioCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));

        table.getColumns().addAll(userCol, bioCol);

        HBox buttons = new HBox(10);
        Button approveBtn = new Button("Approve");
        approveBtn.setOnAction(e -> {
            Map.Entry<String, String> selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String username = selected.getKey();
                ArtistVerification.approveRequest(username);
                requests.remove(selected);
                userListRef.setAll(UserStorage.getAllUsers().values());
                userTableRef.refresh();

                new Alert(Alert.AlertType.INFORMATION,
                        "Artist approved and reflected in user list.").show();
            }
        });
        ;

        Button rejectBtn = new Button("Reject");
        rejectBtn.setOnAction(e -> {
            Map.Entry<String, String> selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArtistVerification.rejectRequest(selected.getKey());
                requests.remove(selected);
            }
        });

        buttons.getChildren().addAll(approveBtn, rejectBtn);
        content.getChildren().addAll(table, buttons);

        return content;
    }
}