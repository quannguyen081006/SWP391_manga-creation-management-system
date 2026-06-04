package manga.controller.api;

import manga.common.ApiResponse;
import manga.common.util.SessionUserUtil;
import manga.model.AuthenticatedUser;
import manga.repository.UserAdminRepository;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes admin-only user management APIs for account creation, profile
 * updates, status changes, and role assignment.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

    @Autowired
    private UserAdminRepository userAdminRepository;

    /**
     * Lists all users for admin management.
     *
     * @param session current HTTP session containing an ADMIN user
     * @return API response containing all users and roles
     */
    @RequestMapping(method = RequestMethod.GET)
    public ApiResponse<List<Map<String, Object>>> list(HttpSession session) {
        requireAdmin(session);
        return ApiResponse.ok(userAdminRepository.listUsers(), "User list");
    }

    /**
     * Creates a new user through the admin API.
     *
     * @param session current HTTP session containing an ADMIN user
     * @param username unique username
     * @param password raw password value used by the current project
     * @param fullName display name
     * @param email unique email address
     * @return API response containing the created user
     */
    @RequestMapping(method = RequestMethod.POST)
    public ApiResponse<Map<String, Object>> create(
            HttpSession session,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email) {
        requireAdmin(session);
        long id = userAdminRepository.createUser(username, password, fullName, email);
        return ApiResponse.ok(userAdminRepository.getUser(id), "User created");
    }

    /**
     * Returns one user's details for admin management.
     *
     * @param id user id
     * @param session current HTTP session containing an ADMIN user
     * @return API response containing user details
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ApiResponse<Map<String, Object>> detail(@PathVariable("id") long id, HttpSession session) {
        requireAdmin(session);
        Map<String, Object> user = userAdminRepository.getUser(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return ApiResponse.ok(user, "User detail");
    }

    /**
     * Updates a user's basic profile fields through the API.
     *
     * @param id user id
     * @param session current HTTP session containing an ADMIN user
     * @param fullName updated full name
     * @param email updated email address
     * @return empty success response
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ApiResponse<Object> update(
            @PathVariable("id") long id,
            HttpSession session,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email) {
        requireAdmin(session);
        userAdminRepository.updateUser(id, fullName, email);
        return ApiResponse.ok(null, "User updated");
    }

    /**
     * Updates a user's ACTIVE/INACTIVE status.
     *
     * @param id user id
     * @param session current HTTP session containing an ADMIN user
     * @param status requested status value
     * @return empty success response
     */
    @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
    public ApiResponse<Object> patchStatus(
            @PathVariable("id") long id,
            HttpSession session,
            @RequestParam("status") String status) {
        requireAdmin(session);
        String normalized = status == null ? "" : status.trim().toUpperCase();
        // BR-SYS: only ACTIVE and INACTIVE are valid account states.
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }
        userAdminRepository.updateStatus(id, normalized);
        return ApiResponse.ok(null, "User status updated");
    }

    /**
     * Assigns a role to a user through the admin API.
     *
     * @param id user id
     * @param session current HTTP session containing an ADMIN user
     * @param role role name to assign
     * @return empty success response
     */
    @RequestMapping(value = "/{id}/roles", method = RequestMethod.POST)
    public ApiResponse<Object> addRole(
            @PathVariable("id") long id,
            HttpSession session,
            @RequestParam("role") String role) {
        requireAdmin(session);
        String normalized = role == null ? "" : role.trim().toUpperCase();
        userAdminRepository.addRole(id, normalized);
        return ApiResponse.ok(null, "Role assigned");
    }

    /**
     * Removes a role from a user through the admin API.
     *
     * @param id user id
     * @param session current HTTP session containing an ADMIN user
     * @param role role name to remove
     * @return empty success response
     */
    @RequestMapping(value = "/{id}/roles", method = RequestMethod.DELETE)
    public ApiResponse<Object> removeRole(
            @PathVariable("id") long id,
            HttpSession session,
            @RequestParam("role") String role) {
        requireAdmin(session);
        String normalized = role == null ? "" : role.trim().toUpperCase();
        userAdminRepository.removeRole(id, normalized);
        return ApiResponse.ok(null, "Role removed");
    }

    private AuthenticatedUser requireAdmin(HttpSession session) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        SessionUserUtil.requireRole(user, "ADMIN", "Only ADMIN can manage users");
        return user;
    }
}

