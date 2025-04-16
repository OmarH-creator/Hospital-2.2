package com.example.hospitalsystemsimpletesting;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.service.DataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.InventoryService;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.PatientServiceImpl;
import com.example.hospitalsystemsimpletesting.service.impl.CSVDataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.impl.InventoryServiceImpl;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Manual test class to check if inventory persistence is working correctly.
 * This is not an automated test but a manual verification tool.
 */
public class InventoryPersistenceTest {

    public static void main(String[] args) {
        // Create services with proper initialization
        PatientServiceImpl tempPatientService = new PatientServiceImpl();
        DataPersistenceService persistenceService = new CSVDataPersistenceService(tempPatientService);
        PatientService patientService = new PatientServiceImpl(persistenceService);
        ((CSVDataPersistenceService) persistenceService).setPatientService(patientService);
        
        // Create inventory service with persistence
        InventoryService inventoryService = new InventoryServiceImpl(persistenceService);
        
        // Check if the data directory exists, create if not
        Path dataDir = Paths.get("hospital_data");
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectory(dataDir);
                System.out.println("Created data directory: " + dataDir.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error creating data directory: " + e.getMessage());
                return;
            }
        }
        
        // Check if any inventory items already exist in the database
        List<InventoryItem> existingItems = inventoryService.getAllItems();
        System.out.println("Found " + existingItems.size() + " existing inventory items.");
        
        for (InventoryItem item : existingItems) {
            System.out.println("Item: " + item.getId() + " - " + item.getName() + 
                               ", Quantity: " + item.getQuantity() + ", Price: " + item.getUnitPrice());
        }
        
        // Add new items if none exist
        if (existingItems.isEmpty()) {
            System.out.println("Adding sample inventory items...");
            addSampleItems(inventoryService);
            
            // Verify items were added
            List<InventoryItem> newItems = inventoryService.getAllItems();
            System.out.println("Added " + newItems.size() + " inventory items.");
            
            for (InventoryItem item : newItems) {
                System.out.println("Item: " + item.getId() + " - " + item.getName() + 
                                  ", Quantity: " + item.getQuantity() + ", Price: " + item.getUnitPrice());
            }
        } else {
            // Update an existing item to test persistence
            if (!existingItems.isEmpty()) {
                InventoryItem firstItem = existingItems.get(0);
                int oldQuantity = firstItem.getQuantity();
                int newQuantity = oldQuantity + 10;
                
                System.out.println("Updating quantity of item " + firstItem.getId() + 
                                   " from " + oldQuantity + " to " + newQuantity);
                
                inventoryService.updateQuantity(firstItem.getId(), newQuantity);
                
                // Verify the update
                InventoryItem updatedItem = inventoryService.findById(firstItem.getId()).orElse(null);
                if (updatedItem != null) {
                    System.out.println("Updated item quantity: " + updatedItem.getQuantity());
                }
            }
        }
        
        System.out.println("Inventory persistence test completed.");
    }
    
    private static void addSampleItems(InventoryService inventoryService) {
        inventoryService.addInventoryItem("MED001", "Antibiotics", 100, new BigDecimal("12.99"));
        inventoryService.addInventoryItem("MED002", "Pain Relievers", 150, new BigDecimal("8.50"));
        inventoryService.addInventoryItem("MED003", "Bandages", 5, new BigDecimal("4.25"));
        inventoryService.addInventoryItem("EQP001", "Stethoscope", 25, new BigDecimal("75.00"));
        inventoryService.addInventoryItem("EQP002", "Thermometer", 10, new BigDecimal("15.75"));
    }
} 