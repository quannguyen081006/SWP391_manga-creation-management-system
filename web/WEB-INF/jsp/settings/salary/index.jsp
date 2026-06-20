<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Salary &amp; KPI Settings</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/salary/salary-settings.css" />
</head>
<body>
<jsp:include page="../../common/header.jsp" />

<main class="container settings-page">
    <div class="settings-page-head">
        <div class="settings-page-icon" aria-hidden="true"></div>
        <div>
            <h2>Salary &amp; KPI Settings</h2>
            <p>Configure automatic bonus and late-task deduction suggestions.</p>
        </div>
    </div>

    <c:if test="${not empty success}"><div class="alert success"><c:out value="${success}" /></div></c:if>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}" /></div></c:if>

    <div class="section-card settings-panel">
        <h3 class="section-title">Task Type Rates</h3>
        <p class="section-desc">Base salary rate per completed page.</p>
        <div class="task-rate-table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Rate per page</th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${taskTypes}" var="item">
                    <tr>
                        <td><strong><c:out value="${item.code}" /></strong></td>
                        <td><c:out value="${item.displayName}" /></td>
                        <td>
                            <form class="task-rate-form" method="post"
                                  action="${pageContext.request.contextPath}/main/settings/salary/task-types/${item.code}/update">
                                <input aria-label="Rate per page" name="ratePerPage" type="number"
                                       min="1000" step="1000" value="${item.ratePerPage}" required />
                                <span>VND / page</span>
                                <button class="btn small" type="submit">Save</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="section-card settings-panel">
        <h3 class="section-title">Bonus &amp; Deduction Suggestions</h3>
        <form class="settings-form" method="post" action="${pageContext.request.contextPath}/main/settings/salary">
            <div class="settings-grid">
                <label class="setting-control-card" for="kpiBonusThreshold">
                    <span class="setting-control-icon setting-control-icon-bonus" aria-hidden="true"></span>
                    <span class="setting-control-copy">
                        <span class="setting-control-title">KPI bonus threshold</span>
                        <span class="setting-control-desc">KPI scores at or above this threshold receive a bonus suggestion.</span>
                    </span>
                    <span class="setting-number-wrap">
                        <input id="kpiBonusThreshold" type="number" name="kpiBonusThreshold"
                               min="0" max="100" value="${settings.kpiBonusThreshold}" required />
                        <span class="setting-number-unit">points</span>
                    </span>
                </label>

                <label class="setting-control-card" for="bonusPercent">
                    <span class="setting-control-icon setting-control-icon-bonus" aria-hidden="true"></span>
                    <span class="setting-control-copy">
                        <span class="setting-control-title">Bonus percentage</span>
                        <span class="setting-control-desc">Percentage of gross salary suggested as a bonus after reaching the KPI threshold.</span>
                    </span>
                    <span class="setting-number-wrap">
                        <input id="bonusPercent" type="number" name="bonusPercent"
                               min="0" max="100" step="0.1" value="${settings.bonusPercent}" required />
                        <span class="setting-number-unit">%</span>
                    </span>
                </label>

                <label class="setting-control-card" for="penaltyPerLateTask">
                    <span class="setting-control-icon setting-control-icon-penalty" aria-hidden="true"></span>
                    <span class="setting-control-copy">
                        <span class="setting-control-title">Penalty per late task</span>
                        <span class="setting-control-desc">Amount suggested for deduction for each task approved after its due date.</span>
                    </span>
                    <span class="setting-number-wrap">
                        <input id="penaltyPerLateTask" type="number" name="penaltyPerLateTask"
                               min="0" step="1000" value="${settings.penaltyPerLateTask}" required />
                        <span class="setting-number-unit">VND / task</span>
                    </span>
                </label>
            </div>

            <div class="settings-actions">
                <a class="btn" href="${pageContext.request.contextPath}/main/settings">&larr; Back to Settings</a>
                <button class="btn primary" type="submit">Save Settings</button>
            </div>
        </form>
    </div>
</main>

<jsp:include page="../../common/footer.jsp" />
</body>
</html>
