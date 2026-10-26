package com.medicinesafetychecker.backend;

import java.util.List;

public record PrescriptionCheckRequest(String newMedicineName, double dose, int timesPerDay,
        List<String> currentMedicineNames, String patientId, List<String> allergies) {
    public PrescriptionCheckRequest(String name, double dose, int times, List<String> current) {
        this(name, dose, times, current, null, null);
    }
    public List<String> currentMedicineNamesOrEmpty() {
        return currentMedicineNames == null ? List.of() : currentMedicineNames;
    }
}
