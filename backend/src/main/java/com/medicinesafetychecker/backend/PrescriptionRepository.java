package com.medicinesafetychecker.backend;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrescriptionRepository extends MongoRepository<PrescriptionDocument, String> {

    List<PrescriptionDocument> findByPatientIdOrderByDateDesc(String patientId);
}
