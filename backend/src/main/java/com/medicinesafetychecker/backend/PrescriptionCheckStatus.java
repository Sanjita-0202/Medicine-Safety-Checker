package com.medicinesafetychecker.backend;

/** Outcome of a POST /api/prescriptions/check request. */
public enum PrescriptionCheckStatus {
    SAFE,
    DANGEROUS_COMBINATION,
    OVERDOSE_RISK
}
