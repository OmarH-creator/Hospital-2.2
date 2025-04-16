package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.service.InventoryService;
import com.example.hospitalsystemsimpletesting.service.impl.InventoryServiceImpl;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the InventoryScreen UI class.
 * These tests verify that the basic functionality of the UI works correctly.
 * 
 * Note: These are not UI interaction tests. For a real application, you would want 
 * to add proper JavaFX tests to simulate user interactions.
 */
public class InventoryScreenTest {
    
    private InventoryScreen inventoryScreen;
    private InventoryService inventoryService;
    
    @BeforeEach
    void setUp() {
        // Initialize test data
        inventoryService = new InventoryServiceImpl();
        // Add some test data
        inventoryService.addInventoryItem("TEST001", "Test Item 1", 10, new BigDecimal("9.99"));
        inventoryService.addInventoryItem("TEST002", "Test Item 2", 5, new BigDecimal("19.99"));
        
        // Create the screen
        inventoryScreen = new InventoryScreen();
    }
    
    @Test
    void testInitialComponentsState() {
        // Verify all necessary components are created
        assertNotNull(inventoryScreen.getItemsTable());
        assertNotNull(inventoryScreen.getSearchField());
        assertNotNull(inventoryScreen.getSearchButton());
        assertNotNull(inventoryScreen.getAddButton());
        assertNotNull(inventoryScreen.getEditButton());
        assertNotNull(inventoryScreen.getDeleteButton());
        assertNotNull(inventoryScreen.getAddStockButton());
        assertNotNull(inventoryScreen.getRemoveStockButton());
        assertNotNull(inventoryScreen.getBackButton());
        assertNotNull(inventoryScreen.getStatusLabel());
        
        // Verify that button states are correct at initialization
        assertFalse(inventoryScreen.getAddButton().isDisabled());
        assertTrue(inventoryScreen.getEditButton().isDisabled());
        assertTrue(inventoryScreen.getDeleteButton().isDisabled());
        assertTrue(inventoryScreen.getAddStockButton().isDisabled());
        assertTrue(inventoryScreen.getRemoveStockButton().isDisabled());
        assertFalse(inventoryScreen.getBackButton().isDisabled());
    }
    
    @Test
    void testComponensHaveCorrectIds() {
        // Verify components have the correct IDs for CSS styling and testing
        assertEquals("inventoryTable", inventoryScreen.getItemsTable().getId());
        assertEquals("searchField", inventoryScreen.getSearchField().getId());
        assertEquals("searchButton", inventoryScreen.getSearchButton().getId());
        assertEquals("addButton", inventoryScreen.getAddButton().getId());
        assertEquals("editButton", inventoryScreen.getEditButton().getId());
        assertEquals("deleteButton", inventoryScreen.getDeleteButton().getId());
        assertEquals("addStockButton", inventoryScreen.getAddStockButton().getId());
        assertEquals("removeStockButton", inventoryScreen.getRemoveStockButton().getId());
        assertEquals("backButton", inventoryScreen.getBackButton().getId());
        assertEquals("statusLabel", inventoryScreen.getStatusLabel().getId());
    }
    
    @Test
    void testTableColumnsSetupCorrectly() {
        TableView<InventoryItem> table = inventoryScreen.getItemsTable();
        
        // Verify the table has the correct number of columns
        assertEquals(5, table.getColumns().size());
        
        // Verify column headers
        assertEquals("ID", table.getColumns().get(0).getText());
        assertEquals("Name", table.getColumns().get(1).getText());
        assertEquals("Quantity", table.getColumns().get(2).getText());
        assertEquals("Unit Price", table.getColumns().get(3).getText());
        assertEquals("Status", table.getColumns().get(4).getText());
    }
    
    // Additional JavaFX tests would be needed to properly test UI interactions
} 