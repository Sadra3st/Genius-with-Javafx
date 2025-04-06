package genius;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {

    private static final String FILE_PATH = "users.txt";

    private static Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // ignore if file doesn't exist yet
        }
        return users;
    }

    public static boolean validateLogin(String username, String password) {
        Map<String, String> users = loadUsers();
        return users.containsKey(username) && users.get(username).equals(password);
    }

    public static boolean userExists(String username) {
        return loadUsers().containsKey(username);
    }

    public static void registerUser(String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing user: " + e.getMessage());
        }
    }
}
