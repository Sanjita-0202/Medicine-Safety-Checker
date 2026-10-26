package com.medicinesafetychecker.backend;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.medicinesafetychecker.core.Antibiotic;
import com.medicinesafetychecker.core.Antihypertensive;
import com.medicinesafetychecker.core.Medicine;
import com.medicinesafetychecker.core.Painkiller;

/**
 * MongoDB-mapped version of {@link Medicine}. Kept separate from the core
 * domain classes so that core stays free of persistence annotations.
 * {@code type} records which Medicine subclass this document represents.
 */
@Document(collection = "medicines")
public class MedicineDocument {

    @Id
    private String id;
    private String name;
    private String drugClass;
    private MedicineType type;
    private String dosageUnit;
    private double maxSingleDose;
    private double maxDailyDose;

    public MedicineDocument() {
    }

    public MedicineDocument(String id, String name, MedicineType type, String dosageUnit,
                             double maxSingleDose, double maxDailyDose) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.dosageUnit = dosageUnit;
        this.maxSingleDose = maxSingleDose;
        this.maxDailyDose = maxDailyDose;
    }

    public Medicine toDomain() {
        Medicine medicine = switch (type) {
            case ANTIBIOTIC -> new Antibiotic(id, name, dosageUnit, maxSingleDose, maxDailyDose);
            case PAINKILLER -> new Painkiller(id, name, dosageUnit, maxSingleDose, maxDailyDose);
            case ANTIHYPERTENSIVE -> new Antihypertensive(id, name, dosageUnit, maxSingleDose, maxDailyDose);
        };
        medicine.setDrugClass(drugClass);
        return medicine;
    }

    public String getDrugClass() { return drugClass; }
    public void setDrugClass(String drugClass) { this.drugClass = drugClass; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MedicineType getType() {
        return type;
    }

    public void setType(MedicineType type) {
        this.type = type;
    }

    public String getDosageUnit() {
        return dosageUnit;
    }

    public void setDosageUnit(String dosageUnit) {
        this.dosageUnit = dosageUnit;
    }

    public double getMaxSingleDose() {
        return maxSingleDose;
    }

    public void setMaxSingleDose(double maxSingleDose) {
        this.maxSingleDose = maxSingleDose;
    }

    public double getMaxDailyDose() {
        return maxDailyDose;
    }

    public void setMaxDailyDose(double maxDailyDose) {
        this.maxDailyDose = maxDailyDose;
    }
}
