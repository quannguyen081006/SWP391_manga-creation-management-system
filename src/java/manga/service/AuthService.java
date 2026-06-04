package manga.service;

import manga.model.AuthenticatedUser;
import manga.repository.UserAdminRepository;
import manga.repository.UserRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Authenticates users and loads authenticated principals for the web/API layer.
 * It also contains local test-account seeding used by the development switch
 * role workflow.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAdminRepository userAdminRepository;

    @Autowired
    private DataSource dataSource;

    /**
     * Authenticates a username/password pair and returns the active user.
     *
     * @param username submitted username
     * @param password submitted password
     * @return authenticated user with roles loaded
     */
    public AuthenticatedUser login(String username, String password) {
        if (username != null) {
            // Development helper: known demo users are created lazily for testing.
            ensureTestingAccountExists(username.trim());
            ensureTestingAssignments(username.trim());
        }

        AuthenticatedUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Username or password is incorrect");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("This account is inactive");
        }

        // Current project stores the plain password value in passwordHash.
        if (password == null || !password.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Username or password is incorrect");
        }

        return user;
    }

    /**
     * Loads an active user by username for the switch-role testing helper.
     *
     * @param username username to switch into
     * @return authenticated user with roles loaded
     */
    public AuthenticatedUser switchUserForTesting(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required for switch role");
        }

        String normalized = username.trim();
        AuthenticatedUser user = userRepository.findByUsername(normalized);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Cannot switch to inactive account");
        }
        return user;
    }

    private void ensureTestingAccountExists(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        if (userRepository.findByUsername(username) != null) {
            return;
        }

        String role;
        String fullName;
        // Demo accounts map directly to the five BR-SYS role families.
        if ("admin".equalsIgnoreCase(username)) {
            role = "ADMIN";
            fullName = "System Admin";
        } else if ("mangaka1".equalsIgnoreCase(username)) {
            role = "MANGAKA";
            fullName = "Yuki Tanaka";
        } else if ("assistant1".equalsIgnoreCase(username)) {
            role = "ASSISTANT";
            fullName = "Aiko Mori";
        } else if ("tantou1".equalsIgnoreCase(username)) {
            role = "TANTOU_EDITOR";
            fullName = "Hiroshi Yamamoto";
        } else if (username.toLowerCase().matches("board[1-5]")) {
            role = "EDITORIAL_BOARD";
            fullName = "Board Member " + username.substring(username.length() - 1);
        } else {
            return;
        }

        String email = username.toLowerCase() + "@mangaflow.local";
        try {
            long userId = userAdminRepository.createUser(username, "12345", fullName, email);
            userAdminRepository.addRole(userId, role);
        } catch (RuntimeException ignored) {
            // Ignore duplicate-create races for testing helper.
        }
    }

    private void ensureTestingAssignments(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        String normalized = username.trim().toLowerCase();
        if (!"mangaka1".equals(normalized)
                && !"assistant1".equals(normalized)
                && !"assistant2".equals(normalized)
                && !"assistant3".equals(normalized)
                && !"assistant4".equals(normalized)) {
            return;
        }

        // Seed a small Mangaka/Assistant relationship set for task testing.
        ensureTestingAccountExists("mangaka1");
        ensureTestingAssistant("assistant1", "Aiko Mori", "asst1@mangaflow.local");
        ensureTestingAssistant("assistant2", "Riku Hayashi", "asst2@mangaflow.local");
        ensureTestingAssistant("assistant3", "Mika Saito", "asst3@mangaflow.local");
        ensureTestingAssistant("assistant4", "Ren Fujimoto", "asst4@mangaflow.local");

        AuthenticatedUser mangaka = userRepository.findByUsername("mangaka1");
        if (mangaka == null) {
            return;
        }

        enrollTestingAssistant(mangaka.getId(), "assistant1");
        enrollTestingAssistant(mangaka.getId(), "assistant2");
        enrollTestingAssistant(mangaka.getId(), "assistant3");
        enrollTestingAssistant(mangaka.getId(), "assistant4");
    }

    private void ensureTestingAssistant(String username, String fullName, String email) {
        AuthenticatedUser user = userRepository.findByUsername(username);
        if (user == null) {
            try {
                long id = userAdminRepository.createUser(username, "12345", fullName, email);
                userAdminRepository.addRole(id, "ASSISTANT");
            } catch (RuntimeException ignored) {
                // Ignore duplicate-create races for testing helper.
            }
            return;
        }
        userAdminRepository.addRole(user.getId(), "ASSISTANT");
    }

    private void enrollTestingAssistant(long mangakaId, String assistantUsername) {
        AuthenticatedUser assistant = userRepository.findByUsername(assistantUsername);
        if (assistant == null) {
            return;
        }

        String existsSql = "SELECT 1 FROM MangakaAssistant WHERE mangakaId = ? AND assistantId = ?";
        String insertSql = "INSERT INTO MangakaAssistant (mangakaId, assistantId, enrolledAt) VALUES (?, ?, GETDATE())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement exists = conn.prepareStatement(existsSql)) {
            exists.setLong(1, mangakaId);
            exists.setLong(2, assistant.getId());
            try (ResultSet rs = exists.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }

            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setLong(1, mangakaId);
                insert.setLong(2, assistant.getId());
                insert.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot ensure testing assistant assignment", ex);
        }
    }
}
