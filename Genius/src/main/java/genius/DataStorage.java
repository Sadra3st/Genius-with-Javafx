package genius;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataStorage {
    private static final String DATA_DIR = "data/";
    private static final String SONGS_DIR = DATA_DIR + "songs/";
    private static final String LYRICS_DIR = DATA_DIR + "lyrics/";
    private static final String COMMENTS_DIR = DATA_DIR + "comments/";

    static {
        createDirectories();
    }

    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(SONGS_DIR));
            Files.createDirectories(Paths.get(LYRICS_DIR));
            Files.createDirectories(Paths.get(COMMENTS_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directories: " + e.getMessage());
        }
    }

    public static void saveSong(Song song) throws IOException {
        // Save song metadata
        Path songFile = Paths.get(SONGS_DIR + song.getId() + ".song");
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(songFile))) {
            oos.writeObject(song);
        }


        Path lyricsFile = Paths.get(LYRICS_DIR + song.getId() + ".txt");
        Files.write(lyricsFile, song.getLyrics().getBytes());
    }

    public static Song loadSong(String songId) throws IOException, ClassNotFoundException {
        Path songFile = Paths.get(SONGS_DIR + songId + ".song");
        if (!Files.exists(songFile)) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(songFile))) {
            Song song = (Song) ois.readObject();


            Path lyricsFile = Paths.get(LYRICS_DIR + songId + ".txt");
            if (Files.exists(lyricsFile)) {
                song.setLyrics(new String(Files.readAllBytes(lyricsFile)));
            }

            return song;
        }
    }

    public static List<Song> loadArtistSongs(String artistUsername) {
        List<Song> songs = new ArrayList<>();
        try {
            Files.list(Paths.get(SONGS_DIR))
                    .filter(path -> path.toString().endsWith(".song"))
                    .forEach(path -> {
                        try {
                            Song song = loadSong(path.getFileName().toString().replace(".song", ""));
                            if (song != null && song.getArtistId().equals(artistUsername)) {
                                songs.add(song);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading song: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing songs: " + e.getMessage());
        }
        return songs;
    }

    public static List<Song> loadAllSongs() {
        List<Song> songs = new ArrayList<>();
        try {
            Files.list(Paths.get(SONGS_DIR))
                    .filter(path -> path.toString().endsWith(".song"))
                    .forEach(path -> {
                        try {
                            Song song = loadSong(path.getFileName().toString().replace(".song", ""));
                            if (song != null) {
                                songs.add(song);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading song: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing songs: " + e.getMessage());
        }
        return songs;
    }

    public static void deleteSong(String songId) throws IOException {

        Path songFile = Paths.get(SONGS_DIR + songId + ".song");
        Files.deleteIfExists(songFile);


        Path lyricsFile = Paths.get(LYRICS_DIR + songId + ".txt");
        Files.deleteIfExists(lyricsFile);


        Files.list(Paths.get(COMMENTS_DIR))
                .filter(path -> path.getFileName().toString().startsWith(songId + "_"))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete comment file: " + path);
                    }
                });
    }

    public static void saveComment(Comment comment) throws IOException {
        Path commentFile = Paths.get(COMMENTS_DIR + comment.getSongId() + "_" + comment.getId() + ".comment");
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(commentFile))) {
            oos.writeObject(comment);
        }
    }

    public static List<Comment> loadSongComments(String songId) {
        List<Comment> comments = new ArrayList<>();
        try {
            Files.list(Paths.get(COMMENTS_DIR))
                    .filter(path -> path.getFileName().toString().startsWith(songId + "_"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                            comments.add((Comment) ois.readObject());
                        } catch (Exception e) {
                            System.err.println("Error loading comment: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing comments: " + e.getMessage());
        }
        return comments;
    }

    public static List<Comment> loadUserComments(String userId) {
        List<Comment> comments = new ArrayList<>();
        try {
            Files.list(Paths.get(COMMENTS_DIR))
                    .filter(path -> path.toString().endsWith(".comment"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                            Comment comment = (Comment) ois.readObject();
                            if (comment.getUserId().equals(userId)) {
                                comments.add(comment);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading comment: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing comments: " + e.getMessage());
        }
        return comments;
    }

    public static void deleteComment(String commentId) throws IOException {
        Files.list(Paths.get(COMMENTS_DIR))
                .filter(path -> path.getFileName().toString().contains(commentId))
                .findFirst()
                .ifPresent(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete comment: " + e.getMessage());
                    }
                });
    }

    public static int countSongComments(String songId) {
        try {
            return (int) Files.list(Paths.get(COMMENTS_DIR))
                    .filter(path -> path.getFileName().toString().startsWith(songId + "_"))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }

    public static int countUserComments(String userId) {
        try {
            return (int) Files.list(Paths.get(COMMENTS_DIR))
                    .filter(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                            Comment comment = (Comment) ois.readObject();
                            return comment.getUserId().equals(userId);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }
    public static void incrementSongViews(String songId) {
        try {
            Song song = loadSong(songId);
            if (song != null) {
                song.incrementViews();
                saveSong(song);
            }
        } catch (Exception e) {
            System.err.println("Error incrementing song views: " + e.getMessage());
        }
    }

    public static int loadSongViews(String songId) {
        try {
            Song song = loadSong(songId);
            return song != null ? song.getViews() : 0;
        } catch (Exception e) {
            System.err.println("Error loading song views: " + e.getMessage());
            return 0;
        }
    }
}