# Feature Overview

## `src/java/manga/service/NotificationService.java`

**What it does:** Service layer for creating notifications and checking duplicate notifications by user, type, and reference.

**Public methods:**
- `notifyUser(long userId, String type, String message, long referenceId, String referenceType)`: validates target user, type, and message, then creates a notification. Takes user id, notification type/message, reference id, and reference type. Returns `void`.
- `existsNotification(long userId, String type, long referenceId)`: checks whether a matching notification already exists. Takes user id, type, and reference id. Returns `boolean`.

**Business logic/rules:** User id must be positive. Type and message are required. Notification type is normalized to uppercase and message is trimmed before saving.

## `src/java/manga/repository/NotificationRepository.java`

**What it does:** Repository for reading, creating, updating, deleting, and mapping `Notification` table rows to `NotificationItem`.

**Public methods:**
- `create(long userId, String type, String message, long referenceId, String referenceType)`: creates a notification using default title and view URL. Takes user id, type, message, reference id, and reference type. Returns `void`.
- `create(long userId, String type, String title, String message, String viewUrl, long referenceId, String referenceType)`: creates a notification with explicit title and view URL. Returns `void`.
- `listByUser(long userId)`: lists up to 100 notifications for a user. Takes user id. Returns `List<NotificationItem>`.
- `listByUser(long userId, int limit)`: lists notifications for a user with a row limit. Returns `List<NotificationItem>`.
- `unreadCount(long userId)`: counts unread notifications for a user. Returns `int`.
- `markRead(long userId, long id)`: marks one notification as read if owned by the user. Returns `void`.
- `markUnread(long userId, long id)`: marks one notification as unread if owned by the user. Returns `void`.
- `delete(long userId, long id)`: deletes one notification if owned by the user. Returns `void`.
- `markAllRead(long userId)`: marks all unread notifications for a user as read. Returns `void`.
- `exists(long userId, String type, long referenceId)`: checks duplicate notification by user, type, and reference id. Returns `boolean`.
- `viewUrlByUser(long userId, long id)`: gets the target URL for a notification owned by the user. Returns `String` or `null`.

**Business logic/rules:** All notification mutations are scoped by `userId`. `referenceId <= 0` is stored as SQL `NULL`. Default titles are derived from notification type keywords. Default view URLs are derived from reference type, with unsupported cases falling back to `null` or `/main/notifications`. `SERIES_DEADLINE_UPDATED` redirects to `/main/chapters?seriesId={referenceId}`.

## `src/java/manga/web/NotificationViewAdvice.java`

**What it does:** Adds notification data to every Spring MVC controller model so the shared header can render badge count and dropdown items.

**Public methods:**
- `unreadCount(HttpSession session)`: adds `headerUnreadNotificationCount`. Takes current session. Returns unread count as `int`, or `0` when unauthenticated.
- `latestNotifications(HttpSession session)`: adds `headerNotifications`. Takes current session. Returns `List<NotificationItem>`, or an empty list when unauthenticated.

**Business logic/rules:** Only sessions containing an `AuthenticatedUser` get notification data. The dropdown uses the repository default list limit.

## `src/java/manga/controller/web/NotificationWebController.java`

**What it does:** Web controller for the full notification page and notification click redirects.

**Public methods:**
- `list(HttpSession session, Model model)`: loads notification list and unread count for the current user. Returns JSP view name `notification/list`.
- `markRead(long id, HttpSession session)`: marks one notification as read from a form post. Returns redirect to `/main/notifications`.
- `click(long id, HttpSession session)`: marks a notification as read and redirects to its supported `viewUrl`. Returns `RedirectView`.
- `markAllRead(HttpSession session)`: marks all current user notifications as read. Returns redirect to `/main/notifications`.

**Business logic/rules:** Requires `AUTH_USER` in session. Click redirects only to supported `/main/...` paths; otherwise it falls back to `/main/notifications`. Query strings are ignored for route allow-list matching, so `/main/chapters?seriesId=...` is allowed through `/main/chapters`.

## `src/java/manga/controller/api/NotificationApiController.java`

**What it does:** JSON API controller for notification list, read/unread toggle, delete, and mark-all-read actions.

**Public methods:**
- `list(HttpSession session)`: returns notifications for the current user as JSON text. Returns `String`.
- `markRead(long id, HttpSession session)`: marks one notification as read. Returns JSON success `String`.
- `markUnread(long id, HttpSession session)`: marks one notification as unread. Returns JSON success `String`.
- `delete(long id, HttpSession session)`: deletes one notification owned by the current user. Returns JSON success `String`.
- `markAllRead(HttpSession session)`: marks all current user notifications as read. Returns JSON success `String`.

**Business logic/rules:** Uses `SessionUserUtil.requireUser` for authentication. All operations are scoped to the current user's id. JSON is built manually with escaping and includes compatibility fields such as `recipientId`, `body`, `relatedEntityId`, and `relatedEntityType`.

## `src/java/manga/controller/web/AuthController.java`

**What it does:** Web controller for login, logout, and test user switching.

