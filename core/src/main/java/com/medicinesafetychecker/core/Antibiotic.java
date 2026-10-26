package com.medicinesafetychecker.core;

/** A course antibiotic: each single dose and the daily total must stay within the labelled limits. */
public class Antibiotic extends Medicine {

    public Antibiotic(String id, String name, String dosageUnit,
                       double maxSingleDose, double maxDailyDose) {
        super(id, name, dosageUnit, maxSingleDose, maxDailyDose);
    }

    @Override
    public void validateDose(double dose, int timesPerDay) throws OverdoseRiskException {
        validateInput(dose, timesPerDay);
        if (dose > getMaxSingleDose()) {
            throw new OverdoseRiskException(this, dose, getMaxSingleDose());
        }
        double dailyTotal = dose * timesPerDay;
        if (dailyTotal > getMaxDailyDose()) {
            throw new OverdoseRiskException(this, dailyTotal, getMaxDailyDose());
        }
    }
}
