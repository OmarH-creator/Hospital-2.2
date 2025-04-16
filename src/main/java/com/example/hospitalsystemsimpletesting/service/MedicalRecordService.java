package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for medical record operations
 */
public interface MedicalRecordService {
    
    /**
     * Create a new medical record
     * @param id The unique identifier for the medical record
     * @param patient The patient the record belongs to
     * @param diagnosis The initial diagnosis
     * @param recordDate The date of the record
     * @return The created medical record
     * @throws IllegalArgumentException if any parameter is invalid
     */
    MedicalRecord createMedicalRecord(String id, Patient patient, String diagnosis, LocalDate recordDate);
    
    /**
     * Create a new medical record for an existing patient by ID
     * @param id The unique identifier for the medical record
     * @param patientId The ID of the patient
     * @param diagnosis The initial diagnosis
     * @param recordDate The date of the record
     * @return The created medical record
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if the patient doesn't exist
     */
    MedicalRecord createMedicalRecordByPatientId(String id, String patientId, String diagnosis, LocalDate recordDate);
    
    /**
     * Create a medical record from an appointment
     * @param id The unique identifier for the medical record
     * @param appointmentId The ID of the related appointment
     * @param diagnosis The initial diagnosis
     * @param notes Additional notes for the record
     * @return The created medical record
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if the appointment doesn't exist
     */
    MedicalRecord createMedicalRecordFromAppointment(String id, String appointmentId, String diagnosis, String notes);
    
    /**
     * Save or update an existing medical record
     * @param medicalRecord The medical record to save
     * @return The saved medical record
     * @throws IllegalArgumentException if the record is invalid
     */
    MedicalRecord saveMedicalRecord(MedicalRecord medicalRecord);
    
    /**
     * Find a medical record by its ID
     * @param id The medical record ID
     * @return An Optional containing the medical record if found, or empty if not found
     */
    Optional<MedicalRecord> findById(String id);
    
    /**
     * Get all medical records in the system
     * @return List of all medical records
     */
    List<MedicalRecord> getAllMedicalRecords();
    
    /**
     * Get all medical records for a specific patient
     * @param patientId The ID of the patient
     * @return List of the patient's medical records
     * @throws IllegalArgumentException if patientId is null or empty
     */
    List<MedicalRecord> findByPatientId(String patientId);
    
    /**
     * Get the medical record associated with a specific appointment
     * @param appointmentId The ID of the appointment
     * @return An Optional containing the medical record if found, or empty if not found
     * @throws IllegalArgumentException if appointmentId is null or empty
     */
    Optional<MedicalRecord> findByAppointmentId(String appointmentId);
    
    /**
     * Get medical records created on a specific date
     * @param date The date to filter by
     * @return List of medical records for the specified date
     * @throws IllegalArgumentException if date is null
     */
    List<MedicalRecord> findByRecordDate(LocalDate date);
    
    /**
     * Update the diagnosis for an existing medical record
     * @param id The ID of the medical record to update
     * @param newDiagnosis The new diagnosis
     * @return The updated medical record if found, empty otherwise
     * @throws IllegalArgumentException if parameters are invalid
     */
    Optional<MedicalRecord> updateDiagnosis(String id, String newDiagnosis);
    
    /**
     * Add notes to an existing medical record
     * @param id The ID of the medical record
     * @param additionalNotes The notes to add
     * @return The updated medical record if found, empty otherwise
     * @throws IllegalArgumentException if parameters are invalid
     */
    Optional<MedicalRecord> addNotes(String id, String additionalNotes);
    
    /**
     * Associate a medical record with an appointment
     * @param medicalRecordId The ID of the medical record
     * @param appointmentId The ID of the appointment
     * @return The updated medical record if found, empty otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if the appointment doesn't exist
     */
    Optional<MedicalRecord> linkToAppointment(String medicalRecordId, String appointmentId);
    
    /**
     * Delete a medical record by its ID
     * @param id The ID of the medical record to delete
     * @return true if deletion was successful, false if the record wasn't found
     */
    boolean deleteMedicalRecord(String id);
} 