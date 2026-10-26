package com.medicinesafetychecker.backend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {
    private final Path path;
    public AuditLogger(@Value("${app.audit.path:logs/checks.log}") String path) { this.path = Path.of(path); }

    public synchronized void log(String patientId, String medicine, String outcome) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) { Files.createDirectories(parent); }
            Files.writeString(path, Instant.now() + "\tpatient=" + clean(patientId)
                    + "\tmedicine=" + clean(medicine) + "\toutcome=" + clean(outcome) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write the check audit log", e);
        }
    }

    private String clean(String value) {
        if (value == null) { return "-"; }
        return value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
    }
}
