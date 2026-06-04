# CAO Feature Map

Scope: frontend-owned feature areas only: Notification, Auth, User Management, Common Layout, and RBAC / role-based UI hiding.

Scanned root: `SWP391_manga-creation-publishing-management-system_delete`

## 1. Notification

### JSP files

- `web/WEB-INF/jsp/common/header.jsp` - Header notification dropdown, unread badge, Mark all read form, notification item click links, 3-dot item action menu, relative-time rendering script.
- `web/WEB-INF/jsp/notification/list.jsp` - Full notification page showing unread count, Mark all read button, notification rows, and per-item 3-dot actions.
- `web/WEB-INF/jsp/common/footer.jsp` - Closes shared shell after pages including notification list.

### Java files

Controllers:

- `src/java/manga/controller/web/NotificationWebController.java` - Web controller for `/main/notifications`; lists notifications, marks read, handles click redirect, and marks all read.
- `src/java/manga/controller/api/NotificationApiController.java` - JSON API for notification list, read/unread toggle, delete, and mark-all-read actions.

Services:

- `src/java/manga/service/NotificationService.java` - Service helper used by other modules to create user notifications.

Repositories:

- `src/java/manga/repository/NotificationRepository.java` - Reads, creates, counts, updates, deletes notifications, and resolves notification `viewUrl`.
- `src/java/manga/web/NotificationViewAdvice.java` - Adds `headerUnreadNotificationCount` and `headerNotifications` to all views for the header dropdown.

### JS files

- No standalone notification asset file in `web/assets/`.
- Inline JS in `web/WEB-INF/jsp/common/header.jsp` around lines 342-435:
  - `closeAllNotiMenus()`
  - `toggleNotiMenu(btn)`
  - `deleteNoti(id)`
  - `toggleReadNoti(id, isRead)`
  - `timeAgo(dateStr)`
  - `renderNotificationTimes()`

### CSS

External CSS in `web/assets/styles.css`:

- Notification dropdown container and badge: `.notify-switcher`, `.notify-toggle`, `.notify-count`, `.notify-menu` around lines 1268-1333.
- Dropdown header and Mark all read form: `.notify-menu-head`, `.notify-mark-all-form`, `.notify-mark-all-form button` around lines 1334-1369.
- Dropdown item read/unread states: `.notify-item`, `.noti-item.unread`, `.noti-item.read`, `.noti-title`, `.noti-message`, `.noti-time`, `.noti-dot` around lines 1380-1454.
- See-all link: `.notify-see-all` around lines 1487-1497.
- Full notification page rows: `.notification-list`, `.notification-row`, `.notification-row.is-unread`, `.notification-row.is-read`, `.notification-title`, `.notification-time`, `.notification-actions` around lines 1980-2058.

Inline CSS in `web/WEB-INF/jsp/common/header.jsp`:

- 3-dot popup menu item styling around lines 151-172: `.noti-actions .noti-menu-item`, hover state, `.noti-menu-delete`, `.noti-menu-toggle`.
- Inline popup container style on dropdown item menu around line 215: absolute positioned, `right:0`, `border-radius:8px`, `box-shadow:0 4px 12px rgba(0,0,0,0.15)`, `min-width:160px`, `padding:8px 0`.
- Inline 3-dot button style around lines 209-214: `btn btn-sm p-0 text-muted noti-menu-btn` plus `background:none; border:none; font-size:16px; line-height:1;`.

### Spring MVC URL mappings

Web:

- `GET /main/notifications` - `NotificationWebController.list`; renders `notification/list.jsp`.
- `POST /main/notifications/{id}/read` - `NotificationWebController.markRead`; legacy/form read action.
- `GET /main/notifications/{id}/click` - `NotificationWebController.click`; marks read, validates `viewUrl`, redirects to destination or `/main/notifications`.
- `POST /main/notifications/mark-all-read` - `NotificationWebController.markAllRead`; used by dropdown and full page Mark all read forms.

API:

- `GET /api/v1/notifications` - list notifications for current user.
- `PATCH /api/v1/notifications/{id}/read` - mark one notification read.
- `PATCH /api/v1/notifications/{id}/unread` - mark one notification unread.
- `DELETE /api/v1/notifications/{id}` - delete one notification.
- `POST /api/v1/notifications/mark-all-read` - mark all notifications read.

