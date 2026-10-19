<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Medicine Safety Checker - Result</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <h1>Medicine Safety Checker</h1>
    <p class="subtitle">
        ${newMedicineName} - ${dose}mg, ${timesPerDay}x/day
    </p>

    <c:choose>
        <c:when test="${status == 'SAFE'}">
            <div class="panel panel-safe">
                <h2>Safe</h2>
                <p>No known overdose risk or dangerous combination was found.</p>
            </div>
        </c:when>

        <c:when test="${status == 'OVERDOSE_RISK'}">
            <div class="panel panel-danger">
                <h2>Overdose risk</h2>
                <p class="detail-row">${message}</p>
            </div>
        </c:when>

        <c:when test="${status == 'DANGEROUS_COMBINATION'}">
            <div class="panel ${severity == 'SEVERE' ? 'panel-danger' : 'panel-warning'}">
                <h2>Dangerous combination</h2>
                <p class="detail-row"><strong>Severity:</strong> ${severity}</p>
                <p class="detail-row">${message}</p>
            </div>
        </c:when>
    </c:choose>

    <a class="back-link" href="${pageContext.request.contextPath}/check">Check another</a>
</div>
</body>
</html>
