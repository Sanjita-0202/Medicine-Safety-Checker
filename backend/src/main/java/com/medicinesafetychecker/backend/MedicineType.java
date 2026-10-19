package com.medicinesafetychecker.backend;

import com.medicinesafetychecker.core.Antibiotic;
import com.medicinesafetychecker.core.Antihypertensive;
import com.medicinesafetychecker.core.Medicine;
import com.medicinesafetychecker.core.Painkiller;

/** Which core Medicine subclass a {@link MedicineDocument} should turn into. */
public enum MedicineType {
    ANTIBIOTIC,
    PAINKILLER,
    ANTIHYPERTENSIVE;

    /** The MedicineType matching a domain Medicine's concrete subclass, for seeding documents. */
    public static MedicineType fromDomain(Medicine medicine) {
        if (medicine instanceof Antibiotic) {
            return ANTIBIOTIC;
        }
        if (medicine instanceof Painkiller) {
            return PAINKILLER;
        }
        if (medicine instanceof Antihypertensive) {
            return ANTIHYPERTENSIVE;
        }
        throw new IllegalArgumentException("Unknown medicine subclass: " + medicine.getClass());
    }
}
