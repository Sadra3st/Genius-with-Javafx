package genius;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String artistUsername;
    private LocalDate releaseDate;
    private List<String> songIds = new ArrayList<>();

    public Album(String id, String title, String artistUsername, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.artistUsername = artistUsername;
        this.releaseDate = releaseDate;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtistUsername() { return artistUsername; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public List<String> getSongIds() { return new ArrayList<>(songIds); }

    public void addSong(String songId) {
        if (!songIds.contains(songId)) songIds.add(songId);
    }

    public void removeSong(String songId) {
        songIds.remove(songId);
    }

    public void setTitle(String title) { this.title = title; }
    public void setReleaseDate(LocalDate date) { this.releaseDate = date; }
    public void setSongIds(List<String> ids) { this.songIds = new ArrayList<>(ids); }
}
