package com.example.hospitalsystemsimpletesting.whitebox;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White box tests for InventoryItem class focusing on:
 * - Statement coverage
 * - Branch coverage
 * - Path coverage
 * - Boundary testing
 */
public class InventoryItemWhiteBoxTest {

    @DisplayName("Test constructor validation with null and empty ID")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testConstructorIdValidation(String id) {
        // Testing branch where ID is null or empty
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new InventoryItem(id, "Ibuprofen", 100, 5.99));
        
        String expectedMessage = id == null ? "ID cannot be null" : "ID cannot be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @DisplayName("Test constructor validation with null and empty name")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testConstructorNameValidation(String name) {
        // Testing branch where name is null or empty
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new InventoryItem("MED001", name, 100, 5.99));
        
        String expectedMessage = name == null ? "Name cannot be null" : "Name cannot be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @DisplayName("Test constructor validation with negative quantity")
    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void testConstructorNegativeQuantityValidation(int quantity) {
        // Testing branch where quantity is negative
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new InventoryItem("MED001", "Ibuprofen", quantity, 5.99));
        
        assertTrue(exception.getMessage().contains("Quantity cannot be negative"));
    }

    @DisplayName("Test constructor validation with negative unit price")
    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -5.99, -100.0})
    void testConstructorNegativeUnitPriceValidation(double unitPrice) {
        // Testing branch where unit price is negative
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new InventoryItem("MED001", "Ibuprofen", 100, unitPrice));
        
        assertTrue(exception.getMessage().contains("Unit price cannot be negative"));
    }

    @DisplayName("Test successful inventory item creation")
    @Test
    void testSuccessfulInventoryItemCreation() {
        // Testing the "happy path" - all inputs valid
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
        
        assertEquals("MED001", item.getId());
        assertEquals("Ibuprofen", item.getName());
        assertEquals(100, item.getQuantity());
        assertEquals(5.99, item.getUnitPrice());
    }

    @DisplayName("Test addStock method")
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 100})
    void testAddStock(int amountToAdd) {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
        int initialQuantity = item.getQuantity();
        
        item.addStock(amountToAdd);
        assertEquals(initialQuantity + amountToAdd, item.getQuantity());
    }

    @DisplayName("Test addStock with zero or negative amount")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void testAddStockZeroOrNegative(int invalidAmount) {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
        int initialQuantity = item.getQuantity();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                item.addStock(invalidAmount));
        
        assertTrue(exception.getMessage().contains("Amount to add must be positive"));
        
        // Verify quantity unchanged
        assertEquals(initialQuantity, item.getQuantity());
    }

    @DisplayName("Test removeStock method")
    @ParameterizedTest
    @CsvSource({
        "100, 50, 50",    // Regular case - remove half
        "100, 1, 99",     // Boundary - remove minimum
        "100, 100, 0"     // Boundary - remove all
    })
    void testRemoveStock(int initialQuantity, int amountToRemove, int expectedFinal) {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", initialQuantity, 5.99);
        
        item.removeStock(amountToRemove);
        assertEquals(expectedFinal, item.getQuantity());
    }

    @DisplayName("Test removeStock with zero or negative amount")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void testRemoveStockZeroOrNegative(int invalidAmount) {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
        int initialQuantity = item.getQuantity();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                item.removeStock(invalidAmount));
        
        assertTrue(exception.getMessage().contains("Amount to remove must be positive"));
        
        // Verify quantity unchanged
        assertEquals(initialQuantity, item.getQuantity());
    }

    @DisplayName("Test removeStock with insufficient stock")
    @Test
    void testRemoveStockInsufficientStock() {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
        int initialQuantity = item.getQuantity();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                item.removeStock(initialQuantity + 1));
        
        assertEquals("Not enough stock available", exception.getMessage());
        
        // Verify quantity unchanged
        assertEquals(initialQuantity, item.getQuantity());
    }

    @DisplayName("Test isLowOnStock method with various thresholds")
    @ParameterizedTest
    @CsvSource({
        "100, 10, false",  // quantity > minQuantity (not low)
        "10, 10, true",    // quantity == minQuantity (low)
        "5, 10, true"      // quantity < minQuantity (low)
    })
    void testIsLowOnStock(int quantity, int minQuantity, boolean expectedResult) {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", quantity, 5.99);
        item.setMinQuantity(minQuantity);
        
        assertEquals(expectedResult, item.isLowOnStock());
    }

    @DisplayName("Test getTotalValue calculation")
    @ParameterizedTest
    @CsvSource({
        "100, 5.99, 599.0",    // Regular case
        "0, 5.99, 0.0",        // Boundary - zero quantity
        "1, 5.99, 5.99"        // Boundary - minimum quantity
    })
    void testGetTotalValue(int quantity, double unitPrice, double expectedTotal) {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", quantity, unitPrice);
        
        assertEquals(expectedTotal, item.getTotalValue(), 0.001);
    }

    @DisplayName("Test all getters and setters")
    @Test
    void testGettersAndSetters() {
        InventoryItem item = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
        
        // Test initial values
        assertEquals("MED001", item.getId());
        assertEquals("Ibuprofen", item.getName());
        assertEquals(100, item.getQuantity());
        assertEquals(5.99, item.getUnitPrice());
        
        // Test setters
        item.setName("Acetaminophen");
        item.setUnitPrice(7.99);
        
        // Verify changes
        assertEquals("Acetaminophen", item.getName());
        assertEquals(7.99, item.getUnitPrice());
    }
} 