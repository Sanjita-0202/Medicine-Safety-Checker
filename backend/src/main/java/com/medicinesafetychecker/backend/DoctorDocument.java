package com.medicinesafetychecker.backend;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "doctors")
public class DoctorDocument {
    @Id
    private String id;
    private String name;
    private String specialisation;
    private String username;
    @JsonIgnore
    private String passwordHash;

    public DoctorDocument() { }

    public DoctorDocument(String id, String name, String specialisation, String username, String passwordHash) {
        this.id = id;
        this.name = name;
        this.specialisation = specialisation;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /** Store identity in the session without retaining the password hash. */
    public DoctorDocument sessionIdentity() {
        return new DoctorDocument(id, name, specialisation, username, null);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialisation() { return specialisation; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
