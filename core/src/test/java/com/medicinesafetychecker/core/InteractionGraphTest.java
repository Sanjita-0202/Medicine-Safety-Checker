package com.medicinesafetychecker.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionGraphTest {

    private final InteractionGraph graph = new InteractionGraph();

    private final Medicine warfarin = new Antihypertensive("w1", "Warfarin", "mg", 10, 10);
    private final Medicine aspirin = new Painkiller("a1", "Aspirin", "mg", 300, 1200);
    private final Medicine ibuprofen = new Painkiller("i1", "Ibuprofen", "mg", 400, 1200);

    @Test
    void addInteractionStoresBothDirections() {
        graph.addInteraction(warfarin, aspirin, Severity.SEVERE, "Increases bleeding risk");

        assertTrue(graph.getConflicts(warfarin).contains(aspirin));
        assertTrue(graph.getConflicts(aspirin).contains(warfarin));
    }

    @Test
    void getConflictsIsEmptyForAnUnknownMedicine() {
        assertTrue(graph.getConflicts(ibuprofen).isEmpty());
    }

    @Test
    void checkPrescriptionThrowsOnTheFirstConflict() {
        graph.addInteraction(warfarin, aspirin, Severity.SEVERE, "Increases bleeding risk");

        DangerousCombinationException ex = assertThrows(DangerousCombinationException.class,
                () -> graph.checkPrescription(aspirin, List.of(ibuprofen, warfarin)));

        assertEquals(Severity.SEVERE, ex.getSeverity());
    }

    @Test
    void checkPrescriptionPassesWhenNothingConflicts() {
        assertDoesNotThrow(() -> graph.checkPrescription(aspirin, List.of(ibuprofen)));
    }

    @Test
    void findAllConflictsReturnsOnlyTheMedicinesThatConflict() {
        graph.addInteraction(warfarin, aspirin, Severity.SEVERE, "Increases bleeding risk");
        graph.addInteraction(warfarin, ibuprofen, Severity.MODERATE, "May reduce anticoagulant effect");

        List<DangerousCombinationException> conflicts =
                graph.findAllConflicts(warfarin, List.of(aspirin, ibuprofen));

        assertEquals(2, conflicts.size());
    }

    @Test
    void findAllConflictsExcludesNonConflictingMedicines() {
        graph.addInteraction(warfarin, aspirin, Severity.SEVERE, "Increases bleeding risk");

        List<DangerousCombinationException> conflicts =
                graph.findAllConflicts(warfarin, List.of(aspirin, ibuprofen));

        assertEquals(1, conflicts.size());
    }
}
