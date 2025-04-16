package com.example.hospitalsystemsimpletesting.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents a patient in the hospital system.
 */
public class Patient {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String contactNumber;
    private String address;
    private String bloodType = "Unknown"; // Initialize with a default value
    private boolean isAdmitted;

    public Patient(String id, String firstName, String lastName, LocalDate dateOfBirth) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        if (firstName == null) {
            throw new IllegalArgumentException("First name cannot be null");
        }
        if (lastName == null) {
            throw new IllegalArgumentException("Last name cannot be null");
        }
        
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.isAdmitted = false;
    }
    
    /**
     * Constructor that takes all patient information
     */
    public Patient(String id, String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        // Split name into first and last name
        String[] nameParts = name.split(" ", 2);
        this.firstName = nameParts[0];
        this.lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.address = address;
        this.isAdmitted = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getName() {
        return getFullName();
    }
    
    public void setName(String name) {
        String[] nameParts = name.split(" ", 2);
        this.firstName = nameParts[0];
        this.lastName = nameParts.length > 1 ? nameParts[1] : "";
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodType() {
        return bloodType != null ? bloodType : "Unknown";
    }

    public void setBloodType(String bloodType) {
        System.out.println("DEBUG: Setting blood type for patient " + id + " from '" + this.bloodType + "' to '" + bloodType + "'");
        // Validate and standardize blood type
        if (bloodType == null || bloodType.trim().isEmpty()) {
            this.bloodType = "Unknown";
        } else {
            // Verify it's a valid blood type format
            String normalizedBloodType = bloodType.trim().toUpperCase();
            if (normalizedBloodType.matches("^(A|B|AB|O)[+-]$") || normalizedBloodType.equals("UNKNOWN")) {
                this.bloodType = normalizedBloodType;
            } else {
                // If not valid, set to Unknown
                System.out.println("WARNING: Invalid blood type format: " + bloodType + ", setting to Unknown");
                this.bloodType = "Unknown";
            }
        }
    }

    public boolean isAdmitted() {
        return isAdmitted;
    }

    public void setAdmitted(boolean admitted) {
        isAdmitted = admitted;
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        // If birth date is in the future, return 0 instead of a negative value
        if (dateOfBirth.isAfter(LocalDate.now())) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public void admit() {
        isAdmitted = true;
    }

    public void discharge() {
        isAdmitted = false;
    }
} 