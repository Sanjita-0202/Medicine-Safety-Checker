package com.medicinesafetychecker.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.medicinesafetychecker.core.Severity;

class SeedMigrationTest {
    @TempDir Path directory;

    @Test void upgradesExistingDataAndRefreshesSeverityWithoutDuplicatingPairs() throws Exception {
        Path medicinesCsv = directory.resolve("medicines.csv");
        Files.writeString(medicinesCsv, "id,name,type,dosageUnit,maxSingleDose,maxDailyDose,drugClass\na,Amoxicillin,ANTIBIOTIC,mg,1000,3000,penicillin\nb,Ibuprofen,PAINKILLER,mg,400,1200,NSAID\nc,Aspirin,PAINKILLER,mg,500,1000,NSAID\n");
        Path interactionsCsv = directory.resolve("interactions.csv");
        Files.writeString(interactionsCsv, "medicineNameA,medicineNameB,severity,description\nAmoxicillin,Ibuprofen,MAJOR,Reviewed override\n");
        var medicines = mock(MedicineRepository.class);
        var interactions = mock(InteractionRepository.class);
        MedicineDocument old = new MedicineDocument("a", "Amoxicillin", MedicineType.ANTIBIOTIC, "mg", 1000, 3000);
        MedicineDocument custom = new MedicineDocument("b", "Ibuprofen", MedicineType.PAINKILLER, "mg", 400, 1200);
        custom.setDrugClass("custom class");
        when(medicines.count()).thenReturn(2L);
        when(medicines.findById("a")).thenReturn(Optional.of(old));
        when(medicines.findById("b")).thenReturn(Optional.of(custom));
        when(medicines.findById("c")).thenReturn(Optional.empty());
        InteractionDocument existing = new InteractionDocument("existing", "IBUPROFEN", "amoxicillin", Severity.LOW, "Old description");
        InteractionDocument extra = new InteractionDocument("extra", "Custom", "Other", Severity.LOW, "Preserved");
        when(interactions.findAll()).thenReturn(List.of(existing, extra));
        var seeder = new DataSeeder(medicines, interactions, medicinesCsv.toString(), interactionsCsv.toString());
        seeder.run(null);
        assertEquals("penicillin", old.getDrugClass());
        assertEquals("custom class", custom.getDrugClass());
        verify(medicines).save(argThat(document -> document.getId().equals("c") && "NSAID".equals(document.getDrugClass())));
        assertEquals(Severity.SEVERE, existing.getSeverity());
        assertEquals("Reviewed override", existing.getDescription());
        assertEquals("existing", existing.getId());
        verify(interactions).save(existing);
        verify(interactions, never()).save(extra);
        seeder.run(null);
        verify(interactions, times(1)).save(existing);
    }

    @Test void legacyPatientWithoutListsIsSafeToRenderAndEdit() {
        PatientDocument patient = new PatientDocument();
        assertTrue(patient.getAllergies().isEmpty());
        assertTrue(patient.getActiveMedicines().isEmpty());
    }

    @Test void legacyGenderAbbreviationsMatchEditFormOptions() {
        PatientDocument patient = new PatientDocument();
        patient.setGender("F");
        assertEquals("Female", patient.getGender());
        patient.setGender("M");
        assertEquals("Male", patient.getGender());
        assertDoesNotThrow(() -> PatientInput.parse(java.util.Map.of(
                "name", "Legacy Patient", "age", "30", "weight", "60", "gender", patient.getGender())));
    }
}
