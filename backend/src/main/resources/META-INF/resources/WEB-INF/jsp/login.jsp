<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Doctor login - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<div class="page login-layout">
<aside class="login-story"><p class="eyebrow">A clearer view of patient care</p><h1>Every prescription.<br>A little more confidence.</h1><p>A focused workspace for patients, medicine checks and the details that matter.</p><div class="feature-line"><span>01</span>Keep patient records in one place</div><div class="feature-line"><span>02</span>Check allergies, dosage and interactions</div><div class="feature-line"><span>03</span>Review history and export reports</div></aside>
<main class="login-page"><span class="demo-tag">Local demo workspace</span>
    <p class="eyebrow">Welcome back</p><h1>Doctor login</h1>
    <p class="subtitle">Sign in to manage patients and check prescriptions.</p>
    <c:if test="${not empty errorMessage}">
        <div class="error-banner" role="alert"><c:out value="${errorMessage}"/></div>
    </c:if>
    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <div class="field">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" autocomplete="username" maxlength="100" required autofocus>
            </div>
            <div class="field">
                <label for="password">Password</label>
                <div class="password-wrap"><input type="password" id="password" name="password" autocomplete="current-password" required><button type="button" data-toggle-password aria-controls="password" aria-pressed="false">Show</button></div>
            </div>
            <button type="submit">Sign in</button>
        </form>
    </div>
<p class="sample-note">Demo credentials: <strong>doctor</strong> / <strong>doctor</strong></p></main>
</div>
</body>
</html>
