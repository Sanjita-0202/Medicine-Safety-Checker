package com.medicinesafetychecker.backend;

/** Thrown when a request references a medicine name that isn't in the database. */
public class UnknownMedicineException extends RuntimeException {
    public UnknownMedicineException(String name) {
        super("Unknown medicine: " + name);
    }
}
