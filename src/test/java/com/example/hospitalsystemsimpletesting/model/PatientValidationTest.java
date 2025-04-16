package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for Patient validation
 */
public class PatientValidationTest {

    /**
     * Test patient creation with various valid parameters
     */
    @ParameterizedTest(name = "Valid patient with id={0}, firstName={1}, lastName={2}")
    @MethodSource("validPatientParameters")
    @DisplayName("Test patient creation with valid parameters")
    void testValidPatientCreation(String id, String firstName, String lastName, LocalDate dateOfBirth) {
        Patient patient = new Patient(id, firstName, lastName, dateOfBirth);
        
        assertEquals(id, patient.getId(), "Patient ID should match the provided value");
        assertEquals(firstName, patient.getFirstName(), "First name should match the provided value");
        assertEquals(lastName, patient.getLastName(), "Last name should match the provided value");
        assertEquals(dateOfBirth, patient.getDateOfBirth(), "Date of birth should match the provided value");
        assertFalse(patient.isAdmitted(), "New patient should not be admitted by default");
    }
    
    /**
     * Test getFullName method with various name combinations
     */
    @ParameterizedTest(name = "Full name with firstName={0}, lastName={1}")
    @MethodSource("validNameParameters")
    @DisplayName("Test getFullName with various name combinations")
    void testFullNameCombinations(String firstName, String lastName, String expectedFullName) {
        Patient patient = new Patient("P001", firstName, lastName, LocalDate.of(1990, 1, 1));
        assertEquals(expectedFullName, patient.getFullName(), "Full name should be correctly formatted");
    }
    
    /**
     * Test age calculation for various birth dates
     */
    @ParameterizedTest(name = "Age calculation with birth date = {0}")
    @MethodSource("birthdateParameters")
    @DisplayName("Test age calculation for various birth dates")
    void testAgeCalculation(LocalDate dateOfBirth, int expectedAge) {
        Patient patient = new Patient("P001", "John", "Doe", dateOfBirth);
        assertEquals(expectedAge, patient.getAge(), "Age should be calculated correctly");
    }

    /**
     * Test patient with null birth date
     */
    @ParameterizedTest
    @NullSource
    @DisplayName("Test patient with null birth date")
    void testNullBirthDate(LocalDate dateOfBirth) {
        Patient patient = new Patient("P001", "John", "Doe", dateOfBirth);
        assertEquals(0, patient.getAge(), "Age should be 0 when birth date is null");
    }

    /**
     * Test blood type validation with various values
     */
    @ParameterizedTest(name = "Blood type validation with value = {0}")
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    @DisplayName("Test valid blood type assignment")
    void testValidBloodTypeAssignment(String bloodType) {
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.now());
        patient.setBloodType(bloodType);
        assertEquals(bloodType, patient.getBloodType(), "Blood type should match the assigned value");
    }

    // MethodSource for parameterized tests
    private static Stream<Arguments> validPatientParameters() {
        return Stream.of(
            Arguments.of("P001", "John", "Doe", LocalDate.of(1990, 1, 1)),
            Arguments.of("P002", "Jane", "Smith", LocalDate.of(1985, 5, 10)),
            Arguments.of("P003", "Robert", "Johnson", LocalDate.of(2000, 12, 31)),
            Arguments.of("P004", "Elizabeth", "Taylor", LocalDate.of(1975, 7, 15)),
            Arguments.of("P005", "Michael", "Brown", LocalDate.of(1995, 3, 22))
        );
    }
    
    private static Stream<Arguments> validNameParameters() {
        return Stream.of(
            Arguments.of("John", "Doe", "John Doe"),
            Arguments.of("Jane", "Smith", "Jane Smith"),
            Arguments.of("Robert", "Johnson", "Robert Johnson"),
            Arguments.of("", "Taylor", " Taylor"),
            Arguments.of("Michael", "", "Michael ")
        );
    }
    
    private static Stream<Arguments> birthdateParameters() {
        LocalDate today = LocalDate.now();
        
        return Stream.of(
            // Today's date from 20 years ago
            Arguments.of(today.minusYears(20), 20),
            
            // Today's date minus 1 day from 30 years ago (30 years old)
            Arguments.of(today.minusYears(30).minusDays(1), 30),
            
            // Today's date plus 1 day from 40 years ago (39 years old as birthday hasn't occurred yet this year)
            Arguments.of(today.minusYears(40).plusDays(1), 39),
            
            // Today's date from 10 years ago (exactly 10 years old today)
            Arguments.of(today.minusYears(10), 10),
            
            // Future date (not born yet) - should handle this case
            Arguments.of(today.plusYears(1), 0)
        );
    }
} 