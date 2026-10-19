package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.medicinesafetychecker.core.CsvLoader;
import com.medicinesafetychecker.core.Interaction;
import com.medicinesafetychecker.core.Medicine;

/**
 * Loads the seed CSVs (data/seed/medicines.csv, data/seed/interactions.csv)
 * into MongoDB on startup. Each collection is only seeded if it is still
 * empty, so restarting the backend never duplicates or overwrites data that
 * is already there.
 */
@Component
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
            System.out.println("Medicines collection already has data - skipping seed.");
        }

        if (interactionRepository.count() == 0) {
            List<Interaction> interactions = CsvLoader.loadInteractions(interactionsCsvPath);
            interactionRepository.saveAll(interactions.stream().map(DataSeeder::toDocument).toList());
            System.out.println("Seeded " + interactions.size() + " interactions into MongoDB.");
        } else {
            System.out.println("Interactions collection already has data - skipping seed.");
        }
    }

    private static MedicineDocument toDocument(Medicine medicine) {
        return new MedicineDocument(medicine.getId(), medicine.getName(), MedicineType.fromDomain(medicine),
                medicine.getDosageUnit(), medicine.getMaxSingleDose(), medicine.getMaxDailyDose());
    }

    private static InteractionDocument toDocument(Interaction interaction) {
        return new InteractionDocument(null, interaction.getMedicineNameA(), interaction.getMedicineNameB(),
                interaction.getSeverity(), interaction.getDescription());
    }
}
