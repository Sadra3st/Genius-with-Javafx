package genius;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Artist implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String bio;
    private int followerCount;
    private List<String> songIds = new ArrayList<>();

    public Artist(String id, String username) {
        this.id = id;
        this.username = username;
        this.followerCount = 0;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getBio() { return bio; }
    public int getFollowerCount() { return followerCount; }
    public List<String> getSongIds() { return new ArrayList<>(songIds); }

    public void setBio(String bio) { this.bio = bio; }
    public void setUsername(String username) { this.username = username; }

    // Methods
    public void addFollower() { followerCount++; }
    public void removeFollower() { followerCount--; }
    public void addSong(String songId) { songIds.add(songId); }
    public void removeSong(String songId) { songIds.remove(songId); }
}