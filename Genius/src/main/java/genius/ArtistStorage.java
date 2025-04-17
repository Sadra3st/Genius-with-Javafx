package genius;

import java.io.*;
import java.util.*;

public class ArtistStorage {
    private static final String ARTISTS_DIR = "data/artists/";
    private static Map<String, Artist> artists = new HashMap<>();

    static {
        new File(ARTISTS_DIR).mkdirs();
        loadArtists();
    }

    private static void loadArtists() {
        File[] files = new File(ARTISTS_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    Artist artist = (Artist) ois.readObject();
                    artists.put(artist.getId(), artist);
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error loading artist: " + e.getMessage());
                }
            }
        }
    }

    public static void saveArtist(Artist artist) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(ARTISTS_DIR + artist.getId() + ".artist"))) {
            oos.writeObject(artist);
            artists.put(artist.getId(), artist);
        } catch (IOException e) {
            System.err.println("Error saving artist: " + e.getMessage());
        }
    }

    public static Artist getArtist(String id) {
        return artists.get(id);
    }

    public static Artist getArtistByUsername(String username) {
        for (Artist artist : artists.values()) {
            if (artist.getUsername().equals(username)) {
                return artist;
            }
        }
        return null;
    }

    public static List<Artist> getAllArtists() {
        return new ArrayList<>(artists.values());
    }
}