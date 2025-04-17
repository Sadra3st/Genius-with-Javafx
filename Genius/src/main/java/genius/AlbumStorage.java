package genius;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AlbumStorage {
    private static final String FILE_PATH = "data/albums.dat";
    private static Map<String, Album> albums = new HashMap<>();

    static {
        loadAlbums();
    }

    private static void loadAlbums() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            albums = (Map<String, Album>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load albums: " + e.getMessage());
        }
    }

    private static void saveAlbums() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(albums);
        } catch (IOException e) {
            System.err.println("Failed to save albums: " + e.getMessage());
        }
    }

    public static void saveAlbum(Album album) {
        albums.put(album.getId(), album);
        saveAlbums();
    }

    public static Album getAlbum(String id) {
        return albums.get(id);
    }

    public static List<Album> getAlbumsByArtist(String artistUsername) {
        return albums.values().stream()
                .filter(a -> a.getArtistUsername().equalsIgnoreCase(artistUsername))
                .collect(Collectors.toList());
    }

    public static List<Album> getAllAlbums() {
        return new ArrayList<>(albums.values());
    }
}
