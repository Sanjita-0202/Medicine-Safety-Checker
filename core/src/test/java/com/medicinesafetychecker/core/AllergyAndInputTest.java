package com.medicinesafetychecker.core;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AllergyAndInputTest {
    @TempDir Path directory;
    private Medicine medicine() {
        Medicine medicine = new Antibiotic("amox", "Amoxicillin", "mg", 1000, 3000);
        medicine.setDrugClass("penicillin");
        return medicine;
    }

    @Test void matchesNameAndDrugClassIgnoringCaseAndWhitespace() {
        for (String allergy : List.of(" AMOXICILLIN ", "PeNiCiLlIn")) {
            var error = assertThrows(AllergyConflictException.class,
                    () -> AllergyChecker.check(medicine(), List.of(allergy)));
            assertTrue(error.getMessage().contains("allergic"));
        }
    }

    @Test void noAllergyOrDifferentClassPasses() {
        assertDoesNotThrow(() -> AllergyChecker.check(medicine(), null));
        assertDoesNotThrow(() -> AllergyChecker.check(medicine(), List.of("NSAID", "")));
        Medicine withoutClass = new Antibiotic("a", "Test", "mg", 100, 200);
        assertDoesNotThrow(() -> AllergyChecker.check(withoutClass, List.of("penicillin")));
    }

    @Test void rejectsNonfiniteAndNonpositiveInputForEverySubclass() {
        for (Medicine medicine : List.of(medicine(), new Painkiller("p", "Painkiller", "mg", 1000, 3000),
                new Antihypertensive("h", "Antihypertensive", "mg", 1000, 3000))) {
            for (double dose : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0, -1}) {
                assertThrows(IllegalArgumentException.class, () -> medicine.validateDose(dose, 1));
            }
            assertThrows(IllegalArgumentException.class, () -> medicine.validateDose(1, 0));
            assertThrows(IllegalArgumentException.class, () -> medicine.validateDose(1, -1));
        }
    }

    @Test void supportsOldAndNewMedicineCsvFormats() throws Exception {
        Path old = directory.resolve("old.csv");
        Files.writeString(old, "id,name,type,dosageUnit,maxSingleDose,maxDailyDose\na,Amoxicillin,ANTIBIOTIC,mg,1000,3000\n");
        assertNull(CsvLoader.loadMedicines(old).getFirst().getDrugClass());
        Path modern = directory.resolve("new.csv");
        Files.writeString(modern, "id,name,type,dosageUnit,maxSingleDose,maxDailyDose,drugClass\na,Amoxicillin,ANTIBIOTIC,mg,1000,3000,penicillin\n");
        assertEquals("penicillin", CsvLoader.loadMedicines(modern).getFirst().getDrugClass());
    }
}
