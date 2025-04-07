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

        Label welcomeLabel = new Label("Welcome, Admin " + Main.currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("You are logged in as an administrator.");
        infoLabel.setTextFill(Color.BLUE);

        // Table for user management
        TableView<User> userTable = new TableView<>();
        ObservableList<User> users = FXCollections.observableArrayList(UserStorage.getAllUsers().values());
        userTable.setItems(users);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, Boolean> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));

        userTable.getColumns().addAll(usernameCol, adminCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Admin controls
        HBox adminControls = new HBox(10);

        Button promoteBtn = new Button("Promote/Demote");
        promoteBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.getUsername().equals("admin")) {
                selected.setAdmin(!selected.isAdmin());
                UserStorage.updateUser(selected);
                userTable.refresh();
            }
        });

        Button deleteBtn = new Button("Delete User");
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

        layout.getChildren().addAll(welcomeLabel, infoLabel, userTable, adminControls);
        Scene scene = new Scene(layout, 500, 400);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.setTitle("Admin Dashboard");
        Main.primaryStage.show();
    }
}