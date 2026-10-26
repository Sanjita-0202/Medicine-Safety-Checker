<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Patients - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="page wide-page">
    <p class="eyebrow">Your practice / Patient workspace</p>

    <div class="page-heading">
        <div><h1>Patients</h1><p class="subtitle">Manage patient details, medicines and prescription history.</p></div>
        <a class="button" href="${pageContext.request.contextPath}/patients/add">Add patient</a>
    </div>
    <c:set var="activeCount" value="0"/><c:set var="allergyCount" value="0"/>
    <c:forEach items="${patients}" var="item"><c:if test="${not empty item.activeMedicines}"><c:set var="activeCount" value="${activeCount + 1}"/></c:if><c:if test="${not empty item.allergies}"><c:set var="allergyCount" value="${allergyCount + 1}"/></c:if></c:forEach>
    <div class="stats-grid" aria-label="Patient summary">
        <div class="stat-card"><strong>${fn:length(patients)}</strong><span>Patients in this list</span></div>
        <div class="stat-card"><strong>${activeCount}</strong><span>With active medicines</span></div>
        <div class="stat-card"><strong>${allergyCount}</strong><span>With recorded allergies</span></div>
    </div>
    <form method="get" action="${pageContext.request.contextPath}/patients" class="search-form" role="search">
        <label for="q">Search by patient name or ID</label>
        <div class="form-actions">
            <input type="search" id="q" name="q" value="${fn:escapeXml(query)}" placeholder="Start typing a name or ID…" data-patient-search>
            <button type="submit">Search</button>
            <a href="${pageContext.request.contextPath}/patients">Clear</a>
        </div>
        <output class="results-count" data-results-count aria-live="polite">${fn:length(patients)} patients in this list</output>
    </form>
    <div class="card">
        <c:if test="${empty patients}"><div class="empty-state"><h2>No patients found</h2><p>Try another search or add a new patient.</p></div></c:if>
        <c:if test="${not empty patients}">
            <table class="patient-table">
                <caption class="sr-only">Patients matching your search</caption>
                <thead><tr><th scope="col">Name</th><th scope="col">Age</th><th scope="col">Gender</th><th scope="col">Active medicines</th><th scope="col">Actions</th></tr></thead>
                <tbody>
                <c:forEach items="${patients}" var="patient">
                    <tr data-patient-row data-patient-id="${patient.id}">
                        <td><c:out value="${patient.name}"/></td><td>${patient.age}</td><td><c:out value="${patient.gender}"/></td>
                        <td>
                            <c:if test="${empty patient.activeMedicines}">None</c:if>
                            <c:forEach items="${patient.activeMedicines}" var="med" varStatus="s"><c:out value="${med}"/>${!s.last ? ', ' : ''}</c:forEach>
                        </td>
                        <td><a href="${pageContext.request.contextPath}/patients/${patient.id}">View profile</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
    </div>
    <p class="empty-state" data-search-empty hidden>No patients match your search. Try a different name.</p>
</main>
</body>
</html>
