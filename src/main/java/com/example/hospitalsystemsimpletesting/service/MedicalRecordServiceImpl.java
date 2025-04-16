package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the MedicalRecordService interface using an in-memory data store
 */
public class MedicalRecordServiceImpl implements MedicalRecordService {

    // In-memory data store for medical records
    private final Map<String, MedicalRecord> medicalRecordDatabase = new HashMap<>();
    
    // Dependencies on other services
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DataPersistenceService persistenceService;
    
    /**
     * Constructor with all dependencies for full functionality
     * @param patientService The PatientService implementation to use
     * @param appointmentService The AppointmentService implementation to use
     * @param persistenceService The DataPersistenceService implementation to use
     */
    public MedicalRecordServiceImpl(PatientService patientService, AppointmentService appointmentService, DataPersistenceService persistenceService) {
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        if (appointmentService == null) {
            throw new IllegalArgumentException("AppointmentService cannot be null");
        }
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.persistenceService = persistenceService;
        loadMedicalRecords();
    }
    
    /**
     * Constructor with PatientService and DataPersistenceService
     * @param patientService The PatientService implementation to use
     * @param persistenceService The DataPersistenceService implementation to use
     */
    public MedicalRecordServiceImpl(PatientService patientService, DataPersistenceService persistenceService) {
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        this.patientService = patientService;
        this.appointmentService = null; // Functions requiring AppointmentService will throw exceptions
        this.persistenceService = persistenceService;
        loadMedicalRecords();
    }
    
    /**
     * Constructor with only PatientService dependency
     * @param patientService The PatientService implementation to use
     */
    public MedicalRecordServiceImpl(PatientService patientService) {
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        this.patientService = patientService;
        this.appointmentService = null; // Functions requiring AppointmentService will throw exceptions
        this.persistenceService = null; // No persistence
    }
    
    /**
     * Default constructor - creates its own PatientService
     */
    public MedicalRecordServiceImpl() {
        this.patientService = new PatientServiceImpl();
        this.appointmentService = new AppointmentServiceImpl(this.patientService);
        this.persistenceService = null; // No persistence
    }
    
    /**
     * Load medical records from persistence
     */
    private void loadMedicalRecords() {
        if (persistenceService != null) {
            List<MedicalRecord> records = persistenceService.loadMedicalRecords();
            for (MedicalRecord record : records) {
                medicalRecordDatabase.put(record.getId(), record);
            }
            System.out.println("Loaded " + records.size() + " medical records from persistence");
        }
    }
    
    /**
     * Save medical records to persistence
     */
    private void saveMedicalRecords() {
        if (persistenceService != null) {
            persistenceService.saveMedicalRecords(new ArrayList<>(medicalRecordDatabase.values()));
            System.out.println("Saved " + medicalRecordDatabase.size() + " medical records to persistence");
        }
    }
    
    @Override
    public MedicalRecord createMedicalRecord(String id, Patient patient, String diagnosis, LocalDate recordDate) {
        // Validate parameters before creating
        validateMedicalRecordParameters(id, patient, diagnosis, recordDate);
        
        // Create and store the medical record
        MedicalRecord medicalRecord = new MedicalRecord(id, patient, diagnosis, recordDate);
        medicalRecordDatabase.put(id, medicalRecord);
        
        // Save changes to persistence
        saveMedicalRecords();
        
        return medicalRecord;
    }
    
    @Override
    public MedicalRecord createMedicalRecordByPatientId(String id, String patientId, String diagnosis, LocalDate recordDate) {
        // Validate id, diagnosis, and recordDate
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be null or empty");
        }
        if (recordDate == null) {
            throw new IllegalArgumentException("Record date cannot be null");
        }
        
        // Find the patient
        Optional<Patient> patientOpt = patientService.findPatientById(patientId);
        if (!patientOpt.isPresent()) {
            throw new IllegalStateException("Patient with ID " + patientId + " not found");
        }
        
