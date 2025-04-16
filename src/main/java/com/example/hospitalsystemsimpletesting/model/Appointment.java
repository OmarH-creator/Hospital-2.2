package com.example.hospitalsystemsimpletesting.model;

import java.time.LocalDateTime;

/**
 * Represents an appointment in the hospital system.
 */
public class Appointment {
    private String id;
    private Patient patient;
    private String type;
    private LocalDateTime dateTime;
    private Status status; // Enum instead of String
    
    /**
     * Status enum for appointment states
     */
    public enum Status {
        SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
    }

    public Appointment(String id, Patient patient, String type, LocalDateTime dateTime) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be empty");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        
        this.id = id;
        this.patient = patient;
        this.type = type;
        this.dateTime = dateTime;
        this.status = Status.SCHEDULED;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Business methods
    public void complete() {
        this.status = Status.COMPLETED;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void reschedule(LocalDateTime newDateTime) {
        if (newDateTime == null) {
            throw new IllegalArgumentException("New date time cannot be null");
        }
        this.dateTime = newDateTime;
        this.status = Status.RESCHEDULED;
    }
} 