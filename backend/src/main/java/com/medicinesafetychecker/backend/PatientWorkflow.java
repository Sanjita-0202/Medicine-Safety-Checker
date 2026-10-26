package com.medicinesafetychecker.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Serializes patient mutations within this application, including duplicate stop requests. */
@Service
public class PatientWorkflow {
    private final PatientRepository patients;
    private final PrescriptionRepository prescriptions;
    private final PrescriptionChecker checker;

    public PatientWorkflow(PatientRepository patients, PrescriptionRepository prescriptions, PrescriptionChecker checker) {
        this.patients = patients; this.prescriptions = prescriptions; this.checker = checker;
    }

    public PatientDocument requirePatient(String id) {
        return patients.findById(id).orElseThrow(() -> new PatientNotFoundException(id));
    }

    public synchronized PatientDocument add(PatientInput input) {
        return patients.save(new PatientDocument(null, input.name(), input.age(), input.gender(),
                input.weight(), input.allergies(), new ArrayList<>()));
    }

    public synchronized PatientDocument edit(String id, PatientInput input) {
        PatientDocument patient = requirePatient(id);
        patient.setName(input.name()); patient.setAge(input.age()); patient.setGender(input.gender());
        patient.setWeight(input.weight()); patient.setAllergies(input.allergies());
        return patients.save(patient);
    }

    public synchronized PrescriptionDocument prescribe(String id, String name, double dose, int times,
            DoctorDocument doctor) {
        PatientDocument patient = requirePatient(id);
        PrescriptionCheckResponse result = checker.check(name, dose, times,
                patient.getActiveMedicines(), patient.getAllergies(), id);
        PrescriptionStatus status = result.status() == PrescriptionCheckStatus.SAFE
                ? PrescriptionStatus.APPROVED : PrescriptionStatus.REJECTED;
        PrescriptionDocument entry = entry(id, name.strip(), dose, times, status,
                status == PrescriptionStatus.REJECTED ? result.message() : null, doctor);
        prescriptions.save(entry);
        if (status == PrescriptionStatus.APPROVED) {
            List<String> active = new ArrayList<>(patient.getActiveMedicines());
            if (active.stream().noneMatch(medicine -> medicine.equalsIgnoreCase(name.strip()))) {
                active.add(name.strip());
                patient.setActiveMedicines(active);
                try { patients.save(patient); }
                catch (RuntimeException e) { rollback(entry); throw e; }
            }
        }
        return entry;
    }

    public synchronized void stop(String id, String medicineName, DoctorDocument doctor) {
        PatientDocument patient = requirePatient(id);
        if (medicineName == null || medicineName.isBlank()) { throw new IllegalArgumentException("Choose a medicine to stop."); }
        String activeName = patient.getActiveMedicines().stream()
                .filter(name -> name.equalsIgnoreCase(medicineName.strip())).findFirst().orElse(null);
        if (activeName == null) { return; } // Idempotent: do not duplicate STOPPED history.
        PrescriptionDocument stopped = entry(id, activeName, 0, 0, PrescriptionStatus.STOPPED,
                "Medicine stopped by doctor.", doctor);
        prescriptions.save(stopped);
        patient.setActiveMedicines(patient.getActiveMedicines().stream()
                .filter(name -> !name.equalsIgnoreCase(activeName)).toList());
        try { patients.save(patient); }
        catch (RuntimeException e) { rollback(stopped); throw e; }
    }

    private void rollback(PrescriptionDocument entry) {
        if (entry.getId() != null) { prescriptions.deleteById(entry.getId()); }
    }

    private PrescriptionDocument entry(String patient, String medicine, double dose, int times,
            PrescriptionStatus status, String reason, DoctorDocument doctor) {
        if (doctor == null || doctor.getId() == null) { throw new IllegalArgumentException("A doctor session is required."); }
        PrescriptionDocument entry = new PrescriptionDocument(null, patient, medicine, dose, times,
                LocalDate.now(), status, reason);
        entry.setDoctorId(doctor.getId()); entry.setDoctorName(doctor.getName());
        return entry;
    }
}