### Button / action locations

- `Mark all read` in dropdown: `web/WEB-INF/jsp/common/header.jsp` around lines 186-189.
  - HTML: form action `${ctx}/main/notifications/mark-all-read`, class `notify-mark-all-form`.
  - Button CSS: `.notify-mark-all-form button` in `web/assets/styles.css` around lines 1359-1369; transparent background, blue text `#0d6efd`, 12px bold.
- Notification bell button: `web/WEB-INF/jsp/common/header.jsp` around lines 175-183.
  - CSS classes: `notify-switcher`, `notify-toggle`, `notify-icon`, `notify-count noti-badge`.
- Notification row click: `web/WEB-INF/jsp/common/header.jsp` around lines 198-202 and `web/WEB-INF/jsp/notification/list.jsp` around lines 34-41.
  - CSS classes: `notify-item/noti-item`, `notification-row/noti-item`, `is-read/read`, `is-unread/unread`.
- 3-dot item menu in dropdown: `web/WEB-INF/jsp/common/header.jsp` around lines 207-223.
  - Button class: `btn btn-sm p-0 text-muted noti-menu-btn`.
  - Menu class: `noti-menu`; item classes `noti-menu-item noti-menu-delete` and `noti-menu-item noti-menu-toggle`.
- 3-dot item menu on full page: `web/WEB-INF/jsp/notification/list.jsp` around lines 48-64.
  - Button class: `btn btn-sm p-0 text-muted noti-menu-btn`; popup uses Bootstrap-style button classes `btn btn-sm w-100 text-start px-3 py-2`.
- `View all notifications`: `web/WEB-INF/jsp/common/header.jsp` around lines 231-232.
  - CSS classes: `dropdown-item text-center text-primary fw-semibold py-2 notify-see-all`.

## 2. Auth

### JSP files

- `web/WEB-INF/jsp/auth/login.jsp` - Login page and credential form posting to `/main/login`.
- `web/WEB-INF/jsp/common/header.jsp` - Authenticated shell includes logout link, session JS include, role switcher, and current user display.
- `web/WEB-INF/jsp/common/footer.jsp` - Closes authenticated shell.
- `web/login.jsp`, `web/logout.jsp`, `web/switch-role.jsp`, `web/redirect.jsp` - Legacy/static entry JSPs still present at web root.

### Java files

Controllers:

- `src/java/manga/controller/web/AuthController.java` - Web login, logout, and dev/test switch-role actions.
- `src/java/manga/controller/web/MainController.java` - `/main/login`, `/main/logout`, and `/main/switch-role` delegate to `AuthController`; also routes `/main/dashboard`.
- `src/java/manga/controller/api/AuthApiController.java` - JSON login/logout/me/current-role/switch-list API.

Services:

- `src/java/manga/service/AuthService.java` - Authenticates username/password, creates testing accounts for switch role, and loads active users.

Repositories:

- `src/java/manga/repository/UserRepository.java` - Loads authenticated user and roles by username.
- `src/java/manga/repository/UserAdminRepository.java` - Supports role-switch list and testing account setup.

Interceptors / utilities:

- `src/java/manga/web/interceptor/AuthInterceptor.java` - Enforces session presence, prevents cached authenticated pages, returns API 401/403, and applies role-based route access.
- `src/java/manga/common/util/SessionUserUtil.java` - Shared API helper for requiring an authenticated session user and role.

### JS files

- `web/assets/auth-session.js` - On bfcache `pageshow`, calls `/api/v1/auth/me`; redirects to `/login` if session is invalid.
- Inline JS in `web/WEB-INF/jsp/common/header.jsp` around lines 242-294 - Loads `/api/v1/auth/switch-list` and builds switch-role menu links.

### CSS

External CSS in `web/assets/styles.css`:

- Login page visual system: `.login-page`, `.login-wrap`, `.login-art`, `.login-panel`, `.login-form`, `.login-input`, `.login-submit` around the login section and theme overrides around lines 2382 onward.
- Logout link: `.logout-link` around lines 1690-1717.
- Role switcher dropdown: `.role-switcher`, `.role-switch-menu`, `.role-switch-item`, `.role-switch-item.active` around lines 1073-1135.

