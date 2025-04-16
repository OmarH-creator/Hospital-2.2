package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for Appointment validation and scheduling
 */
public class AppointmentValidationTest {
    
    private final Patient testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 1, 1));

    /**
     * Test appointment creation with various valid parameters
     */
    @ParameterizedTest(name = "Valid appointment with id={0}, type={1}, dateTime={2}")
    @MethodSource("validAppointmentParameters")
    @DisplayName("Test appointment creation with valid parameters")
    void testValidAppointmentCreation(String id, String type, LocalDateTime dateTime) {
        Appointment appointment = new Appointment(id, testPatient, type, dateTime);
        
        assertEquals(id, appointment.getId(), "Appointment ID should match the provided value");
        assertEquals(testPatient, appointment.getPatient(), "Patient should match the provided value");
        assertEquals(type, appointment.getType(), "Type should match the provided value");
        assertEquals(dateTime, appointment.getDateTime(), "DateTime should match the provided value");
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus(), "New appointment should have SCHEDULED status");
    }
    
    /**
     * Test appointment creation with invalid ID
     */
    @ParameterizedTest(name = "Invalid appointment with id={0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Test appointment creation with invalid ID")
    void testInvalidAppointmentId(String id) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Appointment(id, testPatient, "Checkup", LocalDateTime.now())
        );
        
        String expectedMessage = id == null ? "ID cannot be null" : "ID cannot be empty";
        assertEquals(expectedMessage, exception.getMessage(), "Exception message should indicate the ID issue");
    }
    
    /**
     * Test appointment creation with invalid appointment type
     */
    @ParameterizedTest(name = "Invalid appointment with type={0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Test appointment creation with invalid type")
    void testInvalidAppointmentType(String type) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Appointment("A001", testPatient, type, LocalDateTime.now())
        );
        
        String expectedMessage = type == null ? "Type cannot be null" : "Type cannot be empty";
        assertEquals(expectedMessage, exception.getMessage(), "Exception message should indicate the type issue");
    }
    
    /**
     * Test appointment creation with null patient
     */
    @Test
    @DisplayName("Test appointment creation with null patient")
    void testNullPatient() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Appointment("A001", null, "Checkup", LocalDateTime.now())
        );
        
        assertEquals("Patient cannot be null", exception.getMessage(), 
                     "Exception message should indicate the patient issue");
    }
    
    /**
     * Test appointment creation with null dateTime
     */
    @Test
    @DisplayName("Test appointment creation with null dateTime")
    void testNullDateTime() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Appointment("A001", testPatient, "Checkup", null)
        );
        
        assertEquals("DateTime cannot be null", exception.getMessage(), 
                     "Exception message should indicate the dateTime issue");
    }
    
    /**
     * Test appointment status changes
     */
    @ParameterizedTest(name = "Status change: {0}")
    @MethodSource("statusChangeParameters")
    @DisplayName("Test appointment status changes")
    void testStatusChanges(String action, Appointment.Status expectedStatus) {
        Appointment appointment = new Appointment("A001", testPatient, "Checkup", LocalDateTime.now());
        
        // Apply the action
        switch (action) {
            case "complete":
                appointment.complete();
                break;
            case "cancel":
                appointment.cancel();
                break;
            case "reschedule":
                appointment.reschedule(LocalDateTime.now().plusDays(1));
                break;
            // Default case not needed as we control the input
        }
        
        assertEquals(expectedStatus, appointment.getStatus(), 
                     "Appointment status should be updated correctly after " + action);
    }
    
    /**
     * Test appointment rescheduling with invalid dateTime
     */
    @Test
    @DisplayName("Test appointment rescheduling with null dateTime")
    void testRescheduleWithNullDateTime() {
        Appointment appointment = new Appointment("A001", testPatient, "Checkup", LocalDateTime.now());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            appointment.reschedule(null)
        );
        
        assertEquals("New date time cannot be null", exception.getMessage(), 
                     "Exception message should indicate the reschedule dateTime issue");
    }
    
    // MethodSource for parameterized tests
    private static Stream<Arguments> validAppointmentParameters() {
        LocalDateTime now = LocalDateTime.now();
        
        return Stream.of(
            Arguments.of("A001", "Checkup", now),
            Arguments.of("A002", "Surgery", now.plusDays(7)),
            Arguments.of("A003", "Consultation", now.plusHours(5)),
            Arguments.of("A004", "Physical Therapy", now.plusDays(3).plusHours(2)),
            Arguments.of("A005", "X-Ray", now.plusWeeks(1))
        );
    }
    
    private static Stream<Arguments> statusChangeParameters() {
        return Stream.of(
            Arguments.of("complete", Appointment.Status.COMPLETED),
            Arguments.of("cancel", Appointment.Status.CANCELLED),
            Arguments.of("reschedule", Appointment.Status.RESCHEDULED)
        );
    }
} 