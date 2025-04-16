package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the MedicalRecord model
 */
public class MedicalRecordTest {

    private Patient testPatient;
    private LocalDate recordDate;
    private MedicalRecord testRecord;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        recordDate = LocalDate.now();
        testRecord = new MedicalRecord("MR001", testPatient, "Common Cold", recordDate);
        
        // Create an appointment for testing relationship with medical record
        LocalDateTime appointmentTime = LocalDateTime.now().minusDays(1);
        testAppointment = new Appointment("A001", testPatient, "Check-up", appointmentTime);
    }

    @Test
    @DisplayName("Test medical record initialization")
    void testInitialization() {
        // Verify fields are correctly initialized
        assertEquals("MR001", testRecord.getId(), "ID should match");
        assertEquals(testPatient, testRecord.getPatient(), "Patient should match");
        assertEquals("Common Cold", testRecord.getDiagnosis(), "Diagnosis should match");
        assertEquals(recordDate, testRecord.getRecordDate(), "Record date should match");
        assertNull(testRecord.getNotes(), "Notes should be null initially");
        assertNull(testRecord.getRelatedAppointment(), "Related appointment should be null initially");
    }

    @Test
    @DisplayName("Test constructor validation")
    void testConstructorValidation() {
        // Test null ID
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new MedicalRecord(null, testPatient, "Common Cold", recordDate);
        });
        assertTrue(exception1.getMessage().contains("ID cannot be null"), "Should validate ID is not null");

        // Test empty ID
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new MedicalRecord("", testPatient, "Common Cold", recordDate);
        });
        assertTrue(exception2.getMessage().contains("ID cannot be empty"), "Should validate ID is not empty");

        // Test null patient
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new MedicalRecord("MR001", null, "Common Cold", recordDate);
        });
        assertTrue(exception3.getMessage().contains("Patient cannot be null"), "Should validate patient is not null");

        // Test null diagnosis
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new MedicalRecord("MR001", testPatient, null, recordDate);
        });
        assertTrue(exception4.getMessage().contains("Diagnosis cannot be null"), "Should validate diagnosis is not null");

        // Test empty diagnosis
        Exception exception5 = assertThrows(IllegalArgumentException.class, () -> {
            new MedicalRecord("MR001", testPatient, "", recordDate);
        });
        assertTrue(exception5.getMessage().contains("Diagnosis cannot be empty"), "Should validate diagnosis is not empty");

        // Test null record date
        Exception exception6 = assertThrows(IllegalArgumentException.class, () -> {
            new MedicalRecord("MR001", testPatient, "Common Cold", null);
        });
        assertTrue(exception6.getMessage().contains("Record date cannot be null"), "Should validate record date is not null");
    }

    @Test
    @DisplayName("Test update diagnosis")
    void testUpdateDiagnosis() {
        // Initial diagnosis
        assertEquals("Common Cold", testRecord.getDiagnosis(), "Initial diagnosis should be set");
        
        // Update diagnosis
        testRecord.updateDiagnosis("Influenza");
        
        // Verify diagnosis is updated
        assertEquals("Influenza", testRecord.getDiagnosis(), "Diagnosis should be updated");
        
        // Test null diagnosis validation
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            testRecord.updateDiagnosis(null);
        });
        assertTrue(exception1.getMessage().contains("New diagnosis cannot be null"), "Should validate new diagnosis is not null");
        
        // Test empty diagnosis validation
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            testRecord.updateDiagnosis("");
        });
        assertTrue(exception2.getMessage().contains("New diagnosis cannot be empty"), "Should validate new diagnosis is not empty");
    }

    @Test
    @DisplayName("Test append notes")
    void testAppendNotes() {
        // Initially no notes
        assertNull(testRecord.getNotes(), "Notes should be null initially");
        
        // Append first note
        testRecord.appendNotes("Patient has fever of 38°C.");
        assertEquals("Patient has fever of 38°C.", testRecord.getNotes(), "First note should be set");
        
        // Append additional note
        testRecord.appendNotes("Prescribed Paracetamol 500mg.");
        assertEquals("Patient has fever of 38°C.\nPrescribed Paracetamol 500mg.", testRecord.getNotes(), 
                "Notes should include both entries separated by newline");
        
        // Test null notes handling (should not throw or change notes)
        testRecord.appendNotes(null);
        assertEquals("Patient has fever of 38°C.\nPrescribed Paracetamol 500mg.", testRecord.getNotes(), 
                "Notes should not change when appending null");
        
        // Test empty notes handling (should not throw or change notes)
        testRecord.appendNotes("");
        assertEquals("Patient has fever of 38°C.\nPrescribed Paracetamol 500mg.", testRecord.getNotes(), 
                "Notes should not change when appending empty string");
    }

    @Test
    @DisplayName("Test relationship with appointment")
    void testRelationshipWithAppointment() {
        // Initially no related appointment
        assertNull(testRecord.getRelatedAppointment(), "Related appointment should be null initially");
        
        // Set related appointment
        testRecord.setRelatedAppointment(testAppointment);
        
        // Verify relationship
        assertEquals(testAppointment, testRecord.getRelatedAppointment(), "Related appointment should be set");
    }

    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        // Test setId
        testRecord.setId("MR002");
        assertEquals("MR002", testRecord.getId(), "ID should be updated");
        
        // Test setPatient
        Patient newPatient = new Patient("P002", "Jane", "Smith", LocalDate.of(1985, 3, 20));
        testRecord.setPatient(newPatient);
        assertEquals(newPatient, testRecord.getPatient(), "Patient should be updated");
        
        // Test setDiagnosis
        testRecord.setDiagnosis("Bronchitis");
        assertEquals("Bronchitis", testRecord.getDiagnosis(), "Diagnosis should be updated");
        
        // Test setNotes
        testRecord.setNotes("Initial consultation notes.");
        assertEquals("Initial consultation notes.", testRecord.getNotes(), "Notes should be updated");
        
        // Test setRecordDate
        LocalDate newDate = LocalDate.of(2023, 2, 15);
        testRecord.setRecordDate(newDate);
        assertEquals(newDate, testRecord.getRecordDate(), "Record date should be updated");
    }
} 