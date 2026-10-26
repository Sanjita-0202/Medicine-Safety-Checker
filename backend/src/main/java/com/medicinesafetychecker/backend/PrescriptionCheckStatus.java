package com.medicinesafetychecker.backend;

/** Outcome of a POST /api/prescriptions/check request. */
public enum PrescriptionCheckStatus {
    SAFE,
    ALLERGY_CONFLICT,
    DANGEROUS_COMBINATION,
    OVERDOSE_RISK
}
