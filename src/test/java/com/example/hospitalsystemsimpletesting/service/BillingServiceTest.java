package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Payment;
import com.example.hospitalsystemsimpletesting.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for BillingServiceImpl.
 */
class BillingServiceTest {

    @Mock
    private PatientService patientService;

    private BillingService billingService;
    private Patient testPatient;
    private LocalDate validDueDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        billingService = new BillingServiceImpl(patientService);
        
        // Clear existing bills through reflection to ensure test isolation
        try {
            Field billsByIdField = BillingServiceImpl.class.getDeclaredField("billsById");
            billsByIdField.setAccessible(true);
            Map<String, Bill> billsById = (Map<String, Bill>) billsByIdField.get(billingService);
            billsById.clear();
            
            Field paymentsByBillIdField = BillingServiceImpl.class.getDeclaredField("paymentsByBillId");
            paymentsByBillIdField.setAccessible(true);
            Map<String, List<Payment>> paymentsByBillId = (Map<String, List<Payment>>) paymentsByBillIdField.get(billingService);
            paymentsByBillId.clear();
            
            Field dueDatesByBillIdField = BillingServiceImpl.class.getDeclaredField("dueDatesByBillId");
            dueDatesByBillIdField.setAccessible(true);
            Map<String, LocalDate> dueDatesByBillId = (Map<String, LocalDate>) dueDatesByBillIdField.get(billingService);
            dueDatesByBillId.clear();
        } catch (Exception e) {
            fail("Failed to clear bills: " + e.getMessage());
        }
        
        testPatient = new Patient("P123", "John", "Doe", LocalDate.of(1980, 1, 1));
        validDueDate = LocalDate.now().plusDays(30);

        // Mock patientService to return our test patient
        when(patientService.findPatientById("P123")).thenReturn(Optional.of(testPatient));
        when(patientService.findPatientById("NONEXISTENT")).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Should create a bill successfully with valid data")
    void testCreateBill() {
        // Arrange
        String patientId = "P123";
        String description = "Hospital Services";
        
        // Act
        Bill bill = billingService.createBill(patientId, description, validDueDate);
        
        // Assert
        assertNotNull(bill, "Bill should not be null");
        assertEquals(testPatient, bill.getPatient(), "Bill should be associated with the correct patient");
        assertEquals("UNPAID", bill.getStatus(), "New bill should have UNPAID status");
        assertEquals(0.0, bill.getTotalAmount(), "New bill should have zero amount initially");
    }

