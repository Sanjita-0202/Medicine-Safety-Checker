package com.medicinesafetychecker.backend;
public class DataNotReadyException extends IllegalStateException {
    public DataNotReadyException() { super("Interaction data is still loading - please try again shortly."); }
}
