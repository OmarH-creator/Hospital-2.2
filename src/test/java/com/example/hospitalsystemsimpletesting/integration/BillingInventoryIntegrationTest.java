package com.example.hospitalsystemsimpletesting.integration;

import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.BillingService;
import com.example.hospitalsystemsimpletesting.service.InventoryService;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for the interactions between the Billing and Inventory systems.
 * Tests scenarios where inventory items are used and billed to patients.
 */
public class BillingInventoryIntegrationTest {

    @Mock
    private PatientService patientService;
    
    @Mock
    private InventoryService inventoryService;
    
    private BillingService billingService;
    
    // Test data
    private Patient testPatient;
    private InventoryItem medicationItem;
    private InventoryItem suppliesItem;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize billing service with mocked patient service
        billingService = new BillingServiceImpl(patientService);
        
        // Set up test patient
        testPatient = new Patient("P123", "John", "Doe", LocalDate.of(1980, 1, 1));
        when(patientService.findPatientById("P123")).thenReturn(Optional.of(testPatient));
        
        // Set up inventory items with the correct constructor
        medicationItem = new InventoryItem("M001", "Antibiotic", 50, 25.0);
        medicationItem.setCategory("Medication");
        
        suppliesItem = new InventoryItem("S001", "Bandages", 100, 5.0);
        suppliesItem.setCategory("Supplies");
        
        // Mock inventory service behavior with correct method names
        when(inventoryService.findById("M001")).thenReturn(Optional.of(medicationItem));
        when(inventoryService.findById("S001")).thenReturn(Optional.of(suppliesItem));
    }
    
    @Test
    @DisplayName("Test billing patient for inventory items")
    void testBillingForInventoryItems() {
        // Create a bill for the patient
        Bill bill = billingService.createBill("P123", "Treatment with medication", LocalDate.now().plusDays(30));
        assertNotNull(bill, "Bill should be created");
        
        // Get initial bill item count
        int initialItemCount = bill.getItems().size();
        
        // Simulate using inventory items for patient treatment
        // 1. Use 2 units of medication
        boolean medicationUsed = simulateUseInventoryItem("M001", 2);
        assertTrue(medicationUsed, "Should successfully use medication from inventory");
        
        // 2. Use 5 units of supplies
        boolean suppliesUsed = simulateUseInventoryItem("S001", 5);
        assertTrue(suppliesUsed, "Should successfully use supplies from inventory");
        
        // Add the used items to the bill
        billingService.addItemToBill(bill.getId(), "Antibiotic (2 units)", new BigDecimal("50.00"));
        billingService.addItemToBill(bill.getId(), "Bandages (5 units)", new BigDecimal("25.00"));
        
        // Retrieve the updated bill
        Optional<Bill> updatedBill = billingService.findBillById(bill.getId());
        assertTrue(updatedBill.isPresent(), "Bill should exist");
        
        // Verify the bill total
        assertEquals(75.0, updatedBill.get().getTotalAmount(), 0.01, "Bill total should be correct");
        
        // The actual count may vary depending on whether the bill automatically creates items
        // In our test, we added 2 specific items
        assertEquals(initialItemCount + 2, updatedBill.get().getItems().size(), 
                   "Bill should have 2 more items than when first created");
        
        // Verify inventory quantities were reduced using correct method names
        verify(inventoryService).updateQuantity("M001", 48); // 50 - 2
        verify(inventoryService).updateQuantity("S001", 95); // 100 - 5
    }
    
    @Test
    @DisplayName("Test insufficient inventory quantity")
    void testInsufficientInventoryQuantity() {
        // Set up a billing event that requires more medication than available
        Bill bill = billingService.createBill("P123", "Emergency treatment", LocalDate.now().plusDays(30));
        assertNotNull(bill, "Bill should be created");
        
        // Get initial bill item count
        int initialItemCount = bill.getItems().size();
        
        // Try to use more medication than available
        when(inventoryService.updateQuantity("M001", 0))
            .thenThrow(new IllegalArgumentException("Insufficient quantity in inventory"));
        
        // This should fail as we don't have enough inventory
        assertThrows(IllegalArgumentException.class, () -> {
            simulateUseInventoryItem("M001", 60); // Trying to use 60 when only 50 available
        });
        
        // Verify no new items were added to the bill after the exception
        Optional<Bill> updatedBill = billingService.findBillById(bill.getId());
        assertTrue(updatedBill.isPresent(), "Bill should exist");
        assertEquals(initialItemCount, updatedBill.get().getItems().size(), 
                     "Bill should have same number of items as when first created");
    }
    
    /**
     * Simulates using an inventory item and updating its quantity.
     * This represents what would happen in a real application when an item is used for a patient.
     * 
     * @param itemId The ID of the inventory item
     * @param quantity The quantity to use
     * @return true if the operation was successful
     */
    private boolean simulateUseInventoryItem(String itemId, int quantity) {
        // Check if item exists
        Optional<InventoryItem> itemOpt = inventoryService.findById(itemId);
        if (!itemOpt.isPresent()) {
            return false;
        }
        
        InventoryItem item = itemOpt.get();
        
        // Check if we have enough quantity
        if (item.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient quantity in inventory");
        }
        
        // Update the inventory quantity with the correct method name
        int newQuantity = item.getQuantity() - quantity;
        inventoryService.updateQuantity(itemId, newQuantity);
        
        return true;
    }
} 