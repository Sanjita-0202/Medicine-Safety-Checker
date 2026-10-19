package com.medicinesafetychecker.backend;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatientRepository extends MongoRepository<PatientDocument, String> {
}
