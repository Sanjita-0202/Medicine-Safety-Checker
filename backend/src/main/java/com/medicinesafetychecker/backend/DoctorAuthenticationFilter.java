package com.medicinesafetychecker.backend;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(filterName = "DoctorAuthenticationFilter", urlPatterns = "/*")
public class DoctorAuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if (path.equals("/api") || path.startsWith("/api/") || path.startsWith("/css/") || path.startsWith("/js/")) {
            chain.doFilter(request, response);
            return;
        }
        res.setHeader("Cache-Control", "no-store");
        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("doctor") instanceof DoctorDocument;
        if (!path.equals("/login") && !loggedIn) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        boolean readOnly = "GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod())
                || "OPTIONS".equals(req.getMethod());
        if (!readOnly && !SessionSecurity.validToken(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Your form expired. Reload the page and try again.");
            return;
        }
        SessionSecurity.ensureToken(req.getSession());
        chain.doFilter(request, response);
    }
}
