package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.context.support.WebApplicationContextUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "PrescriptionServlet", urlPatterns = "/check")
public class PrescriptionServlet extends HttpServlet {
    private MedicineRepository medicines;
    private PrescriptionChecker checker;
    private AuditLogger audit;

    @Override
    public void init() throws ServletException {
        var context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        medicines = context.getBean(MedicineRepository.class);
        checker = context.getBean(PrescriptionChecker.class);
        audit = context.getBean(AuditLogger.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("currentMedicineNames", List.of());
        showForm(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] selected = req.getParameterValues("currentMedicineNames");
        List<String> current = selected == null ? List.of() : Arrays.asList(selected);
        req.setAttribute("currentMedicineNames", current);
        String name = req.getParameter("newMedicineName");
        try {
            double dose;
            int times;
            List<String> allergies;
            try {
                dose = Double.parseDouble(req.getParameter("dose"));
                times = Integer.parseInt(req.getParameter("timesPerDay"));
                allergies = PatientInput.parseAllergies(req.getParameter("allergies"));
            } catch (IllegalArgumentException | NullPointerException e) {
                audit.log(null, name, "INVALID_INPUT");
                throw new IllegalArgumentException("Enter valid allergies, dose and whole-number times per day.");
            }
            PrescriptionCheckResponse result = checker.check(name, dose, times, current, allergies, null);
            req.setAttribute("newMedicineName", name);
            req.setAttribute("dose", dose); req.setAttribute("timesPerDay", times);
            req.setAttribute("status", result.status()); req.setAttribute("message", result.message());
            req.setAttribute("severity", result.severity());
            req.getRequestDispatcher("/WEB-INF/jsp/result.jsp").forward(req, res);
        } catch (DataNotReadyException e) {
            res.setStatus(503); req.setAttribute("errorMessage", e.getMessage()); showForm(req, res);
        } catch (IllegalArgumentException | UnknownMedicineException e) {
            res.setStatus(400); req.setAttribute("errorMessage", e.getMessage()); showForm(req, res);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("medicineNames", medicines.findAll().stream().map(MedicineDocument::getName).sorted().toList());
        req.getRequestDispatcher("/WEB-INF/jsp/check.jsp").forward(req, res);
    }
}
