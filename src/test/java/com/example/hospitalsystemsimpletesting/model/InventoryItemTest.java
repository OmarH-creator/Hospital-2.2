package com.example.hospitalsystemsimpletesting.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the InventoryItem model
 */
public class InventoryItemTest {

    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new InventoryItem("INV001", "Paracetamol 500mg", 100, 0.5);
    }

    @Test
    @DisplayName("Test inventory item initialization")
    void testInitialization() {
        // Verify fields are correctly initialized
        assertEquals("INV001", testItem.getId(), "ID should match");
        assertEquals("Paracetamol 500mg", testItem.getName(), "Name should match");
        assertEquals(100, testItem.getQuantity(), "Quantity should match");
        assertEquals(0.5, testItem.getUnitPrice(), "Unit price should match");
        assertEquals(10, testItem.getMinQuantity(), "Min quantity should have default value");
        assertNull(testItem.getCategory(), "Category should be null initially");
    }

    @Test
    @DisplayName("Test constructor validation")
    void testConstructorValidation() {
        // Test null ID
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItem(null, "Paracetamol 500mg", 100, 0.5);
        });
        assertTrue(exception1.getMessage().contains("ID cannot be null"), "Should validate ID is not null");

        // Test empty ID
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItem("", "Paracetamol 500mg", 100, 0.5);
        });
        assertTrue(exception2.getMessage().contains("ID cannot be empty"), "Should validate ID is not empty");

        // Test null name
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItem("INV001", null, 100, 0.5);
        });
        assertTrue(exception3.getMessage().contains("Name cannot be null"), "Should validate name is not null");

        // Test empty name
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItem("INV001", "", 100, 0.5);
        });
        assertTrue(exception4.getMessage().contains("Name cannot be empty"), "Should validate name is not empty");

        // Test negative quantity
        Exception exception5 = assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItem("INV001", "Paracetamol 500mg", -10, 0.5);
        });
        assertTrue(exception5.getMessage().contains("Quantity cannot be negative"), "Should validate quantity is not negative");

        // Test negative unit price
        Exception exception6 = assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItem("INV001", "Paracetamol 500mg", 100, -0.5);
        });
        assertTrue(exception6.getMessage().contains("Unit price cannot be negative"), "Should validate unit price is not negative");
    }

    @Test
    @DisplayName("Test add stock")
    void testAddStock() {
        // Initial quantity
        assertEquals(100, testItem.getQuantity(), "Initial quantity should be 100");
        
        // Add stock
        testItem.addStock(50);
        
        // Verify quantity is updated
        assertEquals(150, testItem.getQuantity(), "Quantity should be increased by 50");
        
        // Test zero amount validation
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            testItem.addStock(0);
        });
        assertTrue(exception1.getMessage().contains("Amount to add must be positive"), "Should validate amount is positive");
        
        // Test negative amount validation
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            testItem.addStock(-10);
        });
        assertTrue(exception2.getMessage().contains("Amount to add must be positive"), "Should validate amount is positive");
    }

    @Test
    @DisplayName("Test remove stock")
    void testRemoveStock() {
        // Initial quantity
        assertEquals(100, testItem.getQuantity(), "Initial quantity should be 100");
        
        // Remove stock
        testItem.removeStock(30);
        
        // Verify quantity is updated
        assertEquals(70, testItem.getQuantity(), "Quantity should be decreased by 30");
        
        // Test zero amount validation
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            testItem.removeStock(0);
        });
        assertTrue(exception1.getMessage().contains("Amount to remove must be positive"), "Should validate amount is positive");
        
        // Test negative amount validation
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            testItem.removeStock(-10);
        });
        assertTrue(exception2.getMessage().contains("Amount to remove must be positive"), "Should validate amount is positive");
        
        // Test insufficient stock validation
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            testItem.removeStock(100);
        });
        assertTrue(exception3.getMessage().contains("Not enough stock available"), "Should validate sufficient stock is available");
    }

    @Test
    @DisplayName("Test is low on stock")
    void testIsLowOnStock() {
        // Initial quantity (100) > minQuantity (10)
        assertFalse(testItem.isLowOnStock(), "Should not be low on stock initially");
        
        // Set quantity equal to minQuantity
        testItem.setQuantity(10);
        assertTrue(testItem.isLowOnStock(), "Should be low on stock when quantity equals minQuantity");
        
        // Set quantity below minQuantity
        testItem.setQuantity(5);
        assertTrue(testItem.isLowOnStock(), "Should be low on stock when quantity is below minQuantity");
        
        // Set quantity above minQuantity
        testItem.setQuantity(15);
        assertFalse(testItem.isLowOnStock(), "Should not be low on stock when quantity is above minQuantity");
        
        // Change minQuantity
        testItem.setMinQuantity(20);
        assertTrue(testItem.isLowOnStock(), "Should be low on stock after changing minQuantity");
    }

    @Test
    @DisplayName("Test get total value")
    void testGetTotalValue() {
        // Initial total value: 100 * 0.5 = 50.0
        assertEquals(50.0, testItem.getTotalValue(), "Total value should be quantity * unitPrice");
        
        // Change quantity
        testItem.setQuantity(200);
        assertEquals(100.0, testItem.getTotalValue(), "Total value should be updated when quantity changes");
        
        // Change unit price
        testItem.setUnitPrice(0.75);
        assertEquals(150.0, testItem.getTotalValue(), "Total value should be updated when unit price changes");
    }

    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        // Test setId
        testItem.setId("INV002");
        assertEquals("INV002", testItem.getId(), "ID should be updated");
        
        // Test setName
        testItem.setName("Ibuprofen 400mg");
        assertEquals("Ibuprofen 400mg", testItem.getName(), "Name should be updated");
        
        // Test setQuantity
        testItem.setQuantity(200);
        assertEquals(200, testItem.getQuantity(), "Quantity should be updated");
        
        // Test setUnitPrice
        testItem.setUnitPrice(0.75);
        assertEquals(0.75, testItem.getUnitPrice(), "Unit price should be updated");
        
        // Test setCategory
        testItem.setCategory("Pain Relievers");
        assertEquals("Pain Relievers", testItem.getCategory(), "Category should be updated");
        
        // Test setMinQuantity
        testItem.setMinQuantity(20);
        assertEquals(20, testItem.getMinQuantity(), "Min quantity should be updated");
    }
} 