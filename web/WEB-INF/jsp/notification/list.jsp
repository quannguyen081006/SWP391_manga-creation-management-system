<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Notifications</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css" />
</head>
<body>
<jsp:include page="../common/header.jsp" />

<%-- Notification page header: unread count and mark-all action. --%>
<div class="section-head">
    <div>
        <h2 class="page-title">Notifications</h2>
        <p class="page-sub">${unreadCount} unread notification${unreadCount == 1 ? '' : 's'}</p>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/main/notifications/mark-all-read">
        <button class="btn primary" type="submit" ${unreadCount == 0 ? 'disabled' : ''}>Mark all read</button>
    </form>
</div>

<%-- Notification list: each row marks read and redirects through /click. --%>
<div class="notification-list">
    <c:choose>
        <c:when test="${empty notifications}">
            <div class="section-card">
                <p class="muted">No notifications yet.</p>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach items="${notifications}" var="n">
                <div class="notification-row noti-item ${n.read ? 'is-read read' : 'is-unread unread'}" data-noti-id="${n.id}" style="position:relative;">
                    <a href="${pageContext.request.contextPath}/main/notifications/${n.id}/click" class="notification-main text-decoration-none" style="flex:1; min-width:0;">
                        <div class="notification-row-head">
                            <span class="notification-title">${empty n.title ? n.type : n.title}</span>
                            <span class="notification-time noti-time" data-time="${n.createdAt}"></span>
                        </div>
                        <p>${n.message}</p>
                        <c:if test="${not empty n.referenceType}">
                            <span class="status-chip status-draft">${n.referenceType} #${n.referenceId}</span>
                        </c:if>
                    </a>
                    <c:if test="${!n.read}">
                        <span class="noti-dot notification-dot" aria-hidden="true"></span>
                    </c:if>
                    <div class="noti-actions ms-2" style="position:relative; z-index:10;">
                        <button type="button"
                                class="btn btn-sm p-0 text-muted noti-menu-btn"
                                data-id="${n.id}"
                                data-read="${n.read}"
                                data-menu-id="list-noti-menu-${n.id}"
                                style="background:none; border:none; font-size:16px; line-height:1;"
                                onclick="event.preventDefault(); event.stopPropagation(); toggleNotiMenu(this);">...</button>
                        <div class="noti-menu shadow-sm" id="list-noti-menu-${n.id}"
                             style="display:none; position:absolute; right:0; top:24px; background:#fff; border:1px solid #ddd; border-radius:6px; min-width:160px; z-index:999;">
                            <button type="button" class="btn btn-sm w-100 text-start px-3 py-2"
                                    onclick="event.stopPropagation(); deleteNoti(${n.id})">Delete</button>
                            <button type="button" class="btn btn-sm w-100 text-start px-3 py-2"
                                    onclick="event.stopPropagation(); toggleReadNoti(${n.id}, ${n.read})">
                                ${n.read ? 'Mark as unread' : 'Mark as read'}
                            </button>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
</body>
</html>
