// ✅ Finalized SongController.java - with I/O handling and album logic
package genius;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongController {

    public static Song createSong(String title, String lyrics, String artistId, String albumId) {
        Song song = new Song(null, title, lyrics, artistId);
        song.setAlbumId(albumId != null ? albumId : Song.SINGLE_ALBUM_ID);

        try {
            DataStorage.saveSong(song);
            if (!song.getAlbumId().equals(Song.SINGLE_ALBUM_ID)) {
                Album album = AlbumStorage.getAlbumById(song.getAlbumId());
                if (album != null) {
                    album.addSong(song.getId());
                    AlbumStorage.saveAlbum(album);
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating song: " + e.getMessage());
        }
        return song;
    }

    public static void updateSong(Song song) {
        try {
            DataStorage.saveSong(song);
        } catch (IOException e) {
            System.err.println("Failed to update song: " + e.getMessage());
        }
    }

    public static void deleteSong(String songId) {
        try {
            DataStorage.deleteSong(songId);
        } catch (IOException e) {
            System.err.println("Failed to delete song: " + e.getMessage());
        }
    }

    public static void incrementViews(String songId) {
        Song song = DataStorage.loadAllSongs().stream()
                .filter(s -> s.getId().equals(songId))
                .findFirst().orElse(null);
        if (song != null) {
            song.incrementViews();
            try {
                DataStorage.saveSong(song);
            } catch (IOException e) {
                System.err.println("Failed to update views: " + e.getMessage());
            }
        }
    }

    public static List<Song> getSongsByAlbum(String albumId) {
        List<Song> albumSongs = new ArrayList<>();
        for (Song song : DataStorage.loadAllSongs()) {
            if (albumId.equals(song.getAlbumId())) {
                albumSongs.add(song);
            }
        }
        return albumSongs;
    }

    public static List<Song> searchSongs(String query) {
        List<Song> results = new ArrayList<>();
        for (Song song : DataStorage.loadAllSongs()) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtistId().toLowerCase().contains(query.toLowerCase()) ||
                    song.getFeaturedArtists().toString().toLowerCase().contains(query.toLowerCase()) ||
                    song.getLyrics().toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
    }
}
