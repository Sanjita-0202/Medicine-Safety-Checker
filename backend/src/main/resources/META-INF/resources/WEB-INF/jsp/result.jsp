<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Check result - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="page wide-page">

    <h1>Check result</h1><p class="subtitle"><c:out value="${newMedicineName}"/> · ${dose} mg · ${timesPerDay} times/day</p>
    <c:choose>
        <c:when test="${status == 'SAFE'}"><section class="panel panel-safe"><h2>Safe</h2><p><c:out value="${message}"/></p></section></c:when>
        <c:when test="${status == 'ALLERGY_CONFLICT'}"><section class="panel panel-danger"><h2>Allergy conflict</h2><p><c:out value="${message}"/></p></section></c:when>
        <c:when test="${status == 'OVERDOSE_RISK'}"><section class="panel panel-danger"><h2>Overdose risk</h2><p><c:out value="${message}"/></p></section></c:when>
        <c:otherwise><section class="panel ${severity == 'SEVERE' ? 'panel-danger' : 'panel-warning'}"><h2>Dangerous combination</h2><p><strong>Severity:</strong> ${severity}</p><p><c:out value="${message}"/></p></section></c:otherwise>
    </c:choose>
    <a class="button" href="${pageContext.request.contextPath}/check">Check another</a>
</main>
</body>
</html>
