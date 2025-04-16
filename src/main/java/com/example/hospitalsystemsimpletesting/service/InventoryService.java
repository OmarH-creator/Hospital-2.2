package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing inventory operations.
 */
public interface InventoryService {
    
    /**
     * Adds a new inventory item.
     *
     * @param id The unique identifier for the item
     * @param name The name of the item
     * @param quantity The initial quantity
     * @return The created inventory item
     * @throws IllegalArgumentException if parameters are invalid
     */
    InventoryItem addInventoryItem(String id, String name, int quantity);
    
    /**
     * Adds a new inventory item with price information.
     *
     * @param id The unique identifier for the item
     * @param name The name of the item
     * @param quantity The initial quantity
     * @param unitPrice The price per unit
     * @return The created inventory item
     * @throws IllegalArgumentException if parameters are invalid
     */
    InventoryItem addInventoryItem(String id, String name, int quantity, BigDecimal unitPrice);
    
    /**
     * Updates the quantity of an existing inventory item.
     *
     * @param id The ID of the item to update
     * @param newQuantity The new quantity
     * @return The updated inventory item
     * @throws IllegalArgumentException if item not found or parameters invalid
     */
    InventoryItem updateQuantity(String id, int newQuantity);
    
    /**
     * Adds stock to an existing inventory item.
     *
     * @param id The ID of the item to update
     * @param quantityToAdd The quantity to add
     * @return The updated inventory item
     * @throws IllegalArgumentException if item not found or parameters invalid
     */
    InventoryItem addStock(String id, int quantityToAdd);
    
    /**
     * Removes stock from an existing inventory item.
     *
     * @param id The ID of the item to update
     * @param quantityToRemove The quantity to remove
     * @return The updated inventory item
     * @throws IllegalArgumentException if item not found, parameters invalid, or insufficient stock
     */
    InventoryItem removeStock(String id, int quantityToRemove);
    
    /**
     * Updates the unit price of an inventory item.
     *
     * @param id The ID of the item to update
     * @param newUnitPrice The new unit price
     * @return The updated inventory item
     * @throws IllegalArgumentException if item not found or parameters invalid
     */
    InventoryItem updatePrice(String id, BigDecimal newUnitPrice);
    
    /**
     * Finds an inventory item by its ID.
     *
     * @param id The ID of the item to find
     * @return Optional containing the item if found, empty otherwise
     */
    Optional<InventoryItem> findById(String id);
    
    /**
     * Searches for inventory items by name.
     *
     * @param searchTerm The search term to look for in item names
     * @return List of matching inventory items
     */
    List<InventoryItem> searchByName(String searchTerm);
    
    /**
     * Gets all inventory items in the system.
     *
     * @return List of all inventory items
     */
    List<InventoryItem> getAllItems();
    
    /**
     * Gets all inventory items that are low on stock 
     * (below their minimum quantity threshold).
     *
     * @return List of items low on stock
     */
    List<InventoryItem> getLowStockItems();
    
    /**
     * Deletes an inventory item.
     *
     * @param id The ID of the item to delete
     * @return true if the item was deleted, false if it wasn't found
     */
    boolean deleteItem(String id);
} 