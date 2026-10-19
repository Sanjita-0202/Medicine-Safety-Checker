package com.medicinesafetychecker.core;

/** Anything that can be checked against another medicine for a dangerous interaction. */
public interface InteractionCheckable {

    void checkConflict(Medicine other) throws DangerousCombinationException;
}
