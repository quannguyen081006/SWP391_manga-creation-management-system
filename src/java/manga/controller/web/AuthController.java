package manga.controller.web;

import manga.model.AuthenticatedUser;
import manga.service.AuthService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles web authentication pages and session changes for login, logout,
 * and the development switch-role helper.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Displays the login form.
     *
     * @return login JSP view name
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Authenticates credentials and stores the user in the HTTP session.
     *
     * @param username submitted username
     * @param password submitted password
     * @param request current request used to create the session
     * @param model MVC model used to return validation errors
     * @return dashboard redirect on success, or login view on failure
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            Model model) {
        try {
            AuthenticatedUser user = authService.login(username, password);
            HttpSession session = request.getSession(true);
            // BR-SYS: authenticated state is centralized in the AUTH_USER session key.
            session.setAttribute("AUTH_USER", user);
            return "redirect:/main/dashboard";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            return "auth/login";
        }
    }

    /**
     * Switches the session to another active user for role testing.
     *
     * @param username username to switch into
     * @param back optional previous path retained for route compatibility
     * @param request current request used to create or update the session
     * @return dashboard redirect after the session user is replaced
     */
    @RequestMapping(value = "/switch-role", method = RequestMethod.GET)
    public String switchRole(
            @RequestParam("username") String username,
            @RequestParam(value = "back", required = false) String back,
            HttpServletRequest request) {
        AuthenticatedUser switched = authService.switchUserForTesting(username);
        HttpSession session = request.getSession(true);
        // This helper replaces the full authenticated principal; it does not merge roles.
        session.setAttribute("AUTH_USER", switched);

        return "redirect:/main/dashboard";
    }

    /**
     * Logs out by invalidating the current HTTP session.
     *
     * @param request current request containing the optional session
     * @return redirect to the login page
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

}
