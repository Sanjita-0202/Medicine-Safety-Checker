<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Patients - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <h1>Patients</h1>
    <p class="subtitle">Select a patient to view their profile and prescription history.</p>

    <div class="card">
        <table class="patient-table">
            <thead>
            <tr>
                <th>Name</th>
                <th>Age</th>
                <th>Gender</th>
                <th>Active medicines</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${patients}" var="patient">
                <tr>
                    <td>${patient.name}</td>
                    <td>${patient.age}</td>
                    <td>${patient.gender}</td>
                    <td>
                        <c:if test="${empty patient.activeMedicines}">-</c:if>
                        <c:forEach items="${patient.activeMedicines}" var="med" varStatus="status">${med}${!status.last ? ', ' : ''}</c:forEach>
                    </td>
                    <td><a href="${pageContext.request.contextPath}/patients/${patient.id}">View profile</a></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <a class="back-link" href="${pageContext.request.contextPath}/check">Standalone medicine check</a>
</div>
</body>
</html>
