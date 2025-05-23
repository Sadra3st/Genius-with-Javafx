package genius;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String email;
    private boolean isAdmin;
    private boolean isArtist;
    private boolean isVerified;


    public User(String username, String password, String email,
                boolean isAdmin, boolean isArtist, boolean isVerified) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isArtist = isArtist;
        this.isVerified = isVerified;
    }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isArtist() { return isArtist; }

    public void setPassword(String password) { this.password = password; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setArtist(boolean artist) { isArtist = artist; }

}