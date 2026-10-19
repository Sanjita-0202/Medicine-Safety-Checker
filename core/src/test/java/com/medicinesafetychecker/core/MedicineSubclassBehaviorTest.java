package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Same dosage limits and the same dose/frequency - different outcome per subclass's rule. */
class MedicineSubclassBehaviorTest {

    @Test
    void frequencyGuidelineMakesPainkillerStricterThanAntibiotic() {
        Antibiotic antibiotic = new Antibiotic("1", "Amoxicillin", "mg", 500, 3000);
        Painkiller painkiller = new Painkiller("2", "Paracetamol", "mg", 500, 3000);

        // 500mg x 5/day = 2500mg: under the 3000mg daily max, so the antibiotic is fine...
        assertDoesNotThrow(() -> antibiotic.validateDose(500, 5));
        // ...but the painkiller caps intake at 4 doses/day (4 x 500 = 2000), so it isn't.
        assertThrows(OverdoseRiskException.class, () -> painkiller.validateDose(500, 5));
    }

    @Test
    void safetyMarginMakesAntihypertensiveStricterThanAntibiotic() {
        Antibiotic antibiotic = new Antibiotic("1", "Amoxicillin", "mg", 500, 2000);
        Antihypertensive antihypertensive = new Antihypertensive("3", "Lisinopril", "mg", 500, 2000);

        // 475mg x 4/day = 1900mg: under the 2000mg daily max, so the antibiotic is fine...
        assertDoesNotThrow(() -> antibiotic.validateDose(475, 4));
        // ...but the antihypertensive only allows 90% of that (1800mg), so it isn't.
        assertThrows(OverdoseRiskException.class, () -> antihypertensive.validateDose(475, 4));
    }
}
