package com.medicinesafetychecker.core;

import java.util.List;

/** Business rules for deciding whether a dose or a combination of medicines is safe. */
public class DosageChecker {

    /** True if doseMg falls within the medicine's safe dosage range (inclusive). */
    public boolean isDosageSafe(Medicine medicine, double doseMg) {
        return doseMg >= medicine.getMinSafeDosageMg() && doseMg <= medicine.getMaxSafeDosageMg();
    }

    /** Finds the known interaction (if any) between two medicines, by name. */
    public Interaction findInteraction(List<Interaction> knownInteractions, Medicine a, Medicine b) {
        return knownInteractions.stream()
                .filter(interaction -> interaction.involves(a.getName(), b.getName()))
                .findFirst()
                .orElse(null);
    }
}
