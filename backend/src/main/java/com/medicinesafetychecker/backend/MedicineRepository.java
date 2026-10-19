package com.medicinesafetychecker.backend;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MedicineRepository extends MongoRepository<MedicineDocument, String> {

    Optional<MedicineDocument> findByNameIgnoreCase(String name);
}
