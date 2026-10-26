package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.web.context.support.WebApplicationContextUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** /patients/{id}/report is routed here by PatientServlet's named dispatch. */
@WebServlet(name = "ReportServlet", urlPatterns = "/reports/*")
public class ReportServlet extends HttpServlet {
    private PatientWorkflow patients;
    private PrescriptionRepository prescriptions;

    @Override
    public void init() throws ServletException {
        var context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        patients = context.getBean(PatientWorkflow.class);
        prescriptions = context.getBean(PrescriptionRepository.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String id = (String) req.getAttribute("reportPatientId");
        if (id == null) {
            String path = req.getPathInfo();
            if (path == null || !path.matches("/[^/]+")) { res.sendError(404); return; }
            id = path.substring(1);
        }
        PatientDocument patient;
        try { patient = patients.requirePatient(id); }
        catch (PatientNotFoundException e) { res.sendError(404, "Patient not found."); return; }
        // Read before committing the response so database errors cannot produce a partial success.
        var history = prescriptions.findByPatientIdOrderByDateDesc(id);
        res.setContentType("text/csv");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"patient-" + id.replaceAll("[^A-Za-z0-9_-]", "_") + ".csv\"");
        res.setHeader("Cache-Control", "no-store");
        PrintWriter out = res.getWriter();
        row(out, "Field", "Value");
        row(out, "Patient ID", patient.getId()); row(out, "Name", patient.getName());
        row(out, "Age", patient.getAge()); row(out, "Gender", patient.getGender()); row(out, "Weight (kg)", patient.getWeight());
        row(out, "Allergies", String.join("; ", patient.getAllergies()));
        row(out, "Active medicines", String.join("; ", patient.getActiveMedicines()));
        out.print("\r\n");
        row(out, "Date", "Medicine", "Dose", "Times/day", "Status", "Doctor ID", "Doctor", "Reason");
        for (PrescriptionDocument entry : history) {
            boolean stopped = entry.getStatus() == PrescriptionStatus.STOPPED;
            row(out, entry.getDate(), entry.getMedicineName(), stopped ? "" : entry.getDose(),
                    stopped ? "" : entry.getTimesPerDay(), entry.getStatus(), entry.getDoctorId(), entry.getDoctorName(), entry.getReason());
        }
        out.flush();
    }

    static String cell(Object value) {
        String text = value == null ? "" : value.toString();
        String trimmed = text.stripLeading();
        // Prevent spreadsheet formulas in user-entered text while preserving CSV quoting.
        if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0
                || text.startsWith("\t") || text.startsWith("\r") || text.startsWith("\n")) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private void row(PrintWriter out, Object... fields) {
        out.print(Arrays.stream(fields).map(ReportServlet::cell).collect(Collectors.joining(",")) + "\r\n");
    }
}
