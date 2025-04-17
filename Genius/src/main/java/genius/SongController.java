package genius;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SongController {
    public static Song createSong(String title, String lyrics, String primaryArtist) {
        Song song = new Song(null, title, lyrics, primaryArtist);
        SongStorage.saveSong(song);
        return song;
    }

    public static void updateSong(Song song) {
        SongStorage.saveSong(song);
    }

    public static void deleteSong(String songId) {
        SongStorage.deleteSong(songId);
    }

    public static void incrementViews(String songId) {
        Song song = SongStorage.getSong(songId);
        if (song != null) {
            song.incrementViews();
            SongStorage.saveSong(song);
        }
    }

    public static void addFeaturedArtist(String songId, String artist) {
        Song song = SongStorage.getSong(songId);
        if (song != null) {
            song.addFeaturedArtist(artist);
            SongStorage.saveSong(song);
        }
    }

    public static void suggestLyricEdit(String songId, String userId, String newLyrics) {
        Song song = SongStorage.getSong(songId);
        if (song != null) {
            song.suggestLyricEdit(userId, newLyrics);
            SongStorage.saveSong(song);
        }
    }

    public static void approveLyricEdit(String songId, int editIndex) {
        Song song = SongStorage.getSong(songId);
        if (song != null) {
            song.approveLyricEdit(editIndex);
            SongStorage.saveSong(song);
        }
    }

    public static List<Song> getSongsByAlbum(String album) {
        List<Song> albumSongs = new ArrayList<>();
        for (Song song : SongStorage.getAllSongs()) {
            if (song.getAlbum().equals(album)) {
                albumSongs.add(song);
            }
        }
        return albumSongs;
    }

    public static List<Song> searchSongs(String query) {
        List<Song> results = new ArrayList<>();
        for (Song song : SongStorage.getAllSongs()) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getPrimaryArtist().toLowerCase().contains(query.toLowerCase()) ||
                    song.getFeaturedArtists().toString().toLowerCase().contains(query.toLowerCase()) ||
                    song.getLyrics().toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
    }
}