package com.example.hospitalsystemsimpletesting.whitebox;

import com.example.hospitalsystemsimpletesting.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White box tests for Patient class focusing on:
 * - Statement coverage
 * - Branch coverage
 * - Path coverage
 * - Boundary testing
 */
public class PatientWhiteBoxTest {

    @DisplayName("Test constructor validation with null and empty ID")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testConstructorIdValidation(String id) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Patient(id, "John", "Doe", LocalDate.of(1990, 1, 1)));
        
        String expectedMessage = id == null ? "ID cannot be null" : "ID cannot be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @DisplayName("Test constructor validation with null firstName")
    @Test
    void testConstructorNullFirstNameValidation() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Patient("P123", null, "Doe", LocalDate.of(1990, 1, 1)));
        
        assertEquals("First name cannot be null", exception.getMessage());
    }

    @DisplayName("Test constructor validation with null lastName")
    @Test
    void testConstructorNullLastNameValidation() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Patient("P123", "John", null, LocalDate.of(1990, 1, 1)));
        
        assertEquals("Last name cannot be null", exception.getMessage());
    }

    @DisplayName("Test getAge() with various birth dates")
    @ParameterizedTest
    @CsvSource({
        "1990-01-01, 33",   // Past date (age will vary based on current year)
        "2100-01-01, 0"     // Future date (should return 0)
    })
    void testGetAgeCalculation(LocalDate birthDate, int expectedAge) {
        // Note: This test assumes it's run in 2023, adjust expected values if running later
        Patient patient = new Patient("P001", "John", "Doe", birthDate);
        
        // Calculate expected age based on current date to make test time-independent
        int actualExpectedAge = expectedAge;
        if (birthDate.isBefore(LocalDate.now())) {
            actualExpectedAge = LocalDate.now().getYear() - birthDate.getYear();
            if (birthDate.plusYears(actualExpectedAge).isAfter(LocalDate.now())) {
                actualExpectedAge--;
            }
        } else {
            actualExpectedAge = 0; // Future birth date should return 0
        }
        
        assertEquals(actualExpectedAge, patient.getAge());
    }

    @DisplayName("Test getAge() with null birth date")
    @Test
    void testGetAgeWithNullBirthDate() {
        // Testing the branch where birth date is null
        Patient patient = new Patient("P001", "John", "Doe", null);
        assertEquals(0, patient.getAge());
    }

    @DisplayName("Test getFullName() method")
    @Test
    void testGetFullName() {
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        assertEquals("John Doe", patient.getFullName());
    }

    @DisplayName("Test admit() and discharge() methods")
    @Test
    void testAdmitAndDischarge() {
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        
        // Test initial state
        assertFalse(patient.isAdmitted());
        
        // Test admit() - changes state to admitted
        patient.admit();
        assertTrue(patient.isAdmitted());
        
        // Test admit() when already admitted - should have no effect
        patient.admit();
        assertTrue(patient.isAdmitted());
        
        // Test discharge() - changes state to not admitted
        patient.discharge();
        assertFalse(patient.isAdmitted());
        
        // Test discharge() when already discharged - should have no effect
        patient.discharge();
        assertFalse(patient.isAdmitted());
    }

    @DisplayName("Test bloodType assignment")
    @ParameterizedTest
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    void testBloodTypeAssignment(String bloodType) {
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        patient.setBloodType(bloodType);
        assertEquals(bloodType, patient.getBloodType());
    }

    @DisplayName("Test all getters")
    @Test
    void testGetters() {
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        Patient patient = new Patient("P001", "John", "Doe", birthDate);
        
        assertEquals("P001", patient.getId());
        assertEquals("John", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
        assertEquals(birthDate, patient.getDateOfBirth());
    }
} 