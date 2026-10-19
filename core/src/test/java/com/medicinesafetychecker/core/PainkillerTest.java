package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PainkillerTest {

    private final Painkiller paracetamol = new Painkiller("2", "Paracetamol", "mg", 500, 3000);

    @Test
    void doseJustUnderMaxSingleDosePasses() {
        assertDoesNotThrow(() -> paracetamol.validateDose(499, 1));
    }

    @Test
    void doseJustOverMaxSingleDoseThrows() {
        OverdoseRiskException ex = assertThrows(OverdoseRiskException.class,
                () -> paracetamol.validateDose(501, 1));
        assertEquals(500, ex.getLimit(), 0.0001);
    }

    @Test
    void takingMoreThanFourTimesADayThrowsEvenUnderTheLabelledDailyMax() {
        // 500 * 5 = 2500, which is under the labelled max daily dose of 3000,
        // but exceeds the "no more than 4 doses a day" guideline (4 * 500 = 2000).
        OverdoseRiskException ex = assertThrows(OverdoseRiskException.class,
                () -> paracetamol.validateDose(500, 5));
        assertEquals(2000, ex.getLimit(), 0.0001);
    }
}
