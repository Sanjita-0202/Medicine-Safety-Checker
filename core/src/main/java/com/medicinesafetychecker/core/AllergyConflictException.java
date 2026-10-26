package com.medicinesafetychecker.core;

/** Checked exception: a recorded allergy matches the medicine or drug class. */
public class AllergyConflictException extends Exception {
    public AllergyConflictException(String allergy, Medicine medicine) {
        super("Patient is allergic to " + allergy + "; " + medicine.getName()
                + " cannot be prescribed.");
    }
}