Inline CSS:

- `web/WEB-INF/jsp/auth/login.jsp` version label around line 60: fixed bottom-right `v1.0`.

### Spring MVC URL mappings

Web:

- `GET /login` - `AuthController.loginPage`; renders `auth/login.jsp`.
- `POST /login` - `AuthController.login`; creates `AUTH_USER` and redirects to `/main/dashboard`.
- `GET /logout` - `AuthController.logout`; invalidates session and redirects to `/login`.
- `GET /switch-role?username=...` - `AuthController.switchRole`; switches session user for testing.
- `GET /main/login` - `MainController.loginPage`; delegates to `AuthController.loginPage`.
- `POST /main/login` - `MainController.login`; delegates to `AuthController.login`.
- `GET /main/logout` - `MainController.logout`; delegates to `AuthController.logout`.
- `GET /main/switch-role?username=...` - `MainController.switchRole`; delegates to `AuthController.switchRole`.

API:

- `POST /api/v1/auth/login` - API login and session creation.
- `POST /api/v1/auth/logout` - API logout and session invalidation.
- `GET /api/v1/auth/me` - Current authenticated user; used by `auth-session.js`.
- `GET /api/v1/auth/roles` - Current user's role switch items.
- `GET /api/v1/auth/switch-list` - Active users for header switch-role menu.

### Button / action locations

- `Sign In`: `web/WEB-INF/jsp/auth/login.jsp` around line 56.
  - CSS class: `login-submit`; styled in `web/assets/styles.css` and theme override around line 2363.
- `Logout`: `web/WEB-INF/jsp/common/header.jsp` around line 301.
  - CSS class: `logout-link`; red pill button styling around lines 1690-1717 in `styles.css`.
- `Switch role`: `web/WEB-INF/jsp/common/header.jsp` around lines 242-244.
  - CSS classes: `role-switcher`, `user-sub switch-toggle`, `role-switch-menu`; menu items generated with `role-switch-item` around line 279.

## 3. User Management

### JSP files

- `web/WEB-INF/jsp/user/list.jsp` - Admin user list, New User button, status toggle, edit link, role chips, remove role, and add-role dropdown.
- `web/WEB-INF/jsp/user/form.jsp` - Admin create/edit form, role assignment checkboxes, Create/Update/Cancel actions.
- `web/WEB-INF/jsp/common/header.jsp` - Sidebar Users navigation item shown only for admins.
- `web/WEB-INF/jsp/common/footer.jsp` - Closes shared shell.

### Java files

Controllers:

- `src/java/manga/controller/web/ModuleWebController.java` - Web user management under `/main/users`: list, new, edit, create, update, status, add role, remove role.
- `src/java/manga/controller/api/UserApiController.java` - Admin-only JSON CRUD/status/role API under `/api/v1/users`.

Services:

- `src/java/manga/service/AuthService.java` - Supports testing-account creation and switch-role data used by user/session workflows.
- `src/java/manga/service/NotificationService.java` - Sends account created/status/role notifications from user management actions.

Repositories:

- `src/java/manga/repository/UserAdminRepository.java` - Admin user CRUD, status updates, role assignment/removal, role lists, active switch users.
- `src/java/manga/repository/UserRepository.java` - Auth user lookup and role loading; also finds users by role.
- `src/java/manga/common/util/RoleCombinationValidator.java` - Validates allowed role combinations when assigning roles.

### JS files

- `web/assets/role-assignment.js` - Enforces frontend role checkbox rules on `.role-choice-grid` and `.role-check-grid`.

### CSS

External CSS in `web/assets/styles.css`:

- General buttons: `.btn`, `.btn.primary`, `.btn.small` around lines 744-760 and additional theme overrides around lines 998-1012 and 2351-2363.
- User role chips and role removal: `.role-list`, `.role-chip-form`, `.role-chip`, `.role-remove`, `.role-cell` around lines 1719-1773.
- Add role popup: `.add-role-cell`, `.add-role-panel`, `.add-role-summary`, `.role-check-form`, `.role-check-grid`, `.role-check` around lines 1792-1858.
- Row actions and status buttons: `.row-actions`, `.status-badge`, `.status-active`, `.status-inactive`, `.btn.danger-soft`, `.btn.success-soft` around lines 1859-1900.
- User form role choices: `.user-form`, `.form-grid.two-col`, `.form-row`, `.role-choice-grid`, `.role-choice`, `.form-actions` around lines 1902-1960.

