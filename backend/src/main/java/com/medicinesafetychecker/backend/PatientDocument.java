package com.medicinesafetychecker.backend;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for a patient. {@code activeMedicines} holds medicine
 * names (not ids), matching how {@link PrescriptionServlet} and
 * {@link PrescriptionController} already refer to medicines by name via
 * {@link MedicineRepository#findByNameIgnoreCase}.
 */
@Document(collection = "patients")
public class PatientDocument {

    @Id
    private String id;
    private String name;
    private int age;
    private String gender;
    private double weight;
    private List<String> allergies;
    private List<String> activeMedicines;

    public PatientDocument() {
    }

    public PatientDocument(String id, String name, int age, String gender, double weight,
                            List<String> allergies, List<String> activeMedicines) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.allergies = allergies;
        this.activeMedicines = activeMedicines;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getActiveMedicines() {
        return activeMedicines;
    }

    public void setActiveMedicines(List<String> activeMedicines) {
        this.activeMedicines = activeMedicines;
    }
}
