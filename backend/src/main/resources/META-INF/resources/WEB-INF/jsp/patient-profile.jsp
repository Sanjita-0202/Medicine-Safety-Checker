<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${patient.name} - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <h1>${patient.name}</h1>
    <p class="subtitle">${patient.age} yrs - ${patient.gender} - ${patient.weight}kg</p>

    <div class="card">
        <h2>Details</h2>
        <p class="detail-row"><strong>Allergies:</strong>
            <c:if test="${empty patient.allergies}">None recorded</c:if>
            <c:forEach items="${patient.allergies}" var="allergy" varStatus="status">${allergy}${!status.last ? ', ' : ''}</c:forEach>
        </p>
        <p class="detail-row"><strong>Active medicines:</strong>
            <c:if test="${empty patient.activeMedicines}">None</c:if>
            <c:forEach items="${patient.activeMedicines}" var="med" varStatus="status">${med}${!status.last ? ', ' : ''}</c:forEach>
        </p>
    </div>

    <div class="card">
        <h2>Prescription history</h2>
        <c:if test="${empty history}">
            <p>No prescriptions recorded yet.</p>
        </c:if>
        <c:if test="${not empty history}">
            <table class="patient-table">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Medicine</th>
                    <th>Dose</th>
                    <th>Times/day</th>
                    <th>Status</th>
                    <th>Reason</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${history}" var="entry">
                    <tr>
                        <td>${entry.date}</td>
                        <td>${entry.medicineName}</td>
                        <td>${entry.dose}</td>
                        <td>${entry.timesPerDay}</td>
                        <td>
                            <span class="status-pill ${entry.status == 'APPROVED' ? 'status-approved' : 'status-rejected'}">${entry.status}</span>
                        </td>
                        <td>${entry.reason}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
    </div>

    <a class="back-link" href="${pageContext.request.contextPath}/patients/${patient.id}/prescribe">New prescription</a>
    &nbsp;|&nbsp;
    <a class="back-link" href="${pageContext.request.contextPath}/patients">Back to patients</a>
</div>
</body>
</html>
