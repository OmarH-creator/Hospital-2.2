package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Patient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the AppointmentService interface using an in-memory data store
 */
public class AppointmentServiceImpl implements AppointmentService {

    // In-memory data store for appointments
    private final Map<String, Appointment> appointmentDatabase = new HashMap<>();
    
    // Dependency on PatientService to find patients by ID
    private final PatientService patientService;
    
    // Dependency on DataPersistenceService for persistence
    private final DataPersistenceService persistenceService;
    
    /**
     * Constructor without explicit dependencies - creates its own PatientService
     */
    public AppointmentServiceImpl() {
        this.patientService = new PatientServiceImpl();
        this.persistenceService = null; // No persistence
    }
    
    /**
     * Constructor with PatientService dependency (for dependency injection)
     * @param patientService The PatientService implementation to use
     */
    public AppointmentServiceImpl(PatientService patientService) {
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        this.patientService = patientService;
        this.persistenceService = null; // No persistence
    }

    /**
     * Constructor with PatientService and DataPersistenceService
     * @param patientService The PatientService implementation to use
     * @param persistenceService The DataPersistenceService implementation to use
     */
    public AppointmentServiceImpl(PatientService patientService, DataPersistenceService persistenceService) {
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        this.patientService = patientService;
        this.persistenceService = persistenceService;
        loadAppointments();
    }
    
    /**
     * Load appointments from persistence
     */
    private void loadAppointments() {
        if (persistenceService != null) {
            List<Appointment> appointments = persistenceService.loadAppointments();
            for (Appointment appointment : appointments) {
                appointmentDatabase.put(appointment.getId(), appointment);
            }
            System.out.println("Loaded " + appointments.size() + " appointments from persistence");
        }
    }
    
    /**
     * Save appointments to persistence
     */
    private void saveAppointments() {
        if (persistenceService != null) {
            persistenceService.saveAppointments(new ArrayList<>(appointmentDatabase.values()));
            System.out.println("Saved " + appointmentDatabase.size() + " appointments to persistence");
        }
    }
    
    @Override
    public Appointment scheduleAppointment(Patient patient, String type, LocalDateTime dateTime) {
        // Validate parameters
        validateAppointmentParameters(patient, type, dateTime);
        
        // Generate a unique ID for the appointment
        String appointmentId = generateAppointmentId(patient);
        
        // Create and store the appointment
        Appointment appointment = new Appointment(appointmentId, patient, type, dateTime);
        appointmentDatabase.put(appointmentId, appointment);
        
        // Save to persistence
        saveAppointments();
        
        return appointment;
    }
    
    @Override
    public Appointment scheduleAppointmentByPatientId(String patientId, String type, LocalDateTime dateTime) {
        // Validate patientId
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        // Find the patient
        Optional<Patient> patientOpt = patientService.findPatientById(patientId);
        if (!patientOpt.isPresent()) {
            throw new IllegalStateException("Patient with ID " + patientId + " not found");
        }
        
        // Schedule the appointment for the found patient
        return scheduleAppointment(patientOpt.get(), type, dateTime);
    }
    
    @Override
    public Optional<Appointment> findAppointmentById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(appointmentDatabase.get(id));
    }
    
    @Override
    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointmentDatabase.values());
    }
    
    @Override
    public List<Appointment> getAppointmentsByPatientId(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return appointmentDatabase.values().stream()
                .filter(appointment -> appointment.getPatient().getId().equals(patientId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Appointment> getAppointmentsByDate(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        // Match the date part only, ignoring time
        return appointmentDatabase.values().stream()
                .filter(appointment -> {
                    LocalDateTime appointmentDate = appointment.getDateTime();
                    return appointmentDate.getYear() == date.getYear() &&
                           appointmentDate.getMonth() == date.getMonth() &&
                           appointmentDate.getDayOfMonth() == date.getDayOfMonth();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Appointment> getAppointmentsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        try {
            Appointment.Status statusEnum = Appointment.Status.valueOf(status.toUpperCase());
            return appointmentDatabase.values().stream()
                    .filter(appointment -> statusEnum == appointment.getStatus())
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
    
    @Override
    public boolean cancelAppointment(String appointmentId) {
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentId);
        
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            
            // Can't cancel already completed appointments
            if (Appointment.Status.COMPLETED == appointment.getStatus()) {
                return false;
            }
            
            appointment.cancel();
            saveAppointments();
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean completeAppointment(String appointmentId) {
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentId);
        
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            
            // Can't complete cancelled appointments
            if (Appointment.Status.CANCELLED == appointment.getStatus()) {
                return false;
            }
            
            appointment.complete();
            saveAppointments();
            return true;
        }
        
        return false;
    }
    
    @Override
    public Optional<Appointment> rescheduleAppointment(String appointmentId, LocalDateTime newDateTime) {
        if (newDateTime == null) {
            throw new IllegalArgumentException("New date time cannot be null");
        }
        
        // Validate the new date is not in the past
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule an appointment in the past");
        }
        
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentId);
        
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            
            // Can't reschedule cancelled appointments
            if (Appointment.Status.CANCELLED == appointment.getStatus()) {
                return Optional.empty();
            }
            
            appointment.reschedule(newDateTime);
            saveAppointments();
            return Optional.of(appointment);
        }
        
        return Optional.empty();
    }
    
    // Helper method to validate appointment parameters
    private void validateAppointmentParameters(Patient patient, String type, LocalDateTime dateTime) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment type cannot be null or empty");
        }
        
        if (dateTime == null) {
            throw new IllegalArgumentException("Appointment date time cannot be null");
        }
        
        // Validate the appointment is not in the past
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule an appointment in the past");
        }
    }
    
    // Helper method to generate a unique appointment ID
    private String generateAppointmentId(Patient patient) {
        // Get current count of appointments for this patient to create a sequence number
        long patientAppointmentCount = appointmentDatabase.values().stream()
                .filter(apt -> apt.getPatient().getId().equals(patient.getId()))
                .count() + 1;
                
        // Format: APT-[Patient ID]-[sequence number]
        return "APT-" + patient.getId() + "-" + patientAppointmentCount;
    }
} 
 