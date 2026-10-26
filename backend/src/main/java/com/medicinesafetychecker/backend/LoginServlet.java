package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.support.WebApplicationContextUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private DoctorRepository doctors;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final String dummyHash = encoder.encode("dummy-password-for-timing");

    @Override
    public void init() throws ServletException {
        doctors = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext())
                .getBean(DoctorRepository.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("doctor") instanceof DoctorDocument) {
            response.sendRedirect(request.getContextPath() + "/patients");
            return;
        }
        SessionSecurity.ensureToken(session);
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String normalized = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
        DoctorDocument doctor = normalized.length() > 100 ? null : doctors.findByUsername(normalized).orElse(null);
        boolean validLength = password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
        String hash = doctor == null || doctor.getPasswordHash() == null ? dummyHash : doctor.getPasswordHash();
        boolean matches = encoder.matches(validLength ? password : "", hash);
        if (doctor == null || !validLength || !matches) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("errorMessage", "Invalid credentials. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }
        HttpSession previous = request.getSession(false);
        if (previous != null) { previous.invalidate(); }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("doctor", doctor.sessionIdentity());
        SessionSecurity.ensureToken(session);
        response.sendRedirect(request.getContextPath() + "/patients");
    }
}
