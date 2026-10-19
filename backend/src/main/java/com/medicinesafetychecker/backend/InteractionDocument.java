package com.medicinesafetychecker.backend;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.medicinesafetychecker.core.Interaction;
import com.medicinesafetychecker.core.Severity;

/**
 * MongoDB-mapped version of {@link Interaction}. Kept separate from the core
 * domain classes so that core stays free of persistence annotations.
 */
@Document(collection = "interactions")
public class InteractionDocument {

    @Id
    private String id;
    private String medicineNameA;
    private String medicineNameB;
    private Severity severity;
    private String description;

    public InteractionDocument() {
    }

    public InteractionDocument(String id, String medicineNameA, String medicineNameB,
                                Severity severity, String description) {
        this.id = id;
        this.medicineNameA = medicineNameA;
        this.medicineNameB = medicineNameB;
        this.severity = severity;
        this.description = description;
    }

    public Interaction toDomain() {
        return new Interaction(medicineNameA, medicineNameB, severity, description);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMedicineNameA() {
        return medicineNameA;
    }

    public void setMedicineNameA(String medicineNameA) {
        this.medicineNameA = medicineNameA;
    }

    public String getMedicineNameB() {
        return medicineNameB;
    }

    public void setMedicineNameB(String medicineNameB) {
        this.medicineNameB = medicineNameB;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
