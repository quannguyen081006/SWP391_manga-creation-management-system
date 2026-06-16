package manga.web.interceptor;

import manga.model.AuthenticatedUser;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class AuthInterceptor implements HandlerInterceptor {

        @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String context = request.getContextPath();

        // login -> pass
        if (uri.endsWith("/login") || uri.endsWith("/logout")
                || uri.contains("/assets/") || uri.endsWith("/redirect.jsp")) {
            return true;
        }

        //check session
        HttpSession session = request.getSession(false);
        AuthenticatedUser user = null;
        if (session != null && session.getAttribute("AUTH_USER") instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) session.getAttribute("AUTH_USER");
        }

        if (user == null) {
            // API callers get JSON status codes; browser pages redirect to login.
            if (uri.contains("/api/v1/")) {
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            } else {
                response.sendRedirect(context + "/login");
            }
            return false;
        }

        //dã login, check RBAC
        request.setAttribute("AUTH_USER_CHECKED", user);
        preventCachedAuthenticatedPage(response, uri);

        if (!isAllowed(user, uri, context)) {
            // BR-SYS RBAC: deny API with 403 but keep web users inside the app shell.
            if (uri.contains("/api/v1/")) {
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            } else {
                response.sendRedirect(context + "/main/dashboard");
            }
            return false;
        }

        return true;
    }

    private void preventCachedAuthenticatedPage(HttpServletResponse response, String uri) {
        if (uri.contains("/assets/")) {
            return;
        }
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"data\":null,\"errors\":[\"" + message + "\"]}");
        response.getWriter().flush();
    }

    private boolean isAllowed(AuthenticatedUser user, String uri, String context) {
        String path = uri.substring(context.length());
        if (path.startsWith("/api/v1/users")) {
            return user.hasRole("ADMIN");
        }
        if (path.startsWith("/main/users")) {
            return user.hasRole("ADMIN");
        }
        if (path.startsWith("/main/settings")) {
            return user.hasRole("ADMIN");
        }
        if (path.startsWith("/main/proposals")) {
            return user.hasRole("ADMIN") || user.hasRole("MANGAKA") || user.hasRole("TANTOU_EDITOR") || user.hasRole("EDITORIAL_BOARD");
        }
        if (path.startsWith("/main/series") || path.startsWith("/main/chapters")) {
            return user.hasRole("ADMIN") || user.hasRole("MANGAKA") || user.hasRole("TANTOU_EDITOR");
        }
        if (path.startsWith("/main/decisions")) {
            return user.hasRole("ADMIN") || user.hasRole("EDITORIAL_BOARD");
        }
        if (path.startsWith("/main/ranking")) {
            return true;
        }
        if (path.startsWith("/main/profile")) {
            return true;
        }
        if (path.startsWith("/main/tasks")) {
            return user.hasRole("ADMIN") || user.hasRole("MANGAKA") || user.hasRole("ASSISTANT") || user.hasRole("TANTOU_EDITOR");
        }
        if (path.startsWith("/main/manuscripts")) {
            return user.hasRole("ADMIN") || user.hasRole("MANGAKA") || user.hasRole("TANTOU_EDITOR");
        }
        return true;
    }

        @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

        @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }
}
