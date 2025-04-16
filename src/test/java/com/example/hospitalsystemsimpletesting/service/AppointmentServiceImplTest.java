package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
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
 * Test class for AppointmentServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImplTest {

    @Mock
    private PatientService mockPatientService;

    private AppointmentService appointmentService;
    private Patient testPatient;
    private LocalDateTime futureDateTime;

    @BeforeEach
    void setUp() {
        // Initialize with mock PatientService
        appointmentService = new AppointmentServiceImpl(mockPatientService);
        
        // Create a test patient
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        
        // Set up a future date for appointments
        futureDateTime = LocalDateTime.now().plusDays(7);
        
        // Set up mock patient service behavior using lenient stubbing
        // This avoids "unnecessary stubbing" errors for tests that don't use these stubs
        lenient().when(mockPatientService.findPatientById("P001")).thenReturn(Optional.of(testPatient));
        lenient().when(mockPatientService.findPatientById("NONEXISTENT")).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Test scheduling an appointment")
    void testScheduleAppointment() {
        // Schedule an appointment
        Appointment appointment = appointmentService.scheduleAppointment(
                testPatient, "Check-up", futureDateTime);
        
        // Verify appointment was created with correct values
        assertNotNull(appointment, "Appointment should not be null");
        assertEquals(testPatient, appointment.getPatient(), "Patient should match");
        assertEquals("Check-up", appointment.getType(), "Type should match");
        assertEquals(futureDateTime, appointment.getDateTime(), "DateTime should match");
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus(), "Status should be SCHEDULED");
        
        // Verify we can retrieve the appointment
        Optional<Appointment> found = appointmentService.findAppointmentById(appointment.getId());
        assertTrue(found.isPresent(), "Should be able to find the appointment by ID");
    }
    
    @Test
    @DisplayName("Test scheduling an appointment by patient ID")
    void testScheduleAppointmentByPatientId() {
        // Schedule an appointment by patient ID
        Appointment appointment = appointmentService.scheduleAppointmentByPatientId(
                "P001", "Check-up", futureDateTime);
        
        // Verify appointment was created with correct values
        assertNotNull(appointment, "Appointment should not be null");
        assertEquals(testPatient, appointment.getPatient(), "Patient should match");
        assertEquals("Check-up", appointment.getType(), "Type should match");
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus(), "Status should be SCHEDULED");
    }
    
    @Test
    @DisplayName("Test scheduling appointment with nonexistent patient ID throws exception")
    void testScheduleAppointmentNonexistentPatient() {
        // Verify exception is thrown for nonexistent patient
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            appointmentService.scheduleAppointmentByPatientId("NONEXISTENT", "Check-up", futureDateTime);
        });
        assertTrue(exception.getMessage().contains("not found"), "Exception message should indicate patient not found");
    }
    
    @Test
    @DisplayName("Test scheduling appointment with past date throws exception")
    void testScheduleAppointmentPastDate() {
        // Set up a past date time
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);
        
        // Verify exception is thrown for past date
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.scheduleAppointment(testPatient, "Check-up", pastDateTime);
        });
        assertTrue(exception.getMessage().contains("past"), "Exception message should indicate past date issue");
    }
    
    @Test
    @DisplayName("Test scheduling appointment with null patient throws exception")
    void testScheduleAppointmentNullPatient() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.scheduleAppointment(null, "Check-up", futureDateTime);
        });
        assertTrue(exception.getMessage().contains("Patient cannot be null"), "Should validate patient is not null");
    }
    
    @Test
    @DisplayName("Test scheduling appointment with null or empty type throws exception")
    void testScheduleAppointmentInvalidType() {
        // Test null type
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.scheduleAppointment(testPatient, null, futureDateTime);
        });
        assertTrue(exception1.getMessage().contains("type cannot be null"), "Should validate type is not null");
        
        // Test empty type
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.scheduleAppointment(testPatient, "", futureDateTime);
        });
        assertTrue(exception2.getMessage().contains("type cannot be null or empty"), "Should validate type is not empty");
    }
    
    @Test
    @DisplayName("Test getting appointments by patient ID")
    void testGetAppointmentsByPatientId() {
        // Schedule two appointments for the same patient
        appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        appointmentService.scheduleAppointment(testPatient, "X-Ray", futureDateTime.plusDays(1));
        
        // Get appointments for the patient
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId("P001");
        
        // Verify we got both appointments
        assertEquals(2, appointments.size(), "Should have 2 appointments");
        assertTrue(appointments.stream().allMatch(a -> a.getPatient().getId().equals("P001")), 
                "All appointments should be for patient P001");
    }
    
    @Test
    @DisplayName("Test getting appointments by date")
    void testGetAppointmentsByDate() {
        // Create two appointments on different dates in the future
        LocalDateTime date1 = LocalDateTime.now().plusDays(10);
        LocalDateTime date2 = LocalDateTime.now().plusDays(11);
        
        appointmentService.scheduleAppointment(testPatient, "Check-up", date1);
        appointmentService.scheduleAppointment(testPatient, "X-Ray", date2);
        
        // Get appointments for the first date
        List<Appointment> appointments = appointmentService.getAppointmentsByDate(date1);
        
        // Verify we got only the appointment for that date
        assertEquals(1, appointments.size(), "Should have 1 appointment on the specified date");
        assertEquals(date1.getYear(), appointments.get(0).getDateTime().getYear(), "Appointment year should match");
        assertEquals(date1.getMonth(), appointments.get(0).getDateTime().getMonth(), "Appointment month should match");
        assertEquals(date1.getDayOfMonth(), appointments.get(0).getDateTime().getDayOfMonth(), "Appointment day should match");
    }
    
    @Test
    @DisplayName("Test getting appointments by status")
    void testGetAppointmentsByStatus() {
        // Schedule two appointments
        Appointment appointment1 = appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        Appointment appointment2 = appointmentService.scheduleAppointment(testPatient, "X-Ray", futureDateTime.plusDays(1));
        
        // Cancel one appointment
        appointmentService.cancelAppointment(appointment1.getId());
        
        // Get scheduled appointments
        List<Appointment> scheduledAppointments = appointmentService.getAppointmentsByStatus(Appointment.Status.SCHEDULED.name());
        
        // Get cancelled appointments
        List<Appointment> cancelledAppointments = appointmentService.getAppointmentsByStatus(Appointment.Status.CANCELLED.name());
        
        // Verify we got the correct appointments for each status
        assertEquals(1, scheduledAppointments.size(), "Should have 1 scheduled appointment");
        assertEquals(1, cancelledAppointments.size(), "Should have 1 cancelled appointment");
    }
    
    @Test
    @DisplayName("Test cancelling an appointment")
    void testCancelAppointment() {
        // Schedule an appointment
        Appointment appointment = appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        
        // Cancel the appointment
        boolean result = appointmentService.cancelAppointment(appointment.getId());
        
        // Verify the cancellation was successful
        assertTrue(result, "Cancellation should be successful");
        
        // Verify the appointment status was updated
        Optional<Appointment> updatedAppointment = appointmentService.findAppointmentById(appointment.getId());
        assertTrue(updatedAppointment.isPresent(), "Appointment should still exist");
        assertEquals(Appointment.Status.CANCELLED, updatedAppointment.get().getStatus(), "Status should be CANCELLED");
    }
    
    @Test
    @DisplayName("Test completing an appointment")
    void testCompleteAppointment() {
        // Schedule an appointment
        Appointment appointment = appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        
        // Complete the appointment
        boolean result = appointmentService.completeAppointment(appointment.getId());
        
        // Verify the completion was successful
        assertTrue(result, "Completion should be successful");
        
        // Verify the appointment status was updated
        Optional<Appointment> updatedAppointment = appointmentService.findAppointmentById(appointment.getId());
        assertTrue(updatedAppointment.isPresent(), "Appointment should still exist");
        assertEquals(Appointment.Status.COMPLETED, updatedAppointment.get().getStatus(), "Status should be COMPLETED");
    }
    
    @Test
    @DisplayName("Test rescheduling an appointment")
    void testRescheduleAppointment() {
        // Schedule an appointment
        Appointment appointment = appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        
        // New date time for rescheduling
        LocalDateTime newDateTime = futureDateTime.plusDays(3);
        
        // Reschedule the appointment
        Optional<Appointment> rescheduledAppointment = appointmentService.rescheduleAppointment(appointment.getId(), newDateTime);
        
        // Verify the rescheduling was successful
        assertTrue(rescheduledAppointment.isPresent(), "Rescheduled appointment should be present");
        assertEquals(newDateTime, rescheduledAppointment.get().getDateTime(), "Date time should be updated");
        assertEquals(Appointment.Status.RESCHEDULED, rescheduledAppointment.get().getStatus(), "Status should be RESCHEDULED");
    }
    
    @Test
    @DisplayName("Test rescheduling a cancelled appointment returns empty")
    void testRescheduleAppointmentCancelled() {
        // Schedule an appointment
        Appointment appointment = appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        
        // Cancel the appointment
        appointmentService.cancelAppointment(appointment.getId());
        
        // Try to reschedule the cancelled appointment
        Optional<Appointment> rescheduledAppointment = appointmentService.rescheduleAppointment(
                appointment.getId(), futureDateTime.plusDays(3));
        
        // Verify the rescheduling failed
        assertFalse(rescheduledAppointment.isPresent(), "Should not be able to reschedule a cancelled appointment");
    }
    
    @Test
    @DisplayName("Test getting all appointments")
    void testGetAllAppointments() {
        // Schedule two appointments
        appointmentService.scheduleAppointment(testPatient, "Check-up", futureDateTime);
        appointmentService.scheduleAppointment(testPatient, "X-Ray", futureDateTime.plusDays(1));
        
        // Get all appointments
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        
        // Verify we got both appointments
        assertEquals(2, allAppointments.size(), "Should have 2 appointments in total");
    }
} 
 