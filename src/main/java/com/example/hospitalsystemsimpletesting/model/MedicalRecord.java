package com.example.hospitalsystemsimpletesting.model;

import java.time.LocalDate;

/**
 * Represents a medical record in the hospital system.
 */
public class MedicalRecord {
    private String id;
    private Patient patient;
    private String diagnosis;
    private String notes;
    private LocalDate recordDate;
    private Appointment relatedAppointment; // Optional
    
    public MedicalRecord(String id, Patient patient, String diagnosis, LocalDate recordDate) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        if (diagnosis == null) {
            throw new IllegalArgumentException("Diagnosis cannot be null");
        }
        if (diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be empty");
        }
        if (recordDate == null) {
            throw new IllegalArgumentException("Record date cannot be null");
        }
        
        this.id = id;
        this.patient = patient;
        this.diagnosis = diagnosis;
        this.recordDate = recordDate;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public Appointment getRelatedAppointment() {
        return relatedAppointment;
    }
    
    public void setRelatedAppointment(Appointment relatedAppointment) {
        this.relatedAppointment = relatedAppointment;
    }
    
    // Business methods
    public void updateDiagnosis(String newDiagnosis) {
        if (newDiagnosis == null) {
            throw new IllegalArgumentException("New diagnosis cannot be null");
        }
        if (newDiagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("New diagnosis cannot be empty");
        }
        this.diagnosis = newDiagnosis;
    }
    
    public void appendNotes(String additionalNotes) {
        if (additionalNotes == null || additionalNotes.trim().isEmpty()) {
            return; // Do nothing if notes are empty
        }
        
        if (this.notes == null || this.notes.isEmpty()) {
            this.notes = additionalNotes;
        } else {
            this.notes += "\n" + additionalNotes;
        }
    }
} 