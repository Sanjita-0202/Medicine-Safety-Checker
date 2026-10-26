package com.medicinesafetychecker.core;

import java.util.List;

public final class AllergyChecker {
    private AllergyChecker() { }

    public static void check(Medicine medicine, List<String> allergies) throws AllergyConflictException {
        if (allergies == null) { return; }
        for (String allergy : allergies) {
            if (allergy == null || allergy.isBlank()) { continue; }
            String value = allergy.strip();
            if (value.equalsIgnoreCase(medicine.getName())
                    || value.equalsIgnoreCase(medicine.getDrugClass())) {
                throw new AllergyConflictException(value, medicine);
            }
        }
    }
}
