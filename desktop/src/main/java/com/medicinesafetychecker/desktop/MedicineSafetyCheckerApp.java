package com.medicinesafetychecker.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** JavaFX client shell. Calls the backend REST API over HTTP (not wired up yet). */
public class MedicineSafetyCheckerApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Medicine Safety Checker");
        stage.setScene(new Scene(new StackPane(new Label("Medicine Safety Checker")), 800, 600));
        stage.show();
    }
}
