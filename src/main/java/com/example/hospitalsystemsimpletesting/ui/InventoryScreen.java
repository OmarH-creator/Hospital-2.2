package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.service.InventoryService;
import com.example.hospitalsystemsimpletesting.service.impl.InventoryServiceImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Screen for inventory management functionality.
 * Allows viewing, adding, editing, and deleting inventory items.
 */
public class InventoryScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<InventoryItem> itemsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button addButton;
    private final Button editButton;
    private final Button deleteButton;
    private final Button addStockButton;
    private final Button removeStockButton;
    private final Button backButton;
    private final Button lowStockButton;
    private final Button testItemButton;
    private final Label titleLabel;
    private final Label statusLabel;
    
    // Service for inventory operations
    private final InventoryService inventoryService;
    
    // Observable list to display in the table
    private final ObservableList<InventoryItem> itemsData = FXCollections.observableArrayList();
    
    // Currently selected item
    private InventoryItem selectedItem;
    
    // Flag to show all items or just low stock items
    private boolean showingLowStock = false;
    
    public InventoryScreen() {
        // Initialize the inventory service using the shared persistence service
        this.inventoryService = new InventoryServiceImpl(HospitalApplication.getDataPersistenceService());
        
        // Create title
        titleLabel = new Label("Inventory Management");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create the table for inventory items
        itemsTable = new TableView<>();
        itemsTable.setId("inventoryTable");
        itemsTable.setPlaceholder(new Label("No items in inventory"));
        
        // Define table columns
        TableColumn<InventoryItem, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);
        
        TableColumn<InventoryItem, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        TableColumn<InventoryItem, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(100);
        
        // Format price as currency
        TableColumn<InventoryItem, Double> priceColumn = new TableColumn<>("Unit Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceColumn.setPrefWidth(100);
        priceColumn.setCellFactory(column -> new TableCell<InventoryItem, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(price));
                }
            }
        });
        
        // Add an indicator for low stock items
        TableColumn<InventoryItem, Boolean> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("lowOnStock"));
        statusColumn.setPrefWidth(100);
        statusColumn.setCellFactory(column -> new TableCell<InventoryItem, Boolean>() {
            @Override
            protected void updateItem(Boolean lowOnStock, boolean empty) {
                super.updateItem(lowOnStock, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (lowOnStock != null && lowOnStock) {
                    setText("LOW STOCK");
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    setText("OK");
                    setStyle("-fx-text-fill: green;");
                }
            }
        });
        
        // Add columns to the table
        itemsTable.getColumns().addAll(idColumn, nameColumn, quantityColumn, priceColumn, statusColumn);
        
        // Set up table selection listener
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedItem = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        itemsTable.setItems(itemsData);
        
        // Create search controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Search by name");
        searchField.setPrefWidth(250);
        
        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> searchItems());
        
        // Reset search when text field is cleared
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshItemsList();
            }
        });
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        addButton = new Button("Add Item");
        addButton.setId("addButton");
        addButton.setOnAction(e -> showAddItemDialog());
        
        editButton = new Button("Edit Item");
        editButton.setId("editButton");
        editButton.setOnAction(e -> showEditItemDialog());
        editButton.setDisable(true);
        
        deleteButton = new Button("Delete Item");
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(e -> deleteSelectedItem());
        deleteButton.setDisable(true);
        
        addStockButton = new Button("Add Stock");
        addStockButton.setId("addStockButton");
        addStockButton.setOnAction(e -> showAddStockDialog());
        addStockButton.setDisable(true);
        
        removeStockButton = new Button("Remove Stock");
        removeStockButton.setId("removeStockButton");
        removeStockButton.setOnAction(e -> showRemoveStockDialog());
        removeStockButton.setDisable(true);
        
        lowStockButton = new Button("Show Low Stock");
        lowStockButton.setId("lowStockButton");
        lowStockButton.setOnAction(e -> toggleLowStockView());
        
        testItemButton = new Button("Test Item Functionality");
        testItemButton.setId("testItemButton");
        testItemButton.setOnAction(e -> openItemTestScreen());
        
        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, addStockButton, removeStockButton, lowStockButton, testItemButton);
        
        // Status label for feedback
        statusLabel = new Label("");
        statusLabel.setId("statusLabel");
        
        // Back button to return to main menu
        backButton = new Button("Back to Main Menu");
        backButton.setId("backButton");
        
        // Arrange components in containers
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(titleLabel, searchBox);
        
        VBox tableContainer = new VBox(15);
        tableContainer.getChildren().addAll(itemsTable);
        tableContainer.setVgrow(itemsTable, Priority.ALWAYS);
        
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(buttonBox, statusLabel, backButton);
        
        // Main container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.getChildren().addAll(headerBox, tableContainer, bottomBox);
        mainContent.setVgrow(tableContainer, Priority.ALWAYS);
        
        // Root container
        rootContainer = new BorderPane();
        rootContainer.setCenter(mainContent);
        
        // Load initial data
        refreshItemsList();
        
        // Add some sample data for demonstration
        if (itemsData.isEmpty()) {
            addSampleData();
        }
    }
    
    /**
     * Add sample inventory items for demonstration
     */
    private void addSampleData() {
        inventoryService.addInventoryItem("MED001", "Antibiotics", 100, new BigDecimal("12.99"));
        inventoryService.addInventoryItem("MED002", "Pain Relievers", 150, new BigDecimal("8.50"));
        inventoryService.addInventoryItem("MED003", "Bandages", 5, new BigDecimal("4.25"));
        inventoryService.addInventoryItem("EQP001", "Stethoscope", 25, new BigDecimal("75.00"));
        inventoryService.addInventoryItem("EQP002", "Thermometer", 10, new BigDecimal("15.75"));
        refreshItemsList();
    }
    
    /**
     * Search for items by name
     */
    private void searchItems() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshItemsList();
            return;
        }
        
        itemsData.clear();
        itemsData.addAll(inventoryService.searchByName(query));
        updateStatusLabel("Found " + itemsData.size() + " item(s) matching '" + query + "'");
    }
    
    /**
     * Toggle between showing all items and low stock items
     */
    private void toggleLowStockView() {
        showingLowStock = !showingLowStock;
        if (showingLowStock) {
            itemsData.clear();
            itemsData.addAll(inventoryService.getLowStockItems());
            lowStockButton.setText("Show All Items");
            updateStatusLabel("Showing " + itemsData.size() + " low stock item(s)");
        } else {
            refreshItemsList();
            lowStockButton.setText("Show Low Stock");
        }
    }
    
    /**
     * Refresh the items list from the service
     */
    private void refreshItemsList() {
        itemsData.clear();
        itemsData.addAll(inventoryService.getAllItems());
        showingLowStock = false;
        lowStockButton.setText("Show Low Stock");
        updateStatusLabel("Showing all " + itemsData.size() + " item(s)");
    }
    
    /**
     * Update status label with a message
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Update the button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedItem != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        addStockButton.setDisable(!hasSelection);
        removeStockButton.setDisable(!hasSelection);
    }
    
    /**
     * Display dialog to add a new inventory item
     */
    private void showAddItemDialog() {
        // Create dialog
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Inventory Item");
        dialog.setHeaderText("Enter item details");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField idField = new TextField();
        idField.setPromptText("Item ID");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        
        TextField priceField = new TextField();
        priceField.setPromptText("Unit Price");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Unit Price:"), 0, 3);
        grid.add(priceField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on ID field
        idField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    BigDecimal price = new BigDecimal(priceField.getText().trim());
                    
                    return inventoryService.addInventoryItem(id, name, quantity, price);
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid input", "Please enter valid numbers for quantity and price.");
                    return null;
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            refreshItemsList();
            updateStatusLabel("Added new item: " + item.getName());
        });
    }
    
    /**
     * Display dialog to edit an existing inventory item
     */
    private void showEditItemDialog() {
        if (selectedItem == null) {
            return;
        }
        
        // Create dialog
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Inventory Item");
        dialog.setHeaderText("Edit item details");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Label idValueLabel = new Label(selectedItem.getId());
        
        TextField nameField = new TextField(selectedItem.getName());
        
        TextField quantityField = new TextField(String.valueOf(selectedItem.getQuantity()));
        
        TextField priceField = new TextField(String.valueOf(selectedItem.getUnitPrice()));
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idValueLabel, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Unit Price:"), 0, 3);
        grid.add(priceField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on name field
        nameField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String id = selectedItem.getId();
                    String name = nameField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    BigDecimal price = new BigDecimal(priceField.getText().trim());
                    
                    // Update item data - in a real app, this would be a single update method
                    // For now, we'll simulate with individual updates
                    InventoryItem item = inventoryService.updateQuantity(id, quantity);
                    item = inventoryService.updatePrice(id, price);
                    return item;
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid input", "Please enter valid numbers for quantity and price.");
                    return null;
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            refreshItemsList();
            updateStatusLabel("Updated item: " + item.getName());
        });
    }
    
    /**
     * Delete the selected inventory item
     */
    private void deleteSelectedItem() {
        if (selectedItem == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Inventory Item");
        alert.setHeaderText("Delete Item");
        alert.setContentText("Are you sure you want to delete " + selectedItem.getName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = inventoryService.deleteItem(selectedItem.getId());
            if (deleted) {
                refreshItemsList();
                updateStatusLabel("Deleted item: " + selectedItem.getName());
            } else {
                showErrorAlert("Delete Failed", "Could not delete the selected item.");
            }
        }
    }
    
    /**
     * Display dialog to add stock to an inventory item
     */
    private void showAddStockDialog() {
        if (selectedItem == null) {
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock to " + selectedItem.getName());
        dialog.setContentText("Enter quantity to add:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantity -> {
            try {
                int qty = Integer.parseInt(quantity);
                inventoryService.addStock(selectedItem.getId(), qty);
                refreshItemsList();
                updateStatusLabel("Added " + qty + " units to " + selectedItem.getName());
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid input", "Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                showErrorAlert("Invalid input", e.getMessage());
            }
        });
    }
    
    /**
     * Display dialog to remove stock from an inventory item
     */
    private void showRemoveStockDialog() {
        if (selectedItem == null) {
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Stock");
        dialog.setHeaderText("Remove stock from " + selectedItem.getName());
        dialog.setContentText("Enter quantity to remove:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantity -> {
            try {
                int qty = Integer.parseInt(quantity);
                inventoryService.removeStock(selectedItem.getId(), qty);
                refreshItemsList();
                updateStatusLabel("Removed " + qty + " units from " + selectedItem.getName());
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid input", "Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                showErrorAlert("Invalid input", e.getMessage());
            }
        });
    }
    
    /**
     * Open the inventory item test screen
     */
    private void openItemTestScreen() {
        try {
            // Get the current scene
            Scene currentScene = testItemButton.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            
            // Create inventory item test screen
            InventoryItemTestScreen testScreen = new InventoryItemTestScreen();
            
            // Set back button action to return to inventory screen
            testScreen.setBackButtonAction(() -> {
                primaryStage.setScene(currentScene);
                primaryStage.setTitle("Hospital Management System - Inventory");
            });
            
            // Switch to test screen
            Scene testScene = new Scene(testScreen.getRoot(), currentScene.getWidth(), currentScene.getHeight());
            testScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            primaryStage.setTitle("Hospital Management System - Inventory Item Testing");
            primaryStage.setScene(testScene);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open the test screen: " + e.getMessage());
        }
    }
    
    /**
     * Display an error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Returns the root container for the inventory screen
     */
    public Parent getRoot() {
        return rootContainer;
    }
    
    /**
     * Set the action for the back button
     */
    public void setBackButtonAction(Runnable action) {
        backButton.setOnAction(e -> action.run());
    }
    
    // Getters for testing
    public TableView<InventoryItem> getItemsTable() {
        return itemsTable;
    }
    
    public TextField getSearchField() {
        return searchField;
    }
    
    public Button getSearchButton() {
        return searchButton;
    }
    
    public Button getAddButton() {
        return addButton;
    }
    
    public Button getEditButton() {
        return editButton;
    }
    
    public Button getDeleteButton() {
        return deleteButton;
    }
    
    public Button getAddStockButton() {
        return addStockButton;
    }
    
    public Button getRemoveStockButton() {
        return removeStockButton;
    }
    
    public Button getBackButton() {
        return backButton;
    }
    
    public Button getLowStockButton() {
        return lowStockButton;
    }
    
    public Button getTestItemButton() {
        return testItemButton;
    }
    
    public Label getStatusLabel() {
        return statusLabel;
    }
} 