package com.example.hospitalsystemsimpletesting;

import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.DataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.MedicalRecordService;
import com.example.hospitalsystemsimpletesting.service.MedicalRecordServiceImpl;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.PatientServiceImpl;
import com.example.hospitalsystemsimpletesting.service.impl.CSVDataPersistenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MedicalRecordService persistence functionality.
 * Tests the complete cycle of saving and loading medical records.
 */
public class MedicalRecordPersistenceTest {
    
    private static final String TEST_DATA_DIR = "test_hospital_data";
    private MedicalRecordService medicalRecordService;
    private PatientService patientService;
    private DataPersistenceService persistenceService;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create test directory
        Path testDir = Paths.get(TEST_DATA_DIR);
        if (!Files.exists(testDir)) {
            Files.createDirectory(testDir);
        }
        
        // Set up the system field to use the test directory
        System.setProperty("hospital.data.dir", TEST_DATA_DIR);
        
        // Create a temporary PatientService to initialize the CSVDataPersistenceService
        PatientService tempPatientService = new PatientServiceImpl();
        
        // Initialize the real persistence service
        persistenceService = new CSVDataPersistenceService(tempPatientService);
        
        // Create real services with persistence
        patientService = new PatientServiceImpl(persistenceService);
        
        // Update the persistence service with the real PatientService
        ((CSVDataPersistenceService) persistenceService).setPatientService(patientService);
        
        // Create the MedicalRecordService with real dependencies
        medicalRecordService = new MedicalRecordServiceImpl(patientService, persistenceService);
        
