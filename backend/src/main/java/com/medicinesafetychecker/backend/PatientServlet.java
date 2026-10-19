package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.medicinesafetychecker.core.DangerousCombinationException;
import com.medicinesafetychecker.core.Medicine;
import com.medicinesafetychecker.core.OverdoseRiskException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Plain Jakarta servlet (not a Spring @Controller) for the patient pages:
 * list, profile (with prescription history), and "new prescription" from a
 * patient's profile. Registered via @ServletComponentScan on
 * {@link BackendApplication}, same as {@link PrescriptionServlet}, with
 * Spring beans fetched by hand in {@link #init()}.
 *
 * <p>One servlet owns the whole /patients/* subtree, dispatching on
 * {@code pathInfo}:
 * <ul>
 *   <li>GET  /patients                 - list all patients</li>
 *   <li>GET  /patients/{id}            - one patient's profile + history</li>
 *   <li>GET  /patients/{id}/prescribe  - "new prescription" form</li>
 *   <li>POST /patients/{id}/prescribe  - run the check, save it, update active medicines</li>
 * </ul>
 */
@WebServlet(name = "PatientServlet", urlPatterns = {"/patients", "/patients/*"})
public class PatientServlet extends HttpServlet {

    private static final String LIST_JSP = "/WEB-INF/jsp/patients.jsp";
    private static final String PROFILE_JSP = "/WEB-INF/jsp/patient-profile.jsp";
    private static final String PRESCRIBE_JSP = "/WEB-INF/jsp/new-prescription.jsp";

    private PatientRepository patientRepository;
    private PrescriptionRepository prescriptionRepository;
    private MedicineRepository medicineRepository;
    private InteractionGraphLoader interactionGraphLoader;

    @Override
    public void init() throws ServletException {
        super.init();
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        this.patientRepository = context.getBean(PatientRepository.class);
        this.prescriptionRepository = context.getBean(PrescriptionRepository.class);
        this.medicineRepository = context.getBean(MedicineRepository.class);
        this.interactionGraphLoader = context.getBean(InteractionGraphLoader.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] segments = pathSegments(request);

        if (segments.length == 0) {
            showPatientList(request, response);
        } else if (segments.length == 1) {
            showPatientProfile(request, response, segments[0]);
        } else if (segments.length == 2 && "prescribe".equals(segments[1])) {
            showPrescribeForm(request, response, segments[0]);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] segments = pathSegments(request);
        if (segments.length == 2 && "prescribe".equals(segments[1])) {
            processPrescription(request, response, segments[0]);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showPatientList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("patients", patientRepository.findAll());
        request.getRequestDispatcher(LIST_JSP).forward(request, response);
    }

    private void showPatientProfile(HttpServletRequest request, HttpServletResponse response, String patientId)
            throws ServletException, IOException {
        Optional<PatientDocument> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown patient: " + patientId);
            return;
        }
        request.setAttribute("patient", patient.get());
        request.setAttribute("history", prescriptionRepository.findByPatientIdOrderByDateDesc(patientId));
        request.getRequestDispatcher(PROFILE_JSP).forward(request, response);
    }

    private void showPrescribeForm(HttpServletRequest request, HttpServletResponse response, String patientId)
            throws ServletException, IOException {
        Optional<PatientDocument> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown patient: " + patientId);
            return;
        }
        request.setAttribute("patient", patient.get());
        request.setAttribute("medicineNames", loadMedicineNames());
        request.getRequestDispatcher(PRESCRIBE_JSP).forward(request, response);
    }

    /**
     * Runs the same dose + interaction checks as {@link PrescriptionServlet},
     * but against the patient's active medicines (instead of a checkbox list
     * the user picks by hand), and persists the outcome as a
     * {@link PrescriptionDocument} either way - approved or rejected - so the
     * profile's history table shows both.
     */
    private void processPrescription(HttpServletRequest request, HttpServletResponse response, String patientId)
            throws ServletException, IOException {
        Optional<PatientDocument> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown patient: " + patientId);
            return;
        }
        PatientDocument patient = patientOpt.get();

        if (!interactionGraphLoader.isReady()) {
            showPrescribeFormWithError(request, response, patient,
                    "Interaction data is still loading - please try again shortly.");
            return;
        }

        String medicineName = request.getParameter("medicineName");

        try {
            double dose = Double.parseDouble(request.getParameter("dose"));
            int timesPerDay = Integer.parseInt(request.getParameter("timesPerDay"));

            Medicine newMedicine = findMedicine(medicineName);
            List<Medicine> currentMedicines = new ArrayList<>();
            for (String name : patient.getActiveMedicines()) {
                currentMedicines.add(findMedicine(name));
            }

            PrescriptionStatus status;
            String reason;
            try {
                newMedicine.validateDose(dose, timesPerDay);
                Medicine.getKnownInteractions().checkPrescription(newMedicine, currentMedicines);
                status = PrescriptionStatus.APPROVED;
                reason = null;
            } catch (OverdoseRiskException | DangerousCombinationException e) {
                status = PrescriptionStatus.REJECTED;
                reason = e.getMessage();
            }

            prescriptionRepository.save(new PrescriptionDocument(null, patientId, medicineName, dose,
                    timesPerDay, LocalDate.now(), status, reason));

            if (status == PrescriptionStatus.APPROVED && !patient.getActiveMedicines().contains(medicineName)) {
                patient.getActiveMedicines().add(medicineName);
                patientRepository.save(patient);
            }
        } catch (NumberFormatException | UnknownMedicineException e) {
            showPrescribeFormWithError(request, response, patient,
                    "Please enter a valid medicine, dose, and times per day.");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/patients/" + patientId);
    }

    private void showPrescribeFormWithError(HttpServletRequest request, HttpServletResponse response,
                                             PatientDocument patient, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("patient", patient);
        request.setAttribute("medicineNames", loadMedicineNames());
        request.setAttribute("errorMessage", errorMessage);
        request.getRequestDispatcher(PRESCRIBE_JSP).forward(request, response);
    }

    /** Splits the servlet path after "/patients" into segments, e.g. "/p1/prescribe" -> ["p1", "prescribe"]. */
    private String[] pathSegments(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.split("/");
    }

    private List<String> loadMedicineNames() {
        List<String> names = new ArrayList<>();
        for (MedicineDocument doc : medicineRepository.findAll()) {
            names.add(doc.getName());
        }
        Collections.sort(names);
        return names;
    }

    private Medicine findMedicine(String name) {
        return medicineRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new UnknownMedicineException(name))
                .toDomain();
    }
}
