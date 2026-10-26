package com.medicinesafetychecker.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.medicinesafetychecker.core.DangerousCombinationException;
import com.medicinesafetychecker.core.OverdoseRiskException;

/**
 * Converts our custom domain exceptions (and bad-input errors) into clean
 * JSON bodies instead of Spring's default HTML/whitelabel error page.
 *
 * <p>OverdoseRiskException and DangerousCombinationException are normally
 * caught inside PrescriptionController and turned into a 200 response with a
 * status field, since they're expected safety-check outcomes, not failures.
 * The handlers here are a safety net for any other path that lets one escape.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DataNotReadyException.class)
    public ResponseEntity<ApiError> handleLoading(DataNotReadyException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiError("LOADING", e.getMessage()));
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiError> handlePatientNotFound(PatientNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("UNKNOWN_PATIENT", e.getMessage()));
    }

    @ExceptionHandler(UnknownMedicineException.class)
    public ResponseEntity<ApiError> handleUnknownMedicine(UnknownMedicineException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("UNKNOWN_MEDICINE", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(OverdoseRiskException.class)
    public ResponseEntity<ApiError> handleOverdoseRisk(OverdoseRiskException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("OVERDOSE_RISK", e.getMessage()));
    }

    @ExceptionHandler(DangerousCombinationException.class)
    public ResponseEntity<ApiError> handleDangerousCombination(DangerousCombinationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("DANGEROUS_COMBINATION", e.getMessage()));
    }
}
