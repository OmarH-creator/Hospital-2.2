package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Screen for testing InventoryItem functionality.
 * This screen allows direct interaction with InventoryItem objects to test their behavior.
 */
public class InventoryItemTestScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<InventoryItem> itemsTable;
    private final Button createItemButton;
    private final Button addStockButton;
    private final Button removeStockButton;
    private final Button calculateValueButton;
    private final Button checkLowStockButton;
    private final Button backButton;
    private final Label titleLabel;
    private final Label statusLabel;
    private final TextArea resultArea;
    
    // Observable list to display in the table
    private final ObservableList<InventoryItem> itemsData = FXCollections.observableArrayList();
    
    // Currently selected item
    private InventoryItem selectedItem;
    
    // Store test results
    private final List<String> testResults = new ArrayList<>();
    
    public InventoryItemTestScreen() {
        // Create title
        titleLabel = new Label("Inventory Item White Box Testing");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create subtitle
        Text subtitleText = new Text(
            "Use this screen to test the functionality of the InventoryItem class directly. " +
            "Create items with different parameters and test the methods to see how they behave."
        );
        subtitleText.setWrappingWidth(600);
        subtitleText.setTextAlignment(TextAlignment.CENTER);
        
        // Create the table for inventory items
        itemsTable = new TableView<>();
        itemsTable.setId("testItemsTable");
        itemsTable.setPlaceholder(new Label("No test items created"));
        
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
        TableColumn<InventoryItem, Boolean> lowStockColumn = new TableColumn<>("Low Stock");
        lowStockColumn.setCellValueFactory(new PropertyValueFactory<>("lowOnStock"));
        lowStockColumn.setPrefWidth(100);
        lowStockColumn.setCellFactory(column -> new TableCell<InventoryItem, Boolean>() {
            @Override
            protected void updateItem(Boolean lowOnStock, boolean empty) {
                super.updateItem(lowOnStock, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (lowOnStock != null && lowOnStock) {
                    setText("YES");
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    setText("NO");
                    setStyle("-fx-text-fill: green;");
                }
            }
        });
        
        // Total value column
        TableColumn<InventoryItem, Double> totalValueColumn = new TableColumn<>("Total Value");
        totalValueColumn.setCellFactory(column -> new TableCell<InventoryItem, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                } else {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    double totalValue = item.getTotalValue();
                    setText(currencyFormat.format(totalValue));
                }
            }
        });
        totalValueColumn.setPrefWidth(120);
        
        // Add columns to the table
        itemsTable.getColumns().addAll(idColumn, nameColumn, quantityColumn, priceColumn, lowStockColumn, totalValueColumn);
        
        // Set up table selection listener
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedItem = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        itemsTable.setItems(itemsData);
        
        // Create action buttons
        createItemButton = new Button("Create Test Item");
        createItemButton.setId("createItemButton");
        createItemButton.setOnAction(e -> showCreateItemDialog());
        
        addStockButton = new Button("Add Stock");
        addStockButton.setId("addStockButton");
        addStockButton.setOnAction(e -> showAddStockDialog());
        addStockButton.setDisable(true);
        
        removeStockButton = new Button("Remove Stock");
        removeStockButton.setId("removeStockButton");
        removeStockButton.setOnAction(e -> showRemoveStockDialog());
        removeStockButton.setDisable(true);
        
        calculateValueButton = new Button("Calculate Value");
        calculateValueButton.setId("calculateValueButton");
        calculateValueButton.setOnAction(e -> calculateItemValue());
        calculateValueButton.setDisable(true);
        
        checkLowStockButton = new Button("Check Low Stock");
        checkLowStockButton.setId("checkLowStockButton");
        checkLowStockButton.setOnAction(e -> checkLowStock());
        checkLowStockButton.setDisable(true);
        
        // Create a flow pane for buttons
        FlowPane buttonPane = new FlowPane(10, 10);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.getChildren().addAll(
                createItemButton, 
                addStockButton, 
                removeStockButton, 
                calculateValueButton, 
                checkLowStockButton);
        
        // Create test result area
        resultArea = new TextArea();
        resultArea.setId("resultArea");
        resultArea.setEditable(false);
        resultArea.setPrefHeight(150);
        resultArea.setWrapText(true);
        
        // Status label for feedback
        statusLabel = new Label("");
        statusLabel.setId("statusLabel");
        
        // Back button to return to main menu
        backButton = new Button("Back to Inventory");
        backButton.setId("backButton");
        
        // Arrange components in containers
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(titleLabel, subtitleText);
        
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(itemsTable);
        tableContainer.setVgrow(itemsTable, Priority.ALWAYS);
        
        VBox resultContainer = new VBox(5);
        resultContainer.getChildren().addAll(new Label("Test Results:"), resultArea);
        
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(buttonPane, resultContainer, statusLabel, backButton);
        
        // Main container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.getChildren().addAll(headerBox, tableContainer, bottomBox);
        mainContent.setVgrow(tableContainer, Priority.ALWAYS);
        
        // Root container
        rootContainer = new BorderPane();
        rootContainer.setCenter(mainContent);
    }
    
    /**
     * Update the button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedItem != null;
        addStockButton.setDisable(!hasSelection);
        removeStockButton.setDisable(!hasSelection);
        calculateValueButton.setDisable(!hasSelection);
        checkLowStockButton.setDisable(!hasSelection);
    }
    
    /**
     * Display dialog to create a new inventory item for testing
     */
    private void showCreateItemDialog() {
        // Create dialog
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Create Test Inventory Item");
        dialog.setHeaderText("Enter item details for testing");
        
        // Set button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField idField = new TextField();
        idField.setPromptText("Item ID");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        
        TextField quantityField = new TextField("0");
        quantityField.setPromptText("Quantity");
        
        TextField priceField = new TextField("0.00");
        priceField.setPromptText("Unit Price");
        
        TextField minStockField = new TextField("10");
        minStockField.setPromptText("Min Stock Level");
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Unit Price:"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Min Stock Level:"), 0, 4);
        grid.add(minStockField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on ID field
        idField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    double price = Double.parseDouble(priceField.getText().trim());
                    int minStock = Integer.parseInt(minStockField.getText().trim());
                    
                    // Add test result entry
                    addTestResult("Creating item with ID: " + id + ", Name: " + name + 
                                 ", Quantity: " + quantity + ", Price: " + price + 
                                 ", Min Stock: " + minStock);
                    
                    // Create the item with the given parameters
                    try {
                        InventoryItem item = new InventoryItem(id, name, quantity, price);
                        item.setMinQuantity(minStock);
                        addTestResult("✅ Item created successfully");
                        return item;
                    } catch (IllegalArgumentException e) {
                        addTestResult("❌ Error: " + e.getMessage());
                        showErrorAlert("Creation Error", e.getMessage());
                        return null;
                    }
                } catch (NumberFormatException e) {
                    addTestResult("❌ Error: Invalid number format");
                    showErrorAlert("Invalid input", "Please enter valid numbers for quantity, price, and minimum stock.");
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            itemsData.add(item);
            updateStatusLabel("Created test item: " + item.getName());
            refreshTable();
        });
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
                addTestResult("Adding " + qty + " stock to item " + selectedItem.getId());
                
                try {
                    selectedItem.addStock(qty);
                    addTestResult("✅ Stock added successfully. New quantity: " + selectedItem.getQuantity());
                    updateStatusLabel("Added " + qty + " units to " + selectedItem.getName());
                    refreshTable();
                } catch (IllegalArgumentException e) {
                    addTestResult("❌ Error: " + e.getMessage());
                    showErrorAlert("Add Stock Error", e.getMessage());
                }
            } catch (NumberFormatException e) {
                addTestResult("❌ Error: Invalid number format");
                showErrorAlert("Invalid input", "Please enter a valid number.");
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
                addTestResult("Removing " + qty + " stock from item " + selectedItem.getId());
                
                try {
                    selectedItem.removeStock(qty);
                    addTestResult("✅ Stock removed successfully. New quantity: " + selectedItem.getQuantity());
                    updateStatusLabel("Removed " + qty + " units from " + selectedItem.getName());
                    refreshTable();
                } catch (IllegalArgumentException e) {
                    addTestResult("❌ Error: " + e.getMessage());
                    showErrorAlert("Remove Stock Error", e.getMessage());
                }
            } catch (NumberFormatException e) {
                addTestResult("❌ Error: Invalid number format");
                showErrorAlert("Invalid input", "Please enter a valid number.");
            }
        });
    }
    
    /**
     * Calculate the total value of the selected item
     */
    private void calculateItemValue() {
        if (selectedItem == null) {
            return;
        }
        
        double totalValue = selectedItem.getTotalValue();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        addTestResult("Calculating total value for item " + selectedItem.getId());
        addTestResult("Quantity: " + selectedItem.getQuantity() + 
                     ", Unit Price: " + currencyFormat.format(selectedItem.getUnitPrice()) + 
                     ", Total Value: " + currencyFormat.format(totalValue));
        
        updateStatusLabel("Total value of " + selectedItem.getName() + ": " + currencyFormat.format(totalValue));
        refreshTable();
    }
    
    /**
     * Check if the selected item is low on stock
     */
    private void checkLowStock() {
        if (selectedItem == null) {
            return;
        }
        
        boolean isLowOnStock = selectedItem.isLowOnStock();
        
        addTestResult("Checking if item " + selectedItem.getId() + " is low on stock");
        addTestResult("Current quantity: " + selectedItem.getQuantity() + 
                     ", Minimum stock level: " + selectedItem.getMinQuantity() + 
                     ", Low on stock: " + (isLowOnStock ? "YES" : "NO"));
        
        updateStatusLabel(selectedItem.getName() + " is " + (isLowOnStock ? "" : "not ") + "low on stock");
        refreshTable();
    }
    
    /**
     * Add a test result to the list and update the UI
     */
    private void addTestResult(String result) {
        testResults.add(result);
        updateResultArea();
    }
    
    /**
     * Update the result area with the latest test results
     */
    private void updateResultArea() {
        StringBuilder sb = new StringBuilder();
        for (int i = Math.max(0, testResults.size() - 10); i < testResults.size(); i++) {
            sb.append(testResults.get(i)).append("\n");
        }
        resultArea.setText(sb.toString());
        resultArea.positionCaret(resultArea.getText().length());
    }
    
    /**
     * Refresh the table to show updated data
     */
    private void refreshTable() {
        itemsTable.refresh();
    }
    
    /**
     * Update status label with a message
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
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
     * Returns the root container for the inventory item test screen
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
    
    public Button getCreateItemButton() {
        return createItemButton;
    }
    
    public Button getAddStockButton() {
        return addStockButton;
    }
    
    public Button getRemoveStockButton() {
        return removeStockButton;
    }
    
    public Button getCalculateValueButton() {
        return calculateValueButton;
    }
    
    public Button getCheckLowStockButton() {
        return checkLowStockButton;
    }
    
    public Button getBackButton() {
        return backButton;
    }
    
    public Label getStatusLabel() {
        return statusLabel;
    }
    
    public TextArea getResultArea() {
        return resultArea;
    }
} 