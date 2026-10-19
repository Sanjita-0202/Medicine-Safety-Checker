package com.medicinesafetychecker.core;

import java.util.List;

/**
 * A medicine with the dosage limits needed to judge whether a given dose is
 * safe. Subclasses supply their own {@link #validateDose} rule.
 */
public abstract class Medicine implements InteractionCheckable {

    /** Known dangerous combinations, looked up by medicine name in {@link #checkConflict}. */
    private static final List<Interaction> KNOWN_INTERACTIONS = List.of(
            new Interaction("Aspirin", "Lisinopril", Severity.MODERATE,
                    "NSAIDs may reduce the blood-pressure-lowering effect of ACE inhibitors"),
            new Interaction("Ibuprofen", "Lisinopril", Severity.MODERATE,
                    "NSAIDs may reduce the blood-pressure-lowering effect of ACE inhibitors"),
            new Interaction("Warfarin", "Aspirin", Severity.SEVERE,
                    "Combined use significantly increases bleeding risk")
    );

    private final String id;
    private final String name;
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
        for (Interaction interaction : KNOWN_INTERACTIONS) {
            if (interaction.involves(this.name, other.name)) {
                throw new DangerousCombinationException(this, other, interaction.getSeverity(), interaction.getDescription());
            }
        }
    }

    public String getId() {
        return id;
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
}
