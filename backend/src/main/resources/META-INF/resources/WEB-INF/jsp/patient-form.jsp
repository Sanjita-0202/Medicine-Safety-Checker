<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:if test="${not empty errorMessage}"><div class="error-banner" role="alert"><c:out value="${errorMessage}"/></div></c:if>
<c:if test="${empty patient}"><section class="sample-banner"><h2>Try a sample patient</h2><p>Prefill an example, adjust the details, then choose Add patient to save.</p><div class="sample-buttons"><button type="button" data-patient-sample="allergy">Maya · Penicillin allergy</button><button type="button" data-patient-sample="general">Arjun · No recorded allergies</button><button type="button" data-patient-sample="nsaid">Neha · NSAID allergy</button></div><p class="sample-note" data-sample-status role="status"></p></section></c:if>
<div class="card">
    <form method="post" action="${pageContext.request.contextPath}/patients/${empty patient ? 'add' : patient.id.concat('/edit')}">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <div class="field"><label for="name">Name</label><input type="text" id="name" name="name" maxlength="100" value="${fn:escapeXml(form.name)}" autocomplete="name" required></div>
        <div class="form-grid">
            <div class="field"><label for="age">Age (years)</label><input type="number" id="age" name="age" min="0" max="130" step="1" value="${fn:escapeXml(form.age)}" required></div>
            <div class="field"><label for="weight">Weight (kg)</label><input type="number" id="weight" name="weight" min="0.01" max="500" step="any" value="${fn:escapeXml(form.weight)}" required></div>
        </div>
        <div class="field">
            <label for="gender">Gender</label>
            <select id="gender" name="gender" required>
                <option value="">Select gender</option>
                <c:forEach items="${['Male', 'Female', 'Other', 'Prefer not to say']}" var="option">
                    <option value="${option}" ${form.gender == option ? 'selected' : ''}>${option}</option>
                </c:forEach>
            </select>
        </div>
        <div class="field">
            <label for="allergies">Allergies (optional)</label>
            <input type="text" id="allergies" name="allergies" maxlength="2000" value="${fn:escapeXml(form.allergies)}" aria-describedby="allergy-help">
            <p class="field-help" id="allergy-help">Separate medicine names or drug classes with commas, for example: penicillin, NSAID.</p>
        </div>
        <div class="form-actions"><button type="submit">${empty patient ? 'Add patient' : 'Save changes'}</button><a href="${pageContext.request.contextPath}/patients${empty patient ? '' : '/'.concat(patient.id)}">Cancel</a></div>
    </form>
</div>
