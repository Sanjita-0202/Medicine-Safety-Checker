package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AntihypertensiveTest {

    // maxDailyDose 2000 -> 90% safety margin is 1800
    private final Antihypertensive lisinopril = new Antihypertensive("3", "Lisinopril", "mg", 500, 2000);

    @Test
    void doseJustUnderMaxSingleDosePasses() {
        assertDoesNotThrow(() -> lisinopril.validateDose(499, 1));
    }

    @Test
    void doseJustOverMaxSingleDoseThrows() {
        OverdoseRiskException ex = assertThrows(OverdoseRiskException.class,
                () -> lisinopril.validateDose(501, 1));
        assertEquals(500, ex.getLimit(), 0.0001);
    }

    @Test
    void dailyTotalJustUnderSafetyMarginPasses() {
        assertDoesNotThrow(() -> lisinopril.validateDose(449, 4)); // total 1796 < 1800
    }

    @Test
    void dailyTotalJustOverSafetyMarginThrows() {
        OverdoseRiskException ex = assertThrows(OverdoseRiskException.class,
                () -> lisinopril.validateDose(451, 4)); // total 1804 > 1800
        assertEquals(1800, ex.getLimit(), 0.0001);
    }
}
