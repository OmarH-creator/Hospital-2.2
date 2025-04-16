package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Test class for MedicalRecordServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class MedicalRecordServiceImplTest {

    @Mock
    private PatientService mockPatientService;

    @Mock
    private AppointmentService mockAppointmentService;
    
    @Mock
    private DataPersistenceService mockDataPersistenceService;

    private MedicalRecordServiceImpl medicalRecordService;
    private Patient testPatient;
    private Appointment testAppointment;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        // Initialize with mocked dependencies
        medicalRecordService = new MedicalRecordServiceImpl(mockPatientService, mockAppointmentService, mockDataPersistenceService);
        
        // Create a test patient
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        
        // Create a test appointment
        LocalDateTime appointmentTime = LocalDateTime.now();
        testAppointment = new Appointment("A001", testPatient, "Check-up", appointmentTime);
        
        // Set up the current date for testing
        testDate = LocalDate.now();
        
        // Set up common mock behaviors
        lenient().when(mockPatientService.findPatientById("P001")).thenReturn(Optional.of(testPatient));
        lenient().when(mockAppointmentService.findAppointmentById("A001")).thenReturn(Optional.of(testAppointment));
    }

    @Test
    @DisplayName("Test creating a medical record directly")
    void testCreateMedicalRecord() {
        // Create a medical record
        MedicalRecord record = medicalRecordService.createMedicalRecord("MR001", testPatient, "Common Cold", testDate);
        
        // Verify the record was created correctly
        assertNotNull(record, "Medical record should not be null");
        assertEquals("MR001", record.getId(), "ID should match");
        assertEquals(testPatient, record.getPatient(), "Patient should match");
        assertEquals("Common Cold", record.getDiagnosis(), "Diagnosis should match");
        assertEquals(testDate, record.getRecordDate(), "Record date should match");
    }
    
    @Test
    @DisplayName("Test creating a medical record with nulls should throw exceptions")
    void testCreateMedicalRecordWithNulls() {
        // Test null ID
        assertThrows(IllegalArgumentException.class, () -> {
            medicalRecordService.createMedicalRecord(null, testPatient, "Common Cold", testDate);
        }, "Should throw exception for null ID");
        
        // Test null patient
        assertThrows(IllegalArgumentException.class, () -> {
            medicalRecordService.createMedicalRecord("MR001", null, "Common Cold", testDate);
        }, "Should throw exception for null patient");
        
        // Test null diagnosis
        assertThrows(IllegalArgumentException.class, () -> {
            medicalRecordService.createMedicalRecord("MR001", testPatient, null, testDate);
        }, "Should throw exception for null diagnosis");
        
        // Test null record date
        assertThrows(IllegalArgumentException.class, () -> {
            medicalRecordService.createMedicalRecord("MR001", testPatient, "Common Cold", null);
        }, "Should throw exception for null record date");
    }
    
    @Test
    @DisplayName("Test creating a medical record by patient ID")
    void testCreateMedicalRecordByPatientId() {
        // Create a medical record by patient ID
        MedicalRecord record = medicalRecordService.createMedicalRecordByPatientId("MR001", "P001", "Flu", testDate);
        
        // Verify the record was created correctly
        assertNotNull(record, "Medical record should not be null");
        assertEquals("MR001", record.getId(), "ID should match");
        assertEquals(testPatient, record.getPatient(), "Patient should match");
        assertEquals("Flu", record.getDiagnosis(), "Diagnosis should match");
        assertEquals(testDate, record.getRecordDate(), "Record date should match");
    }
    
    @Test
    @DisplayName("Test creating a medical record for non-existent patient")
    void testCreateMedicalRecordNonExistentPatient() {
        // Set up mock to return empty for a non-existent patient
        when(mockPatientService.findPatientById("NONEXISTENT")).thenReturn(Optional.empty());
        
        // Try to create a record for a non-existent patient
        assertThrows(IllegalStateException.class, () -> {
            medicalRecordService.createMedicalRecordByPatientId("MR001", "NONEXISTENT", "Flu", testDate);
        }, "Should throw exception for non-existent patient");
    }
    
    @Test
    @DisplayName("Test creating a medical record from an appointment")
    void testCreateMedicalRecordFromAppointment() {
        // Create a medical record from an appointment
        MedicalRecord record = medicalRecordService.createMedicalRecordFromAppointment(
                "MR001", "A001", "Seasonal allergies", "Patient reports sneezing and itchy eyes");
        
        // Verify the record was created correctly
        assertNotNull(record, "Medical record should not be null");
        assertEquals("MR001", record.getId(), "ID should match");
        assertEquals(testPatient, record.getPatient(), "Patient should match");
        assertEquals("Seasonal allergies", record.getDiagnosis(), "Diagnosis should match");
        assertEquals("Patient reports sneezing and itchy eyes", record.getNotes(), "Notes should match");
        assertEquals(testAppointment, record.getRelatedAppointment(), "Related appointment should match");
    }
    
    @Test
    @DisplayName("Test creating a medical record from a non-existent appointment")
    void testCreateMedicalRecordNonExistentAppointment() {
        // Set up mock to return empty for a non-existent appointment
        when(mockAppointmentService.findAppointmentById("NONEXISTENT")).thenReturn(Optional.empty());
        
        // Try to create a record for a non-existent appointment
        assertThrows(IllegalStateException.class, () -> {
            medicalRecordService.createMedicalRecordFromAppointment(
                    "MR001", "NONEXISTENT", "Flu", "Notes");
        }, "Should throw exception for non-existent appointment");
    }
    
    @Test
    @DisplayName("Test saving an existing medical record")
    void testSaveMedicalRecord() {
        // Create an initial record
        MedicalRecord initialRecord = new MedicalRecord("MR001", testPatient, "Initial diagnosis", testDate);
        
        // Save the record
        MedicalRecord savedRecord = medicalRecordService.saveMedicalRecord(initialRecord);
        
        // Verify the record was saved
        assertNotNull(savedRecord, "Saved record should not be null");
        assertEquals("MR001", savedRecord.getId(), "ID should match");
        
        // Retrieve the record to verify it was saved
        Optional<MedicalRecord> retrievedRecord = medicalRecordService.findById("MR001");
        assertTrue(retrievedRecord.isPresent(), "Record should be retrievable after saving");
        assertEquals("Initial diagnosis", retrievedRecord.get().getDiagnosis(), "Diagnosis should match");
    }
    
    @Test
    @DisplayName("Test finding a medical record by ID")
    void testFindById() {
        // Create and save a record
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Test diagnosis", testDate);
        
        // Find the record by ID
        Optional<MedicalRecord> foundRecord = medicalRecordService.findById("MR001");
        
        // Verify the record was found
        assertTrue(foundRecord.isPresent(), "Record should be found");
        assertEquals("Test diagnosis", foundRecord.get().getDiagnosis(), "Diagnosis should match");
    }
    
    @Test
    @DisplayName("Test finding a non-existent medical record returns empty Optional")
    void testFindByIdNonExistent() {
        // Find a non-existent record
        Optional<MedicalRecord> foundRecord = medicalRecordService.findById("NONEXISTENT");
        
        // Verify no record was found
        assertFalse(foundRecord.isPresent(), "No record should be found");
    }
    
    @Test
    @DisplayName("Test getting all medical records")
    void testGetAllMedicalRecords() {
        // Create and save multiple records
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Diagnosis 1", testDate);
        medicalRecordService.createMedicalRecord("MR002", testPatient, "Diagnosis 2", testDate);
        medicalRecordService.createMedicalRecord("MR003", testPatient, "Diagnosis 3", testDate);
        
        // Get all records
        List<MedicalRecord> allRecords = medicalRecordService.getAllMedicalRecords();
        
        // Verify all records were returned
        assertEquals(3, allRecords.size(), "Should return all 3 records");
    }
    
    @Test
    @DisplayName("Test finding medical records by patient ID")
    void testFindByPatientId() {
        // Create a second patient
        Patient secondPatient = new Patient("P002", "Jane", "Smith", LocalDate.of(1985, 3, 10));
        lenient().when(mockPatientService.findPatientById("P002")).thenReturn(Optional.of(secondPatient));
        
        // Create records for both patients
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Patient 1 Diagnosis", testDate);
        medicalRecordService.createMedicalRecord("MR002", secondPatient, "Patient 2 Diagnosis", testDate);
        medicalRecordService.createMedicalRecord("MR003", testPatient, "Patient 1 Follow-up", testDate);
        
        // Find records for the first patient
        List<MedicalRecord> patientRecords = medicalRecordService.findByPatientId("P001");
        
        // Verify correct records were returned
        assertEquals(2, patientRecords.size(), "Should return 2 records for patient 1");
        assertTrue(patientRecords.stream().allMatch(record -> record.getPatient().getId().equals("P001")),
                "All records should be for patient 1");
    }
    
    @Test
    @DisplayName("Test finding medical record by appointment ID")
    void testFindByAppointmentId() {
        // Create a second appointment
        Appointment secondAppointment = new Appointment("A002", testPatient, "Follow-up", LocalDateTime.now());
        lenient().when(mockAppointmentService.findAppointmentById("A002")).thenReturn(Optional.of(secondAppointment));
        
        // Create a record with the first appointment
        MedicalRecord record1 = medicalRecordService.createMedicalRecordFromAppointment(
                "MR001", "A001", "First visit diagnosis", "Notes");
        
        // Create a record with the second appointment
        MedicalRecord record2 = new MedicalRecord("MR002", testPatient, "Second visit", testDate);
        record2.setRelatedAppointment(secondAppointment);
        medicalRecordService.saveMedicalRecord(record2);
        
        // Find record by appointment ID
        Optional<MedicalRecord> foundRecord = medicalRecordService.findByAppointmentId("A002");
        
        // Verify correct record was returned
        assertTrue(foundRecord.isPresent(), "Record should be found");
        assertEquals("MR002", foundRecord.get().getId(), "Should return record for appointment 2");
    }
    
    @Test
    @DisplayName("Test finding medical records by record date")
    void testFindByRecordDate() {
        // Create records with different dates
        LocalDate yesterday = testDate.minusDays(1);
        LocalDate tomorrow = testDate.plusDays(1);
        
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Today's diagnosis", testDate);
        medicalRecordService.createMedicalRecord("MR002", testPatient, "Yesterday's diagnosis", yesterday);
        medicalRecordService.createMedicalRecord("MR003", testPatient, "Tomorrow's diagnosis", tomorrow);
        
        // Find records for today
        List<MedicalRecord> todayRecords = medicalRecordService.findByRecordDate(testDate);
        
        // Verify correct records were returned
        assertEquals(1, todayRecords.size(), "Should return 1 record for today");
        assertEquals("Today's diagnosis", todayRecords.get(0).getDiagnosis(), "Should return today's record");
    }
    
    @Test
    @DisplayName("Test updating diagnosis")
    void testUpdateDiagnosis() {
        // Create a record
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Initial diagnosis", testDate);
        
        // Update the diagnosis
        Optional<MedicalRecord> updatedRecord = medicalRecordService.updateDiagnosis("MR001", "Updated diagnosis");
        
        // Verify the diagnosis was updated
        assertTrue(updatedRecord.isPresent(), "Updated record should be present");
        assertEquals("Updated diagnosis", updatedRecord.get().getDiagnosis(), "Diagnosis should be updated");
        
        // Verify the change is persistent
        Optional<MedicalRecord> retrievedRecord = medicalRecordService.findById("MR001");
        assertEquals("Updated diagnosis", retrievedRecord.get().getDiagnosis(), "Update should be persisted");
    }
    
    @Test
    @DisplayName("Test adding notes")
    void testAddNotes() {
        // Create a record
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Test diagnosis", testDate);
        
        // Add notes
        Optional<MedicalRecord> updatedRecord = medicalRecordService.addNotes("MR001", "Initial notes");
        
        // Verify notes were added
        assertTrue(updatedRecord.isPresent(), "Updated record should be present");
        assertEquals("Initial notes", updatedRecord.get().getNotes(), "Notes should be added");
        
        // Add more notes
        updatedRecord = medicalRecordService.addNotes("MR001", "Additional notes");
        
        // Verify notes were appended
        assertTrue(updatedRecord.isPresent(), "Updated record should be present");
        assertTrue(updatedRecord.get().getNotes().contains("Initial notes"), "Original notes should be preserved");
        assertTrue(updatedRecord.get().getNotes().contains("Additional notes"), "New notes should be added");
    }
    
    @Test
    @DisplayName("Test linking to appointment")
    void testLinkToAppointment() {
        // Create a record without an appointment
        MedicalRecord record = medicalRecordService.createMedicalRecord("MR001", testPatient, "Test diagnosis", testDate);
        assertNull(record.getRelatedAppointment(), "Initially should have no appointment");
        
        // Link to an appointment
        Optional<MedicalRecord> updatedRecord = medicalRecordService.linkToAppointment("MR001", "A001");
        
        // Verify the appointment was linked
        assertTrue(updatedRecord.isPresent(), "Updated record should be present");
        assertNotNull(updatedRecord.get().getRelatedAppointment(), "Should have an appointment linked");
        assertEquals("A001", updatedRecord.get().getRelatedAppointment().getId(), "Appointment ID should match");
    }
    
    @Test
    @DisplayName("Test deleting a medical record")
    void testDeleteMedicalRecord() {
        // Create a record
        medicalRecordService.createMedicalRecord("MR001", testPatient, "Test diagnosis", testDate);
        
        // Verify it exists
        assertTrue(medicalRecordService.findById("MR001").isPresent(), "Record should exist before deletion");
        
        // Delete the record
        boolean deleted = medicalRecordService.deleteMedicalRecord("MR001");
        
        // Verify deletion was successful
        assertTrue(deleted, "Deletion should return true");
        assertFalse(medicalRecordService.findById("MR001").isPresent(), "Record should no longer exist after deletion");
    }
    
    @Test
    @DisplayName("Test deleting a non-existent record")
    void testDeleteNonExistentRecord() {
        // Delete a non-existent record
        boolean deleted = medicalRecordService.deleteMedicalRecord("NONEXISTENT");
        
        // Verify deletion reports failure
        assertFalse(deleted, "Deletion should return false for non-existent record");
    }
} 