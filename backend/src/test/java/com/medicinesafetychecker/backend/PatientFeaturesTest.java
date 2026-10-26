package com.medicinesafetychecker.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

class PatientFeaturesTest {
    @TempDir Path directory;

    private Map<String, String> validForm() {
        return new HashMap<>(Map.of("name", "Test Patient", "age", "30", "gender", "Female", "weight", "60.5", "allergies", " penicillin, NSAID, PENICILLIN "));
    }

    @Test void validatesPatientFieldsAndDeduplicatesAllergies() {
        PatientInput input = PatientInput.parse(validForm());
        assertEquals(List.of("penicillin", "NSAID"), input.allergies());
        for (var invalid : List.of(Map.entry("name", " "), Map.entry("age", "131"), Map.entry("age", "2.5"),
                Map.entry("weight", "NaN"), Map.entry("weight", "Infinity"), Map.entry("weight", "-1"),
                Map.entry("weight", "501"), Map.entry("gender", "invalid"))) {
            var form = validForm(); form.put(invalid.getKey(), invalid.getValue());
            assertThrows(IllegalArgumentException.class, () -> PatientInput.parse(form));
        }
    }

    @Test void allergyWinsBeforeDoseAndGraphReadinessAndIsAudited() {
        MedicineRepository medicines = mock(MedicineRepository.class);
        InteractionGraphLoader graph = mock(InteractionGraphLoader.class);
        AuditLogger audit = mock(AuditLogger.class);
        MedicineDocument medicine = new MedicineDocument("a", "Amoxicillin", MedicineType.ANTIBIOTIC, "mg", 1000, 3000);
        medicine.setDrugClass("penicillin");
        when(medicines.findByNameIgnoreCase("Amoxicillin")).thenReturn(Optional.of(medicine));
        PrescriptionChecker checker = new PrescriptionChecker(medicines, graph, audit);
        var result = checker.check("Amoxicillin", 999999, 4, List.of(), List.of("PENICILLIN"), "p1");
        assertEquals(PrescriptionCheckStatus.ALLERGY_CONFLICT, result.status());
        verifyNoInteractions(graph);
        verify(audit).log("p1", "Amoxicillin", "ALLERGY_CONFLICT");
    }

    @Test void invalidNumbersAndUnknownMedicinesAreAudited() {
        var medicines = mock(MedicineRepository.class);
        var audit = mock(AuditLogger.class);
        when(medicines.findByNameIgnoreCase("Known")).thenReturn(Optional.of(new MedicineDocument("k", "Known", MedicineType.ANTIBIOTIC, "mg", 100, 200)));
        var checker = new PrescriptionChecker(medicines, mock(InteractionGraphLoader.class), audit);
        assertThrows(IllegalArgumentException.class, () -> checker.check("Known", Double.NaN, 1, List.of(), List.of(), "p1"));
        assertThrows(UnknownMedicineException.class, () -> checker.check("Unknown", 10, 1, List.of(), List.of(), "p1"));
        verify(audit).log("p1", "Known", "INVALID_INPUT");
        verify(audit).log("p1", "Unknown", "INVALID_INPUT");
    }

    @Test void editRetainsActiveMedicines() {
        var patients = mock(PatientRepository.class);
        var patient = new PatientDocument("p1", "Old", 20, "Male", 70, List.of(), new ArrayList<>(List.of("Amoxicillin")));
        when(patients.findById("p1")).thenReturn(Optional.of(patient));
        var workflow = new PatientWorkflow(patients, mock(PrescriptionRepository.class), mock(PrescriptionChecker.class));
        workflow.edit("p1", PatientInput.parse(validForm()));
        assertEquals(List.of("Amoxicillin"), patient.getActiveMedicines());
        assertEquals("Test Patient", patient.getName());
        verify(patients).save(patient);
    }

    @Test void concurrentRepeatedStopCreatesExactlyOneHistoryEntry() throws Exception {
        var patients = mock(PatientRepository.class);
        var prescriptions = mock(PrescriptionRepository.class);
        var patient = new PatientDocument("p1", "Patient", 30, "Male", 70, List.of(), new ArrayList<>(List.of("Amoxicillin", "Paracetamol")));
        when(patients.findById("p1")).thenReturn(Optional.of(patient));
        var workflow = new PatientWorkflow(patients, prescriptions, mock(PrescriptionChecker.class));
        var doctor = new DoctorDocument("d1", "Dr Test", "General", "doctor", null);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<java.util.concurrent.Future<?>>();
            for (int i=0; i<20; i++) { futures.add(executor.submit(() -> workflow.stop("p1", "amoxicillin", doctor))); }
            for (var future : futures) { future.get(5, TimeUnit.SECONDS); }
        }
        var saved = ArgumentCaptor.forClass(PrescriptionDocument.class);
        verify(prescriptions, times(1)).save(saved.capture());
        assertEquals(PrescriptionStatus.STOPPED, saved.getValue().getStatus());
        assertEquals("d1", saved.getValue().getDoctorId());
        assertNotNull(saved.getValue().getDate());
        assertEquals(List.of("Paracetamol"), patient.getActiveMedicines());
    }

    @Test void concurrentAuditWritesProduceCompleteSingleLines() throws Exception {
        Path file = directory.resolve("logs/checks.log");
        AuditLogger logger = new AuditLogger(file.toString());
        try (var executor = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<java.util.concurrent.Future<?>>();
            for (int i=0; i<100; i++) { final int n=i; futures.add(executor.submit(() -> logger.log("p"+n, "Medicine\r\ninjected", "SAFE"))); }
            for (var future : futures) { future.get(5, TimeUnit.SECONDS); }
        }
        var lines = Files.readAllLines(file);
        assertEquals(100, lines.size());
        assertTrue(lines.stream().allMatch(line -> line.matches("^.+Z\\tpatient=p\\d+\\tmedicine=Medicine  injected\\toutcome=SAFE$")));
    }

    @Test void csvQuotesCommasQuotesAndNewlinesAndNeutralizesFormulas() {
        assertEquals("\"A, \"\"B\"\"\nC\"", ReportServlet.cell("A, \"B\"\nC"));
        assertEquals("\"'=1+1\"", ReportServlet.cell("=1+1"));
        assertEquals("\"'   @SUM(A1)\"", ReportServlet.cell("   @SUM(A1)"));
        assertEquals("\"\"", ReportServlet.cell(null));
    }
}
