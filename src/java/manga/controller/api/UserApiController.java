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
 * REST controller cho admin quan ly user, status va role.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

    @Autowired
    private UserAdminRepository userAdminRepository;

    /**
     * Tra ve danh sach user cho admin.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ApiResponse<List<Map<String, Object>>> list(HttpSession session) {
        requireAdmin(session);
        return ApiResponse.ok(userAdminRepository.listUsers(), "User list");
    }

    /**
     * Tao user moi qua API admin.
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
     * Tra ve chi tiet mot user cho admin.
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
     * Cap nhat thong tin co ban cua user qua API.
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
     * Cap nhat trang thai ACTIVE/INACTIVE cua user.
     */
    @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
    public ApiResponse<Object> patchStatus(
            @PathVariable("id") long id,
            HttpSession session,
            @RequestParam("status") String status) {
        requireAdmin(session);
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }
        userAdminRepository.updateStatus(id, normalized);
        return ApiResponse.ok(null, "User status updated");
    }

    /**
     * Gan role moi cho user qua API admin.
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
     * Go role khoi user qua API admin.
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

