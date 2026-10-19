package com.medicinesafetychecker.core;

/**
 * A medicine and the dosage range that is considered safe for an adult.
 * Plain OOP class - no framework annotations, so it can be reused by both
 * the backend and the desktop module without pulling in Spring or JavaFX.
 */
public class Medicine {

    private final String id;
    private final String name;
    private final String genericName;
    private final double minSafeDosageMg;
    private final double maxSafeDosageMg;

    public Medicine(String id, String name, String genericName,
                     double minSafeDosageMg, double maxSafeDosageMg) {
        if (minSafeDosageMg < 0 || maxSafeDosageMg < minSafeDosageMg) {
            throw new IllegalArgumentException("Invalid safe dosage range");
        }
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.minSafeDosageMg = minSafeDosageMg;
        this.maxSafeDosageMg = maxSafeDosageMg;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return genericName;
    }

    public double getMinSafeDosageMg() {
        return minSafeDosageMg;
    }

    public double getMaxSafeDosageMg() {
        return maxSafeDosageMg;
    }
}
