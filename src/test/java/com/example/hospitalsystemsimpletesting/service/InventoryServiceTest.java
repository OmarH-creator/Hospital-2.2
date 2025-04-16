package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for InventoryServiceImpl.
 */
class InventoryServiceTest {

    private InventoryService inventoryService;
    private static final String VALID_ID = "MED001";
    private static final String VALID_NAME = "Aspirin";
    private static final int VALID_QUANTITY = 100;
    private static final BigDecimal VALID_PRICE = new BigDecimal("9.99");

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl();
    }

    @Test
    @DisplayName("Should add inventory item with valid data (price specified)")
    void testAddInventoryItemWithPrice() {
        // Act
        InventoryItem item = inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY, VALID_PRICE);
        
        // Assert
        assertNotNull(item, "Inventory item should not be null");
        assertEquals(VALID_ID, item.getId(), "ID should match");
        assertEquals(VALID_NAME, item.getName(), "Name should match");
        assertEquals(VALID_QUANTITY, item.getQuantity(), "Quantity should match");
        assertEquals(VALID_PRICE.doubleValue(), item.getUnitPrice(), "Price should match");
    }

    @Test
    @DisplayName("Should add inventory item with default price when not specified")
    void testAddInventoryItemWithoutPrice() {
        // Act
        InventoryItem item = inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Assert
        assertNotNull(item, "Inventory item should not be null");
        assertEquals(VALID_ID, item.getId(), "ID should match");
        assertEquals(VALID_NAME, item.getName(), "Name should match");
        assertEquals(VALID_QUANTITY, item.getQuantity(), "Quantity should match");
        assertEquals(0.0, item.getUnitPrice(), "Price should be default (0.0)");
    }

    @Test
    @DisplayName("Should throw exception when adding item with null ID")
    void testAddInventoryItemWithNullId() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem(null, VALID_NAME, VALID_QUANTITY);
        });
        
        assertTrue(exception.getMessage().contains("ID cannot be null"), 
                "Exception should mention ID cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when adding item with empty ID")
    void testAddInventoryItemWithEmptyId() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem("", VALID_NAME, VALID_QUANTITY);
        });
        
        assertTrue(exception.getMessage().contains("ID cannot be empty"), 
                "Exception should mention ID cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when adding item with null name")
    void testAddInventoryItemWithNullName() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem(VALID_ID, null, VALID_QUANTITY);
        });
        
        assertTrue(exception.getMessage().contains("Name cannot be null"), 
                "Exception should mention name cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when adding item with empty name")
    void testAddInventoryItemWithEmptyName() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem(VALID_ID, "", VALID_QUANTITY);
        });
        
        assertTrue(exception.getMessage().contains("Name cannot be empty"), 
                "Exception should mention name cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when adding item with negative quantity")
    void testAddInventoryItemWithNegativeQuantity() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem(VALID_ID, VALID_NAME, -10);
        });
        
        assertTrue(exception.getMessage().contains("Quantity cannot be negative"), 
                "Exception should mention quantity cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception when adding item with negative price")
    void testAddInventoryItemWithNegativePrice() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY, new BigDecimal("-1.00"));
        });
        
        assertTrue(exception.getMessage().contains("Unit price cannot be negative"), 
                "Exception should mention price cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception when adding item with duplicate ID")
    void testAddInventoryItemWithDuplicateId() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addInventoryItem(VALID_ID, "Different Name", 50);
        });
        
        assertTrue(exception.getMessage().contains("already exists"), 
                "Exception should mention item already exists");
    }

    @Test
    @DisplayName("Should update quantity successfully")
    void testUpdateQuantity() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        int newQuantity = 200;
        
        // Act
        InventoryItem updatedItem = inventoryService.updateQuantity(VALID_ID, newQuantity);
        
        // Assert
        assertNotNull(updatedItem, "Updated item should not be null");
        assertEquals(VALID_ID, updatedItem.getId(), "ID should be unchanged");
        assertEquals(VALID_NAME, updatedItem.getName(), "Name should be unchanged");
        assertEquals(newQuantity, updatedItem.getQuantity(), "Quantity should be updated");
    }

    @Test
    @DisplayName("Should throw exception when updating quantity for non-existent item")
    void testUpdateQuantityNonExistentItem() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateQuantity("NONEXISTENT", 50);
        });
        
        assertTrue(exception.getMessage().contains("Item not found"), 
                "Exception should mention item not found");
    }

    @Test
    @DisplayName("Should throw exception when updating quantity to negative value")
    void testUpdateQuantityNegativeValue() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateQuantity(VALID_ID, -10);
        });
        
        assertTrue(exception.getMessage().contains("Quantity cannot be negative"), 
                "Exception should mention quantity cannot be negative");
    }

    @Test
    @DisplayName("Should add stock successfully")
    void testAddStock() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        int additionalQuantity = 50;
        
        // Act
        InventoryItem updatedItem = inventoryService.addStock(VALID_ID, additionalQuantity);
        
        // Assert
        assertNotNull(updatedItem, "Updated item should not be null");
        assertEquals(VALID_QUANTITY + additionalQuantity, updatedItem.getQuantity(), 
                "Quantity should be increased by the added amount");
    }

    @Test
    @DisplayName("Should throw exception when adding non-positive stock")
    void testAddStockNonPositive() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addStock(VALID_ID, 0);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"), 
                "Exception should mention quantity must be positive");
    }

    @Test
    @DisplayName("Should remove stock successfully")
    void testRemoveStock() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        int quantityToRemove = 30;
        
        // Act
        InventoryItem updatedItem = inventoryService.removeStock(VALID_ID, quantityToRemove);
        
        // Assert
        assertNotNull(updatedItem, "Updated item should not be null");
        assertEquals(VALID_QUANTITY - quantityToRemove, updatedItem.getQuantity(), 
                "Quantity should be decreased by the removed amount");
    }

    @Test
    @DisplayName("Should throw exception when removing non-positive stock")
    void testRemoveStockNonPositive() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.removeStock(VALID_ID, 0);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"), 
                "Exception should mention quantity must be positive");
    }

    @Test
    @DisplayName("Should throw exception when removing more stock than available")
    void testRemoveExcessiveStock() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.removeStock(VALID_ID, VALID_QUANTITY + 1);
        });
        
        assertTrue(exception.getMessage().contains("Not enough stock"), 
                "Exception should mention not enough stock");
    }

    @Test
    @DisplayName("Should update price successfully")
    void testUpdatePrice() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY, VALID_PRICE);
        BigDecimal newPrice = new BigDecimal("15.99");
        
        // Act
        InventoryItem updatedItem = inventoryService.updatePrice(VALID_ID, newPrice);
        
        // Assert
        assertNotNull(updatedItem, "Updated item should not be null");
        assertEquals(newPrice.doubleValue(), updatedItem.getUnitPrice(), "Price should be updated");
    }

    @Test
    @DisplayName("Should throw exception when updating to negative price")
    void testUpdatePriceNegative() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updatePrice(VALID_ID, new BigDecimal("-1.00"));
        });
        
        assertTrue(exception.getMessage().contains("Unit price cannot be negative"), 
                "Exception should mention price cannot be negative");
    }

    @Test
    @DisplayName("Should find item by ID")
    void testFindById() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act
        Optional<InventoryItem> foundItemOpt = inventoryService.findById(VALID_ID);
        
        // Assert
        assertTrue(foundItemOpt.isPresent(), "Item should be found");
        assertEquals(VALID_ID, foundItemOpt.get().getId(), "Found item should have correct ID");
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent item ID")
    void testFindByIdNonExistent() {
        // Act
        Optional<InventoryItem> result = inventoryService.findById("NONEXISTENT");
        
        // Assert
        assertFalse(result.isPresent(), "Result should be empty for non-existent item");
    }

    @Test
    @DisplayName("Should search items by name")
    void testSearchByName() {
        // Arrange
        inventoryService.addInventoryItem("MED001", "Aspirin", 100);
        inventoryService.addInventoryItem("MED002", "Ibuprofen", 50);
        inventoryService.addInventoryItem("MED003", "Children's Aspirin", 75);
        
        // Act
        List<InventoryItem> searchResults = inventoryService.searchByName("aspirin");
        
        // Assert
        assertEquals(2, searchResults.size(), "Should find 2 items with 'aspirin' in name");
        assertTrue(searchResults.stream().anyMatch(item -> item.getId().equals("MED001")), 
                "Results should include MED001");
        assertTrue(searchResults.stream().anyMatch(item -> item.getId().equals("MED003")), 
                "Results should include MED003");
    }

    @Test
    @DisplayName("Should get all items")
    void testGetAllItems() {
        // Arrange
        inventoryService.addInventoryItem("MED001", "Aspirin", 100);
        inventoryService.addInventoryItem("MED002", "Ibuprofen", 50);
        
        // Act
        List<InventoryItem> allItems = inventoryService.getAllItems();
        
        // Assert
        assertEquals(2, allItems.size(), "Should find all items");
    }

    @Test
    @DisplayName("Should get low stock items")
    void testGetLowStockItems() {
        // Arrange
        InventoryItem regularStock = inventoryService.addInventoryItem("MED001", "Aspirin", 100);
        InventoryItem lowStock = inventoryService.addInventoryItem("MED002", "Ibuprofen", 5);
        
        // Manipulate min quantity threshold for clear testing
        // Note: In a real application, this would require a setter on InventoryItem
        // or a specific method to set the min quantity threshold
        
        // Act
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        
        // Assert
        // By default minQuantity is 10, so only MED002 should be low on stock
        assertTrue(lowStockItems.stream().anyMatch(item -> item.getId().equals("MED002")), 
                "Low stock items should include MED002");
        assertEquals(1, lowStockItems.size(), "Should find 1 low stock item");
    }

    @Test
    @DisplayName("Should delete item successfully")
    void testDeleteItem() {
        // Arrange
        inventoryService.addInventoryItem(VALID_ID, VALID_NAME, VALID_QUANTITY);
        
        // Act
        boolean result = inventoryService.deleteItem(VALID_ID);
        
        // Assert
        assertTrue(result, "Delete operation should return true for existing item");
        assertFalse(inventoryService.findById(VALID_ID).isPresent(), 
                "Item should no longer exist after deletion");
    }

    @Test
    @DisplayName("Should return false when deleting non-existent item")
    void testDeleteNonExistentItem() {
        // Act
        boolean result = inventoryService.deleteItem("NONEXISTENT");
        
        // Assert
        assertFalse(result, "Delete operation should return false for non-existent item");
    }
} 