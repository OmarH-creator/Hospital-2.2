package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Patient;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for patient operations
 */
public interface PatientService {
    
    /**
     * Register a new patient in the system
     * @param name The patient's full name
     * @param dateOfBirth The patient's date of birth
     * @param gender The patient's gender
     * @param contactNumber The patient's contact number
     * @param address The patient's address
     * @return The registered patient
     */
    Patient registerPatient(String name, LocalDate dateOfBirth, String gender, String contactNumber, String address);
    
    /**
     * Save a patient to the system (legacy method)
     * @param patient The patient to save
     * @return The saved patient with updated information
     */
    default Patient savePatient(Patient patient) {
        throw new UnsupportedOperationException("This method is deprecated, use registerPatient instead");
    }
    
    /**
     * Update an existing patient's information
     * @param patientId The ID of the patient to update
     * @param name The patient's updated name
     * @param dateOfBirth The patient's updated date of birth
     * @param gender The patient's updated gender
     * @param contactNumber The patient's updated contact number
     * @param address The patient's updated address
     * @param bloodType The patient's blood type
     */
    void updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender, String contactNumber, String address, String bloodType);
    
    /**
     * Find a patient by their ID
     * @param patientId The patient ID
     * @return An Optional containing the patient if found, or empty if not found
     */
    Optional<Patient> findPatientById(String patientId);
    
    /**
     * Find patients by name (partial match)
     * @param namePart Part of the name to search for
     * @return List of patients matching the name part
     */
    List<Patient> findPatientsByName(String namePart);
    
    /**
     * Get all patients in the system
     * @return List of all patients
     */
    List<Patient> getAllPatients();
    
    /**
     * Admit a patient to the hospital
     * @param patientId The ID of the patient to admit
     */
    void admitPatient(String patientId);
    
    /**
     * Discharge a patient from the hospital
     * @param patientId The ID of the patient to discharge
     */
    void dischargePatient(String patientId);
    
    /**
     * Get all currently admitted patients
     * @return List of admitted patients
     */
    List<Patient> getAdmittedPatients();
    
    /**
     * Delete a patient from the system by ID
     * @param patientId The ID of the patient to delete
     * @return true if patient was successfully deleted, false if not found
     */
    boolean deletePatient(String patientId);
} 