package manga.controller.api;

import manga.common.ApiResponse;
import manga.model.AuthenticatedUser;
import manga.repository.UserAdminRepository;
import manga.service.AuthService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes authentication JSON endpoints for login/logout, current-session
 * checks, and role switch data used by the shared header.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserAdminRepository userAdminRepository;

    /**
     * Authenticates credentials through the API and stores `AUTH_USER`.
     *
     * @param username submitted username
     * @param password submitted password
     * @param session current HTTP session
     * @return API response containing authenticated user summary
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ApiResponse<Map<String, Object>> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session) {
        AuthenticatedUser user = authService.login(username, password);
        session.setAttribute("AUTH_USER", user);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("roles", user.getRoles());

        return ApiResponse.ok(data, "Login successful");
    }

    /**
     * Logs out the current API session.
     *
     * @param session current HTTP session
     * @return empty success response
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ApiResponse<Object> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.ok(null, "Logout successful");
    }

    /**
     * Returns the currently authenticated user's profile and role names.
     *
     * @param session current HTTP session containing `AUTH_USER`
     * @return API response containing id, username, fullName, and roles
     */
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ApiResponse<Map<String, Object>> me(HttpSession session) {
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute("AUTH_USER");
        if (user == null) {
            throw new IllegalStateException("Unauthorized");
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("roles", currentRoleNames(user.getId()));
        return ApiResponse.ok(data, "Current user");
    }

    /**
     * Returns display labels for the current user's assigned roles.
     *
     * @param session current HTTP session containing `AUTH_USER`
     * @return API response containing role switch items
     */
    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public ApiResponse<List<Map<String, Object>>> roles(HttpSession session) {
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute("AUTH_USER");
        if (user == null) {
            throw new IllegalStateException("Unauthorized");
        }
        return ApiResponse.ok(userAdminRepository.listRoleSwitchItems(user.getId()), "Current user roles");
    }

    /**
     * Returns active users and roles for the header switch-role dropdown.
     *
     * @param session current HTTP session containing `AUTH_USER`
     * @return API response containing active switchable users
     */
    @RequestMapping(value = "/switch-list", method = RequestMethod.GET)
    public ApiResponse<List<Map<String, Object>>> switchList(HttpSession session) {
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute("AUTH_USER");
        if (user == null) {
            throw new IllegalStateException("Unauthorized");
        }
        return ApiResponse.ok(userAdminRepository.listActiveUsersForSwitch(), "Switch list");
    }

    private List<String> currentRoleNames(long userId) {
        List<String> roles = new ArrayList<String>();
        List<Map<String, Object>> items = userAdminRepository.listRoleSwitchItems(userId);
        for (Map<String, Object> item : items) {
            Object roleName = item.get("roleName");
            if (roleName != null) {
                roles.add(roleName.toString());
            }
        }
        return roles;
    }
}
