package com.medicinesafetychecker.core;

/** Thrown when two medicines are known to interact unsafely. Checked, so callers must handle it explicitly. */
public class DangerousCombinationException extends Exception {

    private final Medicine medicineA;
    private final Medicine medicineB;
    private final Severity severity;
    private final String description;

    public DangerousCombinationException(Medicine medicineA, Medicine medicineB,
                                          Severity severity, String description) {
        super(String.format("%s + %s is %s: %s",
                medicineA.getName(), medicineB.getName(), severity, description));
        this.medicineA = medicineA;
        this.medicineB = medicineB;
        this.severity = severity;
        this.description = description;
    }

    public Medicine getMedicineA() {
        return medicineA;
    }

    public Medicine getMedicineB() {
        return medicineB;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }
}
