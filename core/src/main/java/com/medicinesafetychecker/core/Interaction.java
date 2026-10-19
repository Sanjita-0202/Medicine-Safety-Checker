package com.medicinesafetychecker.core;

/** A known interaction between two medicines, identified by name. */
public class Interaction {

    private final String medicineNameA;
    private final String medicineNameB;
    private final Severity severity;
    private final String description;

    public Interaction(String medicineNameA, String medicineNameB,
                        Severity severity, String description) {
        this.medicineNameA = medicineNameA;
        this.medicineNameB = medicineNameB;
        this.severity = severity;
        this.description = description;
    }

    /** True if this interaction involves both of the given medicine names, in either order. */
    public boolean involves(String nameA, String nameB) {
        return (medicineNameA.equalsIgnoreCase(nameA) && medicineNameB.equalsIgnoreCase(nameB))
                || (medicineNameA.equalsIgnoreCase(nameB) && medicineNameB.equalsIgnoreCase(nameA));
    }

    public String getMedicineNameA() {
        return medicineNameA;
    }

    public String getMedicineNameB() {
        return medicineNameB;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }
}
