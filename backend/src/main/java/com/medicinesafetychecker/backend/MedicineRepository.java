package com.medicinesafetychecker.backend;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MedicineRepository extends MongoRepository<MedicineDocument, String> {
}
