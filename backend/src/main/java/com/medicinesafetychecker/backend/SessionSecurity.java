package com.medicinesafetychecker.backend;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

final class SessionSecurity {
    private SessionSecurity() { }

    static void ensureToken(HttpSession session) {
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
    }

    static boolean validToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object token = session == null ? null : session.getAttribute("csrfToken");
        String submitted = request.getParameter("csrfToken");
        return token instanceof String expected && submitted != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                        submitted.getBytes(StandardCharsets.UTF_8));
    }
}
