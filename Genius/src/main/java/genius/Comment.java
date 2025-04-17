package genius;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String songId;
    private String text;
    private LocalDateTime timestamp;

    public Comment(String id, String userId, String songId, String text) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = userId;
        this.songId = songId;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getSongId() { return songId; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
}