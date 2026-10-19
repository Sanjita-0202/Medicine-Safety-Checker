package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DosageCheckerTest {

    private final DosageChecker checker = new DosageChecker();
    private final Medicine paracetamol = new Medicine("1", "Paracetamol", "Acetaminophen", 500, 1000);

    @Test
    void doseWithinRangeIsSafe() {
        assertTrue(checker.isDosageSafe(paracetamol, 650));
    }

    @Test
    void doseBelowRangeIsUnsafe() {
        assertFalse(checker.isDosageSafe(paracetamol, 200));
    }

    @Test
    void doseAboveRangeIsUnsafe() {
        assertFalse(checker.isDosageSafe(paracetamol, 1500));
    }

    @Test
    void findsKnownInteractionRegardlessOfOrder() {
        Medicine warfarin = new Medicine("2", "Warfarin", "Warfarin", 2, 10);
        Interaction interaction = new Interaction("Paracetamol", "Warfarin",
                Severity.MODERATE, "May increase anticoagulant effect");

        Interaction found = checker.findInteraction(List.of(interaction), warfarin, paracetamol);

        assertEquals(Severity.MODERATE, found.getSeverity());
    }

    @Test
    void returnsNullWhenNoInteractionIsKnown() {
        Medicine ibuprofen = new Medicine("3", "Ibuprofen", "Ibuprofen", 200, 800);

        Interaction found = checker.findInteraction(List.of(), paracetamol, ibuprofen);

        assertNull(found);
    }
}
