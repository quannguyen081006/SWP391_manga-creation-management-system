<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Salary</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/salary/salary.css" />
</head>
<body>
<jsp:include page="../common/header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="section-card">
    <h3 class="section-title">My settled salary periods</h3>
    <p class="section-desc">Read-only salary and KPI history.</p>

    <c:choose>
        <c:when test="${not empty records}">
            <div class="salary-table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th class="salary-toggle-column"></th>
                            <th>Period</th>
                            <th>KPI score</th>
                            <th>Gross salary</th>
                            <th>Bonus</th>
                            <th>Deduction</th>
                            <th>Net salary</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${records}" var="r">
                        <tr class="salary-row">
                            <td>
                                <button type="button" class="btn-toggle-tasks"
                                        data-salary-toggle="${r.periodId}"
                                        aria-expanded="false"
                                        aria-controls="task-detail-${r.periodId}">&gt;</button>
                            </td>
                            <td><c:out value="${r.periodName}" /></td>
                            <td><fmt:formatNumber value="${r.kpiScore}" minFractionDigits="2" maxFractionDigits="2" /></td>
                            <td><fmt:formatNumber value="${r.grossSalary}" maxFractionDigits="0" /> VND</td>
                            <td><fmt:formatNumber value="${r.bonus}" maxFractionDigits="0" /> VND</td>
                            <td><fmt:formatNumber value="${r.deduction}" maxFractionDigits="0" /> VND</td>
                            <td class="money-strong"><fmt:formatNumber value="${r.netSalary}" maxFractionDigits="0" /> VND</td>
                        </tr>
                        <tr class="task-detail-row" id="task-detail-${r.periodId}">
                            <td colspan="7">
                                <c:choose>
                                    <c:when test="${not empty r.tasks}">
                                        <table class="data-table salary-task-table">
                                            <thead>
                                                <tr>
                                                    <th>Series</th>
                                                    <th>Chapter</th>
                                                    <th>Page</th>
                                                    <th>Task type</th>
                                                    <th>Due date</th>
                                                    <th>Approved date</th>
                                                    <th>Delivery</th>
                                                    <th>Rate</th>
                                                    <th>Amount</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${r.tasks}" var="t">
                                                <tr>
                                                    <td><c:out value="${t.seriesTitle}" /></td>
                                                    <td>Ch.${t.chapterNumber}</td>
                                                    <td>${t.pageNumber}</td>
                                                    <td>${t.taskType}</td>
                                                    <td><fmt:formatDate value="${t.dueDate}" pattern="dd/MM/yyyy" /></td>
                                                    <td><fmt:formatDate value="${t.approvedAt}" pattern="dd/MM/yyyy" /></td>
                                                    <td class="${t.onTime ? 'metric-good' : 'metric-bad'}">${t.onTime ? 'On time' : 'Late'}</td>
                                                    <td><fmt:formatNumber value="${t.ratePerPage}" maxFractionDigits="0" /> VND/page</td>
                                                    <td><fmt:formatNumber value="${t.amount}" maxFractionDigits="0" /> VND</td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="empty-state"><div class="subtitle">No task breakdown is available.</div></div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="title">No settled salary periods</div>
                <div class="subtitle">Your settled salary history will appear here.</div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
<script src="${ctx}/assets/js/salary/salary.js"></script>
</body>
</html>