        // Create the medical record using the found patient
        return createMedicalRecord(id, patientOpt.get(), diagnosis, recordDate);
    }
    
    @Override
    public MedicalRecord createMedicalRecordFromAppointment(String id, String appointmentId, String diagnosis, String notes) {
        // Check if AppointmentService is available
        if (appointmentService == null) {
            throw new UnsupportedOperationException("AppointmentService not configured");
        }
        
        // Validate parameters
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be null or empty");
        }
        
        // Find the appointment
        Optional<Appointment> appointmentOpt = appointmentService.findAppointmentById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new IllegalStateException("Appointment with ID " + appointmentId + " not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Create the medical record
        MedicalRecord medicalRecord = new MedicalRecord(id, appointment.getPatient(), diagnosis, LocalDate.now());
        medicalRecord.setRelatedAppointment(appointment);
        
        // Add notes if provided
        if (notes != null && !notes.trim().isEmpty()) {
            medicalRecord.appendNotes(notes);
        }
        
        // Store and return
        medicalRecordDatabase.put(id, medicalRecord);
        
        // Save changes to persistence
        saveMedicalRecords();
        
        return medicalRecord;
    }
    
    @Override
    public MedicalRecord saveMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            throw new IllegalArgumentException("Medical record cannot be null");
        }
        if (medicalRecord.getId() == null || medicalRecord.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
        }
        
        medicalRecordDatabase.put(medicalRecord.getId(), medicalRecord);
        
        // Save changes to persistence
        saveMedicalRecords();
        
        return medicalRecord;
    }
    
    @Override
    public Optional<MedicalRecord> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(medicalRecordDatabase.get(id));
    }
    
    @Override
    public List<MedicalRecord> getAllMedicalRecords() {
        return new ArrayList<>(medicalRecordDatabase.values());
    }
    
    @Override
    public List<MedicalRecord> findByPatientId(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return medicalRecordDatabase.values().stream()
                .filter(record -> record.getPatient().getId().equals(patientId))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<MedicalRecord> findByAppointmentId(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        return medicalRecordDatabase.values().stream()
                .filter(record -> record.getRelatedAppointment() != null && 
                                 record.getRelatedAppointment().getId().equals(appointmentId))
                .findFirst();
    }
    
    @Override
    public List<MedicalRecord> findByRecordDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return medicalRecordDatabase.values().stream()
                .filter(record -> record.getRecordDate().equals(date))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<MedicalRecord> updateDiagnosis(String id, String newDiagnosis) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (newDiagnosis == null || newDiagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("New diagnosis cannot be null or empty");
        }
        
        Optional<MedicalRecord> recordOpt = findById(id);
        if (recordOpt.isPresent()) {
            MedicalRecord record = recordOpt.get();
            record.updateDiagnosis(newDiagnosis);
            
            // Save changes to persistence
            saveMedicalRecords();
            
            return Optional.of(record);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<MedicalRecord> addNotes(String id, String additionalNotes) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (additionalNotes == null || additionalNotes.trim().isEmpty()) {
            throw new IllegalArgumentException("Additional notes cannot be null or empty");
        }
        
        Optional<MedicalRecord> recordOpt = findById(id);
        if (recordOpt.isPresent()) {
            MedicalRecord record = recordOpt.get();
            record.appendNotes(additionalNotes);
            
            // Save changes to persistence
            saveMedicalRecords();
            
            return Optional.of(record);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<MedicalRecord> linkToAppointment(String medicalRecordId, String appointmentId) {
        // Check if AppointmentService is available
        if (appointmentService == null) {
            throw new UnsupportedOperationException("AppointmentService not configured");
        }
        
        if (medicalRecordId == null || medicalRecordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
        }
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        // Find the medical record
        Optional<MedicalRecord> recordOpt = findById(medicalRecordId);
        if (!recordOpt.isPresent()) {
            return Optional.empty();
        }
        
        // Find the appointment
        Optional<Appointment> appointmentOpt = appointmentService.findAppointmentById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new IllegalStateException("Appointment with ID " + appointmentId + " not found");
        }
        
        // Link the appointment to the medical record
        MedicalRecord record = recordOpt.get();
        record.setRelatedAppointment(appointmentOpt.get());
        
        // Save changes to persistence
        saveMedicalRecords();
        
        return Optional.of(record);
    }
    
    @Override
    public boolean deleteMedicalRecord(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        if (medicalRecordDatabase.containsKey(id)) {
            medicalRecordDatabase.remove(id);
            
            // Save changes to persistence
            saveMedicalRecords();
            
            return true;
        }
        
        return false;
    }
    
    // Helper method to validate parameters for creating a medical record
    private void validateMedicalRecordParameters(String id, Patient patient, String diagnosis, LocalDate recordDate) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        
        if (medicalRecordDatabase.containsKey(id)) {
            throw new IllegalArgumentException("Medical record with ID " + id + " already exists");
        }
        
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be null or empty");
        }
        
        if (recordDate == null) {
            throw new IllegalArgumentException("Record date cannot be null");
        }
    }
} 