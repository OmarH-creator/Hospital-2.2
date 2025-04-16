package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Bill model
 */
public class BillTest {

    private Patient testPatient;
    private LocalDate issueDate;
    private Bill testBill;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        issueDate = LocalDate.now();
        testBill = new Bill("B001", testPatient, issueDate);
    }

    @Test
    @DisplayName("Test bill initialization")
    void testInitialization() {
        // Verify fields are correctly initialized
        assertEquals("B001", testBill.getId(), "ID should match");
        assertEquals(testPatient, testBill.getPatient(), "Patient should match");
        assertEquals(issueDate, testBill.getIssueDate(), "Issue date should match");
        assertEquals(0.0, testBill.getTotalAmount(), "Total amount should be 0 initially");
        assertEquals("UNPAID", testBill.getStatus(), "Status should be UNPAID initially");
        assertTrue(testBill.getItems().isEmpty(), "Items list should be empty initially");
        assertFalse(testBill.isPaid(), "Bill should not be marked as paid initially");
    }

    @Test
    @DisplayName("Test constructor validation")
    void testConstructorValidation() {
        // Test null ID
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new Bill(null, testPatient, issueDate);
        });
        assertTrue(exception1.getMessage().contains("ID cannot be null"), "Should validate ID is not null");

        // Test empty ID
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new Bill("", testPatient, issueDate);
        });
        assertTrue(exception2.getMessage().contains("ID cannot be empty"), "Should validate ID is not empty");

        // Test null patient
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new Bill("B001", null, issueDate);
        });
        assertTrue(exception3.getMessage().contains("Patient cannot be null"), "Should validate patient is not null");

        // Test null issue date
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new Bill("B001", testPatient, null);
        });
        assertTrue(exception4.getMessage().contains("Issue date cannot be null"), "Should validate issue date is not null");
    }

    @Test
    @DisplayName("Test addItem method")
    void testAddItem() {
        // Add an item
        testBill.addItem("Consultation Fee", 100.0);
        
        // Verify item was added and total amount updated
        List<Bill.BillItem> items = testBill.getItems();
        assertEquals(1, items.size(), "Should have one item");
        assertEquals("Consultation Fee", items.get(0).getDescription(), "Description should match");
        assertEquals(100.0, items.get(0).getAmount(), "Amount should match");
        assertEquals(100.0, testBill.getTotalAmount(), "Total amount should be updated");
        
        // Add another item
        testBill.addItem("Medication", 50.0);
        
        // Verify second item was added and total amount updated
        items = testBill.getItems();
        assertEquals(2, items.size(), "Should have two items");
        assertEquals(150.0, testBill.getTotalAmount(), "Total amount should include both items");
    }

    @Test
    @DisplayName("Test bill item validation")
    void testBillItemValidation() {
        // Test null description
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            testBill.addItem(null, 100.0);
        });
        assertTrue(exception1.getMessage().contains("Description cannot be null"), "Should validate description is not null");

        // Test empty description
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            testBill.addItem("", 100.0);
        });
        assertTrue(exception2.getMessage().contains("Description cannot be empty"), "Should validate description is not empty");

        // Test negative amount
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            testBill.addItem("Consultation Fee", -50.0);
        });
        assertTrue(exception3.getMessage().contains("Amount cannot be negative"), "Should validate amount is not negative");
    }

    @Test
    @DisplayName("Test items list immutability")
    void testItemsListImmutability() {
        // Add an item to the bill
        testBill.addItem("Consultation Fee", 100.0);
        
        // Get the items list
        List<Bill.BillItem> items = testBill.getItems();
        
        // Try to modify the list
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            items.clear();
        });
        
        // Verify the items are still there
        assertEquals(1, testBill.getItems().size(), "Original list should not be modified");
    }

    @Test
    @DisplayName("Test payment status methods")
    void testPaymentStatusMethods() {
        // Initially unpaid
        assertEquals("UNPAID", testBill.getStatus(), "Status should be UNPAID initially");
        assertFalse(testBill.isPaid(), "isPaid() should return false initially");
        
        // Mark as partially paid
        testBill.markAsPartiallyPaid();
        assertEquals("PARTIAL", testBill.getStatus(), "Status should be PARTIAL");
        assertFalse(testBill.isPaid(), "isPaid() should still return false");
        
        // Mark as paid
        testBill.markAsPaid();
        assertEquals("PAID", testBill.getStatus(), "Status should be PAID");
        assertTrue(testBill.isPaid(), "isPaid() should return true");
    }

    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        // Test setId
        testBill.setId("B002");
        assertEquals("B002", testBill.getId(), "ID should be updated");
        
        // Test setPatient
        Patient newPatient = new Patient("P002", "Jane", "Smith", LocalDate.of(1985, 3, 20));
        testBill.setPatient(newPatient);
        assertEquals(newPatient, testBill.getPatient(), "Patient should be updated");
        
        // Test setIssueDate
        LocalDate newDate = LocalDate.of(2023, 1, 15);
        testBill.setIssueDate(newDate);
        assertEquals(newDate, testBill.getIssueDate(), "Issue date should be updated");
        
        // Test setStatus
        testBill.setStatus("REFUNDED");
        assertEquals("REFUNDED", testBill.getStatus(), "Status should be updated");
    }
} 