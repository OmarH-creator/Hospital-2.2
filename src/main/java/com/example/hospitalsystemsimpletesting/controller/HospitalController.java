package com.example.hospitalsystemsimpletesting.controller;

import com.example.hospitalsystemsimpletesting.model.*;
import com.example.hospitalsystemsimpletesting.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for hospital operations
 */
public class HospitalController {
    
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;
    private final InventoryService inventoryService;
    
    /**
     * Constructor with all service dependencies
     */
    public HospitalController(
            PatientService patientService,
            AppointmentService appointmentService,
            MedicalRecordService medicalRecordService,
            BillingService billingService,
            InventoryService inventoryService) {
        
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        if (appointmentService == null) {
            throw new IllegalArgumentException("AppointmentService cannot be null");
        }
        if (medicalRecordService == null) {
            throw new IllegalArgumentException("MedicalRecordService cannot be null");
        }
        if (billingService == null) {
            throw new IllegalArgumentException("BillingService cannot be null");
        }
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null");
        }
        
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
        this.billingService = billingService;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Constructor with minimal dependencies (for backward compatibility)
     */
    public HospitalController(PatientService patientService) {
        if (patientService == null) {
            throw new IllegalArgumentException("PatientService cannot be null");
        }
        this.patientService = patientService;
        
        // Create default implementations for other services
        this.appointmentService = null;
        this.medicalRecordService = null;
        this.billingService = null;
        this.inventoryService = null;
    }
    
    // region Patient Management Methods
    
    /**
     * Register a new patient
     */
    public Patient registerPatient(String id, String firstName, String lastName, LocalDate dateOfBirth, String bloodType) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        
        // Check if patient already exists
        Optional<Patient> existingPatient = patientService.findPatientById(id);
        if (existingPatient.isPresent()) {
            throw new IllegalStateException("Patient with ID " + id + " already exists");
        }
        
        // Create new patient
        Patient patient = new Patient(id, firstName, lastName, dateOfBirth);
        
        // Set blood type if provided
        if (bloodType != null && !bloodType.trim().isEmpty()) {
            patient.setBloodType(bloodType);
        }
        
