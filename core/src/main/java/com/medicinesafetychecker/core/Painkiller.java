package com.medicinesafetychecker.core;

/**
 * An OTC-style painkiller. In addition to the single-dose limit, common
 * painkiller guidance caps intake at 4 doses a day, so the effective daily
 * limit is whichever is smaller: the labelled max daily dose, or 4 single doses.
 */
public class Painkiller extends Medicine {

    private static final int MAX_RECOMMENDED_TIMES_PER_DAY = 4;

    public Painkiller(String id, String name, String dosageUnit,
                       double maxSingleDose, double maxDailyDose) {
        super(id, name, dosageUnit, maxSingleDose, maxDailyDose);
    }

    @Override
    public void validateDose(double dose, int timesPerDay) throws OverdoseRiskException {
        validateInput(dose, timesPerDay);
        if (dose > getMaxSingleDose()) {
            throw new OverdoseRiskException(this, dose, getMaxSingleDose());
        }
        double effectiveDailyLimit = Math.min(getMaxDailyDose(), getMaxSingleDose() * MAX_RECOMMENDED_TIMES_PER_DAY);
        double dailyTotal = dose * timesPerDay;
        if (dailyTotal > effectiveDailyLimit) {
            throw new OverdoseRiskException(this, dailyTotal, effectiveDailyLimit);
        }
    }
}
