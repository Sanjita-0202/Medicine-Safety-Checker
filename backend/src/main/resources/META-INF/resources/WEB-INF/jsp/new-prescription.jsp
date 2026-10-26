<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>New prescription - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="page wide-page">
    <p class="eyebrow">Prescription workspace / Safety check</p>

    <h1>New prescription</h1>
    <p class="subtitle">Patient: <c:out value="${patient.name}"/>. Checked against recorded allergies and active medicines.</p>
    <p><strong>Recorded allergies:</strong> <c:if test="${empty patient.allergies}">None</c:if><c:forEach items="${patient.allergies}" var="allergy" varStatus="s"><c:out value="${allergy}"/>${!s.last ? ', ' : ''}</c:forEach></p>
    <c:if test="${not empty errorMessage}"><div class="error-banner" role="alert"><c:out value="${errorMessage}"/></div></c:if>
    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/patients/${patient.id}/prescribe">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <div class="field"><label for="medicineName">Medicine</label>
                <select id="medicineName" name="medicineName" required>
                    <option value="">Select a medicine</option>
                    <c:forEach items="${medicineNames}" var="name"><option value="${fn:escapeXml(name)}" ${name == param.medicineName ? 'selected' : ''}><c:out value="${name}"/></option></c:forEach>
                </select>
            </div>
            <div class="form-grid">
                <div class="field"><label for="dose">Dose per intake (mg)</label><input type="number" id="dose" name="dose" step="any" min="0.01" value="${fn:escapeXml(param.dose)}" required></div>
                <div class="field"><label for="timesPerDay">Times per day</label><input type="number" id="timesPerDay" name="timesPerDay" step="1" min="1" value="${fn:escapeXml(param.timesPerDay)}" required></div>
            </div>
            <output class="dose-preview" data-dose-summary aria-live="polite">Enter a dose and frequency to preview the daily total.</output>
            <button type="submit">Check and save</button>
        </form>
    </div>
    <a class="back-link" href="${pageContext.request.contextPath}/patients/${patient.id}">Back to profile</a>
</main>
</body>
</html>
