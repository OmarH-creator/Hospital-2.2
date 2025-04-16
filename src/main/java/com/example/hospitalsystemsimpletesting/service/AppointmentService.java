package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Patient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for appointment operations
 */
public interface AppointmentService {
    
    /**
     * Schedule a new appointment
     * @param patient The patient for whom the appointment is scheduled
     * @param type The type of appointment (e.g., "Check-up", "Surgery", "Consultation")
     * @param dateTime The date and time of the appointment
     * @return The created appointment
     * @throws IllegalArgumentException if any parameter is invalid
     */
    Appointment scheduleAppointment(Patient patient, String type, LocalDateTime dateTime);
    
    /**
     * Schedule a new appointment for an existing patient by ID
     * @param patientId The ID of the patient
     * @param type The type of appointment
     * @param dateTime The date and time of the appointment
     * @return The created appointment
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if the patient doesn't exist
     */
    Appointment scheduleAppointmentByPatientId(String patientId, String type, LocalDateTime dateTime);
    
    /**
     * Find an appointment by its ID
     * @param id The appointment ID
     * @return An Optional containing the appointment if found, or empty if not found
     */
    Optional<Appointment> findAppointmentById(String id);
    
    /**
     * Get all appointments
     * @return List of all appointments
     */
    List<Appointment> getAllAppointments();
    
    /**
     * Get all appointments for a specific patient
     * @param patientId The ID of the patient
     * @return List of the patient's appointments
     */
    List<Appointment> getAppointmentsByPatientId(String patientId);
    
    /**
     * Get appointments scheduled for a specific date
     * @param date The date to filter by
     * @return List of appointments for the specified date
     */
    List<Appointment> getAppointmentsByDate(LocalDateTime date);
    
    /**
     * Get appointments by status
     * @param status The status to filter by (using Appointment.Status enum values as strings: "SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED")
     * @return List of appointments with the specified status
     * @throws IllegalArgumentException if the status string doesn't match any value in the Status enum
     */
    List<Appointment> getAppointmentsByStatus(String status);
    
    /**
     * Cancel an appointment
     * @param appointmentId The ID of the appointment to cancel
     * @return true if cancellation was successful, false otherwise
     */
    boolean cancelAppointment(String appointmentId);
    
    /**
     * Mark an appointment as completed
     * @param appointmentId The ID of the appointment to complete
     * @return true if completion was successful, false otherwise
     */
    boolean completeAppointment(String appointmentId);
    
    /**
     * Reschedule an appointment
     * @param appointmentId The ID of the appointment to reschedule
     * @param newDateTime The new date and time
     * @return The updated appointment if found, empty otherwise
     * @throws IllegalArgumentException if the new date time is invalid
     */
    Optional<Appointment> rescheduleAppointment(String appointmentId, LocalDateTime newDateTime);
} 
 