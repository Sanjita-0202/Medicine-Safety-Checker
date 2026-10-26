package com.medicinesafetychecker.core;

import java.util.List;

/**
 * A medicine with the dosage limits needed to judge whether a given dose is
 * safe. Subclasses supply their own {@link #validateDose} rule.
 */
public abstract class Medicine implements InteractionCheckable {

    /** Shared known-interactions database, queried by {@link #checkConflict}. */
    private static final InteractionGraph KNOWN_INTERACTIONS = new InteractionGraph();

    public static InteractionGraph getKnownInteractions() {
        return KNOWN_INTERACTIONS;
    }

    private final String id;
    private final String name;
    private String drugClass;
    private final String dosageUnit;
    private final double maxSingleDose;
    private final double maxDailyDose;

    protected Medicine(String id, String name, String dosageUnit,
                        double maxSingleDose, double maxDailyDose) {
        if (maxSingleDose < 0 || maxDailyDose < maxSingleDose) {
            throw new IllegalArgumentException("Invalid dosage limits");
        }
        this.id = id;
        this.name = name;
        this.dosageUnit = dosageUnit;
        this.maxSingleDose = maxSingleDose;
        this.maxDailyDose = maxDailyDose;
    }

    /** Checks whether taking {@code dose} {@code timesPerDay} times a day is safe for this medicine. */
    public abstract void validateDose(double dose, int timesPerDay) throws OverdoseRiskException;

    @Override
    public void checkConflict(Medicine other) throws DangerousCombinationException {
        KNOWN_INTERACTIONS.checkPrescription(this, List.of(other));
    }

    public String getId() {
        return id;
    }

    public String getDrugClass() { return drugClass; }

    public void setDrugClass(String drugClass) {
        this.drugClass = drugClass == null ? null : drugClass.strip();
    }

    protected void validateInput(double dose, int timesPerDay) {
        if (!Double.isFinite(dose) || dose <= 0 || timesPerDay <= 0) {
            throw new IllegalArgumentException("Dose and times per day must be positive, finite numbers.");
        }
    }

    public String getName() {
        return name;
    }

    public String getDosageUnit() {
        return dosageUnit;
    }

    public double getMaxSingleDose() {
        return maxSingleDose;
    }

    public double getMaxDailyDose() {
        return maxDailyDose;
    }

    // Equality is by id, not instance - the interaction graph may be checked
    // against a freshly loaded Medicine object that represents the same drug.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Medicine other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