    @Test
    @DisplayName("Should throw exception when creating bill with invalid patient ID")
    void testCreateBillInvalidPatientId() {
        // Arrange
        String nonExistentPatientId = "NONEXISTENT";
        String description = "Hospital Services";
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            billingService.createBill(nonExistentPatientId, description, validDueDate);
        });
        
        assertTrue(exception.getMessage().contains("Patient not found"), 
                "Exception should mention patient not found");
    }

    @Test
    @DisplayName("Should throw exception when creating bill with null patient ID")
    void testCreateBillNullPatientId() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            billingService.createBill(null, "Test", validDueDate);
        });
        
        assertTrue(exception.getMessage().contains("Patient ID cannot be null"), 
                "Exception should mention patient ID cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when creating bill with null description")
    void testCreateBillNullDescription() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            billingService.createBill("P123", null, validDueDate);
        });
        
        assertTrue(exception.getMessage().contains("Description cannot be null"), 
                "Exception should mention description cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when creating bill with past due date")
    void testCreateBillPastDueDate() {
        // Arrange
        LocalDate pastDueDate = LocalDate.now().minusDays(1);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            billingService.createBill("P123", "Test", pastDueDate);
        });
        
        assertTrue(exception.getMessage().contains("Due date cannot be in the past"), 
                "Exception should mention due date cannot be in the past");
    }

    @Test
    @DisplayName("Should add item to bill successfully")
    void testAddItemToBill() {
        // Arrange
        Bill bill = billingService.createBill("P123", "Initial Services", validDueDate);
        String itemDescription = "Consultation Fee";
        BigDecimal amount = new BigDecimal("150.00");
        
        // Act
        Bill updatedBill = billingService.addItemToBill(bill.getId(), itemDescription, amount);
        
        // Assert
        assertNotNull(updatedBill, "Updated bill should not be null");
        assertEquals(bill.getId(), updatedBill.getId(), "Bill ID should remain the same");
        assertEquals(150.00, updatedBill.getTotalAmount(), "Bill amount should be updated");
        
        // Check that item was added
        List<Bill.BillItem> items = updatedBill.getItems();
        assertTrue(items.stream().anyMatch(item -> 
                item.getDescription().equals(itemDescription) && 
                item.getAmount() == 150.00), 
                "Bill should contain the added item");
    }

    @Test
    @DisplayName("Should throw exception when adding item to non-existent bill")
    void testAddItemToNonExistentBill() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            billingService.addItemToBill("NONEXISTENT", "Test", new BigDecimal("10.00"));
        });
        
        assertTrue(exception.getMessage().contains("Bill not found"), 
                "Exception should mention bill not found");
    }

    @Test
    @DisplayName("Should throw exception when adding item with invalid amount")
    void testAddItemWithInvalidAmount() {
        // Arrange
        Bill bill = billingService.createBill("P123", "Initial Services", validDueDate);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            billingService.addItemToBill(bill.getId(), "Test", new BigDecimal("-10.00"));
        });
        
        assertTrue(exception.getMessage().contains("Amount must be greater than zero"), 
                "Exception should mention amount must be greater than zero");
    }

    @Test
    @DisplayName("Should process payment successfully")
    void testProcessPayment() {
        // Arrange
        Bill bill = billingService.createBill("P123", "Initial Services", validDueDate);
        billingService.addItemToBill(bill.getId(), "Consultation Fee", new BigDecimal("150.00"));
        
        // Act
        Payment payment = billingService.processPayment(bill.getId(), new BigDecimal("150.00"), "CREDIT_CARD");
        
        // Assert
        assertNotNull(payment, "Payment should not be null");
        assertEquals(bill.getId(), payment.getBillId(), "Payment should be associated with correct bill");
        assertEquals(new BigDecimal("150.00"), payment.getAmount(), "Payment amount should match");
        assertEquals("CREDIT_CARD", payment.getPaymentMethod(), "Payment method should match");
        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus(), "Payment status should be COMPLETED");
        
        // Check that bill status was updated
        Optional<Bill> updatedBillOpt = billingService.findBillById(bill.getId());
        assertTrue(updatedBillOpt.isPresent(), "Bill should exist");
        Bill updatedBill = updatedBillOpt.get();
        assertTrue(updatedBill.isPaid(), "Bill should be marked as paid");
    }

    @Test
    @DisplayName("Should mark bill as partially paid for partial payment")
    void testPartialPayment() {
        // Arrange
        Bill bill = billingService.createBill("P123", "Initial Services", validDueDate);
        billingService.addItemToBill(bill.getId(), "Consultation Fee", new BigDecimal("150.00"));
        
        // Act
        Payment payment = billingService.processPayment(bill.getId(), new BigDecimal("50.00"), "CASH");
        
        // Assert
        Optional<Bill> updatedBillOpt = billingService.findBillById(bill.getId());
        assertTrue(updatedBillOpt.isPresent(), "Bill should exist");
        Bill updatedBill = updatedBillOpt.get();
        assertEquals("PARTIAL", updatedBill.getStatus(), "Bill should be marked as partially paid");
    }

    @Test
    @DisplayName("Should find bill by ID")
    void testFindBillById() {
        // Arrange
        Bill bill = billingService.createBill("P123", "Initial Services", validDueDate);
        
        // Act
        Optional<Bill> foundBillOpt = billingService.findBillById(bill.getId());
        
        // Assert
        assertTrue(foundBillOpt.isPresent(), "Bill should be found");
        assertEquals(bill.getId(), foundBillOpt.get().getId(), "Found bill should have correct ID");
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent bill ID")
    void testFindNonExistentBillById() {
        // Act
        Optional<Bill> result = billingService.findBillById("NONEXISTENT");
        
        // Assert
        assertFalse(result.isPresent(), "Result should be empty for non-existent bill");
    }

    @Test
    @DisplayName("Should find bills by patient ID")
    void testFindBillsByPatientId() {
        // Arrange
        Bill bill1 = billingService.createBill("P123", "Service 1", validDueDate);
        Bill bill2 = billingService.createBill("P123", "Service 2", validDueDate);
        
        // Act
        List<Bill> patientBills = billingService.findBillsByPatientId("P123");
        
        // Debug
        System.out.println("Number of bills for patient P123: " + patientBills.size());
        patientBills.forEach(bill -> System.out.println("Bill ID: " + bill.getId()));
        
        // Assert
        assertEquals(2, patientBills.size(), "Should find 2 bills for patient");
        assertTrue(patientBills.stream().allMatch(b -> b.getPatient().getId().equals("P123")),
                "All bills should belong to correct patient");
    }

    @Test
    @DisplayName("Should return empty list for patient with no bills")
    void testFindBillsForPatientWithNoBills() {
        // Arrange
        when(patientService.findPatientById("P456")).thenReturn(Optional.of(new Patient("P456", "Jane", "Smith", LocalDate.now())));
        
        // Act
        List<Bill> patientBills = billingService.findBillsByPatientId("P456");
        
        // Assert
        assertTrue(patientBills.isEmpty(), "Should return empty list for patient with no bills");
    }

    @Test
    @DisplayName("Should get all bills")
    void testGetAllBills() {
        // Arrange
        Bill bill1 = billingService.createBill("P123", "Service 1", validDueDate);
        Bill bill2 = billingService.createBill("P123", "Service 2", validDueDate);
        
        // Debug
        System.out.println("Created bill1: " + bill1.getId());
        System.out.println("Created bill2: " + bill2.getId());
        
        // Act
        List<Bill> allBills = billingService.getAllBills();
        
        // Debug
        System.out.println("Total bills: " + allBills.size());
        allBills.forEach(bill -> System.out.println("Bill in getAllBills: " + bill.getId()));
        
        // Assert
        assertEquals(2, allBills.size(), "Should find all bills");
    }

    @Test
    @DisplayName("Should get bills by paid status")
    void testGetBillsByStatus() {
        // Arrange
        Bill bill1 = billingService.createBill("P123", "Service 1", validDueDate);
        Bill bill2 = billingService.createBill("P123", "Service 2", validDueDate);
        
        // Debug
        System.out.println("Created bill1: " + bill1.getId() + ", Status: " + bill1.getStatus());
        System.out.println("Created bill2: " + bill2.getId() + ", Status: " + bill2.getStatus());
        
        // Add items and make payment for first bill
        billingService.addItemToBill(bill1.getId(), "Item", new BigDecimal("100.00"));
        billingService.processPayment(bill1.getId(), new BigDecimal("100.00"), "CASH");
        
        // Debug after payment
        System.out.println("After payment bill1 status: " + bill1.getStatus());
        
        // Act
        List<Bill> paidBills = billingService.getBillsByStatus(true);
        List<Bill> unpaidBills = billingService.getBillsByStatus(false);
        
        // Debug
        System.out.println("Paid bills count: " + paidBills.size());
        paidBills.forEach(bill -> System.out.println("Paid bill: " + bill.getId() + ", Status: " + bill.getStatus()));
        System.out.println("Unpaid bills count: " + unpaidBills.size());
        unpaidBills.forEach(bill -> System.out.println("Unpaid bill: " + bill.getId() + ", Status: " + bill.getStatus()));
        
        // Assert
        assertEquals(1, paidBills.size(), "Should find 1 paid bill");
        assertEquals(1, unpaidBills.size(), "Should find 1 unpaid bill");
        assertEquals(bill1.getId(), paidBills.get(0).getId(), "Paid bill should be bill1");
        assertEquals(bill2.getId(), unpaidBills.get(0).getId(), "Unpaid bill should be bill2");
    }

    @Test
    @DisplayName("Should get overdue bills")
    void testGetOverdueBills() {
        // Arrange
        // Create a bill due tomorrow (not overdue)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Bill futureBill = billingService.createBill("P123", "Future Service", tomorrow);
        
        // We can't easily manipulate the current date, but we can create a bill
        // that's already marked as unpaid and has a due date in the past
        Bill bill = billingService.createBill("P123", "Past Service", LocalDate.now().plusDays(2));
        
        // Manipulate the due date in the dueDatesByBillId map using reflection
        try {
            // Get the dueDatesByBillId field
            Field dueDatesField = BillingServiceImpl.class.getDeclaredField("dueDatesByBillId");
            dueDatesField.setAccessible(true);
            
            // Get the map instance
            @SuppressWarnings("unchecked")
            Map<String, LocalDate> dueDatesByBillId = (Map<String, LocalDate>) dueDatesField.get(billingService);
            
            // Set the due date to yesterday
            dueDatesByBillId.put(bill.getId(), LocalDate.now().minusDays(1));
        } catch (Exception e) {
            fail("Failed to set up test using reflection: " + e.getMessage());
        }
        
        // Act
        List<Bill> overdueBills = billingService.getOverdueBills();
        
        // Assert
        assertEquals(1, overdueBills.size(), "Should have 1 overdue bill");
        assertEquals(bill.getId(), overdueBills.get(0).getId(), "Overdue bill ID should match");
    }
} 