**Public methods:**
- `loginPage()`: displays the login form. Returns `auth/login`.
- `login(String username, String password, HttpServletRequest request, Model model)`: authenticates credentials and stores `AUTH_USER` in session. Returns redirect to `/main/dashboard` or `auth/login` on error.
- `switchRole(String username, String back, HttpServletRequest request)`: switches session to another active user for testing. Returns redirect to `/main/dashboard`.
- `logout(HttpServletRequest request)`: invalidates session. Returns redirect to `/login`.

**Business logic/rules:** Login errors preserve the submitted username and display the auth error. `switchRole` currently ignores `back` and always returns dashboard.

## `src/java/manga/controller/api/AuthApiController.java`

**What it does:** JSON API controller for auth actions, session identity, current roles, and switch-role list data.

**Public methods:**
- `login(String username, String password, HttpSession session)`: authenticates user and stores `AUTH_USER`. Returns `ApiResponse<Map<String,Object>>` with id, username, fullName, and roles.
- `logout(HttpSession session)`: invalidates current session. Returns `ApiResponse<Object>`.
- `me(HttpSession session)`: returns current user profile and role names. Returns `ApiResponse<Map<String,Object>>`.
- `roles(HttpSession session)`: returns display-ready role switch items for the current user. Returns `ApiResponse<List<Map<String,Object>>>`.
- `switchList(HttpSession session)`: returns active users and roles for the header switch-role dropdown. Returns `ApiResponse<List<Map<String,Object>>>`.

**Business logic/rules:** All non-login endpoints require `AUTH_USER`. Role labels are loaded from `UserAdminRepository`.

## `src/java/manga/service/AuthService.java`

**What it does:** Authenticates users, supports test account creation, and supports switching to active users.

**Public methods:**
- `login(String username, String password)`: validates credentials and account status. Takes username and password. Returns `AuthenticatedUser`.
- `switchUserForTesting(String username)`: loads an active user by username for session switching. Returns `AuthenticatedUser`.

**Business logic/rules:** Password is compared directly to `passwordHash`. Accounts must be `ACTIVE`. Known test usernames can auto-create test users and assistant assignments. Invalid login uses a generic credential error; inactive accounts get a specific error.

## `src/java/manga/web/interceptor/AuthInterceptor.java`

**What it does:** Spring MVC interceptor that protects authenticated routes, prevents cached authenticated pages, and enforces URL-based RBAC.

**Public methods:**
- `preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)`: checks whether the request can proceed. Returns `boolean`.
- `postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)`: no-op after controller handling. Returns `void`.
- `afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)`: no-op after request completion. Returns `void`.

**Business logic/rules:** Login, logout, switch-role, assets, and redirect JSP are public. Unauthenticated API calls receive JSON 401; unauthenticated web pages redirect to `/login`. Forbidden API calls receive JSON 403; forbidden web pages redirect to dashboard. Admin-only paths include `/api/v1/users` and `/main/users`. Other page groups are allowed by role according to their feature area.

## `src/java/manga/repository/UserRepository.java`

**What it does:** Repository for loading authenticated users and finding users by role.

**Public methods:**
- `findByUsername(String username)`: loads a user by username and populates roles. Returns `AuthenticatedUser` or `null`.
- `findByRole(String roleName)`: lists users assigned to a role. Returns `List<Map<String,Object>>` with id, username, and fullName.

**Business logic/rules:** Roles are loaded from `UserRole` joined with `Role`. `findByRole` does not filter inactive users.

## `src/java/manga/repository/UserAdminRepository.java`

**What it does:** Repository for admin user management, role assignment, role switch data, status changes, and user lookup helpers.

**Public methods:**
- `listUsers()`: lists all users with roles. Returns `List<Map<String,Object>>`.
- `listActiveUsersForSwitch()`: lists active users with roles and switch labels. Returns `List<Map<String,Object>>`.
- `getUser(long id)`: loads one user with roles. Returns `Map<String,Object>` or `null`.
- `createUser(String username, String passwordHash, String fullName, String email)`: validates and creates an active user. Returns generated user id as `long`.
- `updateUser(long id, String fullName, String email)`: updates full name and email. Returns `void`.
- `updateStatus(long id, String status)`: updates status to `ACTIVE` or `INACTIVE`. Returns `void`.
- `addRole(long userId, String roleName)`: assigns a valid role if not already assigned. Returns `void`.
- `removeRole(long userId, String roleName)`: removes a role from a user. Returns `void`.
- `listRoles(long userId)`: lists role names for a user. Returns `List<String>`.
- `listRoleSwitchItems(long userId)`: lists current user's roles with display labels and role indexes. Returns `List<Map<String,Object>>`.
- `hasAnyAdmin()`: checks whether any admin role exists. Returns `boolean`.
- `hasRole(long userId, String roleName)`: checks whether a user has a role. Returns `boolean`.
- `countUsersWithRole(String roleName)`: counts users with a role. Returns `int`.
- `getFullNameById(long userId)`: gets a user's full name. Returns `String` or `null`.

