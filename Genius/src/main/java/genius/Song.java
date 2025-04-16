package genius;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String lyrics;
    private String artistId;
    private int duration; // in seconds
    private int likeCount;
    private int dislikeCount;
    private List<Comment> comments;
    private List<Annotation> annotations;

    public Song(String id, String title, String lyrics, int duration, String artistId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.lyrics = lyrics;
        this.duration = duration;
        this.artistId = artistId;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.comments = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getLyrics() { return lyrics; }
    public String getArtistId() { return artistId; }
    public int getDuration() { return duration; }
    public int getLikeCount() { return likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public List<Comment> getComments() { return new ArrayList<>(comments); }
    public List<Annotation> getAnnotations() { return new ArrayList<>(annotations); }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setArtistId(String artistId) { this.artistId = artistId; }

    // Helper methods
    public String getLyricsPreview() {
        return lyrics.length() > 50 ? lyrics.substring(0, 50) + "..." : lyrics;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    public void incrementLikes() { likeCount++; }
    public void incrementDislikes() { dislikeCount++; }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", artistId='" + artistId + '\'' +
                ", duration=" + duration +
                ", likes=" + likeCount +
                '}';
    }
}