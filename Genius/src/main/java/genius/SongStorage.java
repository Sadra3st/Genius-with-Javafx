package genius;

import java.io.*;
import java.util.*;

public class SongStorage {
    private static final String SONGS_DIR = "data/songs/";

    static {
        new File(SONGS_DIR).mkdirs();
    }

    public static void saveSong(Song song) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SONGS_DIR + song.getId() + ".song"))) {
            oos.writeObject(song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Song getSong(String id) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SONGS_DIR + id + ".song"))) {
            return (Song) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public static List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        File[] files = new File(SONGS_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                System.out.println("Loading song file: " + file.getName()); // Debug log
                Song song = getSong(file.getName().replace(".song", ""));
                if (song != null) {
                    songs.add(song);
                    System.out.println("Loaded song: " + song.getTitle()); // Debug log
                }
            }
        }
        return songs;
    }

    public static void deleteSong(String id) {
    }
}