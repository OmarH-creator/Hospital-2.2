package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Bill;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the PatientService interface
 */
public class PatientServiceImpl implements PatientService {
    
    private final Map<String, Patient> patientsById = new HashMap<>();
    private final DataPersistenceService persistenceService;
    private AppointmentService appointmentService;
    private BillingService billingService;
    
    /**
     * Constructor without persistence - for testing only
     */
    public PatientServiceImpl() {
        this.persistenceService = null; // No persistence
    }
    
    /**
     * Constructor with persistence
     * @param persistenceService The data persistence service to use
     */
    public PatientServiceImpl(DataPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        loadPatients();
    }
    
    /**
     * Load patients from persistence
     */
    private void loadPatients() {
        if (persistenceService != null) {
            List<Patient> patients = persistenceService.loadPatients();
            for (Patient patient : patients) {
                patientsById.put(patient.getId(), patient);
            }
        }
    }
    
    /**
     * Save patients to persistence
     */
    private void savePatients() {
        if (persistenceService != null) {
            persistenceService.savePatients(new ArrayList<>(patientsById.values()));
        }
    }
    
    @Override
    public Patient registerPatient(String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
        String patientId = "P" + (100 + patientsById.size());
        Patient patient = new Patient(patientId, name, dateOfBirth, gender, contactNumber, address);
        patientsById.put(patientId, patient);
        savePatients();
        return patient;
    }
    
    @Override
    public Optional<Patient> findPatientById(String patientId) {
        return Optional.ofNullable(patientsById.get(patientId));
    }
    
    @Override
    public List<Patient> findPatientsByName(String namePart) {
        List<Patient> results = new ArrayList<>();
        String lowerNamePart = namePart.toLowerCase();
        
        for (Patient patient : patientsById.values()) {
            if (patient.getName().toLowerCase().contains(lowerNamePart)) {
                results.add(patient);
            }
        }
        
        return results;
    }
    
