<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Logout - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="page"><div class="card">
    <h1>Log out?</h1>
    <form method="post" action="${pageContext.request.contextPath}/logout">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
        <a href="${pageContext.request.contextPath}/patients">Back to patients</a>
    </form>
</div></main>
</body>
</html>
