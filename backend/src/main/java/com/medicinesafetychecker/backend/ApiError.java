package com.medicinesafetychecker.backend;

/** Clean JSON shape for error responses, used by {@link ApiExceptionHandler}. */
public record ApiError(String error, String message) {}
