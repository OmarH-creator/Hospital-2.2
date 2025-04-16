package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class to verify that MedicalRecordService correctly calls persistence methods
 */
@ExtendWith(MockitoExtension.class)
public class MedicalRecordServicePersistenceErrorTest {

    @Mock
    private PatientService mockPatientService;
    
    @Mock
    private DataPersistenceService mockPersistenceService;
    
    private MedicalRecordService medicalRecordService;
    private Patient testPatient;
    
    @BeforeEach
    void setUp() {
        // Setup a real patient
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 1, 15));
        
        // Setup common mock behaviors using lenient() to avoid "unnecessary stubbing" errors
        lenient().when(mockPatientService.findPatientById("P001")).thenReturn(Optional.of(testPatient));
        
        // Initialize the service with mocked dependencies
        medicalRecordService = new MedicalRecordServiceImpl(mockPatientService, mockPersistenceService);
    }
    
    @Test
    @DisplayName("Test persistence is called when creating a medical record")
    void testPersistenceCalledOnCreate() {
        // Create a medical record
        medicalRecordService.createMedicalRecord(
                "MR001", testPatient, "Test Diagnosis", LocalDate.now());
        
        // Verify persistence was called
        verify(mockPersistenceService, times(1)).saveMedicalRecords(any());
    }
    
    @Test
    @DisplayName("Test persistence is called when updating a medical record")
    void testPersistenceCalledOnUpdate() {
        // First create a record
        medicalRecordService.createMedicalRecord(
                "MR002", testPatient, "Initial Diagnosis", LocalDate.now());
        
        // Reset mock to clear the create operation
        reset(mockPersistenceService);
        
        // Update the record
        medicalRecordService.updateDiagnosis("MR002", "Updated Diagnosis");
        
        // Verify persistence was called
        verify(mockPersistenceService, times(1)).saveMedicalRecords(any());
    }
    
    @Test
    @DisplayName("Test persistence is called when deleting a medical record")
    void testPersistenceCalledOnDelete() {
        // First create a record
        medicalRecordService.createMedicalRecord(
                "MR003", testPatient, "Test Diagnosis", LocalDate.now());
        
        // Reset mock to clear the create operation
        reset(mockPersistenceService);
        
        // Delete the record
        medicalRecordService.deleteMedicalRecord("MR003");
        
        // Verify persistence was called
        verify(mockPersistenceService, times(1)).saveMedicalRecords(any());
    }
    
    @Test
    @DisplayName("Test persistence is called when loading records at initialization")
    void testPersistenceCalledOnLoad() {
        // Just creating the service should call loadMedicalRecords
        new MedicalRecordServiceImpl(mockPatientService, mockPersistenceService);
        
        // Verify persistence load was called
        verify(mockPersistenceService, atLeast(1)).loadMedicalRecords();
    }
} 