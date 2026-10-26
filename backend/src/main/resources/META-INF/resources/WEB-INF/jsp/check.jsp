<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Medicine check - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="page wide-page">
    <p class="eyebrow">Prescription workspace / Safety check</p>

    <h1>Medicine Safety Checker</h1><p class="subtitle">Check allergies, dosage and known interactions without saving a prescription.</p>
    <c:if test="${not empty errorMessage}"><div class="error-banner" role="alert"><c:out value="${errorMessage}"/></div></c:if>
    <section class="sample-banner"><h2>Explore a sample check</h2><p>Fill a scenario, review the values, then press Check. These examples use the project’s demo dataset.</p><div class="sample-buttons"><button type="button" data-scenario="safe">✓ Safe dose</button><button type="button" data-scenario="allergy">Allergy conflict</button><button type="button" data-scenario="overdose">Overdose risk</button><button type="button" data-scenario="interaction">Drug interaction</button></div><p class="sample-note" data-sample-status role="status"></p></section>
    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/check">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <div class="field"><label for="newMedicineName">New medicine</label>
                <select id="newMedicineName" name="newMedicineName" required><option value="">Select a medicine</option>
                    <c:forEach items="${medicineNames}" var="name"><option value="${fn:escapeXml(name)}" ${name == param.newMedicineName ? 'selected' : ''}><c:out value="${name}"/></option></c:forEach>
                </select>
            </div>
            <div class="form-grid">
                <div class="field"><label for="dose">Dose per intake (mg)</label><input type="number" id="dose" name="dose" step="any" min="0.01" value="${fn:escapeXml(param.dose)}" required></div>
                <div class="field"><label for="timesPerDay">Times per day</label><input type="number" id="timesPerDay" name="timesPerDay" step="1" min="1" value="${fn:escapeXml(param.timesPerDay)}" required></div>
            </div>
            <div class="field"><label for="allergies">Allergies (optional, comma separated)</label><input type="text" id="allergies" name="allergies" maxlength="2000" value="${fn:escapeXml(param.allergies)}" placeholder="e.g. penicillin, NSAID"></div>
            <fieldset class="field"><legend>Current medicines (optional)</legend>
                <label class="sr-only" for="medicine-filter">Filter current medicines</label><input class="med-filter" type="search" id="medicine-filter" placeholder="Find a current medicine…" data-medicine-filter><p class="field-help" data-selected-count aria-live="polite">0 medicines selected</p><div class="checkbox-list"><c:forEach items="${medicineNames}" var="name"><label><input type="checkbox" name="currentMedicineNames" value="${fn:escapeXml(name)}" ${currentMedicineNames.contains(name) ? 'checked' : ''}><c:out value="${name}"/></label></c:forEach></div>
            </fieldset>
            <output class="dose-preview" data-dose-summary aria-live="polite">Enter a dose and frequency to preview the daily total.</output>
            <button type="submit">Check</button>
        </form>
    </div>
</main>
</body>
</html>
