package genius;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String artistUsername; // owner
    private LocalDate releaseDate;
    private List<String> songIds = new ArrayList<>(); // ordered

    public Album(String id, String title, String artistUsername, LocalDate releaseDate) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.artistUsername = artistUsername;
        this.releaseDate = releaseDate != null ? releaseDate : LocalDate.now();
    }

    public static Album createSingleAlbum() {
        return new Album(Song.SINGLE_ALBUM_ID, "Single", "none", LocalDate.now());
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtistUsername() { return artistUsername; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public List<String> getSongIds() { return new ArrayList<>(songIds); }

    public void addSong(String songId) {
        if (!songIds.contains(songId)) {
            songIds.add(songId);
        }
    }

    public void removeSong(String songId) {
        songIds.remove(songId);
    }
}
