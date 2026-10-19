package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
 * Plain Jakarta servlet (not a Spring @Controller) that drives the JSP UI at
 * /check. It is registered with the embedded container via
 * {@code @ServletComponentScan} on {@link BackendApplication}, so Spring
 * beans are fetched by hand in {@link #init()} rather than @Autowired.
 *
 * <p>It runs the same dose/interaction checks as
 * {@link PrescriptionController#checkPrescription}, just rendered as HTML
 * instead of JSON.
 */
@WebServlet(name = "PrescriptionServlet", urlPatterns = "/check")
public class PrescriptionServlet extends HttpServlet {

    private static final String CHECK_JSP = "/WEB-INF/jsp/check.jsp";
    private static final String RESULT_JSP = "/WEB-INF/jsp/result.jsp";

    private MedicineRepository medicineRepository;
    private InteractionGraphLoader interactionGraphLoader;

    @Override
    public void init() throws ServletException {
        super.init();
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        this.medicineRepository = context.getBean(MedicineRepository.class);
        this.interactionGraphLoader = context.getBean(InteractionGraphLoader.class);
    }

    /** GET /check - show the empty form. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("medicineNames", loadMedicineNames());
        request.setAttribute("currentMedicineNames", List.of());
        request.getRequestDispatcher(CHECK_JSP).forward(request, response);
    }

    /** POST /check - run the dose + interaction checks and show the outcome. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<String> medicineNames = loadMedicineNames();
        request.setAttribute("medicineNames", medicineNames);

        if (!interactionGraphLoader.isReady()) {
            request.setAttribute("errorMessage", "Interaction data is still loading - please try again shortly.");
            request.getRequestDispatcher(CHECK_JSP).forward(request, response);
            return;
        }

        String newMedicineName = request.getParameter("newMedicineName");
        String[] selectedCurrentMedicines = request.getParameterValues("currentMedicineNames");
        List<String> currentMedicineNames = selectedCurrentMedicines == null
                ? List.of() : Arrays.asList(selectedCurrentMedicines);

        request.setAttribute("newMedicineName", newMedicineName);
        request.setAttribute("dose", request.getParameter("dose"));
        request.setAttribute("timesPerDay", request.getParameter("timesPerDay"));
        request.setAttribute("currentMedicineNames", currentMedicineNames);

        try {
            double dose = Double.parseDouble(request.getParameter("dose"));
            int timesPerDay = Integer.parseInt(request.getParameter("timesPerDay"));

            Medicine newMedicine = findMedicine(newMedicineName);
            List<Medicine> currentMedicines = new ArrayList<>();
            for (String name : currentMedicineNames) {
                currentMedicines.add(findMedicine(name));
            }

            newMedicine.validateDose(dose, timesPerDay);
            Medicine.getKnownInteractions().checkPrescription(newMedicine, currentMedicines);

            request.setAttribute("status", PrescriptionCheckStatus.SAFE);
        } catch (NumberFormatException | UnknownMedicineException e) {
            request.setAttribute("errorMessage", "Please enter a valid dose, times per day, and medicine name.");
            request.getRequestDispatcher(CHECK_JSP).forward(request, response);
            return;
        } catch (OverdoseRiskException e) {
            request.setAttribute("status", PrescriptionCheckStatus.OVERDOSE_RISK);
            request.setAttribute("message", e.getMessage());
        } catch (DangerousCombinationException e) {
            request.setAttribute("status", PrescriptionCheckStatus.DANGEROUS_COMBINATION);
            request.setAttribute("message", e.getMessage());
            request.setAttribute("severity", e.getSeverity());
        }

        request.getRequestDispatcher(RESULT_JSP).forward(request, response);
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
