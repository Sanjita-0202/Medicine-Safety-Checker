package com.medicinesafetychecker.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class DoctorAuthenticationTest {
    private final DoctorAuthenticationFilter filter = new DoctorAuthenticationFilter();

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    @Test
    void anonymousPatientRequestRedirectsWithoutCreatingSession() throws Exception {
        var req = request("GET", "/patients/p1");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filter.doFilter(req, res, chain);
        assertEquals("/login", res.getRedirectedUrl());
        assertNull(chain.getRequest());
        assertNull(req.getSession(false));
    }

    @Test
    void publicRoutesAreExcludedButSimilarPrefixesAreProtected() throws Exception {
        for (String path : new String[]{"/api/medicines", "/api", "/css/style.css", "/js/app.js"}) {
            var chain = new MockFilterChain();
            var req = request("GET", path);
            filter.doFilter(req, new MockHttpServletResponse(), chain);
            assertSame(req, chain.getRequest());
        }
        var res = new MockHttpServletResponse();
        filter.doFilter(request("GET", "/api-private"), res, new MockFilterChain());
        assertEquals("/login", res.getRedirectedUrl());
    }

    @Test
    void loginGetReceivesCsrfTokenAndNoStoreHeader() throws Exception {
        var req = request("GET", "/login");
        var res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        assertNotNull(req.getSession().getAttribute("csrfToken"));
        assertEquals("no-store", res.getHeader("Cache-Control"));
    }

    @Test
    void authenticatedPostsRequireMatchingCsrfToken() throws Exception {
        var req = request("POST", "/patients/p1/prescribe");
        req.getSession().setAttribute("doctor", new DoctorDocument("d1", "Doctor", "General", "doctor", null));
        SessionSecurity.ensureToken(req.getSession());
        var rejected = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filter.doFilter(req, rejected, chain);
        assertEquals(403, rejected.getStatus());
        assertNull(chain.getRequest());
        req.setParameter("csrfToken", (String) req.getSession().getAttribute("csrfToken"));
        var allowed = new MockFilterChain();
        filter.doFilter(req, new MockHttpServletResponse(), allowed);
        assertSame(req, allowed.getRequest());
    }

    @Test
    void loginPostAlsoRequiresCsrfToken() throws Exception {
        var res = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/login"), res, new MockFilterChain());
        assertEquals(403, res.getStatus());
    }

    @Test
    void validLoginNormalizesUsernameReplacesSessionAndOmitsHash() throws Exception {
        DoctorRepository repository = mock(DoctorRepository.class);
        String hash = new BCryptPasswordEncoder(4).encode("CorrectPassword123!");
        when(repository.findByUsername("doctor")).thenReturn(Optional.of(
                new DoctorDocument("d1", "Dr Test", "General", "doctor", hash)));
        LoginServlet servlet = new LoginServlet();
        ReflectionTestUtils.setField(servlet, "doctors", repository);
        var req = request("POST", "/login");
        MockHttpSession old = (MockHttpSession) req.getSession();
        req.setParameter("username", " DOCTOR ");
        req.setParameter("password", "CorrectPassword123!");
        var res = new MockHttpServletResponse();
        servlet.doPost(req, res);
        assertTrue(old.isInvalid());
        assertNotSame(old, req.getSession());
        DoctorDocument identity = (DoctorDocument) req.getSession().getAttribute("doctor");
        assertEquals("d1", identity.getId());
        assertNull(identity.getPasswordHash());
        assertEquals(1800, req.getSession().getMaxInactiveInterval());
        assertNotNull(req.getSession().getAttribute("csrfToken"));
        assertEquals("/patients", res.getRedirectedUrl());
    }

    @Test
    void invalidLoginDoesNotAuthenticateAndUsesGenericError() throws Exception {
        DoctorRepository repository = mock(DoctorRepository.class);
        String hash = new BCryptPasswordEncoder(4).encode("CorrectPassword123!");
        when(repository.findByUsername("doctor")).thenReturn(Optional.of(
                new DoctorDocument("d1", "Doctor", "General", "doctor", hash)));
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());
        LoginServlet servlet = new LoginServlet();
        ReflectionTestUtils.setField(servlet, "doctors", repository);
        for (String username : new String[]{"doctor", "unknown"}) {
            var req = request("POST", "/login");
            req.setParameter("username", username);
            req.setParameter("password", "wrong-password");
            var res = new MockHttpServletResponse();
            servlet.doPost(req, res);
            assertEquals(401, res.getStatus());
            assertEquals("Invalid credentials. Please try again.", req.getAttribute("errorMessage"));
            assertEquals("/WEB-INF/jsp/login.jsp", res.getForwardedUrl());
            assertNull(req.getSession().getAttribute("doctor"));
        }
    }

    @Test
    void approvedAndRejectedPrescriptionsBothRecordSessionDoctor() throws Exception {
        for (double dose : new double[]{500, 99999}) {
            PatientRepository patients = mock(PatientRepository.class);
            PrescriptionRepository prescriptions = mock(PrescriptionRepository.class);
            MedicineRepository medicines = mock(MedicineRepository.class);
            InteractionGraphLoader loader = mock(InteractionGraphLoader.class);
            when(loader.isReady()).thenReturn(true);
            when(patients.findById("p1")).thenReturn(Optional.of(new PatientDocument(
                    "p1", "Patient", 30, "Female", 60, List.of(), new ArrayList<>())));
            when(medicines.findByNameIgnoreCase("Amoxicillin")).thenReturn(Optional.of(
                    new MedicineDocument("amoxicillin", "Amoxicillin", MedicineType.ANTIBIOTIC, "mg", 1000, 3000)));
            PatientServlet servlet = new PatientServlet();
            ReflectionTestUtils.setField(servlet, "patientRepository", patients);
            ReflectionTestUtils.setField(servlet, "prescriptionRepository", prescriptions);
            ReflectionTestUtils.setField(servlet, "medicineRepository", medicines);
            ReflectionTestUtils.setField(servlet, "workflow", new PatientWorkflow(patients, prescriptions,
                    new PrescriptionChecker(medicines, loader, mock(AuditLogger.class))));
            var req = request("POST", "/patients");
            req.setPathInfo("/p1/prescribe");
            req.getSession().setAttribute("doctor", new DoctorDocument("d1", "Dr Test", "General", "doctor", null));
            req.setParameter("medicineName", "Amoxicillin");
            req.setParameter("dose", Double.toString(dose));
            req.setParameter("timesPerDay", "2");
            // Submitted identity fields must never override the authenticated identity.
            req.setParameter("doctorId", "attacker");
            var res = new MockHttpServletResponse();
            servlet.doPost(req, res);
            var saved = ArgumentCaptor.forClass(PrescriptionDocument.class);
            verify(prescriptions).save(saved.capture());
            assertEquals("d1", saved.getValue().getDoctorId());
            assertEquals("Dr Test", saved.getValue().getDoctorName());
            assertEquals(dose == 500 ? PrescriptionStatus.APPROVED : PrescriptionStatus.REJECTED,
                    saved.getValue().getStatus());
            if (dose != 500) { assertNotNull(saved.getValue().getReason()); }
        }
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        var req = request("POST", "/logout");
        MockHttpSession session = (MockHttpSession) req.getSession();
        session.setAttribute("doctor", new DoctorDocument("d1", "Doctor", "General", "doctor", null));
        var res = new MockHttpServletResponse();
        new LogoutServlet().doPost(req, res);
        assertTrue(session.isInvalid());
        assertEquals("/login", res.getRedirectedUrl());
    }
}
