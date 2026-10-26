<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Patient profile - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="page wide-page">
    <p class="eyebrow">Patient workspace / Patient profile</p>
    <jsp:include page="feedback.jsp"/>

    <div class="page-heading">
        <div><h1><c:out value="${patient.name}"/></h1><p class="subtitle">${patient.age} yrs · <c:out value="${patient.gender}"/> · ${patient.weight} kg</p></div>
        <div class="form-actions">
            <a class="button button-secondary" href="${pageContext.request.contextPath}/patients/${patient.id}/edit">Edit patient</a>
            <a class="button" href="${pageContext.request.contextPath}/patients/${patient.id}/report">Download report</a>
        </div>
    </div>
    <section class="card">
        <h2>Patient details</h2>
        <p><strong>Allergies:</strong>
            <c:if test="${empty patient.allergies}">None recorded</c:if>
            <c:forEach items="${patient.allergies}" var="allergy" varStatus="s"><c:out value="${allergy}"/>${!s.last ? ', ' : ''}</c:forEach>
        </p>
        <h3>Active medicines</h3>
        <c:if test="${empty patient.activeMedicines}"><p>None</p></c:if>
        <ul class="medicine-list">
            <c:forEach items="${patient.activeMedicines}" var="medicine">
                <li>
                    <span><c:out value="${medicine}"/></span>
                    <form method="post" data-confirm-stop action="${pageContext.request.contextPath}/patients/${patient.id}/stop">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="medicineName" value="${fn:escapeXml(medicine)}">
                        <button type="submit" class="button-danger" aria-label="Stop ${fn:escapeXml(medicine)}">Stop</button>
                    </form>
                </li>
            </c:forEach>
        </ul>
        <a class="button" href="${pageContext.request.contextPath}/patients/${patient.id}/prescribe">New prescription</a>
    </section>
    <section class="card">
        <h2>Prescription history</h2>
        <c:if test="${empty history}"><p>No prescriptions recorded yet.</p></c:if>
        <c:if test="${not empty history}">
            <table class="patient-table">
                <caption class="sr-only">Prescription checks and stopped medicines</caption>
                <thead><tr><th scope="col">Date</th><th scope="col">Medicine</th><th scope="col">Dose</th><th scope="col">Times/day</th><th scope="col">Doctor</th><th scope="col">Status</th><th scope="col">Reason</th></tr></thead>
                <tbody>
                <c:forEach items="${history}" var="entry">
                    <tr>
                        <td>${entry.date}</td><td><c:out value="${entry.medicineName}"/></td>
                        <td><c:choose><c:when test="${entry.status == 'STOPPED'}">—</c:when><c:otherwise>${entry.dose}</c:otherwise></c:choose></td>
                        <td><c:choose><c:when test="${entry.status == 'STOPPED'}">—</c:when><c:otherwise>${entry.timesPerDay}</c:otherwise></c:choose></td>
                        <td><c:out value="${entry.doctorName}" default="Not recorded"/></td>
                        <td><span class="status-pill ${entry.status == 'APPROVED' ? 'status-approved' : entry.status == 'STOPPED' ? 'status-stopped' : 'status-rejected'}">${entry.status}</span></td>
                        <td><c:out value="${entry.reason}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
    </section>
    <a class="back-link" href="${pageContext.request.contextPath}/patients">Back to patients</a>
    <dialog id="stop-dialog" aria-labelledby="stop-title"><h2 id="stop-title">Stop this medicine?</h2><p data-stop-message></p><div class="form-actions"><button type="button" class="button-danger" data-stop-confirm>Yes, stop medicine</button><button type="button" class="button-secondary" data-stop-cancel>Keep medicine</button></div></dialog>
</main>
</body>
</html>