### Spring MVC URL mappings

Web:

- `GET /main/users` - `ModuleWebController.users`; admin user list.
- `GET /main/users/new` - `ModuleWebController.userNew`; create form.
- `GET /main/users/{id}/edit` - `ModuleWebController.userEdit`; edit form.
- `POST /main/users/create` - `ModuleWebController.userCreate`; create user and assign roles.
- `POST /main/users/{id}/update` - `ModuleWebController.userUpdate`; update basic fields.
- `POST /main/users/{id}/status` - `ModuleWebController.userStatus`; activate/deactivate.
- `POST /main/users/{id}/roles` - `ModuleWebController.userRole`; add one or more roles.
- `POST /main/users/{id}/roles/remove` - `ModuleWebController.userRoleRemove`; remove role.

API:

- `GET /api/v1/users` - list users.
- `POST /api/v1/users` - create user.
- `GET /api/v1/users/{id}` - user detail.
- `PUT /api/v1/users/{id}` - update user.
- `PATCH /api/v1/users/{id}/status` - update status.
- `POST /api/v1/users/{id}/roles` - add role.
- `DELETE /api/v1/users/{id}/roles` - remove role.

### Button / action locations

- `+ New User`: `web/WEB-INF/jsp/user/list.jsp` around lines 19-22.
  - CSS class: `btn primary`; CSS around lines 744-755 and theme override around line 2363.
- `Add` role dropdown: `web/WEB-INF/jsp/user/list.jsp` around lines 77-85.
  - CSS classes: `btn small add-role-summary`, `role-check-form`, `role-check-grid compact`, `role-check`, `btn small primary`.
- Role remove `x`: `web/WEB-INF/jsp/user/list.jsp` around lines 55-60.
  - CSS classes: `role-chip-form`, `role-chip`, `role-remove`.
- `Edit`: `web/WEB-INF/jsp/user/list.jsp` around line 92.
  - CSS class: `btn small`.
- `Deactivate` / `Activate`: `web/WEB-INF/jsp/user/list.jsp` around lines 94-97.
  - CSS classes: `btn small danger-soft` for active users, `btn small success-soft` for inactive users.
- `Update User`: `web/WEB-INF/jsp/user/form.jsp` around lines 37-39.
  - CSS class: `btn primary`.
- `Create User`: `web/WEB-INF/jsp/user/form.jsp` around lines 85-87.
  - CSS class: `btn primary`.
- `Cancel`: `web/WEB-INF/jsp/user/form.jsp` around lines 38-39 and 86-87.
  - CSS class: `btn`.

## 4. Common Layout

### JSP files

- `web/WEB-INF/jsp/common/header.jsp` - Shared authenticated layout start: app shell, sidebar, top header, role pills, notification dropdown, user controls, sidebar persistence scripts.
- `web/WEB-INF/jsp/common/footer.jsp` - Shared authenticated layout close: closes `<main>`, `<section>`, and app shell wrapper opened by header.
- `web/WEB-INF/jsp/common/error.jsp` - Error page includes common header/footer.
- `web/WEB-INF/jsp/dashboard/index.jsp` - Dashboard includes common header/footer and is the default authenticated landing page.
- Feature JSPs such as `web/WEB-INF/jsp/user/list.jsp`, `web/WEB-INF/jsp/user/form.jsp`, and `web/WEB-INF/jsp/notification/list.jsp` include common header/footer.

### Java files

Controllers:

- `src/java/manga/controller/web/MainController.java` - `/main` entry routes and common top-level page routing.
- `src/java/manga/controller/web/DashboardController.java` - Renders dashboard content for authenticated users.

Services:

- No layout-only service; layout data mainly comes from session and `NotificationViewAdvice`.

Repositories:

- `src/java/manga/web/NotificationViewAdvice.java` - Supplies header notification model attributes across pages.
- `src/java/manga/repository/NotificationRepository.java` - Provides header notification data for advice.

### JS files