        // Save and return patient
        return patientService.savePatient(patient);
    }
    
    /**
     * Get a patient by ID
     */
    public Optional<Patient> getPatient(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return patientService.findPatientById(id);
    }
    
    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }
    
    /**
     * Admit a patient to the hospital
     */
    public void admitPatient(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        patientService.admitPatient(id);
    }
    
    /**
     * Discharge a patient from the hospital
     */
    public void dischargePatient(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        patientService.dischargePatient(id);
    }
    
    // endregion
    
    // region Appointment Management Methods
    
    /**
     * Schedule a new appointment
     */
    public Appointment scheduleAppointment(String patientId, String type, LocalDateTime dateTime) {
        validateServiceAvailability(appointmentService, "Cannot schedule appointments");
        
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment type cannot be null or empty");
        }
        
        if (dateTime == null) {
            throw new IllegalArgumentException("Appointment date/time cannot be null");
        }
        
        return appointmentService.scheduleAppointmentByPatientId(patientId, type, dateTime);
    }
    
    /**
     * Get an appointment by ID
     */
    public Optional<Appointment> getAppointment(String id) {
        validateServiceAvailability(appointmentService, "Cannot get appointment");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        return appointmentService.findAppointmentById(id);
    }
    
    /**
     * Get all appointments
     */
    public List<Appointment> getAllAppointments() {
        validateServiceAvailability(appointmentService, "Cannot get appointments");
        
        return appointmentService.getAllAppointments();
    }
    
    /**
     * Get appointments for a specific patient
     */
    public List<Appointment> getPatientAppointments(String patientId) {
        validateServiceAvailability(appointmentService, "Cannot get patient appointments");
        
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return appointmentService.getAppointmentsByPatientId(patientId);
    }
    
    /**
     * Get appointments for a specific date
     */
    public List<Appointment> getAppointmentsByDate(LocalDateTime date) {
        validateServiceAvailability(appointmentService, "Cannot get appointments by date");
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return appointmentService.getAppointmentsByDate(date);
    }
    
    /**
     * Cancel an appointment
     */
    public boolean cancelAppointment(String appointmentId) {
        validateServiceAvailability(appointmentService, "Cannot cancel appointment");
        
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        return appointmentService.cancelAppointment(appointmentId);
    }
    
    /**
     * Complete an appointment
     */
    public boolean completeAppointment(String appointmentId) {
        validateServiceAvailability(appointmentService, "Cannot complete appointment");
        
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        return appointmentService.completeAppointment(appointmentId);
    }
    
    /**
     * Reschedule an appointment
     */
    public Optional<Appointment> rescheduleAppointment(String appointmentId, LocalDateTime newDateTime) {
        validateServiceAvailability(appointmentService, "Cannot reschedule appointment");
        
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        if (newDateTime == null) {
            throw new IllegalArgumentException("New date/time cannot be null");
        }
        
        return appointmentService.rescheduleAppointment(appointmentId, newDateTime);
    }
    
    // endregion
    
    // region Medical Record Management Methods
    
    /**
     * Create a new medical record for a patient
     */
    public MedicalRecord createMedicalRecord(String id, String patientId, String diagnosis, LocalDate recordDate) {
        validateServiceAvailability(medicalRecordService, "Cannot create medical record");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
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
        
        return medicalRecordService.createMedicalRecordByPatientId(id, patientId, diagnosis, recordDate);
    }
    
    /**
     * Create a medical record from an appointment
     */
    public MedicalRecord createMedicalRecordFromAppointment(String id, String appointmentId, String diagnosis, String notes) {
        validateServiceAvailability(medicalRecordService, "Cannot create medical record from appointment");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
        }
        
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be null or empty");
        }
        
        return medicalRecordService.createMedicalRecordFromAppointment(id, appointmentId, diagnosis, notes);
    }
    
    /**
     * Get a medical record by ID
     */
    public Optional<MedicalRecord> getMedicalRecord(String id) {
        validateServiceAvailability(medicalRecordService, "Cannot get medical record");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
        }
        
        return medicalRecordService.findById(id);
    }
    
    /**
     * Get all medical records
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        validateServiceAvailability(medicalRecordService, "Cannot get medical records");
        
        return medicalRecordService.getAllMedicalRecords();
    }
    
    /**
     * Get medical records for a specific patient
     */
    public List<MedicalRecord> getPatientMedicalRecords(String patientId) {
        validateServiceAvailability(medicalRecordService, "Cannot get patient medical records");
        
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return medicalRecordService.findByPatientId(patientId);
    }
    
    /**
     * Get the medical record for a specific appointment
     */
    public Optional<MedicalRecord> getAppointmentMedicalRecord(String appointmentId) {
        validateServiceAvailability(medicalRecordService, "Cannot get appointment medical record");
        
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        
        return medicalRecordService.findByAppointmentId(appointmentId);
    }
    
    /**
     * Update diagnosis in a medical record
     */
    public Optional<MedicalRecord> updateDiagnosis(String medicalRecordId, String newDiagnosis) {
        validateServiceAvailability(medicalRecordService, "Cannot update diagnosis");
        
        if (medicalRecordId == null || medicalRecordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
        }
        
        if (newDiagnosis == null || newDiagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("New diagnosis cannot be null or empty");
        }
        
        return medicalRecordService.updateDiagnosis(medicalRecordId, newDiagnosis);
    }
    
    /**
     * Add notes to a medical record
     */
    public Optional<MedicalRecord> addNotes(String medicalRecordId, String notes) {
        validateServiceAvailability(medicalRecordService, "Cannot add notes");
        
        if (medicalRecordId == null || medicalRecordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical record ID cannot be null or empty");
        }
        
        if (notes == null || notes.trim().isEmpty()) {
            throw new IllegalArgumentException("Notes cannot be null or empty");
        }
        
        return medicalRecordService.addNotes(medicalRecordId, notes);
    }
    
    // endregion
    
    // region Billing Management Methods
    
    /**
     * Create a new bill for a patient
     */
    public Bill createBill(String patientId, String description, LocalDate dueDate) {
        validateServiceAvailability(billingService, "Cannot create bill");
        
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        
        return billingService.createBill(patientId, description, dueDate);
    }
    
    /**
     * Add an item to a bill
     */
    public Bill addItemToBill(String billId, String description, BigDecimal amount) {
        validateServiceAvailability(billingService, "Cannot add item to bill");
        
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        return billingService.addItemToBill(billId, description, amount);
    }
    
    /**
     * Process a payment for a bill
     */
    public Payment processPayment(String billId, BigDecimal amount, String paymentMethod) {
        validateServiceAvailability(billingService, "Cannot process payment");
        
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }
        
        return billingService.processPayment(billId, amount, paymentMethod);
    }
    
    /**
     * Get a bill by ID
     */
    public Optional<Bill> getBill(String billId) {
        validateServiceAvailability(billingService, "Cannot get bill");
        
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        
        return billingService.findBillById(billId);
    }
    
    /**
     * Get all bills
     */
    public List<Bill> getAllBills() {
        validateServiceAvailability(billingService, "Cannot get bills");
        
        return billingService.getAllBills();
    }
    
    /**
     * Get bills for a specific patient
     */
    public List<Bill> getPatientBills(String patientId) {
        validateServiceAvailability(billingService, "Cannot get patient bills");
        
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return billingService.findBillsByPatientId(patientId);
    }
    
    /**
     * Get bills by status (paid/unpaid)
     */
    public List<Bill> getBillsByStatus(boolean isPaid) {
        validateServiceAvailability(billingService, "Cannot get bills by status");
        
        return billingService.getBillsByStatus(isPaid);
    }
    
    /**
     * Get overdue bills
     */
    public List<Bill> getOverdueBills() {
        validateServiceAvailability(billingService, "Cannot get overdue bills");
        
        return billingService.getOverdueBills();
    }
    
    // endregion
    
    // region Inventory Management Methods
    
    /**
     * Add a new inventory item
     */
    public InventoryItem addInventoryItem(String id, String name, int quantity, BigDecimal unitPrice) {
        validateServiceAvailability(inventoryService, "Cannot add inventory item");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        return unitPrice == null 
                ? inventoryService.addInventoryItem(id, name, quantity) 
                : inventoryService.addInventoryItem(id, name, quantity, unitPrice);
    }
    
    /**
     * Get an inventory item by ID
     */
    public Optional<InventoryItem> getInventoryItem(String id) {
        validateServiceAvailability(inventoryService, "Cannot get inventory item");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        return inventoryService.findById(id);
    }
    
    /**
     * Get all inventory items
     */
    public List<InventoryItem> getAllInventoryItems() {
        validateServiceAvailability(inventoryService, "Cannot get inventory items");
        
        return inventoryService.getAllItems();
    }
    
    /**
     * Update the quantity of an inventory item
     */
    public InventoryItem updateItemQuantity(String id, int newQuantity) {
        validateServiceAvailability(inventoryService, "Cannot update item quantity");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        return inventoryService.updateQuantity(id, newQuantity);
    }
    
    /**
     * Update the price of an inventory item
     */
    public InventoryItem updateItemPrice(String id, BigDecimal newPrice) {
        validateServiceAvailability(inventoryService, "Cannot update item price");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        
        return inventoryService.updatePrice(id, newPrice);
    }
    
    /**
     * Add stock to an inventory item
     */
    public InventoryItem addItemStock(String id, int quantity) {
        validateServiceAvailability(inventoryService, "Cannot add item stock");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        return inventoryService.addStock(id, quantity);
    }
    
    /**
     * Remove stock from an inventory item
     */
    public InventoryItem removeItemStock(String id, int quantity) {
        validateServiceAvailability(inventoryService, "Cannot remove item stock");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        return inventoryService.removeStock(id, quantity);
    }
    
    /**
     * Search for inventory items by name
     */
    public List<InventoryItem> searchInventoryItems(String searchTerm) {
        validateServiceAvailability(inventoryService, "Cannot search inventory items");
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        return inventoryService.searchByName(searchTerm);
    }
    
    /**
     * Get inventory items with low stock
     */
    public List<InventoryItem> getLowStockItems() {
        validateServiceAvailability(inventoryService, "Cannot get low stock items");
        
        return inventoryService.getLowStockItems();
    }
    
    /**
     * Delete an inventory item
     */
    public boolean deleteInventoryItem(String id) {
        validateServiceAvailability(inventoryService, "Cannot delete inventory item");
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        
        return inventoryService.deleteItem(id);
    }
    
    // endregion
    
    // region Cross-Service Workflow Methods
    
    /**
     * Complete appointment and create a medical record in one operation
     */
    public MedicalRecord completeAppointmentWithRecord(String appointmentId, String medicalRecordId, 
                                                     String diagnosis, String notes) {
        validateServiceAvailability(appointmentService, "Cannot complete appointment workflow");
        validateServiceAvailability(medicalRecordService, "Cannot complete appointment workflow");
        
        // Complete the appointment
        boolean completed = completeAppointment(appointmentId);
        if (!completed) {
            throw new IllegalStateException("Failed to complete appointment: " + appointmentId);
        }
        
        // Create medical record from the appointment
        return createMedicalRecordFromAppointment(medicalRecordId, appointmentId, diagnosis, notes);
    }
    
    /**
     * Create a bill for a completed appointment
     */
    public Bill createBillFromAppointment(String appointmentId, String billDescription, 
                                        LocalDate dueDate, BigDecimal consultationFee) {
        validateServiceAvailability(appointmentService, "Cannot create bill from appointment");
        validateServiceAvailability(billingService, "Cannot create bill from appointment");
        
        // Get the appointment
        Appointment appointment = getAppointment(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
        
        // Verify appointment is completed
        if (Appointment.Status.COMPLETED != appointment.getStatus()) {
            throw new IllegalStateException("Cannot create bill for uncompleted appointment");
        }
        
        // Create bill for the patient
        String patientId = appointment.getPatient().getId();
        Bill bill = createBill(patientId, billDescription, dueDate);
        
        // Add consultation fee if provided
        if (consultationFee != null && consultationFee.compareTo(BigDecimal.ZERO) > 0) {
            String itemDesc = "Consultation fee for " + appointment.getType();
            addItemToBill(bill.getId(), itemDesc, consultationFee);
        }
        
        return bill;
    }
    
    /**
     * Register a new patient and schedule an appointment in one operation
     */
    public Appointment registerPatientWithAppointment(
            String patientId, String firstName, String lastName, LocalDate dateOfBirth,
            String appointmentType, LocalDateTime appointmentDateTime) {
        
        validateServiceAvailability(appointmentService, "Cannot schedule appointment for new patient");
        
        // Register the patient first
        Patient patient = registerPatient(patientId, firstName, lastName, dateOfBirth, null);
        
        // Then schedule the appointment
        return scheduleAppointment(patient.getId(), appointmentType, appointmentDateTime);
    }
    
    /**
     * Handle medication dispensing: check inventory and update stock
     */
    public boolean dispenseMedication(String medicalRecordId, String medicationId, int quantity) {
        validateServiceAvailability(medicalRecordService, "Cannot dispense medication");
        validateServiceAvailability(inventoryService, "Cannot dispense medication");
        
        // Check if the medical record exists
        Optional<MedicalRecord> record = getMedicalRecord(medicalRecordId);
        if (!record.isPresent()) {
            throw new IllegalArgumentException("Medical record not found: " + medicalRecordId);
        }
        
        // Check if the medication exists in inventory
        Optional<InventoryItem> medication = getInventoryItem(medicationId);
        if (!medication.isPresent()) {
            throw new IllegalArgumentException("Medication not found in inventory: " + medicationId);
        }
        
        // Check if there's enough stock
        InventoryItem med = medication.get();
        if (med.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for medication: " + medicationId);
        }
        
        // Remove the stock
        removeItemStock(medicationId, quantity);
        
        // Update the medical record notes
        String notes = "Dispensed " + quantity + " units of " + med.getName();
        addNotes(medicalRecordId, notes);
        
        return true;
    }
    
    // endregion
    
    // region Helper Methods
    
    /**
     * Validate that a required service is available
     */
    private <T> void validateServiceAvailability(T service, String errorMessage) {
        if (service == null) {
            throw new UnsupportedOperationException(errorMessage + ": required service not available");
        }
    }
    
    // endregion
} 