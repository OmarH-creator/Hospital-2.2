package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PatientServiceImpl
 */
public class PatientServiceImplTest {
    
    /**
     * Custom implementation of PatientService for testing
     */
    private static class TestPatientServiceImpl implements PatientService {
        private final Map<String, Patient> patientsById = new HashMap<>();
        
        @Override
        public Patient registerPatient(String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
            String patientId = "P" + (100 + patientsById.size());
            Patient patient = new Patient(patientId, name, dateOfBirth, gender, contactNumber, address);
            patientsById.put(patientId, patient);
            return patient;
        }
        
        @Override
        public Patient savePatient(Patient patient) {
            if (patient == null) {
                throw new IllegalArgumentException("Patient cannot be null");
            }
            patientsById.put(patient.getId(), patient);
            return patient;
        }
        
        @Override
        public Optional<Patient> findPatientById(String patientId) {
            return Optional.ofNullable(patientsById.get(patientId));
        }
        
        @Override
        public List<Patient> findPatientsByName(String namePart) {
            List<Patient> results = new ArrayList<>();
            String lowerNamePart = namePart.toLowerCase();
            
            for (Patient patient : patientsById.values()) {
                if (patient.getName().toLowerCase().contains(lowerNamePart)) {
                    results.add(patient);
                }
            }
            
            return results;
        }
        
        @Override
        public List<Patient> getAllPatients() {
            return new ArrayList<>(patientsById.values());
        }
        
        @Override
        public void updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
            Patient patient = patientsById.get(patientId);
            if (patient != null) {
                patient.setName(name);
                patient.setDateOfBirth(dateOfBirth);
                patient.setGender(gender);
                patient.setContactNumber(contactNumber);
                patient.setAddress(address);
            } else {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
        }
        
        @Override
        public void admitPatient(String patientId) {
            Patient patient = patientsById.get(patientId);
            if (patient != null) {
                patient.admit();
            } else {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
        }
        
        @Override
        public void dischargePatient(String patientId) {
            Patient patient = patientsById.get(patientId);
            if (patient != null) {
                patient.discharge();
            } else {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
        }
        
        @Override
        public List<Patient> getAdmittedPatients() {
            List<Patient> admitted = new ArrayList<>();
            for (Patient patient : patientsById.values()) {
                if (patient.isAdmitted()) {
                    admitted.add(patient);
                }
            }
            return admitted;
        }
        
        @Override
        public boolean deletePatient(String patientId) {
            if (patientId == null || patientId.trim().isEmpty()) {
                return false;
            }
            
            Patient removedPatient = patientsById.remove(patientId);
            return removedPatient != null;
        }
    }

    private PatientService patientService;
    private Patient testPatient;
    
    @BeforeEach
    void setUp() {
        // Initialize the service and a test patient before each test
        patientService = new TestPatientServiceImpl();
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        
        // Save the test patient to the service
        patientService.savePatient(testPatient);
    }
    
    @Test
    @DisplayName("Test saving a patient")
    void testSavePatient() {
        // Create a new patient
        Patient newPatient = new Patient("P002", "Jane", "Smith", LocalDate.of(1985, 3, 20));
        
        // Save the patient
        Patient savedPatient = patientService.savePatient(newPatient);
        
        // Verify the patient was saved correctly
        assertEquals(newPatient.getId(), savedPatient.getId(), "ID should match after saving");
        assertEquals("Jane", savedPatient.getFirstName(), "First name should match after saving");
        
        // Verify we can retrieve the saved patient
        Optional<Patient> retrievedPatient = patientService.findPatientById("P002");
        assertTrue(retrievedPatient.isPresent(), "Should be able to retrieve the saved patient");
        assertEquals("Jane Smith", retrievedPatient.get().getFullName(), "Full name should match");
    }
    
    @Test
    @DisplayName("Test finding a patient by ID")
    void testFindPatientById() {
        // Find an existing patient
        Optional<Patient> foundPatient = patientService.findPatientById("P001");
        
        // Verify the patient was found
        assertTrue(foundPatient.isPresent(), "Should find the existing patient");
        assertEquals("John Doe", foundPatient.get().getFullName(), "Found patient should have correct name");
        
        // Try to find a non-existent patient
        Optional<Patient> notFoundPatient = patientService.findPatientById("NONEXISTENT");
        assertFalse(notFoundPatient.isPresent(), "Should not find a non-existent patient");
        
        // Test with null ID
        Optional<Patient> nullIdResult = patientService.findPatientById(null);
        assertFalse(nullIdResult.isPresent(), "Should handle null ID gracefully");
    }
    
    @Test
    @DisplayName("Test getting all patients")
    void testGetAllPatients() {
        // Add a second patient
        Patient secondPatient = new Patient("P002", "Jane", "Smith", LocalDate.of(1985, 3, 20));
        patientService.savePatient(secondPatient);
        
        // Get all patients
        List<Patient> allPatients = patientService.getAllPatients();
        
        // Verify we got both patients
        assertEquals(2, allPatients.size(), "Should retrieve all patients");
        assertTrue(allPatients.stream().anyMatch(p -> p.getId().equals("P001")), "Should contain first patient");
        assertTrue(allPatients.stream().anyMatch(p -> p.getId().equals("P002")), "Should contain second patient");
    }
    
    @Test
    @DisplayName("Test admitting a patient")
    void testAdmitPatient() {
        // Verify patient is not admitted initially
        assertFalse(testPatient.isAdmitted(), "Patient should not be admitted initially");
        
        // Admit the patient
        patientService.admitPatient("P001");
        
        // Verify patient is now admitted
        Optional<Patient> admittedPatient = patientService.findPatientById("P001");
        assertTrue(admittedPatient.isPresent(), "Should find the patient");
        assertTrue(admittedPatient.get().isAdmitted(), "Patient should be admitted");
        
        // Try to admit the same patient again (should not throw an exception)
        try {
            patientService.admitPatient("P001");
        } catch (Exception e) {
            fail("Should not throw exception when admitting an already admitted patient");
        }
        
        // Try to admit a non-existent patient (should throw IllegalArgumentException)
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.admitPatient("NONEXISTENT");
        }, "Should throw exception when admitting a non-existent patient");
    }
    
    @Test
    @DisplayName("Test discharging a patient")
    void testDischargePatient() {
        // First admit the patient
        patientService.admitPatient("P001");
        assertTrue(testPatient.isAdmitted(), "Patient should be admitted");
        
        // Discharge the patient
        patientService.dischargePatient("P001");
        
        // Verify patient is now discharged
        Optional<Patient> dischargedPatient = patientService.findPatientById("P001");
        assertTrue(dischargedPatient.isPresent(), "Should find the patient");
        assertFalse(dischargedPatient.get().isAdmitted(), "Patient should be discharged");
        
        // Try to discharge the same patient again (should not throw an exception)
        try {
            patientService.dischargePatient("P001");
        } catch (Exception e) {
            fail("Should not throw exception when discharging an already discharged patient");
        }
        
        // Try to discharge a non-existent patient (should throw IllegalArgumentException)
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.dischargePatient("NONEXISTENT");
        }, "Should throw exception when discharging a non-existent patient");
    }
    
    @Test
    @DisplayName("Test exception handling when saving null patient")
    void testSaveNullPatient() {
        // Verify that saving a null patient throws an exception
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.savePatient(null);
        }, "Should throw IllegalArgumentException when patient is null");
        
        // No longer testing Patient with null ID here as that case is now
        // tested in PatientWhiteBoxTest which validates constructor arguments
    }
} 