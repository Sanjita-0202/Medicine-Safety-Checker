<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <h1>Medicine Safety Checker</h1>
    <p class="subtitle">Check a new medicine for overdose risk and dangerous interactions.</p>

    <c:if test="${not empty errorMessage}">
        <div class="error-banner">${errorMessage}</div>
    </c:if>

    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/check">
            <div class="field">
                <label for="newMedicineName">New medicine</label>
                <select id="newMedicineName" name="newMedicineName" required>
                    <option value="" disabled ${empty newMedicineName ? 'selected' : ''}>Select a medicine</option>
                    <c:forEach items="${medicineNames}" var="name">
                        <option value="${name}" ${name eq newMedicineName ? 'selected' : ''}>${name}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="field">
                <label for="dose">Dose per intake (mg)</label>
                <input type="number" id="dose" name="dose" step="any" min="0"
                       value="${dose}" required>
            </div>

            <div class="field">
                <label for="timesPerDay">Times per day</label>
                <input type="number" id="timesPerDay" name="timesPerDay" step="1" min="1"
                       value="${timesPerDay}" required>
            </div>

            <div class="field">
                <label>Current medicines (optional)</label>
                <div class="checkbox-list">
                    <c:forEach items="${medicineNames}" var="name">
                        <label>
                            <input type="checkbox" name="currentMedicineNames" value="${name}"
                                   ${currentMedicineNames.contains(name) ? 'checked' : ''}>
                            ${name}
                        </label>
                    </c:forEach>
                </div>
            </div>

            <button type="submit">Check</button>
        </form>
    </div>
</div>
</body>
</html>
