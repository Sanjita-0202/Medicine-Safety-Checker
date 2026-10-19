package com.medicinesafetychecker.backend;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InteractionRepository extends MongoRepository<InteractionDocument, String> {
}
