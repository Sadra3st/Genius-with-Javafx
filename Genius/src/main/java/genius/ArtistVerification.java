package genius;

import java.io.*;
import java.util.*;

public class ArtistVerification {
    private static final String REQUESTS_FILE = "data/artist_requests.txt";
    private static Map<String, String> pendingRequests = new HashMap<>();

    static {
        loadRequests();
    }

    public static void loadRequests() {
        try {
            File file = new File(REQUESTS_FILE);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return;
            }

            pendingRequests.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        pendingRequests.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading artist requests: " + e.getMessage());
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
            if (ArtistStorage.getArtistByUsername(username) == null) {
                Artist artist = new Artist(UUID.randomUUID().toString(), username);
                artist.setBio(pendingRequests.get(username)); // include bio from request
                ArtistStorage.saveArtist(artist);
            }
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