- `web/assets/auth-session.js` - Included by header for authenticated session validation on cached page restores.
- Inline sidebar script in `web/WEB-INF/jsp/common/header.jsp` around lines 309-339 - Persists sidebar pinned/collapsed state in `localStorage`.
- Inline role switcher script in `web/WEB-INF/jsp/common/header.jsp` around lines 242-294.
- Inline notification script in `web/WEB-INF/jsp/common/header.jsp` around lines 342-435.

### CSS

External CSS in `web/assets/styles.css`:

- App shell, sidebar, and topbar: `.app-shell`, `.side-nav`, `.main-shell`, `.top-shell`, `.page-wrap`, `.side-brand`, `.brand-icon`, `.brand-name`, `.brand-sub`.
- Sidebar nav: `.nav-item`, `.nav-item.active`, `.nav-item:hover`, `.nav-label` around lines 89-100 and refined around lines 2279-2293 and 2729-2743.
- Sidebar pin/collapse behavior: `.sidebar-pin`, `.pin-icon`, `.app-shell.sidebar-pinned`, `.app-shell.sidebar-hover-suspended` around lines 2645-2790.
- Header role pills/avatar: `.role-pill`, `.avatar.role-*`, `.role-pill.role-*` around lines 143 and 1043-1069.
- Buttons/cards/tables shared by pages: `.btn`, `.section-card`, `.section-head`, `.page-title`, `.page-sub`, `.data-table`.

### Spring MVC URL mappings

- `GET /main` - `MainController` root redirect/landing route.
- `GET /main/dashboard` - `MainController.dashboard` or dashboard routing; renders dashboard.
- `GET /` and `GET /dashboard` - `DashboardController.dashboard` mappings.
- Layout itself is included by JSPs; it has no dedicated controller mapping.

### Button / action locations

- Brand/dashboard link: `web/WEB-INF/jsp/common/header.jsp` around lines 66-72.
  - CSS classes: `side-brand`, `brand-icon`, `brand-name`, `brand-sub`.
- Sidebar collapse button: `web/WEB-INF/jsp/common/header.jsp` around lines 74-78.
  - CSS class: `sidebar-pin`; icon class `pin-icon`; label class `pin-label`.
- Sidebar nav buttons: `web/WEB-INF/jsp/common/header.jsp` around lines 80-122.
  - CSS classes: `nav-item nav-dashboard`, `nav-proposals`, `nav-series`, `nav-tasks`, `nav-decisions`, `nav-manuscript-review`, `nav-ranking`, `nav-users`, with conditional `active`.
- Top role pills: `web/WEB-INF/jsp/common/header.jsp` around lines 134-146.
  - CSS classes: `role-pill role-admin`, `role-mangaka`, `role-assistant`, `role-tantou`, `role-board`.

## 5. RBAC / Role-Based UI Hiding

### JSP files

- `web/WEB-INF/jsp/common/header.jsp` - Computes role flags and hides/shows sidebar items and role pills with JSTL conditions.
- `web/WEB-INF/jsp/user/list.jsp` - Admin-only page; UI hides role-add panel unless the current target user is eligible for valid dual-role additions and locks ADMIN role removal.
- `web/WEB-INF/jsp/user/form.jsp` - Hides ADMIN checkbox in create-user form.
- `web/WEB-INF/jsp/proposal/list.jsp` - Shows `+ New Proposal` and proposal actions based on role-derived model flags; included because `New Proposal` was requested as a button example.

### Java files

Controllers / interceptors:

- `src/java/manga/web/interceptor/AuthInterceptor.java` - Main request-level RBAC gate for `/main/users`, `/api/v1/users`, proposals, series/chapters, decisions, tasks, manuscripts.
- `src/java/manga/controller/web/ModuleWebController.java` - Performs admin checks for user management and role validation before mutation.
- `src/java/manga/controller/api/UserApiController.java` - Uses `SessionUserUtil.requireRole(..., "ADMIN", ...)` for admin-only API.
- `src/java/manga/controller/web/MainController.java` - Adds role flags such as `isMangaka`, `isTantou`, and `isBoard` for proposal/list/detail UI.
- `src/java/manga/controller/web/DashboardController.java` - Adds `roles` and dashboard model values for current user.

Services:

- `src/java/manga/service/AuthService.java` - Populates authenticated user's roles during login/switch.

Repositories:

