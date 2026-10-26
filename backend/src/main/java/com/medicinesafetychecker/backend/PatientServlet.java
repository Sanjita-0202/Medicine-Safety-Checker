package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.context.support.WebApplicationContextUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "PatientServlet", urlPatterns = {"/patients", "/patients/*"})
public class PatientServlet extends HttpServlet {
    private PatientRepository patientRepository;
    private PrescriptionRepository prescriptionRepository;
    private MedicineRepository medicineRepository;
    private PatientWorkflow workflow;
    private AuditLogger audit;

    @Override
    public void init() throws ServletException {
        var context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        patientRepository = context.getBean(PatientRepository.class);
        prescriptionRepository = context.getBean(PrescriptionRepository.class);
        medicineRepository = context.getBean(MedicineRepository.class);
        workflow = context.getBean(PatientWorkflow.class);
        audit = context.getBean(AuditLogger.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] path = segments(req);
        try {
            if (path.length == 0) {
                String query = req.getParameter("q");
                query = query == null ? "" : query.strip();
                String lower = query.toLowerCase(Locale.ROOT);
                req.setAttribute("query", query);
                req.setAttribute("patients", patientRepository.findAll().stream()
                        .filter(patient -> patient.getName().toLowerCase(Locale.ROOT).contains(lower)
                                || patient.getId().toLowerCase(Locale.ROOT).contains(lower))
                        .sorted(Comparator.comparing(PatientDocument::getName, String.CASE_INSENSITIVE_ORDER)).toList());
                view(req, res, "patients");
            } else if (path.length == 1 && path[0].equals("add")) {
                req.setAttribute("form", Map.of());
                view(req, res, "add-patient");
            } else if (path.length == 1) {
                req.setAttribute("patient", workflow.requirePatient(path[0]));
                req.setAttribute("history", prescriptionRepository.findByPatientIdOrderByDateDesc(path[0]));
                view(req, res, "patient-profile");
            } else if (path.length == 2 && path[1].equals("edit")) {
                PatientDocument patient = workflow.requirePatient(path[0]);
                req.setAttribute("patient", patient);
                req.setAttribute("form", Map.of("name", patient.getName(), "age", Integer.toString(patient.getAge()),
                        "gender", patient.getGender(), "weight", Double.toString(patient.getWeight()),
                        "allergies", String.join(", ", patient.getAllergies())));
                view(req, res, "edit-patient");
            } else if (path.length == 2 && path[1].equals("prescribe")) {
                showPrescribe(req, res, path[0]);
            } else if (path.length == 2 && path[1].equals("report")) {
                req.setAttribute("reportPatientId", path[0]);
                getServletContext().getNamedDispatcher("ReportServlet").forward(req, res);
            } else { res.sendError(404); }
        } catch (PatientNotFoundException e) { res.sendError(404, "Patient not found."); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] path = segments(req);
        try {
            if (path.length == 1 && path[0].equals("add")) {
                savePatient(req, res, null);
            } else if (path.length == 2 && path[1].equals("edit")) {
                savePatient(req, res, path[0]);
            } else if (path.length == 2 && path[1].equals("stop")) {
                workflow.stop(path[0], req.getParameter("medicineName"), doctor(req));
                redirect(req, res, path[0]);
            } else if (path.length == 2 && path[1].equals("prescribe")) {
                prescribe(req, res, path[0]);
            } else { res.sendError(404); }
        } catch (PatientNotFoundException e) { res.sendError(404, "Patient not found."); }
        catch (IllegalArgumentException e) { res.sendError(400, e.getMessage()); }
    }

    private void savePatient(HttpServletRequest req, HttpServletResponse res, String id)
            throws ServletException, IOException {
        if (id != null) { req.setAttribute("patient", workflow.requirePatient(id)); }
        Map<String, String> form = new LinkedHashMap<>();
        for (String key : List.of("name", "age", "gender", "weight", "allergies")) {
            String value = req.getParameter(key);
            form.put(key, value == null ? "" : value);
        }
        req.setAttribute("form", form);
        try {
            PatientInput input = PatientInput.parse(form);
            PatientDocument saved = id == null ? workflow.add(input) : workflow.edit(id, input);
            redirect(req, res, saved.getId());
        } catch (IllegalArgumentException e) {
            res.setStatus(400);
            req.setAttribute("errorMessage", e.getMessage());
            view(req, res, id == null ? "add-patient" : "edit-patient");
        }
    }

    private void prescribe(HttpServletRequest req, HttpServletResponse res, String id)
            throws ServletException, IOException {
        workflow.requirePatient(id);
        try {
            double dose;
            int times;
            try {
                dose = Double.parseDouble(req.getParameter("dose"));
                times = Integer.parseInt(req.getParameter("timesPerDay"));
            } catch (NumberFormatException | NullPointerException e) {
                audit.log(id, req.getParameter("medicineName"), "INVALID_INPUT");
                throw new IllegalArgumentException("Enter a valid dose and whole-number times per day.");
            }
            workflow.prescribe(id, req.getParameter("medicineName"), dose, times, doctor(req));
            redirect(req, res, id);
        } catch (DataNotReadyException e) {
            res.setStatus(503); req.setAttribute("errorMessage", e.getMessage()); showPrescribe(req, res, id);
        } catch (IllegalArgumentException | UnknownMedicineException e) {
            res.setStatus(400); req.setAttribute("errorMessage", e.getMessage()); showPrescribe(req, res, id);
        }
    }

    private void showPrescribe(HttpServletRequest req, HttpServletResponse res, String id)
            throws ServletException, IOException {
        req.setAttribute("patient", workflow.requirePatient(id));
        req.setAttribute("medicineNames", medicineRepository.findAll().stream().map(MedicineDocument::getName).sorted().toList());
        view(req, res, "new-prescription");
    }

    private DoctorDocument doctor(HttpServletRequest req) {
        return (DoctorDocument) req.getSession(false).getAttribute("doctor");
    }

    private void redirect(HttpServletRequest req, HttpServletResponse res, String id) throws IOException {
        String path = req.getPathInfo();
        String notice = path != null && path.endsWith("/prescribe") ? "prescription"
                : path != null && path.endsWith("/stop") ? "stopped" : "patient";
        res.sendRedirect(req.getContextPath() + "/patients/" + id + "?notice=" + notice);
    }

    private void view(HttpServletRequest req, HttpServletResponse res, String name) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/" + name + ".jsp").forward(req, res);
    }

    private String[] segments(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) { return new String[0]; }
        return path.replaceAll("^/|/$", "").split("/");
    }
}
