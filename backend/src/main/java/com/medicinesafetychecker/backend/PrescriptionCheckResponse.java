package com.medicinesafetychecker.backend;

import com.medicinesafetychecker.core.DangerousCombinationException;
import com.medicinesafetychecker.core.OverdoseRiskException;
import com.medicinesafetychecker.core.Severity;

/** Response body for POST /api/prescriptions/check. */
public record PrescriptionCheckResponse(
        PrescriptionCheckStatus status,
        String message,
        Severity severity) {

    public static PrescriptionCheckResponse safe() {
        return new PrescriptionCheckResponse(PrescriptionCheckStatus.SAFE,
                "No known overdose risk or dangerous combination.", null);
    }

    public static PrescriptionCheckResponse overdoseRisk(OverdoseRiskException e) {
        return new PrescriptionCheckResponse(PrescriptionCheckStatus.OVERDOSE_RISK, e.getMessage(), null);
    }

    public static PrescriptionCheckResponse dangerousCombination(DangerousCombinationException e) {
        return new PrescriptionCheckResponse(PrescriptionCheckStatus.DANGEROUS_COMBINATION,
                e.getMessage(), e.getSeverity());
    }
}
