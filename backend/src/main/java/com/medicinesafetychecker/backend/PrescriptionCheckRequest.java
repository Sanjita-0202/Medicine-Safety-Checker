package com.medicinesafetychecker.backend;

import java.util.List;

/** Request body for POST /api/prescriptions/check. */
public record PrescriptionCheckRequest(
        String newMedicineName,
        double dose,
        int timesPerDay,
        List<String> currentMedicineNames) {

    public List<String> currentMedicineNamesOrEmpty() {
        return currentMedicineNames == null ? List.of() : currentMedicineNames;
    }
}
