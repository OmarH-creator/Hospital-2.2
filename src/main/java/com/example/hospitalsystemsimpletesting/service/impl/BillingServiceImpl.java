package com.example.hospitalsystemsimpletesting.service.impl;

import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Payment;
import com.example.hospitalsystemsimpletesting.service.BillingService;
import com.example.hospitalsystemsimpletesting.service.DataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.PatientService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the BillingService interface.
 */
public class BillingServiceImpl implements BillingService {
    
    private final Map<String, Bill> billsById = new HashMap<>();
    private final Map<String, List<Payment>> paymentsByBillId = new HashMap<>();
    private final Map<String, LocalDate> dueDatesByBillId = new HashMap<>(); // Store due dates separately
    private final PatientService patientService;
    private final DataPersistenceService persistenceService;
    private long lastBillId = 0;
    
    /**
     * Constructor for use without persistence (for testing)
     * @param patientService The patient service to use for patient lookups
     */
    public BillingServiceImpl(PatientService patientService) {
        this.patientService = patientService;
        this.persistenceService = null;
    }
    
    /**
     * Constructor with persistence support
     * @param patientService The patient service to use for patient lookups
     * @param persistenceService The data persistence service to use
     */
    public BillingServiceImpl(PatientService patientService, DataPersistenceService persistenceService) {
        this.patientService = patientService;
        this.persistenceService = persistenceService;
        loadBills();
    }
    
    /**
     * Load bills from persistence
     */
    private void loadBills() {
        if (persistenceService != null) {
            List<Bill> bills = persistenceService.loadBills();
            for (Bill bill : bills) {
                billsById.put(bill.getId(), bill);
                
                // Extract bill ID number for lastBillId tracking
                if (bill.getId().startsWith("B")) {
                    try {
                        long billIdNum = Long.parseLong(bill.getId().substring(1));
                        if (billIdNum > lastBillId) {
                            lastBillId = billIdNum;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore if not a numeric ID
                    }
                }
                
                // Set due date to 30 days after issue date if not already set
                if (!dueDatesByBillId.containsKey(bill.getId())) {
                    dueDatesByBillId.put(bill.getId(), bill.getDateIssued().plusDays(30));
                }
            }
        }
    }
    
    /**
     * Save bills to persistence
     */
    private void saveBills() {
        if (persistenceService != null) {
            persistenceService.saveBills(new ArrayList<>(billsById.values()));
        }
    }
    
    @Override
    public Bill createBill(String patientId, String description, LocalDate dueDate) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        
        Optional<Patient> patientOpt = patientService.findPatientById(patientId);
        if (!patientOpt.isPresent()) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        
        Patient patient = patientOpt.get();
        
        // Generate a unique ID even if called in the same millisecond
        long timestamp = System.currentTimeMillis();
        if (timestamp <= lastBillId) {
            timestamp = lastBillId + 1;
        }
        lastBillId = timestamp;
        String billId = "B" + timestamp;
        
        Bill bill = new Bill(billId, patient, LocalDate.now());
        
        // Store bill and its due date
        billsById.put(billId, bill);
        dueDatesByBillId.put(billId, dueDate);
        
        // Add the description as the first item if provided
        if (!description.trim().isEmpty()) {
            bill.addItem(description, 0.0); // Add with zero amount initially
            
            // Make sure the bill stays in UNPAID status since we just added a zero-amount item
            bill.setStatus("UNPAID");
        }
        
        // Save to persistence
        saveBills();
        
        return bill;
    }
    
    @Override
    public Bill addItemToBill(String billId, String description, BigDecimal amount) {
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        Bill bill = findBillById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));
        
        // Convert BigDecimal to double for Bill.addItem
        bill.addItem(description, amount.doubleValue());
        
        // Save to persistence
        saveBills();
        
        return bill;
    }
    
    @Override
    public Payment processPayment(String billId, BigDecimal amount, String paymentMethod) {
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }
        
        Bill bill = findBillById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));
        
        // Create payment
        String paymentId = "PMT" + System.currentTimeMillis();
        Payment payment = new Payment(paymentId, billId, amount, paymentMethod);
        
        // Store payment
        if (!paymentsByBillId.containsKey(billId)) {
            paymentsByBillId.put(billId, new ArrayList<>());
        }
        paymentsByBillId.get(billId).add(payment);
        
        // Update bill status based on payment amount
        BigDecimal totalPaid = getTotalPaymentsForBill(billId);
        // Convert double to BigDecimal for comparison
        BigDecimal billTotal = BigDecimal.valueOf(bill.getTotalAmount());
        
        // Only compare if bill total is greater than zero
        if (billTotal.compareTo(BigDecimal.ZERO) > 0) {
            if (totalPaid.compareTo(billTotal) >= 0) {
                bill.markAsPaid();
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                bill.markAsPartiallyPaid();
            }
        } else {
            // If bill has no items/amount yet, mark as paid if any payment was made
            if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                bill.markAsPaid();
            }
        }
        
        // Save to persistence
        saveBills();
        
        return payment;
    }
    
    private BigDecimal getTotalPaymentsForBill(String billId) {
        if (!paymentsByBillId.containsKey(billId)) {
            return BigDecimal.ZERO;
        }
        
        return paymentsByBillId.get(billId).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public Optional<Bill> findBillById(String billId) {
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        return Optional.ofNullable(billsById.get(billId));
    }
    
    @Override
    public List<Bill> findBillsByPatientId(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        
        return billsById.values().stream()
                .filter(bill -> bill.getPatient().getId().equals(patientId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Bill> getAllBills() {
        return new ArrayList<>(billsById.values());
    }
    
    @Override
    public List<Bill> getBillsByStatus(boolean isPaid) {
        String statusToMatch = isPaid ? "PAID" : "UNPAID";
        return billsById.values().stream()
                .filter(bill -> statusToMatch.equals(bill.getStatus()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Bill> getOverdueBills() {
        LocalDate today = LocalDate.now();
        return billsById.values().stream()
                .filter(bill -> !"PAID".equals(bill.getStatus()))
                .filter(bill -> {
                    String billId = bill.getId();
                    LocalDate dueDate = dueDatesByBillId.get(billId);
                    return dueDate != null && dueDate.isBefore(today);
                })
                .collect(Collectors.toList());
    }
} 