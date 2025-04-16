package com.example.hospitalsystemsimpletesting.whitebox;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White box tests for Appointment class focusing on:
 * - Statement coverage
 * - Branch coverage
 * - Path coverage
 * - Boundary testing
 */
public class AppointmentWhiteBoxTest {

    private Patient testPatient;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        testDateTime = LocalDateTime.now().plusDays(1);
    }

    @DisplayName("Test constructor validation with null and empty ID")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testConstructorIdValidation(String id) {
        // Testing branch where ID is null or empty
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Appointment(id, testPatient, "Check-up", testDateTime));
        
        String expectedMessage = id == null ? "ID cannot be null" : "ID cannot be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @DisplayName("Test constructor validation with null patient")
    @Test
    void testConstructorNullPatientValidation() {
        // Testing branch where patient is null
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Appointment("A001", null, "Check-up", testDateTime));
        
        assertEquals("Patient cannot be null", exception.getMessage());
    }

    @DisplayName("Test constructor validation with null and empty type")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testConstructorTypeValidation(String type) {
        // Testing branch where type is null or empty
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Appointment("A001", testPatient, type, testDateTime));
        
        String expectedMessage = type == null ? "Type cannot be null" : "Type cannot be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @DisplayName("Test constructor validation with null dateTime")
    @Test
    void testConstructorNullDateTimeValidation() {
        // Testing branch where dateTime is null
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Appointment("A001", testPatient, "Check-up", null));
        
        assertEquals("DateTime cannot be null", exception.getMessage());
    }

    @DisplayName("Test successful appointment creation")
    @Test
    void testSuccessfulAppointmentCreation() {
        // Testing the "happy path" - all inputs valid
        Appointment appointment = new Appointment("A001", testPatient, "Check-up", testDateTime);
        
        assertEquals("A001", appointment.getId());
        assertEquals(testPatient, appointment.getPatient());
        assertEquals("Check-up", appointment.getType());
        assertEquals(testDateTime, appointment.getDateTime());
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus());
    }

    @DisplayName("Test complete() method")
    @Test
    void testCompleteMethod() {
        Appointment appointment = new Appointment("A001", testPatient, "Check-up", testDateTime);
        
        // Test initial state
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus());
        
        // Test complete() - changes status to COMPLETED
        appointment.complete();
        assertEquals(Appointment.Status.COMPLETED, appointment.getStatus());
        
        // Test complete() when already completed - should have no effect
        appointment.complete();
        assertEquals(Appointment.Status.COMPLETED, appointment.getStatus());
    }

    @DisplayName("Test cancel() method")
    @Test
    void testCancelMethod() {
        Appointment appointment = new Appointment("A001", testPatient, "Check-up", testDateTime);
        
        // Test initial state
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus());
        
        // Test cancel() - changes status to CANCELLED
        appointment.cancel();
        assertEquals(Appointment.Status.CANCELLED, appointment.getStatus());
        
        // Test cancel() when already cancelled - should have no effect
        appointment.cancel();
        assertEquals(Appointment.Status.CANCELLED, appointment.getStatus());
    }

    @DisplayName("Test reschedule() method")
    @Test
    void testRescheduleMethod() {
        Appointment appointment = new Appointment("A001", testPatient, "Check-up", testDateTime);
        LocalDateTime newDateTime = testDateTime.plusDays(7);
        
        // Test initial state
        assertEquals(testDateTime, appointment.getDateTime());
        
        // Test reschedule() - changes dateTime and status to RESCHEDULED
        appointment.reschedule(newDateTime);
        assertEquals(newDateTime, appointment.getDateTime());
        assertEquals(Appointment.Status.RESCHEDULED, appointment.getStatus());
        
        // Test reschedule() with null dateTime - should throw exception
        LocalDateTime finalNewDateTime = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                appointment.reschedule(finalNewDateTime));
        assertTrue(exception.getMessage().contains("New date time cannot be null"));
        
        // Check appointment state is unchanged after failed reschedule
        assertEquals(newDateTime, appointment.getDateTime());
    }

    @DisplayName("Test reschedule() after cancellation")
    @Test
    void testRescheduleAfterCancellation() {
        Appointment appointment = new Appointment("A001", testPatient, "Check-up", testDateTime);
        LocalDateTime newDateTime = testDateTime.plusDays(7);
        
        // First cancel the appointment
        appointment.cancel();
        
        // Try to reschedule the cancelled appointment - this should still work in the model
        // (service layer might enforce additional rules)
        appointment.reschedule(newDateTime);
        assertEquals(newDateTime, appointment.getDateTime());
        assertEquals(Appointment.Status.RESCHEDULED, appointment.getStatus());
    }

    @DisplayName("Test all getters")
    @Test
    void testGetters() {
        Appointment appointment = new Appointment("A001", testPatient, "Check-up", testDateTime);
        
        assertEquals("A001", appointment.getId());
        assertEquals(testPatient, appointment.getPatient());
        assertEquals("Check-up", appointment.getType());
        assertEquals(testDateTime, appointment.getDateTime());
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus());
    }
} 