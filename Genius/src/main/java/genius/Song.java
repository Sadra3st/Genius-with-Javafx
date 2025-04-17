// ✅ Finalized Song.java - aligns with full song/album system
package genius;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String lyrics;
    private String artistId; // main artist
    private List<String> featuredArtists = new ArrayList<>();
    private String albumId; // "SINGLE" or an album UUID
    private String genre;
    private List<String> tags = new ArrayList<>();
    private int views;
    private LocalDate releaseDate;
    private Map<String, Boolean> editPermissions = new HashMap<>();
    private List<LyricEdit> pendingEdits = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    public static final String SINGLE_ALBUM_ID = "SINGLE";

    public Song(String id, String title, String lyrics, String artistId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.lyrics = lyrics;
        this.artistId = artistId;
        this.albumId = SINGLE_ALBUM_ID; // default to single
        this.genre = "Unknown";
        this.views = 0;
        this.releaseDate = LocalDate.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getLyrics() { return lyrics; }
    public String getArtistId() { return artistId; }
    public List<String> getFeaturedArtists() { return new ArrayList<>(featuredArtists); }
    public String getAlbumId() { return albumId; }
    public String getGenre() { return genre; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public int getViews() { return views; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public List<LyricEdit> getPendingEdits() { return new ArrayList<>(pendingEdits); }
    public List<Comment> getComments() { return new ArrayList<>(comments); }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    // Permissions
    public void addFeaturedArtist(String artist) {
        if (!featuredArtists.contains(artist)) {
            featuredArtists.add(artist);
            editPermissions.put(artist, false);
        }
    }

    public void removeFeaturedArtist(String artist) {
        featuredArtists.remove(artist);
        editPermissions.remove(artist);
    }

    public void setEditPermission(String artist, boolean allowed) {
        if (featuredArtists.contains(artist)) {
            editPermissions.put(artist, allowed);
        }
    }

    public boolean canEdit(String artist) {
        return artistId.equals(artist) || Boolean.TRUE.equals(editPermissions.get(artist));
    }

    public void incrementViews() { views++; }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void suggestLyricEdit(String userId, String newLyrics) {
        pendingEdits.add(new LyricEdit(userId, newLyrics));
    }

    public void approveLyricEdit(int editIndex) {
        if (editIndex >= 0 && editIndex < pendingEdits.size()) {
            lyrics = pendingEdits.get(editIndex).getNewLyrics();
            pendingEdits.remove(editIndex);
        }
    }

    public void rejectLyricEdit(int editIndex) {
        if (editIndex >= 0 && editIndex < pendingEdits.size()) {
            pendingEdits.remove(editIndex);
        }
    }
}
