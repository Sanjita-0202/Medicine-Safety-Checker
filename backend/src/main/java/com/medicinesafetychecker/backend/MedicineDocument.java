package com.medicinesafetychecker.backend;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.medicinesafetychecker.core.Medicine;

/**
 * MongoDB-mapped version of {@link Medicine}. Kept separate from the core
 * domain class so that core stays free of persistence annotations.
 */
@Document(collection = "medicines")
public class MedicineDocument {

    @Id
    private String id;
    private String name;
    private String genericName;
    private double minSafeDosageMg;
    private double maxSafeDosageMg;

    public MedicineDocument() {
    }

    public MedicineDocument(String id, String name, String genericName,
                             double minSafeDosageMg, double maxSafeDosageMg) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.minSafeDosageMg = minSafeDosageMg;
        this.maxSafeDosageMg = maxSafeDosageMg;
    }

    public Medicine toDomain() {
        return new Medicine(id, name, genericName, minSafeDosageMg, maxSafeDosageMg);
    }

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

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public double getMinSafeDosageMg() {
        return minSafeDosageMg;
    }

    public void setMinSafeDosageMg(double minSafeDosageMg) {
        this.minSafeDosageMg = minSafeDosageMg;
    }

    public double getMaxSafeDosageMg() {
        return maxSafeDosageMg;
    }

    public void setMaxSafeDosageMg(double maxSafeDosageMg) {
        this.maxSafeDosageMg = maxSafeDosageMg;
    }
}
