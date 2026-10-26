<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<script src="${pageContext.request.contextPath}/js/app.js" defer></script>
<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/patients"><span class="brand-mark" aria-hidden="true">+</span><span><strong>Medicine Safety Checker</strong><small>Care with confidence</small></span></a>
    <c:if test="${not empty sessionScope.doctor}">
        <nav aria-label="Main navigation">
            <a href="${pageContext.request.contextPath}/patients" ${fn:startsWith(requestScope['jakarta.servlet.forward.request_uri'], pageContext.request.contextPath.concat('/patients')) ? 'aria-current="page"' : ''}>Patients</a>
            <a href="${pageContext.request.contextPath}/check" ${fn:endsWith(requestScope['jakarta.servlet.forward.request_uri'], '/check') ? 'aria-current="page"' : ''}>Check</a>
            <a href="${pageContext.request.contextPath}/samples" ${fn:endsWith(requestScope['jakarta.servlet.forward.request_uri'], '/samples') ? 'aria-current="page"' : ''}>Try samples</a>
            <span class="doctor-name"><span class="online-dot" aria-hidden="true"></span><c:out value="${sessionScope.doctor.name}"/></span>
            <form method="post" action="${pageContext.request.contextPath}/logout" class="inline-form"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><button type="submit" class="button-secondary">Logout</button></form>
        </nav>
    </c:if>
</header>
