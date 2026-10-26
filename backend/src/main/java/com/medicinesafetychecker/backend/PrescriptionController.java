package com.medicinesafetychecker.backend;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionChecker checker;
    private final PatientWorkflow patients;
    public PrescriptionController(PrescriptionChecker checker, PatientWorkflow patients) {
        this.checker = checker; this.patients = patients;
    }

    @PostMapping("/check")
    public PrescriptionCheckResponse checkPrescription(@RequestBody PrescriptionCheckRequest request) {
        List<String> current = new ArrayList<>(request.currentMedicineNamesOrEmpty());
        List<String> allergies = new ArrayList<>(request.allergies() == null ? List.of() : request.allergies());
        if (request.patientId() != null && !request.patientId().isBlank()) {
            PatientDocument patient = patients.requirePatient(request.patientId());
            current.addAll(patient.getActiveMedicines());
            allergies.addAll(patient.getAllergies());
        }
        return checker.check(request.newMedicineName(), request.dose(), request.timesPerDay(),
                current, allergies, request.patientId());
    }
}
