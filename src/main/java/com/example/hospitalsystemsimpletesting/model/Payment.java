package com.example.hospitalsystemsimpletesting.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment made for a bill in the hospital system.
 */
public class Payment {
    private String id;
    private String billId;
    private BigDecimal amount;
    private LocalDateTime paymentDateTime;
    private String paymentMethod;
    private PaymentStatus status;
    
    /**
     * Constructs a new Payment.
     *
     * @param id The unique identifier for this payment
     * @param billId The ID of the bill this payment is for
     * @param amount The payment amount
     * @param paymentMethod The method of payment (e.g., "CASH", "CREDIT_CARD")
     */
    public Payment(String id, String billId, BigDecimal amount, String paymentMethod) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty");
        }
        if (billId == null || billId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill ID cannot be null or empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }
        
        this.id = id;
        this.billId = billId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDateTime = LocalDateTime.now();
        this.status = PaymentStatus.COMPLETED;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public String getBillId() {
        return billId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Payment status cannot be null");
        }
        this.status = status;
    }
    
    /**
     * Marks this payment as failed.
     */
    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }
    
    /**
     * Marks this payment as refunded.
     */
    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }
    
    /**
     * Enum representing the possible statuses of a payment.
     */
    public enum PaymentStatus {
        COMPLETED,
        PENDING,
        FAILED,
        REFUNDED
    }
} 