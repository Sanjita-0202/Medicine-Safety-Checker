package com.medicinesafetychecker.core;

/** Thrown when a dose breaches a medicine's safe-dose limit. Checked, so callers must handle it explicitly. */
public class OverdoseRiskException extends Exception {

    private final Medicine medicine;
    private final double givenDose;
    private final double limit;

    public OverdoseRiskException(Medicine medicine, double givenDose, double limit) {
        super(String.format("%.2f%s exceeds the safe limit of %.2f%s for %s",
                givenDose, medicine.getDosageUnit(), limit, medicine.getDosageUnit(), medicine.getName()));
        this.medicine = medicine;
        this.givenDose = givenDose;
        this.limit = limit;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public double getGivenDose() {
        return givenDose;
    }

    public double getLimit() {
        return limit;
    }
}