    @Override
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientsById.values());
    }
    
    @Override
    public void updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender,
                             String contactNumber, String address, String bloodType) {
        System.out.println("DEBUG: Updating patient " + patientId + " with blood type: " + bloodType);
        
        Optional<Patient> optionalPatient = findPatientById(patientId);
        if (optionalPatient.isPresent()) {
            Patient patient = optionalPatient.get();
            System.out.println("DEBUG: Found patient " + patientId + " with current blood type: " + patient.getBloodType());
            
            patient.setFirstName(name.split(" ")[0]);
            if (name.split(" ").length > 1) {
                patient.setLastName(name.split(" ")[1]);
            }
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setContactNumber(contactNumber);
            patient.setAddress(address);
            
            // Explicitly set blood type with verification
            String oldBloodType = patient.getBloodType();
            patient.setBloodType(bloodType);
            System.out.println("DEBUG: Set blood type from '" + oldBloodType + "' to '" + patient.getBloodType() + "'");
            
            // Save and verify
            savePatients();
            
            // Verify the update worked by getting a fresh copy
            Optional<Patient> verifyPatient = findPatientById(patientId);
            if (verifyPatient.isPresent()) {
                System.out.println("DEBUG: Verification - Patient " + patientId + " blood type is now: " + 
                                  verifyPatient.get().getBloodType());
            } else {
                System.out.println("DEBUG: ERROR - Could not verify patient update, patient not found after save");
            }
        } else {
            System.out.println("DEBUG: ERROR - Patient not found with ID: " + patientId);
            throw new IllegalArgumentException("Patient with ID " + patientId + " not found.");
        }
    }
    
    @Override
    public void admitPatient(String patientId) {
        Patient patient = patientsById.get(patientId);
        if (patient != null) {
            patient.admit();
            savePatients();
        } else {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
    }
    
    @Override
    public void dischargePatient(String patientId) {
        Patient patient = patientsById.get(patientId);
        if (patient != null) {
            patient.discharge();
            savePatients();
        } else {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
    }
    
    @Override
    public List<Patient> getAdmittedPatients() {
        List<Patient> admitted = new ArrayList<>();
        for (Patient patient : patientsById.values()) {
            if (patient.isAdmitted()) {
                admitted.add(patient);
            }
        }
        return admitted;
    }
    
    /**
     * Set the appointment service for dependency checking
     * (This method would typically be called by a dependency injection framework)
     * @param appointmentService The appointment service
     */
    public void setAppointmentService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    
    /**
     * Set the billing service for dependency checking
     * (This method would typically be called by a dependency injection framework)
     * @param billingService The billing service
     */
    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }
    
    /**
     * Get the appointment service
     * @return The appointment service
     */
    public AppointmentService getAppointmentService() {
        return appointmentService;
    }
    
    /**
     * Get the billing service
     * @return The billing service
     */
    public BillingService getBillingService() {
        return billingService;
    }
    
    @Override
    public boolean deletePatient(String patientId) {
        System.out.println("DEBUG: Attempting to delete patient: " + patientId);
        
        if (patientId == null || patientId.trim().isEmpty()) {
            System.out.println("DEBUG: Cannot delete: Patient ID is null or empty");
            return false;
        }
        
        // First, check if the patient exists
        Patient patient = patientsById.get(patientId);
        if (patient == null) {
            System.out.println("DEBUG: Cannot delete: Patient not found with ID " + patientId);
            return false;
        }
        
        System.out.println("DEBUG: Patient found: " + patient.getName());
        
        // Check if the patient has any associated appointments
        if (appointmentService != null) {
            try {
                System.out.println("DEBUG: Checking appointments with service: " + appointmentService.getClass().getName());
                List<Appointment> patientAppointments = appointmentService.getAppointmentsByPatientId(patientId);
                System.out.println("DEBUG: Found " + (patientAppointments != null ? patientAppointments.size() : "null") + " appointments");
                
                if (patientAppointments != null && !patientAppointments.isEmpty()) {
                    System.out.println("DEBUG: Cannot delete patient " + patientId + ": Has " + 
                                       patientAppointments.size() + " appointment(s)");
                    return false;
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error checking appointments: " + e.getMessage());
                e.printStackTrace();
                // Continue with deletion if we can't check appointments
            }
        } else {
            System.out.println("DEBUG: AppointmentService is null, skipping appointment check");
        }
        
        // Check if the patient has any bills
        if (billingService != null) {
            try {
                System.out.println("DEBUG: Checking bills with service: " + billingService.getClass().getName());
                List<Bill> patientBills = billingService.findBillsByPatientId(patientId);
                System.out.println("DEBUG: Found " + (patientBills != null ? patientBills.size() : "null") + " bills");
                
                if (patientBills != null && !patientBills.isEmpty()) {
                    System.out.println("DEBUG: Cannot delete patient " + patientId + ": Has " + 
                                      patientBills.size() + " bill(s)");
                    return false;
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error checking bills: " + e.getMessage());
                e.printStackTrace();
                // Continue with deletion if we can't check bills
            }
        } else {
            System.out.println("DEBUG: BillingService is null, skipping bill check");
        }
        
        try {
            // If we got here, we can safely delete the patient
            System.out.println("DEBUG: All checks passed, removing patient from map");
            Patient removedPatient = patientsById.remove(patientId);
            if (removedPatient != null) {
                System.out.println("DEBUG: Patient removed from map, saving changes");
                savePatients(); // Update persistence
                System.out.println("DEBUG: Patient deleted successfully: " + patientId);
                return true;
            } else {
                System.out.println("DEBUG: Failed to remove patient from map: " + patientId);
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error deleting patient: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG: Deletion failed for unknown reason");
        return false;
    }
    
    /**
     * Set the blood type for a patient and ensure it's saved
     * @param patientId The patient ID
     * @param bloodType The blood type to set
     * @return true if successful, false otherwise
     */
    public boolean updatePatientBloodType(String patientId, String bloodType) {
        System.out.println("DEBUG: Direct blood type update for patient " + patientId + " to " + bloodType);
        
        Optional<Patient> optionalPatient = findPatientById(patientId);
        if (optionalPatient.isPresent()) {
            Patient patient = optionalPatient.get();
            String oldBloodType = patient.getBloodType();
            
            // Set the blood type
            patient.setBloodType(bloodType);
            System.out.println("DEBUG: Changed blood type from '" + oldBloodType + "' to '" + patient.getBloodType() + "'");
            
            // Save changes
            savePatients();
            
            return true;
        } else {
            System.out.println("DEBUG: Cannot update blood type - patient not found: " + patientId);
            return false;
        }
    }
} 