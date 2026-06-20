<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Salary Period Details</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/salary/salary.css" />
</head>
<body>
<jsp:include page="../common/header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:if test="${not empty error}"><div class="alert error"><c:out value="${error}" /></div></c:if>

<div class="section-card">
    <div class="salary-head">
        <div>
            <h3 class="section-title salary-title"><c:out value="${period.name}" /></h3>
            <div>${period.startDate} &rarr; ${period.endDate}
                <span class="status-badge salary-status-badge ${period.status}">${period.status}</span>
            </div>
        </div>
        <c:if test="${period.status == 'OPEN'}">
            <div class="salary-actions">
                <form id="calc-form" method="post" action="${ctx}/main/salary/periods/${period.id}/calculate">
                    <button class="btn primary" type="submit">Refresh calculation</button>
                </form>
                <form method="post" action="${ctx}/main/salary/periods/${period.id}/settle">
                    <button class="btn danger" type="submit"
                            data-confirm="Settling locks this salary period and prevents further recalculation. Continue?">
                        Settle period
                    </button>
                </form>
            </div>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${not empty records}">
            <div class="salary-table-wrap">
                <table class="data-table salary-detail-table">
                    <thead>
                        <tr>
                            <th class="salary-toggle-column"></th>
                            <th>Assistant</th>
                            <th>Approved tasks</th>
                            <th>Pages</th>
                            <th>On-time rate</th>
                            <th>KPI</th>
                            <th>Gross salary</th>
                            <th>Bonus / Deduction</th>
                            <th>Net salary</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${records}" var="r">
                        <c:choose>
                            <c:when test="${r.onTimeRate >= 90}"><c:set var="onTimeClass" value="metric-good" /></c:when>
                            <c:when test="${r.onTimeRate >= 80}"><c:set var="onTimeClass" value="metric-warn" /></c:when>
                            <c:otherwise><c:set var="onTimeClass" value="metric-bad" /></c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${r.kpiScore >= 90}"><c:set var="kpiClass" value="metric-good" /></c:when>
                            <c:when test="${r.kpiScore >= 80}"><c:set var="kpiClass" value="metric-warn" /></c:when>
                            <c:otherwise><c:set var="kpiClass" value="metric-bad" /></c:otherwise>
                        </c:choose>
                        <tr class="salary-row" data-assistant="${r.assistantId}">
                            <td>
                                <button type="button" class="btn-toggle-tasks"
                                        data-salary-toggle="${r.assistantId}"
                                        aria-expanded="false"
                                        aria-controls="task-detail-${r.assistantId}">&gt;</button>
                            </td>
                            <td><c:out value="${r.assistantName}" /></td>
                            <td>${r.totalTasksApproved}</td>
                            <td>${r.totalPagesCompleted}</td>
                            <td class="${onTimeClass}"><fmt:formatNumber value="${r.onTimeRate}" minFractionDigits="2" maxFractionDigits="2" />%</td>
                            <td class="${kpiClass}"><fmt:formatNumber value="${r.kpiScore}" minFractionDigits="2" maxFractionDigits="2" /></td>
                            <td><fmt:formatNumber value="${r.grossSalary}" type="number" maxFractionDigits="0" /> VND</td>
                            <td class="salary-adjust-summary">
                                <span class="salary-bonus">+<fmt:formatNumber value="${r.bonus}" maxFractionDigits="0" /> VND</span>
                                <span class="salary-deduction">-<fmt:formatNumber value="${r.deduction}" maxFractionDigits="0" /> VND</span>
                            </td>
                            <td class="money-strong"><fmt:formatNumber value="${r.netSalary}" maxFractionDigits="0" /> VND</td>
                        </tr>
                        <tr class="task-detail-row" id="task-detail-${r.assistantId}">
                            <td colspan="9">
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
                                                    <td class="${t.onTime ? 'metric-good' : 'metric-bad'}">
                                                        ${t.onTime ? 'On time' : 'Late'}
                                                    </td>
                                                    <td><fmt:formatNumber value="${t.ratePerPage}" maxFractionDigits="0" /> VND/page</td>
                                                    <td><fmt:formatNumber value="${t.amount}" maxFractionDigits="0" /> VND</td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="empty-state">
                                            <div class="subtitle">No approved tasks in this period.</div>
                                        </div>
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
                <div class="title">No salary data yet</div>
                <div class="subtitle">Open periods refresh automatically when viewed. You can also use “Refresh calculation”.</div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="stack-actions content-section-large">
    <a class="btn" href="${ctx}/main/salary/periods">&larr; Back to salary periods</a>
</div>

<jsp:include page="../common/footer.jsp" />
<script src="${ctx}/assets/js/salary/salary.js"></script>
</body>
</html>
