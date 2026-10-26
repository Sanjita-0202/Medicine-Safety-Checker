package com.medicinesafetychecker.backend;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Optional first account provisioning; existing accounts are never overwritten. */
@Component
public class DoctorBootstrap implements ApplicationRunner {
    private final DoctorRepository doctors;
    private final MongoTemplate mongo;
    private final String username;
    private final String password;
    private final String name;
    private final String specialisation;
    private final boolean allowDemoPassword;

    public DoctorBootstrap(DoctorRepository doctors, MongoTemplate mongo,
            @Value("${MSC_DOCTOR_USERNAME:}") String username,
            @Value("${MSC_DOCTOR_PASSWORD:}") String password,
            @Value("${MSC_DOCTOR_NAME:Demo Doctor}") String name,
            @Value("${MSC_DOCTOR_SPECIALISATION:General Medicine}") String specialisation,
            @Value("${MSC_ALLOW_DEMO_PASSWORD:false}") boolean allowDemoPassword) {
        this.doctors = doctors;
        this.mongo = mongo;
        this.username = username.strip().toLowerCase(Locale.ROOT);
        this.password = password;
        this.name = name;
        this.specialisation = specialisation;
        this.allowDemoPassword = allowDemoPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        mongo.indexOps(DoctorDocument.class).createIndex(new Index().on("username", Direction.ASC).unique());
        if (username.isEmpty() || doctors.findByUsername(username).isPresent()) {
            return;
        }
        boolean localDemo = allowDemoPassword && username.equals("doctor") && password.equals("doctor");
        if ((!localDemo && password.length() < 12) || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("MSC_DOCTOR_PASSWORD must be at least 12 characters and at most 72 UTF-8 bytes.");
        }
        doctors.save(new DoctorDocument(null, name, specialisation, username,
                new BCryptPasswordEncoder(12).encode(password)));
    }
}
