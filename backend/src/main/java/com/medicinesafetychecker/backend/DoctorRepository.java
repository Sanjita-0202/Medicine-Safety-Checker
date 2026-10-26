package com.medicinesafetychecker.backend;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DoctorRepository extends MongoRepository<DoctorDocument, String> {
    Optional<DoctorDocument> findByUsername(String username);
}
