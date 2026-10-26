package com.medicinesafetychecker.backend;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.medicinesafetychecker.core.*;

/** One ordered checking pipeline for patient prescriptions, the standalone page and REST. */
@Service
public class PrescriptionChecker {
    private final MedicineRepository medicines;
    private final InteractionGraphLoader graph;
    private final AuditLogger audit;
    public PrescriptionChecker(MedicineRepository medicines, InteractionGraphLoader graph, AuditLogger audit) {
        this.medicines = medicines; this.graph = graph; this.audit = audit;
    }

    public PrescriptionCheckResponse check(String name, double dose, int timesPerDay,
            List<String> currentNames, List<String> allergies, String patientId) {
        String outcome = "INVALID_INPUT";
        try {
            Medicine medicine = find(name);
            try {
                // Allergy validation precedes dosage, graph readiness and interaction validation.
                AllergyChecker.check(medicine, allergies);
                medicine.validateDose(dose, timesPerDay);
                if (!graph.isReady()) { outcome = "LOADING"; throw new DataNotReadyException(); }
                List<Medicine> current = new ArrayList<>();
                if (currentNames != null) {
                    for (String currentName : currentNames) { current.add(find(currentName)); }
                }
                Medicine.getKnownInteractions().checkPrescription(medicine, current);
                outcome = "SAFE";
                return PrescriptionCheckResponse.safe();
            } catch (AllergyConflictException e) {
                outcome = "ALLERGY_CONFLICT";
                return new PrescriptionCheckResponse(PrescriptionCheckStatus.ALLERGY_CONFLICT, e.getMessage(), null);
            } catch (OverdoseRiskException e) {
                outcome = "OVERDOSE_RISK";
                return PrescriptionCheckResponse.overdoseRisk(e);
            } catch (DangerousCombinationException e) {
                outcome = "DANGEROUS_COMBINATION";
                return PrescriptionCheckResponse.dangerousCombination(e);
            }
        } finally {
            audit.log(patientId, name, outcome);
        }
    }

    private Medicine find(String name) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new IllegalArgumentException("Please choose a valid medicine.");
        }
        return medicines.findByNameIgnoreCase(name.strip())
                .orElseThrow(() -> new UnknownMedicineException(name)).toDomain();
    }
}
