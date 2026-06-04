package manga.common.util;

import manga.model.AuthenticatedUser;
import javax.servlet.http.HttpSession;

/**
 * Central helper for reading authenticated users from the session and enforcing
 * role checks in API controllers.
 */
public final class SessionUserUtil {

    private SessionUserUtil() {
    }

    /**
     * Returns the authenticated session user or raises an Unauthorized error.
     *
     * @param session current HTTP session
     * @return authenticated user stored in `AUTH_USER`
     */
    public static AuthenticatedUser requireUser(HttpSession session) {
        Object auth = session == null ? null : session.getAttribute("AUTH_USER");
        // BR-SYS: the session principal must be the application's AuthenticatedUser type.
        if (auth == null || !(auth instanceof AuthenticatedUser)) {
            throw new IllegalStateException("Unauthorized");
        }
        return (AuthenticatedUser) auth;
    }

    /**
     * Requires a user to have a specific role.
     *
     * @param user authenticated user to check
     * @param role required role name
     * @param message exception message when the role is missing
     * @return nothing; throws an exception when the role is missing
     */
    public static void requireRole(AuthenticatedUser user, String role, String message) {
        if (user == null || !user.hasRole(role)) {
            throw new IllegalArgumentException(message);
        }
    }
}

