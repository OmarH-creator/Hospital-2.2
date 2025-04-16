package com.example.hospitalsystemsimpletesting.service.impl;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.service.DataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.InventoryService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the InventoryService interface.
 */
public class InventoryServiceImpl implements InventoryService {
    
    private final Map<String, InventoryItem> itemsById = new HashMap<>();
    private final DataPersistenceService persistenceService;
    
    /**
     * Creates a new inventory service without persistence
     */
    public InventoryServiceImpl() {
        this.persistenceService = null; // No persistence
    }
    
    /**
     * Creates a new inventory service with persistence
     * 
     * @param persistenceService The data persistence service to use
     */
    public InventoryServiceImpl(DataPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        loadPersistedData();
    }
    
    /**
     * Loads inventory data from persistence service.
     */
    private void loadPersistedData() {
        if (persistenceService != null) {
            List<InventoryItem> items = persistenceService.loadInventory();
            for (InventoryItem item : items) {
                itemsById.put(item.getId(), item);
            }
            System.out.println("Loaded " + items.size() + " inventory items from persistence");
        }
    }
    
    /**
     * Saves inventory data to persistence service.
     */
    private void saveData() {
        if (persistenceService != null) {
            persistenceService.saveInventory(new ArrayList<>(itemsById.values()));
            System.out.println("Saved " + itemsById.size() + " inventory items to persistence");
        }
    }
    
    @Override
    public InventoryItem addInventoryItem(String id, String name, int quantity) {
        // Use default unit price of 0.0 when not specified
        return addInventoryItem(id, name, quantity, BigDecimal.ZERO);
    }
    
    @Override
    public InventoryItem addInventoryItem(String id, String name, int quantity, BigDecimal unitPrice) {
        validateInventoryParams(id, name, quantity);
        
        if (itemsById.containsKey(id)) {
            throw new IllegalArgumentException("Item with ID " + id + " already exists");
        }
        
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        double priceValue = (unitPrice != null) ? unitPrice.doubleValue() : 0.0;
        InventoryItem item = new InventoryItem(id, name, quantity, priceValue);
        
        itemsById.put(id, item);
        saveData(); // Save after adding
        return item;
    }
    
    @Override
    public InventoryItem updateQuantity(String id, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        InventoryItem item = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
        
        // For simplicity, create a new item with updated quantity
        InventoryItem updatedItem = new InventoryItem(
                item.getId(), 
                item.getName(), 
                newQuantity,
                item.getUnitPrice()
        );
        
        // Copy other properties
        if (item.getCategory() != null) {
            updatedItem.setCategory(item.getCategory());
        }
        updatedItem.setMinQuantity(item.getMinQuantity());
        
        itemsById.put(id, updatedItem);
        saveData(); // Save after updating
        return updatedItem;
    }
    
    @Override
    public InventoryItem addStock(String id, int quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        
        InventoryItem item = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
        
        item.addStock(quantityToAdd);
        saveData(); // Save after modifying
        return item;
    }
    
    @Override
    public InventoryItem removeStock(String id, int quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }
        
        InventoryItem item = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
        
        item.removeStock(quantityToRemove);
        saveData(); // Save after modifying
        return item;
    }
    
    @Override
    public InventoryItem updatePrice(String id, BigDecimal newUnitPrice) {
        if (newUnitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        InventoryItem item = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
        
        // For simplicity, create a new item with updated price
        InventoryItem updatedItem = new InventoryItem(
                item.getId(), 
                item.getName(), 
                item.getQuantity(),
                newUnitPrice.doubleValue()
        );
        
        // Copy other properties
        if (item.getCategory() != null) {
            updatedItem.setCategory(item.getCategory());
        }
        updatedItem.setMinQuantity(item.getMinQuantity());
        
        itemsById.put(id, updatedItem);
        saveData(); // Save after updating
        return updatedItem;
    }
    
    @Override
    public Optional<InventoryItem> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        return Optional.ofNullable(itemsById.get(id));
    }
    
    @Override
    public List<InventoryItem> searchByName(String searchTerm) {
        if (searchTerm == null) {
            throw new IllegalArgumentException("Search term cannot be null");
        }
        
        String term = searchTerm.toLowerCase().trim();
        return itemsById.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryItem> getAllItems() {
        return new ArrayList<>(itemsById.values());
    }
    
    @Override
    public List<InventoryItem> getLowStockItems() {
        return itemsById.values().stream()
                .filter(InventoryItem::isLowOnStock)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean deleteItem(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        
        boolean removed = itemsById.remove(id) != null;
        if (removed) {
            saveData(); // Save after deleting
        }
        return removed;
    }
    
    private void validateInventoryParams(String id, String name, int quantity) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }
} 