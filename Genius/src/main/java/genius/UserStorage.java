package genius;

import java.io.*;
import java.util.*;

import static genius.DataStorage.DATA_DIR;

public class UserStorage {
    private static final String FILE_PATH = "users.txt";
    private static final String BANNED_EMAILS_PATH = "banned_emails.txt";
    private static Map<String, User> users = new HashMap<>();
    private static Set<String> bannedEmails = new HashSet<>();

    public static void initialize() {
        loadUsers();
        loadBannedEmails();
        if (!users.containsKey("admin")) {
            registerUser("admin", "admin123", "admin@genius.com", true, false, true);
        }
    }

    private static void loadUsers() {
        users.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    String username = parts[0];
                    String password = parts[1];
                    String email = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    boolean isArtist = Boolean.parseBoolean(parts[4]);
                    boolean isVerified = Boolean.parseBoolean(parts[5]);
                    users.put(username, new User(username, password, email, isAdmin, isArtist, isVerified));
                }
            }
        } catch (IOException e) {
            // File doesn't exist yet, that's fine
        }
    }

    private static void loadBannedEmails() {
        bannedEmails.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(BANNED_EMAILS_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bannedEmails.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            // File doesn't exist yet, that's fine
        }
    }

    private static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users.values()) {
                writer.write(user.getUsername() + "|" +
                        user.getPassword() + "|" +
                        user.getEmail() + "|" +
                        user.isAdmin() + "|" +
                        user.isArtist() + "|" +
                        user.isVerified());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private static void saveBannedEmails() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BANNED_EMAILS_PATH))) {
            for (String email : bannedEmails) {
                writer.write(email);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving banned emails: " + e.getMessage());
        }
    }

    public static boolean isEmailBanned(String email) {
        return bannedEmails.contains(email.toLowerCase());
    }

    public static void banEmail(String email) {
        bannedEmails.add(email.toLowerCase());
        saveBannedEmails();
    }

    // Rest of the methods with email parameter added
    public static boolean validateLogin(String username, String password) {
        User user = users.get(username);
        if (user == null) return false;
        if (user.isArtist() && !user.isVerified()) return false;
        return user.getPassword().equals(password);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static boolean emailExists(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public static void registerUser(String username, String password, String email, boolean isAdmin, boolean isArtist, boolean isVerified) {
        users.put(username, new User(username, password, email, isAdmin, isArtist, isVerified));
        saveUsers();
    }

    public static User getUser(String username) {
        return users.get(username);
    }

    public static Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }

    public static void updateUser(User user) {
        users.put(user.getUsername(), user);
        saveUsers();
    }

    public static void deleteUser(String username) {
        User user = users.get(username);
        if (user != null) {
            banEmail(user.getEmail());
        }
        users.remove(username);
        saveUsers();
    }
    // Add to DataStorage class
    public static void saveComment(Comment comment) throws IOException {
        String commentsDir = DATA_DIR + "comments/";
        new File(commentsDir).mkdirs();

        String commentFile = commentsDir + comment.getId() + ".comment";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(commentFile))) {
            oos.writeObject(comment);
        }
    }

    public static List<Comment> loadSongComments(String songId) throws IOException, ClassNotFoundException {
        List<Comment> comments = new ArrayList<>();
        File[] files = new File(DATA_DIR + "comments/").listFiles((dir, name) -> name.endsWith(".comment"));

        if (files != null) {
            for (File file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    Comment comment = (Comment) ois.readObject();
                    if (comment.getSongId().equals(songId)) {
                        comments.add(comment);
                    }
                }
            }
        }
        return comments;
    }
}