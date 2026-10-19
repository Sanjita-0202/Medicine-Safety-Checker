package com.medicinesafetychecker.desktop;

import javafx.application.Application;

/**
 * Entry point. Kept separate from the JavaFX Application subclass because
 * some launchers (and the fat-jar runner) expect a plain main() method.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MedicineSafetyCheckerApp.class, args);
    }
}
