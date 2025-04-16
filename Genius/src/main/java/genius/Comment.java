package genius;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {
    private String id;
    private String userId;
    private String songId;
    private String text;
    private LocalDateTime timestamp;
    private int likeCount;

    public Comment(String id, String userId, String songId, String text) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = userId;
        this.songId = songId;
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.likeCount = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getSongId() { return songId; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getLikeCount() { return likeCount; }

    // Setters
    public void setText(String text) { this.text = text; }

    // Helper methods
    public void incrementLikes() { likeCount++; }

    @Override
    public String toString() {
        return "Comment{" +
                "userId='" + userId + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", likes=" + likeCount +
                '}';
    }
}