package genius;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final String FILE_PATH = "users.txt";
    private static Map<String, User> users = new HashMap<>();

    public static void initialize() {
        loadUsers();
        // Create admin user if none exists
        if (!users.containsKey("admin")) {
            registerUser("admin", "admin123", true);
        }
    }

    private static void loadUsers() {
        users.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String username = parts[0];
                    String password = parts[1];
                    boolean isAdmin = Boolean.parseBoolean(parts[2]);
                    users.put(username, new User(username, password, isAdmin));
                }
            }
        } catch (IOException e) {
            // File doesn't exist yet, that's fine
        }
    }

    private static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users.values()) {
                writer.write(user.getUsername() + "|" +
                        user.getPassword() + "|" +
                        user.isAdmin());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static boolean validateLogin(String username, String password) {
        User user = users.get(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static void registerUser(String username, String password, boolean isAdmin) {
        users.put(username, new User(username, password, isAdmin));
        saveUsers();
    }

    public static void registerUser(String username, String password) {
        registerUser(username, password, false);
    }

    public static User getUser(String username) {
        return users.get(username);
    }

    public static Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }

    public static void updateUser(User user) {
        users.put(user.getUsername(), user);
        saveUsers();
    }

    public static void deleteUser(String username) {
        users.remove(username);
        saveUsers();
    }
}