package com.medicinesafetychecker.backend;
public class PatientNotFoundException extends IllegalArgumentException {
    public PatientNotFoundException(String id) { super("Patient not found: " + id); }
}
