package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Appointment model
 */
public class AppointmentTest {

    private Patient testPatient;
    private LocalDateTime dateTime;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        dateTime = LocalDateTime.now().plusDays(1); // Future appointment
        testAppointment = new Appointment("A001", testPatient, "Check-up", dateTime);
    }

    @Test
    @DisplayName("Test appointment initialization")
    void testInitialization() {
        // Verify fields are correctly initialized
        assertEquals("A001", testAppointment.getId(), "ID should match");
        assertEquals(testPatient, testAppointment.getPatient(), "Patient should match");
        assertEquals("Check-up", testAppointment.getType(), "Type should match");
        assertEquals(dateTime, testAppointment.getDateTime(), "DateTime should match");
        assertEquals(Appointment.Status.SCHEDULED, testAppointment.getStatus(), "Status should be SCHEDULED initially");
    }

    @Test
    @DisplayName("Test constructor validation")
    void testConstructorValidation() {
        // Test null ID
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new Appointment(null, testPatient, "Check-up", dateTime);
        });
        assertTrue(exception1.getMessage().contains("ID cannot be null"), "Should validate ID is not null");

        // Test empty ID
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new Appointment("", testPatient, "Check-up", dateTime);
        });
        assertTrue(exception2.getMessage().contains("ID cannot be empty"), "Should validate ID is not empty");

        // Test null patient
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new Appointment("A001", null, "Check-up", dateTime);
        });
        assertTrue(exception3.getMessage().contains("Patient cannot be null"), "Should validate patient is not null");

        // Test null type
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new Appointment("A001", testPatient, null, dateTime);
        });
        assertTrue(exception4.getMessage().contains("Type cannot be null"), "Should validate type is not null");

        // Test empty type
        Exception exception5 = assertThrows(IllegalArgumentException.class, () -> {
            new Appointment("A001", testPatient, "", dateTime);
        });
        assertTrue(exception5.getMessage().contains("Type cannot be empty"), "Should validate type is not empty");

        // Test null dateTime
        Exception exception6 = assertThrows(IllegalArgumentException.class, () -> {
            new Appointment("A001", testPatient, "Check-up", null);
        });
        assertTrue(exception6.getMessage().contains("DateTime cannot be null"), "Should validate dateTime is not null");
    }

    @Test
    @DisplayName("Test complete method")
    void testCompleteMethod() {
        // Initially scheduled
        assertEquals(Appointment.Status.SCHEDULED, testAppointment.getStatus(), "Status should be SCHEDULED initially");
        
        // Complete appointment
        testAppointment.complete();
        
        // Verify status is updated
        assertEquals(Appointment.Status.COMPLETED, testAppointment.getStatus(), "Status should be COMPLETED after complete()");
    }

    @Test
    @DisplayName("Test cancel method")
    void testCancelMethod() {
        // Initially scheduled
        assertEquals(Appointment.Status.SCHEDULED, testAppointment.getStatus(), "Status should be SCHEDULED initially");
        
        // Cancel appointment
        testAppointment.cancel();
        
        // Verify status is updated
        assertEquals(Appointment.Status.CANCELLED, testAppointment.getStatus(), "Status should be CANCELLED after cancel()");
    }

    @Test
    @DisplayName("Test reschedule method")
    void testRescheduleMethod() {
        // Initially scheduled with original date
        assertEquals(dateTime, testAppointment.getDateTime(), "Initially has the original date/time");
        
        // New date time for rescheduling
        LocalDateTime newDateTime = dateTime.plusDays(7);
        
        // Reschedule appointment
        testAppointment.reschedule(newDateTime);
        
        // Verify date and status are updated
        assertEquals(newDateTime, testAppointment.getDateTime(), "DateTime should be updated");
        assertEquals(Appointment.Status.RESCHEDULED, testAppointment.getStatus(), "Status should be RESCHEDULED");
        
        // Test null newDateTime validation
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            testAppointment.reschedule(null);
        });
        assertTrue(exception.getMessage().contains("New date time cannot be null"), "Should validate new date time is not null");
    }

    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        // Test setId
        testAppointment.setId("A002");
        assertEquals("A002", testAppointment.getId(), "ID should be updated");
        
        // Test setPatient
        Patient newPatient = new Patient("P002", "Jane", "Smith", LocalDate.of(1985, 3, 20));
        testAppointment.setPatient(newPatient);
        assertEquals(newPatient, testAppointment.getPatient(), "Patient should be updated");
        
        // Test setType
        testAppointment.setType("Surgery");
        assertEquals("Surgery", testAppointment.getType(), "Type should be updated");
        
        // Test setDateTime
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(14);
        testAppointment.setDateTime(newDateTime);
        assertEquals(newDateTime, testAppointment.getDateTime(), "DateTime should be updated");
        
        // Test setStatus
        testAppointment.setStatus(Appointment.Status.COMPLETED);
        assertEquals(Appointment.Status.COMPLETED, testAppointment.getStatus(), "Status should be updated");
    }
} 