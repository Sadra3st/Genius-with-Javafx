package genius;

import java.io.Serializable;

public class LyricEdit implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String newLyrics;

    public LyricEdit(String userId, String newLyrics) {
        this.userId = userId;
        this.newLyrics = newLyrics;
    }

    public String getUserId() {
        return userId;
    }

    public String getNewLyrics() {
        return newLyrics;
    }
}

