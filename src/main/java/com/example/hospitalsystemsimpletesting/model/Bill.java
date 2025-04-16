package com.example.hospitalsystemsimpletesting.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a hospital bill for a patient
 */
public class Bill {
    private String billId;
    private Patient patient;
    private LocalDate dateIssued;
    private LocalDate datePaid;
    private String status; // "PENDING", "PAID", "OVERDUE", "PARTIAL"
    private List<BillItem> items;
    private double totalAmount;
    private double amountPaid;
    
    /**
     * Nested class to represent items on a bill
     */
    public static class BillItem {
        private String description;
        private double amount;
        
        public BillItem(String description, double amount) {
            if (description == null) {
                throw new IllegalArgumentException("Description cannot be null");
            }
            if (description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be empty");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            this.description = description;
            this.amount = amount;
        }
        
        public String getDescription() {
            return description;
        }
        
        public double getAmount() {
            return amount;
        }
    }
    
    /**
     * Constructor for a new bill
     * @param patient The patient associated with this bill
     */
    public Bill(Patient patient) {
        this.billId = UUID.randomUUID().toString().substring(0, 8);
        this.patient = patient;
        this.dateIssued = LocalDate.now();
        this.status = "UNPAID";
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
        this.amountPaid = 0.0;
    }
    
    /**
     * Constructor with billId, patient and dateIssued
     */
    public Bill(String billId, Patient patient, LocalDate dateIssued) {
        if (billId == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (billId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        if (dateIssued == null) {
            throw new IllegalArgumentException("Issue date cannot be null");
        }
        this.billId = billId;
        this.patient = patient;
        this.dateIssued = dateIssued;
        this.status = "UNPAID";
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
        this.amountPaid = 0.0;
    }
    
    /**
     * Constructor for a bill with all parameters
     */
    public Bill(String billId, Patient patient, LocalDate dateIssued, LocalDate datePaid, 
                String status, double totalAmount, double amountPaid) {
        this.billId = billId;
        this.patient = patient;
        this.dateIssued = dateIssued;
        this.datePaid = datePaid;
        this.status = status;
        this.items = new ArrayList<>();
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
    }
    
    /**
     * Add an item to the bill
     * @param item The item to add
     */
    public void addItem(BillItem item) {
        items.add(item);
        recalculateTotal();
    }
    
    /**
     * Add an item to the bill using description and amount
     * @param description The description of the item
     * @param amount The amount of the item
     */
    public void addItem(String description, double amount) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        BillItem item = new BillItem(description, amount);
        items.add(item);
        recalculateTotal();
    }
    
    /**
     * Recalculate the total amount of the bill
     */
    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(BillItem::getAmount)
                .sum();
        updateStatus();
    }
    
    /**
     * Process a payment for this bill
     * @param amount The amount to be paid
     * @throws IllegalArgumentException if amount is negative or greater than the remaining balance
     */
    public void processPayment(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        
        double remainingBalance = totalAmount - amountPaid;
        if (amount > remainingBalance) {
            throw new IllegalArgumentException("Payment amount exceeds remaining balance");
        }
        
        this.amountPaid += amount;
        
        if (Math.abs(this.amountPaid - this.totalAmount) < 0.01) {
            this.status = "PAID";
            this.datePaid = LocalDate.now();
        } else if (this.amountPaid > 0) {
            this.status = "PARTIAL";
        }
    }
    
    /**
     * Update the status of the bill based on current amounts and dates
     */
    private void updateStatus() {
        if (this.amountPaid >= this.totalAmount) {
            this.status = "PAID";
        } else if (this.amountPaid > 0) {
            this.status = "PARTIAL";
        } else if (this.dateIssued.plusDays(30).isBefore(LocalDate.now())) {
            this.status = "OVERDUE";
        } else {
            this.status = "UNPAID";
        }
    }
    
    /**
     * Marks the bill as paid
     */
    public void markAsPaid() {
        this.status = "PAID";
        this.datePaid = LocalDate.now();
        this.amountPaid = this.totalAmount;
    }
    
    /**
     * Marks the bill as partially paid
     */
    public void markAsPartiallyPaid() {
        this.status = "PARTIAL";
    }
    
    /**
     * Checks if the bill is fully paid
     * @return true if the bill is paid, false otherwise
     */
    public boolean isPaid() {
        return "PAID".equals(this.status);
    }
    
    /**
     * Get the remaining balance on this bill
     * @return The remaining balance
     */
    public double getRemainingBalance() {
        return totalAmount - amountPaid;
    }
    
    // Getters and setters
    public String getId() {
        return billId;
    }
    
    public String getBillId() {
        return billId;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public LocalDate getDateIssued() {
        return dateIssued;
    }
    
    public LocalDate getIssueDate() {
        return dateIssued;
    }
    
    public LocalDate getDatePaid() {
        return datePaid;
    }
    
    public String getStatus() {
        return status;
    }
    
    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public double getAmountPaid() {
        return amountPaid;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Sets the ID of this bill
     * @param id The new ID for the bill
     */
    public void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        this.billId = id;
    }
    
    /**
     * Sets the patient associated with this bill
     * @param patient The new patient for the bill
     */
    public void setPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        this.patient = patient;
    }
    
    /**
     * Sets the issue date of this bill
     * @param issueDate The new issue date for the bill
     */
    public void setIssueDate(LocalDate issueDate) {
        if (issueDate == null) {
            throw new IllegalArgumentException("Issue date cannot be null");
        }
        this.dateIssued = issueDate;
    }
} 