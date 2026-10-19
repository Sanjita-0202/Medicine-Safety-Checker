package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Medicine.checkConflict() delegates to Medicine.getKnownInteractions(), the
 * shared InteractionGraph. Uses ids unique to this test so it doesn't
 * collide with interactions added by other tests sharing that same graph.
 */
class MedicineCheckConflictTest {

    @Test
    void checkConflictThrowsWhenTheSharedGraphHasAKnownInteraction() {
        Medicine medA = new Antibiotic("check-conflict-a", "TestAmoxicillin", "mg", 500, 2000);
        Medicine medB = new Painkiller("check-conflict-b", "TestAspirin", "mg", 300, 1200);
        Medicine.getKnownInteractions().addInteraction(medA, medB, Severity.MODERATE, "Test-only interaction");

        assertThrows(DangerousCombinationException.class, () -> medA.checkConflict(medB));
    }

    @Test
    void checkConflictPassesWhenTheSharedGraphHasNoKnownInteraction() {
        Medicine medA = new Antibiotic("check-conflict-c", "TestCefalexin", "mg", 500, 2000);
        Medicine medB = new Antihypertensive("check-conflict-d", "TestLisinopril", "mg", 20, 40);

        assertDoesNotThrow(() -> medA.checkConflict(medB));
    }
}
