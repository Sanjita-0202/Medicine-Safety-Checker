package com.medicinesafetychecker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MedicineSafetyCheckerApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        context = new SpringApplicationBuilder(MedicineSafetyCheckerApplication.class)
                .headless(false)
                .run(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Medicine Safety Checker");
        stage.setScene(new Scene(new StackPane(new Label("Medicine Safety Checker")), 800, 600));
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}
