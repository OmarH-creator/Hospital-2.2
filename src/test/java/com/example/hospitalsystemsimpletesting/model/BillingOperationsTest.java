package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for billing operations
 */
public class BillingOperationsTest {
    
    private final Patient testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 1, 1));
    private final LocalDate issueDate = LocalDate.now();

    /**
     * Test bill creation with various valid parameters
     */
    @ParameterizedTest(name = "Valid bill with id={0}, issueDate={1}")
    @MethodSource("validBillParameters")
    @DisplayName("Test bill creation with valid parameters")
    void testValidBillCreation(String id, LocalDate issueDate) {
        Bill bill = new Bill(id, testPatient, issueDate);
        
        assertEquals(id, bill.getId(), "Bill ID should match the provided value");
        assertEquals(testPatient, bill.getPatient(), "Patient should match the provided value");
        assertEquals(issueDate, bill.getIssueDate(), "Issue date should match the provided value");
        assertEquals(0.0, bill.getTotalAmount(), "New bill should have zero total amount");
        assertEquals("UNPAID", bill.getStatus(), "New bill should have UNPAID status");
        assertTrue(bill.getItems().isEmpty(), "New bill should have no items");
    }
    
    /**
     * Test bill creation with invalid ID
     */
    @ParameterizedTest(name = "Invalid bill with id={0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Test bill creation with invalid ID")
    void testInvalidBillId(String id) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Bill(id, testPatient, issueDate)
        );
        
        String expectedMessage = id == null ? "ID cannot be null" : "ID cannot be empty";
        assertEquals(expectedMessage, exception.getMessage(), "Exception message should indicate the ID issue");
    }
    
    /**
     * Test bill creation with null patient
     */
    @Test
    @DisplayName("Test bill creation with null patient")
    void testNullPatient() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Bill("B001", null, issueDate)
        );
        
        assertEquals("Patient cannot be null", exception.getMessage(), 
                     "Exception message should indicate the patient issue");
    }
    
    /**
     * Test bill creation with null issue date
     */
    @Test
    @DisplayName("Test bill creation with null issue date")
    void testNullIssueDate() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            new Bill("B001", testPatient, null)
        );
        
        assertEquals("Issue date cannot be null", exception.getMessage(), 
                     "Exception message should indicate the issue date issue");
    }
    
    /**
     * Test adding items to bill with various descriptions and amounts
     */
    @ParameterizedTest(name = "Add item: {0}, ${1}")
    @CsvSource({
        "Consultation, 100.00",
        "X-Ray, 250.50",
        "Blood Test, 75.25",
        "Surgery, 5000.00",
        "Prescription, 45.99"
    })
    @DisplayName("Test adding items to bill")
    void testAddItemToBill(String description, double amount) {
        Bill bill = new Bill("B001", testPatient, issueDate);
        bill.addItem(description, amount);
        
        assertEquals(1, bill.getItems().size(), "Bill should have one item");
        Bill.BillItem item = bill.getItems().get(0);
        assertEquals(description, item.getDescription(), "Item description should match");
        assertEquals(amount, item.getAmount(), "Item amount should match");
        assertEquals(amount, bill.getTotalAmount(), "Bill total should be updated correctly");
    }
    
    /**
     * Test adding multiple items to bill
     */
    @ParameterizedTest(name = "Multiple items test")
    @MethodSource("multipleItemsParameters")
    @DisplayName("Test adding multiple items to bill")
    void testAddMultipleItemsToBill(String[] descriptions, double[] amounts, double expectedTotal) {
        Bill bill = new Bill("B001", testPatient, issueDate);
        
        for (int i = 0; i < descriptions.length; i++) {
            bill.addItem(descriptions[i], amounts[i]);
        }
        
        assertEquals(descriptions.length, bill.getItems().size(), "Bill should have correct number of items");
        assertEquals(expectedTotal, bill.getTotalAmount(), 0.001, "Bill total should be calculated correctly");
    }
    
    /**
     * Test adding item with invalid description
     */
    @ParameterizedTest(name = "Invalid item description: {0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Test adding item with invalid description")
    void testAddItemWithInvalidDescription(String description) {
        Bill bill = new Bill("B001", testPatient, issueDate);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            bill.addItem(description, 100.0)
        );
        
        String expectedMessage = description == null ? "Description cannot be null" : "Description cannot be empty";
        assertEquals(expectedMessage, exception.getMessage(), "Exception message should indicate the description issue");
    }
    
    /**
     * Test adding item with negative amount
     */
    @ParameterizedTest(name = "Invalid item amount: {0}")
    @ValueSource(doubles = {-1.0, -100.50, -0.01})
    @DisplayName("Test adding item with negative amount")
    void testAddItemWithNegativeAmount(double amount) {
        Bill bill = new Bill("B001", testPatient, issueDate);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            bill.addItem("Test Item", amount)
        );
        
        assertEquals("Amount cannot be negative", exception.getMessage(), 
                     "Exception message should indicate the amount issue");
    }
    
    /**
     * Test bill status changes
     */
    @ParameterizedTest(name = "Status change: {0}")
    @MethodSource("statusChangeParameters")
    @DisplayName("Test bill status changes")
    void testStatusChanges(String action, String expectedStatus, boolean expectedPaidStatus) {
        Bill bill = new Bill("B001", testPatient, issueDate);
        
        // Apply the action
        switch (action) {
            case "markAsPaid":
                bill.markAsPaid();
                break;
            case "markAsPartiallyPaid":
                bill.markAsPartiallyPaid();
                break;
            // Default case not needed as we control the input
        }
        
        assertEquals(expectedStatus, bill.getStatus(), 
                     "Bill status should be updated correctly after " + action);
        assertEquals(expectedPaidStatus, bill.isPaid(), 
                     "Bill isPaid() should return correctly after " + action);
    }
    
    // MethodSource for parameterized tests
    private static Stream<Arguments> validBillParameters() {
        return Stream.of(
            Arguments.of("B001", LocalDate.now()),
            Arguments.of("B002", LocalDate.now().minusDays(1)),
            Arguments.of("B003", LocalDate.now().minusWeeks(1)),
            Arguments.of("B004", LocalDate.now().minusMonths(1)),
            Arguments.of("B005", LocalDate.of(2022, 1, 1))
        );
    }
    
    private static Stream<Arguments> multipleItemsParameters() {
        return Stream.of(
            // Three items
            Arguments.of(
                new String[]{"Consultation", "X-Ray", "Medication"},
                new double[]{100.0, 250.0, 75.0},
                425.0
            ),
            // Five items
            Arguments.of(
                new String[]{"Room charge", "Surgery", "Anesthesia", "Recovery", "Medication"},
                new double[]{500.0, 3000.0, 1000.0, 800.0, 200.0},
                5500.0
            ),
            // Single item
            Arguments.of(
                new String[]{"Emergency visit"},
                new double[]{350.0},
                350.0
            ),
            // Zero items (empty arrays)
            Arguments.of(
                new String[]{},
                new double[]{},
                0.0
            )
        );
    }
    
    private static Stream<Arguments> statusChangeParameters() {
        return Stream.of(
            Arguments.of("markAsPaid", "PAID", true),
            Arguments.of("markAsPartiallyPaid", "PARTIAL", false)
        );
    }
} 