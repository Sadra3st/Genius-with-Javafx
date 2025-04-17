package genius;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SongStorage {
    private static final String SONGS_DIR = "data/songs/";
    private static final String LYRICS_DIR = "data/lyrics/";
    private static Map<String, Song> songs = new HashMap<>();

    static {
        new File(SONGS_DIR).mkdirs();
        new File(LYRICS_DIR).mkdirs();
        loadAllSongs();
    }

    public static void saveSong(Song song) {
        try {
            Path songFile = Paths.get(SONGS_DIR + song.getId() + ".song");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(songFile))) {
                oos.writeObject(song);
            }

            Path lyricsFile = Paths.get(LYRICS_DIR + song.getId() + ".txt");
            Files.write(lyricsFile, song.getLyrics().getBytes());

            songs.put(song.getId(), song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Song getSong(String id) {
        Song song = songs.get(id);
        if (song == null) {
            try {
                Path songFile = Paths.get(SONGS_DIR + id + ".song");
                if (Files.exists(songFile)) {
                    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(songFile))) {
                        song = (Song) ois.readObject();
                        songs.put(id, song);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return song;
    }

    public static List<Song> getSongsByArtist(String artist) {
        List<Song> artistSongs = new ArrayList<>();
        for (Song song : songs.values()) {
            if (song.getPrimaryArtist().equals(artist) ||
                    song.getFeaturedArtists().contains(artist)) {
                artistSongs.add(song);
            }
        }
        return artistSongs;
    }

    public static List<Song> getAllSongs() {
        return new ArrayList<>(songs.values());
    }

    public static void deleteSong(String id) {
        try {
            Files.deleteIfExists(Paths.get(SONGS_DIR + id + ".song"));
            Files.deleteIfExists(Paths.get(LYRICS_DIR + id + ".txt"));
            songs.remove(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadAllSongs() {
        try {
            Files.list(Paths.get(SONGS_DIR))
                    .filter(path -> path.toString().endsWith(".song"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                            Song song = (Song) ois.readObject();
                            songs.put(song.getId(), song);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}