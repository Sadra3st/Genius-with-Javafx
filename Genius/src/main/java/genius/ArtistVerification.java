package genius;

import java.io.*;
import java.util.*;

public class ArtistVerification {

    private static final String REQUESTS_FILE = "artist_requests.txt";
    private static Map<String, String> pendingRequests = new HashMap<>(); // username -> bio

    public static void loadRequests() {
        try (BufferedReader reader = new BufferedReader(new FileReader(REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    pendingRequests.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // File doesn't exist yet
        }
    }

    public static void submitRequest(String username, String artistBio) {
        pendingRequests.put(username, artistBio);
        saveRequests();
    }

    public static void approveRequest(String username) {
        User user = UserStorage.getUser(username);
        if (user != null) {
            user.setArtist(true);
            user.setVerified(true);
            UserStorage.updateUser(user);
            pendingRequests.remove(username);
            saveRequests();
        }
    }

    public static void rejectRequest(String username) {
        pendingRequests.remove(username);
        saveRequests();
    }

    private static void saveRequests() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REQUESTS_FILE))) {
            for (Map.Entry<String, String> entry : pendingRequests.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving artist requests: " + e.getMessage());
        }
    }

    public static Map<String, String> getPendingRequests() {
        return new HashMap<>(pendingRequests);
    }
}