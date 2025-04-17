package genius;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;

    private String artistId;
    private String id;
    private String title;
    private String lyrics;
    private String primaryArtist;
    private List<String> featuredArtists;
    private String album;
    private String genre;
    private List<String> tags;
    private int views;
    private LocalDate releaseDate;
    private Map<String, Boolean> editPermissions;
    private List<LyricEdit> pendingEdits;
    private List<Comment> comments;

    public Song(String id, String title, String lyrics, String primaryArtist) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.lyrics = Objects.requireNonNull(lyrics, "Lyrics cannot be null");
        this.artistId = Objects.requireNonNull(artistId, "Artist ID cannot be null");
        this.primaryArtist = primaryArtist;
        this.featuredArtists = new ArrayList<>();
        this.album = "Single";
        this.genre = "Unknown";
        this.tags = new ArrayList<>();
        this.views = 0;
        this.releaseDate = LocalDate.now();
        this.editPermissions = new HashMap<>();
        this.pendingEdits = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getLyrics() { return lyrics; }
    public String getPrimaryArtist() { return primaryArtist; }
    public String getArtistId() { return artistId;}
    public List<String> getFeaturedArtists() { return new ArrayList<>(featuredArtists); }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public int getViews() { return views; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public List<LyricEdit> getPendingEdits() { return new ArrayList<>(pendingEdits); }
    public List<Comment> getComments() { return new ArrayList<>(comments); }

    public void setTitle(String title) { this.title = title; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    public void setAlbum(String album) { this.album = album; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

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

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void incrementViews() {
        views++;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void suggestLyricEdit(String userId, String newLyrics) {
        pendingEdits.add(new LyricEdit(userId, newLyrics));
    }

    public void approveLyricEdit(int editIndex) {
        if (editIndex >= 0 && editIndex < pendingEdits.size()) {
            LyricEdit edit = pendingEdits.get(editIndex);
            lyrics = edit.getNewLyrics();
            pendingEdits.remove(editIndex);
        }
    }

    public void rejectLyricEdit(int editIndex) {
        if (editIndex >= 0 && editIndex < pendingEdits.size()) {
            pendingEdits.remove(editIndex);
        }
    }

    public void setEditPermission(String artist, boolean allowed) {
        if (featuredArtists.contains(artist)) {
            editPermissions.put(artist, allowed);
        }
    }

    public boolean canEdit(String artist) {
        return primaryArtist.equals(artist) ||
                Boolean.TRUE.equals(editPermissions.get(artist));
    }

    public List<String> getAllArtists() {
        List<String> allArtists = new ArrayList<>();
        allArtists.add(primaryArtist);
        allArtists.addAll(featuredArtists);
        return allArtists;
    }

}

class LyricEdit implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String newLyrics;
    private LocalDate editDate;

    public LyricEdit(String userId, String newLyrics) {
        this.userId = userId;
        this.newLyrics = newLyrics;
        this.editDate = LocalDate.now();
    }

    public String getUserId() { return userId; }
    public String getNewLyrics() { return newLyrics; }
    public LocalDate getEditDate() { return editDate; }
}