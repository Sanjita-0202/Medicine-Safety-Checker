package com.medicinesafetychecker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Known drug-drug interactions as a graph: medicines are nodes, a known
 * dangerous combination is an edge. The adjacency list answers "what does
 * this medicine conflict with" in O(1); a parallel map holds the severity
 * and description for each edge.
 */
public class InteractionGraph {

    private final Map<Medicine, Set<Medicine>> adjacency = new ConcurrentHashMap<>();
    private final Map<Medicine, Map<Medicine, Interaction>> edgeDetails = new ConcurrentHashMap<>();

    /** Records a known interaction between a and b, in both directions. */
    public void addInteraction(Medicine a, Medicine b, Severity severity, String description) {
        Interaction interaction = new Interaction(a.getName(), b.getName(), severity, description);

        adjacency.computeIfAbsent(a, m -> ConcurrentHashMap.newKeySet()).add(b);
        adjacency.computeIfAbsent(b, m -> ConcurrentHashMap.newKeySet()).add(a);

        edgeDetails.computeIfAbsent(a, m -> new ConcurrentHashMap<>()).put(b, interaction);
        edgeDetails.computeIfAbsent(b, m -> new ConcurrentHashMap<>()).put(a, interaction);
    }

    /** Medicines known to conflict with m (empty set if none, or m is unknown to the graph). */
    public Set<Medicine> getConflicts(Medicine m) {
        return Collections.unmodifiableSet(adjacency.getOrDefault(m, Set.of()));
    }

    /** Throws on the first known conflict between newMed and any of currentMeds. */
    public void checkPrescription(Medicine newMed, List<Medicine> currentMeds) throws DangerousCombinationException {
        for (Medicine current : currentMeds) {
            Interaction interaction = interactionBetween(newMed, current);
            if (interaction != null) {
                throw new DangerousCombinationException(newMed, current, interaction.getSeverity(), interaction.getDescription());
            }
        }
    }

    /** Every known conflict between newMed and any of currentMeds (empty list if none). */
    public List<DangerousCombinationException> findAllConflicts(Medicine newMed, List<Medicine> currentMeds) {
        List<DangerousCombinationException> conflicts = new ArrayList<>();
        for (Medicine current : currentMeds) {
            Interaction interaction = interactionBetween(newMed, current);
            if (interaction != null) {
                conflicts.add(new DangerousCombinationException(newMed, current, interaction.getSeverity(), interaction.getDescription()));
            }
        }
        return conflicts;
    }

    private Interaction interactionBetween(Medicine a, Medicine b) {
        Map<Medicine, Interaction> neighbours = edgeDetails.get(a);
        return neighbours == null ? null : neighbours.get(b);
    }
}
