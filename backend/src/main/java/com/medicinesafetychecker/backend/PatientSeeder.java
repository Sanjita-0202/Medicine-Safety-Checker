package com.medicinesafetychecker.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Loads data/seed/patients.csv into MongoDB on startup, following the same
 * pattern as {@link DataSeeder}: only seeds if the patients collection is
 * still empty, so restarting the backend never duplicates demo data.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class PatientSeeder implements ApplicationRunner {

    private final PatientRepository patientRepository;
    private final Path patientsCsvPath;

    public PatientSeeder(PatientRepository patientRepository,
                          @Value("${app.seed.patients-csv:../data/seed/patients.csv}") String patientsCsvPath) {
        this.patientRepository = patientRepository;
        this.patientsCsvPath = Path.of(patientsCsvPath);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (patientRepository.count() > 0) {
            System.out.println("Patients collection already has data - skipping seed.");
            return;
        }

        List<PatientDocument> patients = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(patientsCsvPath)) {
            String line;
            boolean headerSeen = false;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (!headerSeen) {
                    headerSeen = true;
                    continue;
                }
                patients.add(parsePatient(line));
            }
        }
        patientRepository.saveAll(patients);
        System.out.println("Seeded " + patients.size() + " patients into MongoDB.");
    }

    // Columns: id,name,age,gender,weight,allergies,activeMedicines
    // allergies/activeMedicines may hold several values separated by "|"; blank means none.
    private static PatientDocument parsePatient(String line) {
        String[] fields = line.split(",", -1);
        String id = fields[0].strip();
        String name = fields[1].strip();
        int age = Integer.parseInt(fields[2].strip());
        String gender = fields[3].strip();
        double weight = Double.parseDouble(fields[4].strip());
        List<String> allergies = splitMultiValue(fields[5]);
        List<String> activeMedicines = splitMultiValue(fields[6]);
        return new PatientDocument(id, name, age, gender, weight, allergies, activeMedicines);
    }

    private static List<String> splitMultiValue(String field) {
        String trimmed = field.strip();
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(trimmed.split("\\|")));
    }
}