- `src/java/manga/repository/UserRepository.java` - Loads roles into `AuthenticatedUser`.
- `src/java/manga/repository/UserAdminRepository.java` - Lists and validates role assignments; protects last admin role/status.
- `src/java/manga/common/util/RoleCombinationValidator.java` - Enforces valid role combinations.
- `src/java/manga/common/util/SessionUserUtil.java` - Shared require-user/require-role helper.

### JS files

- `web/assets/role-assignment.js` - Frontend role-combination enforcement:
  - `MANGAKA` and `ASSISTANT` are single-role only.
  - `TANTOU_EDITOR` + `EDITORIAL_BOARD` is the allowed dual-role pair.
  - `ADMIN` is disabled in create-user role grids if present.
- Inline header role switcher script in `web/WEB-INF/jsp/common/header.jsp` - Generates switch-role menu from `/api/v1/auth/switch-list`.

### CSS

External CSS in `web/assets/styles.css`:

- Sidebar RBAC-visible nav items reuse `.nav-item` and role-specific nav classes.
- Role pills/avatar colors: `.role-pill.role-admin`, `.role-mangaka`, `.role-assistant`, `.role-tantou`, `.role-board`, and matching `.avatar.role-*` around lines 1043-1069.
- Role management UI: `.role-chip.locked`, `.role-lock`, `.role-choice`, `.role-check`, `.btn.danger-soft`, `.btn.success-soft` around lines 1719-1960 and 2376-2382.

### Spring MVC URL mappings

RBAC-gated routes from `AuthInterceptor`:

- `/api/v1/users/**` - ADMIN only.
- `/main/users/**` - ADMIN only.
- `/main/proposals/**` - ADMIN, MANGAKA, TANTOU_EDITOR, EDITORIAL_BOARD.
- `/main/series/**` and `/main/chapters/**` - ADMIN, MANGAKA, TANTOU_EDITOR.
- `/main/decisions/**` - ADMIN, EDITORIAL_BOARD.
- `/main/ranking/**` - any authenticated user.
- `/main/tasks/**` - ADMIN, MANGAKA, ASSISTANT, TANTOU_EDITOR.
- `/main/manuscripts/**` - ADMIN, MANGAKA, TANTOU_EDITOR.

Relevant role/session APIs:

- `GET /api/v1/auth/me` - current session user and roles.
- `GET /api/v1/auth/roles` - current user's role switch items.
- `GET /api/v1/auth/switch-list` - active switch users for testing UI.

### Button / action locations

- Sidebar Proposals link: `web/WEB-INF/jsp/common/header.jsp` around lines 84-88.
  - Visibility: `${isAdmin || isMangaka || isTantou || isBoard}`.
  - CSS classes: `nav-item nav-proposals ...`.
- Sidebar Series link: `web/WEB-INF/jsp/common/header.jsp` around lines 90-94.
  - Visibility: `${isAdmin || isMangaka || isTantou}`.
  - CSS classes: `nav-item nav-series ...`.
- Sidebar Tasks link: `web/WEB-INF/jsp/common/header.jsp` around lines 96-100.
  - Visibility: `${isAdmin || isAssistant || isTantou}`.
  - CSS classes: `nav-item nav-tasks ...`.
- Sidebar Decisions link: `web/WEB-INF/jsp/common/header.jsp` around lines 102-106.
  - Visibility: `${isAdmin || isBoard}`.
  - CSS classes: `nav-item nav-decisions ...`.
- Sidebar Manuscript Reviews link: `web/WEB-INF/jsp/common/header.jsp` around lines 108-112.
  - Visibility: `${isAdmin || isTantou}`.
  - CSS classes: `nav-item nav-manuscript-review ...`.
- Sidebar Users link: `web/WEB-INF/jsp/common/header.jsp` around lines 119-123.
  - Visibility: `${isAdmin}`.
  - CSS classes: `nav-item nav-users ...`.
- `+ New Proposal`: `web/WEB-INF/jsp/proposal/list.jsp` around line 20.
  - CSS class: `btn primary`.
  - URL: `/main/proposals/create`.
- `+ New User`: `web/WEB-INF/jsp/user/list.jsp` around line 22.
  - Visibility by controller/interceptor: admin-only page.
  - CSS class: `btn primary`.
