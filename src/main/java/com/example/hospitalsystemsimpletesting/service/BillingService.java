package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing billing operations.
 */
public interface BillingService {
    
    /**
     * Creates a new bill for a patient.
     *
     * @param patientId The ID of the patient
     * @param description A description of the bill
     * @param dueDate Due date for the bill
     * @return The created bill
     * @throws IllegalArgumentException if patient not found or parameters invalid
     */
    Bill createBill(String patientId, String description, LocalDate dueDate);
    
    /**
     * Adds an item to an existing bill.
     *
     * @param billId The ID of the bill to add the item to
     * @param description The description of the item
     * @param amount The amount to charge for the item
     * @return The updated bill
     * @throws IllegalArgumentException if bill not found or parameters invalid
     */
    Bill addItemToBill(String billId, String description, BigDecimal amount);
    
    /**
     * Processes a payment for a bill.
     *
     * @param billId The ID of the bill to process payment for
     * @param amount The payment amount
     * @param paymentMethod The method of payment (e.g., "CASH", "CREDIT_CARD")
     * @return The payment record
     * @throws IllegalArgumentException if bill not found or parameters invalid
     */
    Payment processPayment(String billId, BigDecimal amount, String paymentMethod);
    
    /**
     * Finds a bill by its ID.
     *
     * @param billId The ID of the bill to find
     * @return Optional containing the bill if found, empty otherwise
     */
    Optional<Bill> findBillById(String billId);
    
    /**
     * Finds all bills for a specific patient.
     *
     * @param patientId The ID of the patient
     * @return List of bills for the patient
     */
    List<Bill> findBillsByPatientId(String patientId);
    
    /**
     * Gets all bills in the system.
     *
     * @return List of all bills
     */
    List<Bill> getAllBills();
    
    /**
     * Gets all bills with a specific status (paid, unpaid).
     *
     * @param isPaid true for paid bills, false for unpaid bills
     * @return List of bills matching the status
     */
    List<Bill> getBillsByStatus(boolean isPaid);
    
    /**
     * Gets all overdue bills (unpaid bills past their due date).
     *
     * @return List of overdue bills
     */
    List<Bill> getOverdueBills();
} 