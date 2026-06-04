package manga.common.util;

import manga.model.AuthenticatedUser;
import javax.servlet.http.HttpSession;

/**
 * Helper doc user dang nhap tu session va kiem tra role bat buoc.
 */
public final class SessionUserUtil {

    private SessionUserUtil() {
    }

    /**
     * Tra ve AUTH_USER hop le hoac nem loi Unauthorized.
     */
    public static AuthenticatedUser requireUser(HttpSession session) {
        Object auth = session == null ? null : session.getAttribute("AUTH_USER");
        if (auth == null || !(auth instanceof AuthenticatedUser)) {
            throw new IllegalStateException("Unauthorized");
        }
        return (AuthenticatedUser) auth;
    }

    /**
     * Dam bao user co role duoc yeu cau.
     */
    public static void requireRole(AuthenticatedUser user, String role, String message) {
        if (user == null || !user.hasRole(role)) {
            throw new IllegalArgumentException(message);
        }
    }
}

