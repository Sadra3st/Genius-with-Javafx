package genius;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class UserStorage {
    private static final String PROJECT_DIR = System.getProperty("user.dir");
    private static final String DATA_DIR = PROJECT_DIR + File.separator + "data" + File.separator;
    private static final String USERS_FILE = DATA_DIR + "users.txt";
    private static final String BANNED_EMAILS_FILE = DATA_DIR + "banned_emails.txt";
    public static final String FOLLOWING_FILE = DATA_DIR + "following.txt";

    private static Map<String, User> users = new HashMap<>();
    private static Set<String> bannedEmails = new HashSet<>();

    static {
        ensureDataDirectoryExists();
        loadAllData();
        createDefaultAdmin();
    }

    private static void ensureDataDirectoryExists() {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            if (!Files.exists(Paths.get(USERS_FILE))) {
                Files.createFile(Paths.get(USERS_FILE));
            }
            if (!Files.exists(Paths.get(BANNED_EMAILS_FILE))) {
                Files.createFile(Paths.get(BANNED_EMAILS_FILE));
            }
            if (!Files.exists(Paths.get(FOLLOWING_FILE))) {
                Files.createFile(Paths.get(FOLLOWING_FILE));
            }
        } catch (IOException e) {
            System.err.println("Error creating data files: " + e.getMessage());
        }
    }

    private static void loadAllData() {
        loadUsers();
        loadBannedEmails();
    }

    private static void loadUsers() {
        users.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    User user = new User(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            Boolean.parseBoolean(parts[3].trim()),
                            Boolean.parseBoolean(parts[4].trim()),
                            Boolean.parseBoolean(parts[5].trim())
                    );
                    users.put(user.getUsername().toLowerCase(), user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private static void loadBannedEmails() {
        try {
            if (Files.exists(Paths.get(BANNED_EMAILS_FILE))) {
                bannedEmails.addAll(Files.readAllLines(Paths.get(BANNED_EMAILS_FILE)));
            }
        } catch (IOException e) {
            System.err.println("Error loading banned emails: " + e.getMessage());
        }
    }

    private static void createDefaultAdmin() {
        if (!users.containsKey("admin")) {
            registerUser("admin", "admin123", "admin@genius.com", true, false, true);
        }
    }

    public static boolean validateLogin(String username, String password) {
        User user = users.get(username.toLowerCase());
        if (user == null) return false;
        return user.getPassword().equals(password);
    }


    public static boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public static boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public static boolean isEmailBanned(String email) {
        return bannedEmails.contains(email.toLowerCase());
    }

    public static void banEmail(String email) {
        bannedEmails.add(email.toLowerCase());
        saveBannedEmails();
    }

    public static void registerUser(String username, String password, String email,
                                    boolean isAdmin, boolean isArtist, boolean isVerified) {
        users.put(username.toLowerCase(), new User(username, password, email, isAdmin, isArtist, isVerified));
        saveUsers();
    }

    public static User getUser(String username)
    {
        UserStorage.loadUsers();
        return users.get(username.toLowerCase());
    }

    public static Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }

    public static void updateUser(User user) {
        if (user == null) return;
        users.put(user.getUsername().toLowerCase(), user);
        saveUsers();
        if (user.isArtist() && user.isVerified()) {
            Artist artist = ArtistStorage.getArtistByUsername(user.getUsername());
            if (artist == null) {
                artist = new Artist(UUID.randomUUID().toString(), user.getUsername());
                ArtistStorage.saveArtist(artist);
            }
        }
    }

    public static void deleteUser(String username) {
        User user = users.get(username.toLowerCase());
        if (user != null) {
            banEmail(user.getEmail());
        }
        users.remove(username.toLowerCase());
        saveUsers();
    }

    private static void saveUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(USERS_FILE))) {
            for (User user : users.values()) {
                writer.write(String.format("%s|%s|%s|%b|%b|%b%n",
                        user.getUsername(),
                        user.getPassword(),
                        user.getEmail(),
                        user.isAdmin(),
                        user.isArtist(),
                        user.isVerified()));
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private static void saveBannedEmails() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(BANNED_EMAILS_FILE))) {
            for (String email : bannedEmails) {
                writer.write(email);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving banned emails: " + e.getMessage());
        }
    }

    public static int countFollowing(String username) {
        try {
            if (Files.exists(Paths.get(FOLLOWING_FILE))) {
                return (int) Files.lines(Paths.get(FOLLOWING_FILE))
                        .filter(line -> line.startsWith(username + "|"))
                        .count();
            }
        } catch (IOException e) {
            System.err.println("Error counting following: " + e.getMessage());
        }
        return 0;
    }

    public static int countFollowers(String username) {
        try {
            if (Files.exists(Paths.get(FOLLOWING_FILE))) {
                return (int) Files.lines(Paths.get(FOLLOWING_FILE))
                        .filter(line -> line.endsWith("|" + username))
                        .count();
            }
        } catch (IOException e) {
            System.err.println("Error counting followers: " + e.getMessage());
        }
        return 0;
    }

    public static boolean isFollowing(String follower, String artist) {
        try {
            if (Files.exists(Paths.get(FOLLOWING_FILE))) {
                return Files.lines(Paths.get(FOLLOWING_FILE))
                        .anyMatch(line -> line.equals(follower + "|" + artist));
            }
        } catch (IOException e) {
            System.err.println("Error checking follow status: " + e.getMessage());
        }
        return false;
    }

    public static void followArtist(String follower, String artist) {
        try {
            Files.write(Paths.get(FOLLOWING_FILE),
                    (follower + "|" + artist + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error following artist: " + e.getMessage());
        }
    }

    public static void unfollowArtist(String follower, String artist) {
        try {
            if (Files.exists(Paths.get(FOLLOWING_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(FOLLOWING_FILE));
                lines.removeIf(line -> line.equals(follower + "|" + artist));
                Files.write(Paths.get(FOLLOWING_FILE), lines);
            }
        } catch (IOException e) {
            System.err.println("Error unfollowing artist: " + e.getMessage());
        }
    }

    public static List<String> getFollowedArtists(String username) {
        try {
            if (Files.exists(Paths.get(FOLLOWING_FILE))) {
                return Files.lines(Paths.get(FOLLOWING_FILE))
                        .filter(line -> line.startsWith(username + "|"))
                        .map(line -> line.split("\\|")[1])
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("Error getting followed artists: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}