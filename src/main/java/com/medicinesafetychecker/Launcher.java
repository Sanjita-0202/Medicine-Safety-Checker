package com.medicinesafetychecker;

import javafx.application.Application;

/**
 * Entry point. Kept separate from the JavaFX Application subclass so the app
 * can be launched from a plain classpath (e.g. a Spring Boot fat jar).
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MedicineSafetyCheckerApplication.class, args);
    }
}
