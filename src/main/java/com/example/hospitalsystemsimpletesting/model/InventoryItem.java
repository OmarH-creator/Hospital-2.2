package com.example.hospitalsystemsimpletesting.model;

/**
 * Represents an inventory item in the hospital system.
 */
public class InventoryItem {
    private String id;
    private String name;
    private int quantity;
    private double unitPrice;
    private String category;
    private int minQuantity; // Minimum quantity before reordering
    
    public InventoryItem(String id, String name, int quantity, double unitPrice) {
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
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.minQuantity = 10; // Default value
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getMinQuantity() {
        return minQuantity;
    }
    
    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }
    
    // Business methods
    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }
        this.quantity += amount;
    }
    
    public void removeStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to remove must be positive");
        }
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        this.quantity -= amount;
    }
    
    public boolean isLowOnStock() {
        return this.quantity <= this.minQuantity;
    }
    
    public double getTotalValue() {
        return this.quantity * this.unitPrice;
    }
} 