package genius;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataStorage {
    static final String DATA_DIR = "data/";
    private static final String SONGS_DIR = DATA_DIR + "songs/";
    private static final String LYRICS_DIR = DATA_DIR + "lyrics/";

    static {
        new File(DATA_DIR).mkdirs();
        new File(SONGS_DIR).mkdirs();
        new File(LYRICS_DIR).mkdirs();
    }

    public static void saveSong(Song song) throws IOException {
        // Save song metadata
        String songFile = SONGS_DIR + song.getId() + ".song";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(songFile))) {
            oos.writeObject(song);
        }

        // Save lyrics to separate text file
        String lyricsFile = LYRICS_DIR + song.getId() + ".txt";
        Files.write(Paths.get(lyricsFile), song.getLyrics().getBytes());
    }

    public static Song loadSong(String songId) throws IOException, ClassNotFoundException {
        // Load song metadata
        String songFile = SONGS_DIR + songId + ".song";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(songFile))) {
            Song song = (Song) ois.readObject();

            // Load lyrics from text file
            String lyricsFile = LYRICS_DIR + songId + ".txt";
            if (new File(lyricsFile).exists()) {
                String lyrics = new String(Files.readAllBytes(Paths.get(lyricsFile)));
                song.setLyrics(lyrics);
            }

            return song;
        }
    }

    public static List<Song> loadArtistSongs(String artistUsername) throws IOException, ClassNotFoundException {
        List<Song> songs = new ArrayList<>();
        File[] files = new File(SONGS_DIR).listFiles((dir, name) -> name.endsWith(".song"));

        if (files != null) {
            for (File file : files) {
                Song song = loadSong(file.getName().replace(".song", ""));
                if (song != null && song.getArtistId().equals(artistUsername)) {
                    songs.add(song);
                }
            }
        }
        return songs;
    }

    public static List<Song> loadAllSongs() throws IOException, ClassNotFoundException {
        List<Song> songs = new ArrayList<>();
        File[] files = new File(SONGS_DIR).listFiles((dir, name) -> name.endsWith(".song"));

        if (files != null) {
            for (File file : files) {
                Song song = loadSong(file.getName().replace(".song", ""));
                if (song != null) {
                    songs.add(song);
                }
            }
        }
        return songs;
    }

    public static void deleteSong(String songId) throws IOException {
        Files.deleteIfExists(Paths.get(SONGS_DIR + songId + ".song"));
        Files.deleteIfExists(Paths.get(LYRICS_DIR + songId + ".txt"));
    }
}