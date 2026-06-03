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
 * Controller xu ly dang nhap, dang xuat va switch user trong moi truong dev.
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Hien thi form dang nhap.
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Xac thuc user va tao session dang nhap.
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
            session.setAttribute("AUTH_USER", user);
            return "redirect:/main/dashboard";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            return "auth/login";
        }
    }

    /**
     * Chuyen nhanh sang user khac cho qua trinh test role.
     */
    @RequestMapping(value = "/switch-role", method = RequestMethod.GET)
    public String switchRole(
            @RequestParam("username") String username,
            @RequestParam(value = "back", required = false) String back,
            HttpServletRequest request) {
        AuthenticatedUser switched = authService.switchUserForTesting(username);
        HttpSession session = request.getSession(true);
        session.setAttribute("AUTH_USER", switched);

        return "redirect:/main/dashboard";
    }

    /**
     * Huy session hien tai va quay ve trang dang nhap.
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
