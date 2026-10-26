<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:if test="${param.notice == 'patient' || param.notice == 'prescription' || param.notice == 'stopped'}">
    <div class="toast" role="status"><span><c:choose><c:when test="${param.notice == 'prescription'}">Check complete. The result is saved in prescription history.</c:when><c:when test="${param.notice == 'stopped'}">Medicine stopped. Active medicines and history are up to date.</c:when><c:otherwise>Patient details saved successfully.</c:otherwise></c:choose></span><button type="button" data-dismiss-toast aria-label="Dismiss notification">×</button></div>
</c:if>
