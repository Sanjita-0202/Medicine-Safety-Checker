package com.medicinesafetychecker.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class EnvironmentConfigurationTest {
    @TempDir Path directory;

    private ApplicationContextRunner runner(Path file) {
        return new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.config.location=optional:classpath:/isolated-config-test/",
                        "spring.config.import=optional:file:" + file.toUri().getPath() + "[.properties]",
                        "spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/medicine_safety_checker}",
                        "spring.data.mongodb.database=${MONGODB_DATABASE:medicine_safety_checker}",
                        "app.seed.enabled=${APP_SEED_ENABLED:true}");
    }

    @Test void dotenvLoadsUriDatabaseAndSeedFlag() throws Exception {
        Path file = directory.resolve(".env");
        Files.writeString(file, "MONGODB_URI=mongodb://example.invalid:27017/\nMONGODB_DATABASE=fixture_database\nAPP_SEED_ENABLED=false\n");
        runner(file).run(context -> {
            assertNull(context.getStartupFailure());
            var env = context.getEnvironment();
            assertEquals("mongodb://example.invalid:27017/", env.getProperty("spring.data.mongodb.uri"));
            assertEquals("fixture_database", env.getProperty("spring.data.mongodb.database"));
            assertEquals("false", env.getProperty("app.seed.enabled"));
        });
    }

    @Test void missingDotenvKeepsLocalDefaults() {
        runner(directory.resolve("missing.env")).run(context -> {
            assertNull(context.getStartupFailure());
            assertEquals("mongodb://localhost:27017/medicine_safety_checker", context.getEnvironment().getProperty("spring.data.mongodb.uri"));
            assertEquals("medicine_safety_checker", context.getEnvironment().getProperty("spring.data.mongodb.database"));
            assertEquals("true", context.getEnvironment().getProperty("app.seed.enabled"));
        });
    }

    @Test void explicitEnvironmentOverrideWinsOverDotenv() throws Exception {
        Path file = directory.resolve(".env");
        Files.writeString(file, "MONGODB_URI=mongodb://file.invalid:27017/\n");
        runner(file).withPropertyValues("MONGODB_URI=mongodb://override.invalid:27017/").run(context ->
                assertEquals("mongodb://override.invalid:27017/", context.getEnvironment().getProperty("spring.data.mongodb.uri")));
    }

    @Test void disablingSeedDataDoesNotCreateDatabaseWriters() {
        new ApplicationContextRunner().withPropertyValues("app.seed.enabled=false")
                .withUserConfiguration(DataSeeder.class, PatientSeeder.class)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(0, context.getBeanNamesForType(DataSeeder.class).length);
                    assertEquals(0, context.getBeanNamesForType(PatientSeeder.class).length);
                });
    }
}
