package com.medicinesafetychecker.core;

/**
 * A blood-pressure medicine. These have a narrow therapeutic index, so the
 * daily total is held to 90% of the labelled max daily dose as a safety margin.
 */
public class Antihypertensive extends Medicine {

    private static final double SAFETY_MARGIN = 0.9;

    public Antihypertensive(String id, String name, String dosageUnit,
                             double maxSingleDose, double maxDailyDose) {
        super(id, name, dosageUnit, maxSingleDose, maxDailyDose);
    }

    @Override
    public void validateDose(double dose, int timesPerDay) throws OverdoseRiskException {
        if (dose > getMaxSingleDose()) {
            throw new OverdoseRiskException(this, dose, getMaxSingleDose());
        }
        double safeDailyLimit = getMaxDailyDose() * SAFETY_MARGIN;
        double dailyTotal = dose * timesPerDay;
        if (dailyTotal > safeDailyLimit) {
            throw new OverdoseRiskException(this, dailyTotal, safeDailyLimit);
        }
    }
}
