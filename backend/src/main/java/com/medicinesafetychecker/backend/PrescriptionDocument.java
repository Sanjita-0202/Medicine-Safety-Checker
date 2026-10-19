package com.medicinesafetychecker.backend;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document recording one prescription check made for a patient: what
 * was prescribed, whether it was approved, and - if rejected - why.
 */
@Document(collection = "prescriptions")
public class PrescriptionDocument {

    @Id
    private String id;
    private String patientId;
    private String medicineName;
    private double dose;
    private int timesPerDay;
    private LocalDate date;
    private PrescriptionStatus status;
    private String reason;

    public PrescriptionDocument() {
    }

    public PrescriptionDocument(String id, String patientId, String medicineName, double dose,
                                 int timesPerDay, LocalDate date, PrescriptionStatus status, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.medicineName = medicineName;
        this.dose = dose;
        this.timesPerDay = timesPerDay;
        this.date = date;
        this.status = status;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public double getDose() {
        return dose;
    }

    public void setDose(double dose) {
        this.dose = dose;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
