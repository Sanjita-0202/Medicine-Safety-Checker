package com.medicinesafetychecker.backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medicinesafetychecker.core.DangerousCombinationException;
import com.medicinesafetychecker.core.Medicine;
import com.medicinesafetychecker.core.OverdoseRiskException;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final MedicineRepository medicineRepository;
    private final InteractionGraphLoader interactionGraphLoader;

    public PrescriptionController(MedicineRepository medicineRepository,
                                   InteractionGraphLoader interactionGraphLoader) {
        this.medicineRepository = medicineRepository;
        this.interactionGraphLoader = interactionGraphLoader;
    }

    /** POST /api/prescriptions/check - dosage check + known-interaction check for a new medicine. */
    @PostMapping("/check")
    public ResponseEntity<?> checkPrescription(@RequestBody PrescriptionCheckRequest request) {
        if (!interactionGraphLoader.isReady()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiError("LOADING", "Interaction data is still loading - please retry shortly."));
        }

        Medicine newMedicine = findMedicine(request.newMedicineName());
        List<Medicine> currentMedicines = new ArrayList<>();
        for (String name : request.currentMedicineNamesOrEmpty()) {
            currentMedicines.add(findMedicine(name));
        }

        try {
            newMedicine.validateDose(request.dose(), request.timesPerDay());
        } catch (OverdoseRiskException e) {
            return ResponseEntity.ok(PrescriptionCheckResponse.overdoseRisk(e));
        }

        try {
            Medicine.getKnownInteractions().checkPrescription(newMedicine, currentMedicines);
        } catch (DangerousCombinationException e) {
            return ResponseEntity.ok(PrescriptionCheckResponse.dangerousCombination(e));
        }

        return ResponseEntity.ok(PrescriptionCheckResponse.safe());
    }

    private Medicine findMedicine(String name) {
        return medicineRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new UnknownMedicineException(name))
                .toDomain();
    }
}
