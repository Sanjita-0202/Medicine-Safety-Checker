package com.medicinesafetychecker.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class DoctorBootstrapTest {
    @Test void shortDemoPasswordNeedsExplicitLocalOptIn() {
        var doctors = mock(DoctorRepository.class);
        when(doctors.findByUsername("doctor")).thenReturn(Optional.empty());
        var mongo = mock(MongoTemplate.class, RETURNS_DEEP_STUBS);
        var bootstrap = new DoctorBootstrap(doctors, mongo, "doctor", "doctor", "Demo Doctor", "General", false);
        assertThrows(IllegalArgumentException.class, () -> bootstrap.run(null));
        verify(doctors, never()).save(any());
    }

    @Test void explicitDemoOptInStoresOnlyBcryptHash() {
        var doctors = mock(DoctorRepository.class);
        when(doctors.findByUsername("doctor")).thenReturn(Optional.empty());
        var mongo = mock(MongoTemplate.class, RETURNS_DEEP_STUBS);
        new DoctorBootstrap(doctors, mongo, "doctor", "doctor", "Demo Doctor", "General", true).run(null);
        var saved = ArgumentCaptor.forClass(DoctorDocument.class);
        verify(doctors).save(saved.capture());
        assertNotEquals("doctor", saved.getValue().getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("doctor", saved.getValue().getPasswordHash()));
    }

    @Test void restartingNeverResetsExistingAccounts() {
        var doctors = mock(DoctorRepository.class);
        when(doctors.findByUsername("doctor")).thenReturn(Optional.of(new DoctorDocument("d1", "Doctor", "General", "doctor", "existing")));
        new DoctorBootstrap(doctors, mock(MongoTemplate.class, RETURNS_DEEP_STUBS), "doctor", "doctor", "Demo Doctor", "General", true).run(null);
        verify(doctors, never()).save(any());
    }
}