        // Prepare test data
        createTestPatients();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Delete test data directory and all files
        Path testDir = Paths.get(TEST_DATA_DIR);
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                 .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         System.err.println("Error deleting: " + path + " - " + e.getMessage());
                     }
                 });
        }
    }
    
    private void createTestPatients() {
        // Create and register test patients
        patientService.registerPatient("John Doe", LocalDate.of(1980, 1, 15), "Male", "123-456-7890", "123 Main St");
        patientService.registerPatient("Jane Smith", LocalDate.of(1985, 5, 20), "Female", "987-654-3210", "456 Oak Ave");
        
        // Update the IDs to known values for easier reference in tests
        List<Patient> patients = patientService.getAllPatients();
        if (patients.size() >= 2) {
            Patient patient1 = patients.get(0);
            Patient patient2 = patients.get(1);
            try {
                patientService.updatePatient(patient1.getId(), patient1.getFullName(), patient1.getDateOfBirth(), 
                                         patient1.getGender(), patient1.getContactNumber(), patient1.getAddress());
                patientService.updatePatient(patient2.getId(), patient2.getFullName(), patient2.getDateOfBirth(), 
                                         patient2.getGender(), patient2.getContactNumber(), patient2.getAddress());
                // Save references to the IDs for later tests
                System.setProperty("test.patient1.id", patient1.getId());
                System.setProperty("test.patient2.id", patient2.getId());
            } catch (Exception e) {
                System.err.println("Error updating patient IDs: " + e.getMessage());
            }
        }
    }
    
    @Test
    @DisplayName("Test complete medical record persistence cycle")
    void testMedicalRecordPersistenceCycle() {
        // Get the patient IDs from system properties
        String patientId1 = System.getProperty("test.patient1.id");
        String patientId2 = System.getProperty("test.patient2.id");
        
        // Get count of existing records for comparison
        int initialRecordCount = medicalRecordService.getAllMedicalRecords().size();
        
        // 1. Create and save medical records
        String recordId1 = "MR" + UUID.randomUUID().toString().substring(0, 8);
        String recordId2 = "MR" + UUID.randomUUID().toString().substring(0, 8);
        
        MedicalRecord record1 = medicalRecordService.createMedicalRecordByPatientId(
                recordId1, patientId1, "Flu symptoms", LocalDate.now());
        MedicalRecord record2 = medicalRecordService.createMedicalRecordByPatientId(
                recordId2, patientId2, "Routine checkup", LocalDate.now().minusDays(5));
        
        // Add some notes to one record
        medicalRecordService.addNotes(recordId1, "Prescribed: Acetaminophen, rest for 3 days");
        
        // 2. Verify records were saved correctly
        List<MedicalRecord> allRecords = medicalRecordService.getAllMedicalRecords();
        assertEquals(initialRecordCount + 2, allRecords.size(), 
                "Should have " + (initialRecordCount + 2) + " records in memory");
        
        // 3. Create a new service instance that will load records from persistence
        MedicalRecordService newMedicalRecordService = new MedicalRecordServiceImpl(patientService, persistenceService);
        
        // 4. Verify the new service loaded the records correctly
        List<MedicalRecord> loadedRecords = newMedicalRecordService.getAllMedicalRecords();
        assertEquals(initialRecordCount + 2, loadedRecords.size(), 
                "Should have loaded " + (initialRecordCount + 2) + " records from persistence");
        
        // 5. Verify specific record details were preserved
        Optional<MedicalRecord> loadedRecord1 = newMedicalRecordService.findById(recordId1);
        assertTrue(loadedRecord1.isPresent(), "Record 1 should be loaded from persistence");
        assertEquals("Flu symptoms", loadedRecord1.get().getDiagnosis(), "Diagnosis should match");
        assertTrue(loadedRecord1.get().getNotes().contains("Prescribed: Acetaminophen"), 
                   "Notes should be preserved");
        assertEquals(patientId1, loadedRecord1.get().getPatient().getId(), "Patient ID should match");
        
        Optional<MedicalRecord> loadedRecord2 = newMedicalRecordService.findById(recordId2);
        assertTrue(loadedRecord2.isPresent(), "Record 2 should be loaded from persistence");
        assertEquals("Routine checkup", loadedRecord2.get().getDiagnosis(), "Diagnosis should match");
        assertEquals(patientId2, loadedRecord2.get().getPatient().getId(), "Patient ID should match");
        assertEquals(LocalDate.now().minusDays(5), loadedRecord2.get().getRecordDate(), 
                    "Record date should be preserved");
    }
    
    @Test
    @DisplayName("Test updating medical record persists changes")
    void testMedicalRecordUpdatePersistence() {
        // Get the patient ID from system properties
        String patientId = System.getProperty("test.patient1.id");
        
        // 1. Create a medical record
        String recordId = "MR" + UUID.randomUUID().toString().substring(0, 8);
        MedicalRecord record = medicalRecordService.createMedicalRecordByPatientId(
                recordId, patientId, "Initial diagnosis", LocalDate.now());
        
        // 2. Update the diagnosis
        medicalRecordService.updateDiagnosis(recordId, "Updated diagnosis");
        
        // 3. Create a new service instance that will load records from persistence
        MedicalRecordService newMedicalRecordService = new MedicalRecordServiceImpl(patientService, persistenceService);
        
        // 4. Verify the update was persisted
        Optional<MedicalRecord> loadedRecord = newMedicalRecordService.findById(recordId);
        assertTrue(loadedRecord.isPresent(), "Record should be loaded from persistence");
        assertEquals("Updated diagnosis", loadedRecord.get().getDiagnosis(), 
                    "Updated diagnosis should be persisted");
    }
    
    @Test
    @DisplayName("Test deleting medical record removes it from persistence")
    void testMedicalRecordDeletePersistence() {
        // Get the patient ID from system properties
        String patientId = System.getProperty("test.patient1.id");
        
        // 1. Create a medical record
        String recordId = "MR" + UUID.randomUUID().toString().substring(0, 8);
        MedicalRecord record = medicalRecordService.createMedicalRecordByPatientId(
                recordId, patientId, "Test diagnosis", LocalDate.now());
        
        // 2. Delete the record
        boolean deleted = medicalRecordService.deleteMedicalRecord(recordId);
        assertTrue(deleted, "Record should be deleted successfully");
        
        // 3. Create a new service instance that will load records from persistence
        MedicalRecordService newMedicalRecordService = new MedicalRecordServiceImpl(patientService, persistenceService);
        
        // 4. Verify the record was removed from persistence
        Optional<MedicalRecord> loadedRecord = newMedicalRecordService.findById(recordId);
        assertFalse(loadedRecord.isPresent(), "Record should not be loaded from persistence after deletion");
    }
} 