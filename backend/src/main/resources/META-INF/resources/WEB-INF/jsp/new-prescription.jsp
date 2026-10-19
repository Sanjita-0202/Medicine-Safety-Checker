<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>New prescription - ${patient.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <h1>New prescription</h1>
    <p class="subtitle">${patient.name} - currently on:
        <c:if test="${empty patient.activeMedicines}">nothing</c:if>
        <c:forEach items="${patient.activeMedicines}" var="med" varStatus="status">${med}${!status.last ? ', ' : ''}</c:forEach>
    </p>

    <c:if test="${not empty errorMessage}">
        <div class="error-banner">${errorMessage}</div>
    </c:if>

    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/patients/${patient.id}/prescribe">
            <div class="field">
                <label for="medicineName">Medicine</label>
                <select id="medicineName" name="medicineName" required>
                    <option value="" disabled selected>Select a medicine</option>
                    <c:forEach items="${medicineNames}" var="name">
                        <option value="${name}">${name}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="field">
                <label for="dose">Dose per intake (mg)</label>
                <input type="number" id="dose" name="dose" step="any" min="0" required>
            </div>

            <div class="field">
                <label for="timesPerDay">Times per day</label>
                <input type="number" id="timesPerDay" name="timesPerDay" step="1" min="1" required>
            </div>

            <button type="submit">Check and save</button>
        </form>
    </div>

    <a class="back-link" href="${pageContext.request.contextPath}/patients/${patient.id}">Back to profile</a>
</div>
</body>
</html>
