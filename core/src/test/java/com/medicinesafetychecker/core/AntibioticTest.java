package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AntibioticTest {

    private final Antibiotic amoxicillin = new Antibiotic("1", "Amoxicillin", "mg", 500, 2000);

    @Test
    void doseJustUnderMaxSingleDosePasses() {
        assertDoesNotThrow(() -> amoxicillin.validateDose(499, 1));
    }

    @Test
    void doseJustOverMaxSingleDoseThrows() {
        OverdoseRiskException ex = assertThrows(OverdoseRiskException.class,
                () -> amoxicillin.validateDose(501, 1));
        assertEquals(500, ex.getLimit(), 0.0001);
    }

    @Test
    void dailyTotalOverLimitThrowsEvenWhenSingleDoseIsSafe() {
        OverdoseRiskException ex = assertThrows(OverdoseRiskException.class,
                () -> amoxicillin.validateDose(500, 5));
        assertEquals(2000, ex.getLimit(), 0.0001);
    }
}