**Business logic/rules:** Username, password, full name, and valid email are required. Password must be at least 5 characters. Username and email must be unique. Status is limited to `ACTIVE` or `INACTIVE`. Valid roles are `ADMIN`, `MANGAKA`, `ASSISTANT`, `TANTOU_EDITOR`, and `EDITORIAL_BOARD`. Only one admin account is allowed; the last admin cannot be deactivated or lose admin role. Role combinations are validated by `RoleCombinationValidator`.

## `src/java/manga/controller/api/UserApiController.java`

**What it does:** Admin-only JSON API for user CRUD-style actions, status updates, and role assignment.

**Public methods:**
- `list(HttpSession session)`: returns all users. Returns `ApiResponse<List<Map<String,Object>>>`.
- `create(HttpSession session, String username, String password, String fullName, String email)`: creates a user and returns the created record. Returns `ApiResponse<Map<String,Object>>`.
- `detail(long id, HttpSession session)`: returns one user by id. Returns `ApiResponse<Map<String,Object>>`.
- `update(long id, HttpSession session, String fullName, String email)`: updates a user's basic info. Returns `ApiResponse<Object>`.
- `patchStatus(long id, HttpSession session, String status)`: updates user status. Returns `ApiResponse<Object>`.
- `addRole(long id, HttpSession session, String role)`: assigns a role. Returns `ApiResponse<Object>`.
- `removeRole(long id, HttpSession session, String role)`: removes a role. Returns `ApiResponse<Object>`.

**Business logic/rules:** Every endpoint requires an authenticated admin. Status input is normalized and limited to `ACTIVE` or `INACTIVE`. Role input is normalized before repository validation.

## `src/java/manga/common/util/RoleCombinationValidator.java`

**What it does:** Static validator for allowed role combinations on user accounts.

**Public methods:**
- `validate(List<String> roles)`: validates a role list. Takes a list of role names. Returns `void` or throws `IllegalArgumentException`.

**Business logic/rules:** At least one nonblank role is required. `MANGAKA` and `ASSISTANT` must be single-role accounts. `ADMIN` cannot be combined with other roles. The only valid dual-role combination is `TANTOU_EDITOR` plus `EDITORIAL_BOARD`.

## `src/java/manga/common/util/SessionUserUtil.java`

**What it does:** Utility for reading the authenticated user from session and enforcing required roles.

**Public methods:**
- `requireUser(HttpSession session)`: returns `AUTH_USER` if present and valid. Returns `AuthenticatedUser` or throws `IllegalStateException`.
- `requireRole(AuthenticatedUser user, String role, String message)`: requires a role on the given user. Returns `void` or throws `IllegalArgumentException`.

**Business logic/rules:** Only `AuthenticatedUser` stored in `AUTH_USER` is accepted as logged in. Missing role uses the provided error message.

## `web/WEB-INF/jsp/common/header.jsp`

**What it does:** Shared authenticated layout header/sidebar with role-based navigation, page title, notification dropdown, account display, logout, and switch-role UI.

**Public methods:** No Java public methods. Client-side functions exposed in inline scripts:
- `closeAllNotiMenus()`: closes all notification action menus. Takes no arguments. Returns `void`.
- `toggleNotiMenu(btn)`: opens/closes a notification menu and sets the read/unread label from the item's read state. Takes the clicked button element. Returns `void`.
- `deleteNoti(id)`: deletes a notification through the notification API and removes matching DOM items. Takes notification id. Returns `void`.
- `toggleReadNoti(id, isRead)`: toggles one notification read state through the API and reloads on success. Takes notification id and current read state. Returns `void`.
- `timeAgo(dateStr)`: converts a timestamp string to relative text. Returns `String`.
- `renderNotificationTimes()`: updates all `.noti-time` elements with relative time. Returns `void`.

**Business logic/rules:** Role flags determine which sidebar links show. Page title is derived from current URI. Notifications use `data-is-read` and unread dot/background classes. Clicking a notification goes through `/main/notifications/{id}/click`. Switch-role fetches `/api/v1/auth/switch-list`, groups users into Admin, Mangaka, Assistant, Tantou Editor, and Board Member, displays full names, and links to `/main/switch-role?username=...`.

## `web/assets/role-assignment.js`

**What it does:** Client-side role checkbox rule enforcement for user create/edit role grids.

**Public methods:** No exported public methods. Internal functions:
- `findRole(root, role)`: finds a role checkbox in a role grid. Returns an input element or `null`.
- `enforceRoleRules(root, changed)`: applies role combination rules after a checkbox change. Returns `void`.
- `bindRoleForms()`: binds change listeners to role grids and applies initial enforcement. Returns `void`.

**Business logic/rules:** In create-user role grids, `ADMIN` is unchecked and disabled. `MANGAKA` and `ASSISTANT` are single-role only. `TANTOU_EDITOR` and `EDITORIAL_BOARD` can be selected together, but selecting either clears `MANGAKA` and `ASSISTANT`.
