package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Patient model
 */
public class PatientTest {

    private Patient patient;
    private final LocalDate birthDate = LocalDate.of(1990, 5, 15);
    
    @BeforeEach
    void setUp() {
        // This method runs before each test
        patient = new Patient("P001", "John", "Doe", birthDate);
    }
    
    @Test
    @DisplayName("Test patient initialization")
    void testInitialization() {
        assertEquals("P001", patient.getId(), "Patient ID should match the constructor argument");
        assertEquals("John", patient.getFirstName(), "First name should match the constructor argument");
        assertEquals("Doe", patient.getLastName(), "Last name should match the constructor argument");
        assertEquals(birthDate, patient.getDateOfBirth(), "Birth date should match the constructor argument");
        assertFalse(patient.isAdmitted(), "New patient should not be admitted by default");
        assertNull(patient.getBloodType(), "Blood type should be null initially");
    }
    
    @Test
    @DisplayName("Test getFullName method")
    void testGetFullName() {
        assertEquals("John Doe", patient.getFullName(), "Full name should be firstName + space + lastName");
        
        // Test with different values
        patient.setFirstName("Jane");
        assertEquals("Jane Doe", patient.getFullName(), "Full name should update when first name changes");
        
        patient.setLastName("Smith");
        assertEquals("Jane Smith", patient.getFullName(), "Full name should update when last name changes");
    }
    
    @Test
    @DisplayName("Test admit and discharge methods")
    void testAdmitAndDischarge() {
        // Initially not admitted
        assertFalse(patient.isAdmitted(), "Patient should not be admitted initially");
        
        // Admit the patient
        patient.admit();
        assertTrue(patient.isAdmitted(), "Patient should be admitted after calling admit()");
        
        // Discharge the patient
        patient.discharge();
        assertFalse(patient.isAdmitted(), "Patient should not be admitted after discharge()");
    }
    
    @Test
    @DisplayName("Test getAge calculation")
    void testGetAge() {
        int expectedAge = LocalDate.now().getYear() - birthDate.getYear();
        
        // Adjust for birthdays that haven't occurred yet this year
        if (birthDate.getDayOfYear() > LocalDate.now().getDayOfYear()) {
            expectedAge--;
        }
        
        assertEquals(expectedAge, patient.getAge(), "Age calculation should be accurate");
        
        // Test with null birthdate
        Patient patientNoBirthDate = new Patient("P002", "Test", "Patient", null);
        assertEquals(0, patientNoBirthDate.getAge(), "Age should be 0 when birth date is null");
    }
    
    @Test
    @DisplayName("Test blood type assignment")
    void testBloodTypeAssignment() {
        // Initially null
        assertNull(patient.getBloodType(), "Blood type should initially be null");
        
        // Set blood type
        patient.setBloodType("O+");
        assertEquals("O+", patient.getBloodType(), "Blood type should match the assigned value");
    }
} 