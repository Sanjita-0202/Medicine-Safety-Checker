package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.medicinesafetychecker.core.CsvLoader;
import com.medicinesafetychecker.core.Interaction;
import com.medicinesafetychecker.core.Medicine;

/**
 * Loads the seed CSVs (data/seed/medicines.csv, data/seed/interactions.csv)
 * into MongoDB on startup. Adds missing medicines and drug classes while
 * retaining custom classifications. Refreshes seed interaction pairs by
 * unordered names so regenerated severity overrides reach the runtime graph.
 * Database-only interaction pairs are retained.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private final MedicineRepository medicineRepository;
    private final InteractionRepository interactionRepository;
    private final Path medicinesCsvPath;
    private final Path interactionsCsvPath;

    public DataSeeder(MedicineRepository medicineRepository,
                       InteractionRepository interactionRepository,
                       @Value("${app.seed.medicines-csv:../data/seed/medicines.csv}") String medicinesCsvPath,
                       @Value("${app.seed.interactions-csv:../data/seed/interactions.csv}") String interactionsCsvPath) {
        this.medicineRepository = medicineRepository;
        this.interactionRepository = interactionRepository;
        this.medicinesCsvPath = Path.of(medicinesCsvPath);
        this.interactionsCsvPath = Path.of(interactionsCsvPath);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (medicineRepository.count() == 0) {
            List<Medicine> medicines = CsvLoader.loadMedicines(medicinesCsvPath);
            medicineRepository.saveAll(medicines.stream().map(DataSeeder::toDocument).toList());
            System.out.println("Seeded " + medicines.size() + " medicines into MongoDB.");
        } else {
            // Upgrade existing local data without replacing custom classifications.
            for (Medicine seed : CsvLoader.loadMedicines(medicinesCsvPath)) {
                var found = medicineRepository.findById(seed.getId());
                if (found.isEmpty()) {
                    medicineRepository.save(toDocument(seed));
                } else {
                    MedicineDocument existing = found.get();
                    if (existing.getDrugClass() == null || existing.getDrugClass().isBlank()) {
                        existing.setDrugClass(seed.getDrugClass());
                        medicineRepository.save(existing);
                    }
                }
            }
            System.out.println("Existing medicines retained; missing drug classes filled from seed data.");
        }

        // Seed CSV is authoritative for its pairs. Preserve extra database-only pairs.
        // This makes regenerated severity overrides effective on the next restart.
        Map<String, InteractionDocument> existingPairs = new HashMap<>();
        for (InteractionDocument existing : interactionRepository.findAll()) {
            existingPairs.put(pair(existing.getMedicineNameA(), existing.getMedicineNameB()), existing);
        }
        int updated = 0;
        for (Interaction interaction : CsvLoader.loadInteractions(interactionsCsvPath)) {
            String key = pair(interaction.getMedicineNameA(), interaction.getMedicineNameB());
            InteractionDocument existing = existingPairs.get(key);
            if (existing == null) {
                InteractionDocument added = toDocument(interaction);
                interactionRepository.save(added);
                existingPairs.put(key, added);
                updated++;
            } else if (existing.getSeverity() != interaction.getSeverity()
                    || !java.util.Objects.equals(existing.getDescription(), interaction.getDescription())) {
                existing.setSeverity(interaction.getSeverity());
                existing.setDescription(interaction.getDescription());
                interactionRepository.save(existing);
                updated++;
            }
        }
        System.out.println("Interaction seed synchronization complete: " + updated + " pairs added or updated.");
    }

    private static String pair(String a, String b) {
        String lowerA = a.strip().toLowerCase(Locale.ROOT);
        String lowerB = b.strip().toLowerCase(Locale.ROOT);
        return lowerA.compareTo(lowerB) <= 0 ? lowerA + "|" + lowerB : lowerB + "|" + lowerA;
    }

    private static MedicineDocument toDocument(Medicine medicine) {
        MedicineDocument document = new MedicineDocument(medicine.getId(), medicine.getName(), MedicineType.fromDomain(medicine),
                medicine.getDosageUnit(), medicine.getMaxSingleDose(), medicine.getMaxDailyDose());
        document.setDrugClass(medicine.getDrugClass());
        return document;
    }

    private static InteractionDocument toDocument(Interaction interaction) {
        return new InteractionDocument(null, interaction.getMedicineNameA(), interaction.getMedicineNameB(),
                interaction.getSeverity(), interaction.getDescription());
    }
}
