package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Payment;
import com.example.hospitalsystemsimpletesting.service.BillingService;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.impl.BillingServiceImpl;
import com.example.hospitalsystemsimpletesting.HospitalApplication;

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
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Screen for billing management functionality.
 * Allows creating, viewing, and managing bills and payments.
 */
public class BillingScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<Bill> billsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button createBillButton;
    private final Button addItemButton;
    private final Button viewDetailsButton;
    private final Button processPaymentButton;
    private final Button refreshButton;
    private final Button backButton;
    private final Label titleLabel;
    private final Label statusLabel;
    private final DatePicker dueDatePicker;
    private final ComboBox<String> statusFilterComboBox;
    private final ComboBox<Patient> patientFilterComboBox;
    
    // Services for operations
    private final BillingService billingService;
    private final PatientService patientService;
    
    // Observable list to display in the table
    private final ObservableList<Bill> billsData = FXCollections.observableArrayList();
    
    // Currently selected bill
    private Bill selectedBill;
    
    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    // Payment methods available
    private final String[] PAYMENT_METHODS = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "INSURANCE", "CHECK", "BANK_TRANSFER"};
    
    public BillingScreen() {
        // Initialize services using shared instances
        this.patientService = HospitalApplication.getPatientService();
        this.billingService = new BillingServiceImpl(patientService);
        
        // Create title
        titleLabel = new Label("Billing Management");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create the table for bills
        billsTable = new TableView<>();
        billsTable.setId("billsTable");
        billsTable.setPlaceholder(new Label("No bills found"));
        
        // Define table columns
        TableColumn<Bill, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        
        // Patient column that shows patient's full name
        TableColumn<Bill, Patient> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientColumn.setPrefWidth(150);
        patientColumn.setCellFactory(column -> new TableCell<Bill, Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setText(null);
                } else {
                    setText(patient.getFullName());
                }
            }
        });
        
        // Patient ID column
        TableColumn<Bill, Patient> patientIdColumn = new TableColumn<>("ID");
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientIdColumn.setPrefWidth(80);
        patientIdColumn.setCellFactory(column -> new TableCell<Bill, Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setText(null);
                } else {
                    setText(patient.getId());
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        
        // Issue Date column
        TableColumn<Bill, LocalDate> issueDateColumn = new TableColumn<>("Issue Date");
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        issueDateColumn.setPrefWidth(100);
        issueDateColumn.setCellFactory(column -> new TableCell<Bill, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });
        
        // Total Amount column
        TableColumn<Bill, Double> amountColumn = new TableColumn<>("Total Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        amountColumn.setPrefWidth(120);
        amountColumn.setCellFactory(column -> new TableCell<Bill, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // Status column with color coding
        TableColumn<Bill, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(100);
        statusColumn.setCellFactory(column -> new TableCell<Bill, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PAID":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green
                            break;
                        case "PARTIAL":
                            setStyle("-fx-text-fill: #f39c12;"); // Orange
                            break;
                        case "UNPAID":
                            setStyle("-fx-text-fill: #e74c3c;"); // Red
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Bill ID column (moved to the end)
        TableColumn<Bill, String> billIdColumn = new TableColumn<>("Bill ID");
        billIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        billIdColumn.setPrefWidth(120);
        
        // Number of items column
        TableColumn<Bill, List> itemsColumn = new TableColumn<>("Items");
        itemsColumn.setCellValueFactory(new PropertyValueFactory<>("items"));
        itemsColumn.setPrefWidth(80);
        itemsColumn.setCellFactory(column -> new TableCell<Bill, List>() {
            @Override
            protected void updateItem(List items, boolean empty) {
                super.updateItem(items, empty);
                if (empty || items == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(items.size()));
                }
            }
        });
        
        // Add columns to the table
        billsTable.getColumns().addAll(
                patientIdColumn, patientColumn, issueDateColumn, amountColumn, itemsColumn, statusColumn, billIdColumn
        );
        
        // Set up table selection listener
        billsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBill = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        billsTable.setItems(billsData);
        
        // Create search and filter controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Search by bill ID");
        searchField.setPrefWidth(200);
        
        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> searchBills());
        
        // Create patient filter
        patientFilterComboBox = new ComboBox<>();
        patientFilterComboBox.setId("patientFilterComboBox");
        patientFilterComboBox.setPromptText("Filter by patient");
        patientFilterComboBox.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
        patientFilterComboBox.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "All Patients" : "[" + patient.getId() + "] " + patient.getFullName();
            }
            
            @Override
            public Patient fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        patientFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                filterBillsByPatient(newValue);
            } else {
                refreshBillsList();
            }
        });
        
        // Create status filter
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.setId("statusFilterComboBox");
        statusFilterComboBox.setPromptText("Filter by status");
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "PAID", "UNPAID", "PARTIAL", "OVERDUE"
        ));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                filterBillsByStatus(newValue);
            } else {
                refreshBillsList();
            }
        });
        
        // Date picker for due date filtering
        dueDatePicker = new DatePicker();
        dueDatePicker.setId("dueDatePicker");
        dueDatePicker.setPromptText("Filter by due date");
        
        // Reset search when text field is cleared
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshBillsList();
            }
        });
        
        searchBox.getChildren().addAll(searchField, searchButton, patientFilterComboBox, statusFilterComboBox);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        createBillButton = new Button("Create New Bill");
        createBillButton.setId("createBillButton");
        createBillButton.setOnAction(e -> showCreateBillDialog());
        
        addItemButton = new Button("Add Item");
        addItemButton.setId("addItemButton");
        addItemButton.setOnAction(e -> showAddItemDialog());
        addItemButton.setDisable(true);
        
        viewDetailsButton = new Button("View Details");
        viewDetailsButton.setId("viewDetailsButton");
        viewDetailsButton.setOnAction(e -> showBillDetailsDialog());
        viewDetailsButton.setDisable(true);
        
        processPaymentButton = new Button("Process Payment");
        processPaymentButton.setId("processPaymentButton");
        processPaymentButton.setOnAction(e -> showProcessPaymentDialog());
        processPaymentButton.setDisable(true);
        
        refreshButton = new Button("Refresh");
        refreshButton.setId("refreshButton");
        refreshButton.setOnAction(e -> refreshBillsList());
        
        buttonBox.getChildren().addAll(createBillButton, addItemButton, viewDetailsButton, processPaymentButton, refreshButton);
        
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
        tableContainer.getChildren().addAll(billsTable);
        tableContainer.setVgrow(billsTable, Priority.ALWAYS);
        
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
        refreshBillsList();
        
        // Add some sample data for demonstration
        if (billsData.isEmpty()) {
            addSampleData();
        }
    }
    
    /**
     * Add sample billing data for demonstration
     */
    private void addSampleData() {
        try {
            // First, ensure we have some patients to work with
            if (patientService.getAllPatients().isEmpty()) {
                // Register patients using the recommended method
                Patient p1 = patientService.registerPatient(
                    "John Doe", 
                    LocalDate.of(1980, 5, 15),
                    "Male",
                    "555-123-4567",
                    "123 Main St"
                );
                
                Patient p2 = patientService.registerPatient(
                    "Jane Smith", 
                    LocalDate.of(1992, 8, 22),
                    "Female",
                    "555-987-6543",
                    "456 Oak Ave"
                );
                
                Patient p3 = patientService.registerPatient(
                    "Robert Johnson", 
                    LocalDate.of(1975, 12, 3),
                    "Male",
                    "555-456-7890",
                    "789 Pine St"
                );
            }
            
            // Create sample bills
            LocalDate today = LocalDate.now();
            LocalDate nextMonth = today.plusMonths(1);
            
            // Get patient IDs from the service
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                throw new IllegalStateException("No patients available to create bills for");
            }
            
            String patient1Id = patients.get(0).getId();
            String patient2Id = patients.size() > 1 ? patients.get(1).getId() : patient1Id;
            String patient3Id = patients.size() > 2 ? patients.get(2).getId() : patient1Id;
            
            // Bill 1 - Paid bill for patient 1
            Bill bill1 = billingService.createBill(patient1Id, "Hospital Stay", nextMonth);
            billingService.addItemToBill(bill1.getId(), "Room & Board (3 days)", new BigDecimal("1500.00"));
            billingService.addItemToBill(bill1.getId(), "Laboratory Tests", new BigDecimal("350.00"));
            billingService.addItemToBill(bill1.getId(), "Medication", new BigDecimal("125.50"));
            billingService.processPayment(bill1.getId(), new BigDecimal("1975.50"), "CREDIT_CARD");
            
            // Bill 2 - Unpaid bill for patient 2 
            Bill bill2 = billingService.createBill(patient2Id, "Outpatient Procedure", nextMonth);
            billingService.addItemToBill(bill2.getId(), "Consultation", new BigDecimal("200.00"));
            billingService.addItemToBill(bill2.getId(), "X-Ray", new BigDecimal("450.00"));
            
            // Bill 3 - Partially paid bill for patient 3
            Bill bill3 = billingService.createBill(patient3Id, "Emergency Room Visit", nextMonth);
            billingService.addItemToBill(bill3.getId(), "ER Services", new BigDecimal("1200.00"));
            billingService.addItemToBill(bill3.getId(), "CT Scan", new BigDecimal("800.00"));
            billingService.addItemToBill(bill3.getId(), "Medication", new BigDecimal("95.75"));
            billingService.processPayment(bill3.getId(), new BigDecimal("1000.00"), "INSURANCE");
            
            refreshBillsList();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error Adding Sample Data", "Could not add sample billing data: " + e.getMessage());
        }
    }
    
    /**
     * Search for bills by ID
     */
    private void searchBills() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshBillsList();
            return;
        }
        
        // Search by ID
        Optional<Bill> bill = billingService.findBillById(query);
        
        billsData.clear();
        bill.ifPresent(billsData::add);
        
        updateStatusLabel(bill.isPresent() 
                ? "Found bill with ID: " + query 
                : "No bill found with ID: " + query);
    }
    
    /**
     * Filter bills by patient
     */
    private void filterBillsByPatient(Patient patient) {
        if (patient == null) {
            refreshBillsList();
            return;
        }
        
        List<Bill> patientBills = billingService.findBillsByPatientId(patient.getId());
        billsData.clear();
        billsData.addAll(patientBills);
        updateStatusLabel("Showing " + billsData.size() + " bill(s) for " + patient.getFullName());
    }
    
    /**
     * Filter bills by status
     */
    private void filterBillsByStatus(String status) {
        if (status == null || status.equals("All")) {
            refreshBillsList();
            return;
        }
        
        List<Bill> filteredBills;
        
        if (status.equals("OVERDUE")) {
            filteredBills = billingService.getOverdueBills();
        } else if (status.equals("PAID")) {
            filteredBills = billingService.getBillsByStatus(true);
        } else if (status.equals("UNPAID")) {
            filteredBills = billingService.getBillsByStatus(false);
        } else {
            // For PARTIAL or other statuses, filter manually
            filteredBills = billingService.getAllBills().stream()
                    .filter(bill -> status.equals(bill.getStatus()))
                    .collect(Collectors.toList());
        }
        
        billsData.clear();
        billsData.addAll(filteredBills);
        updateStatusLabel("Showing " + billsData.size() + " " + status.toLowerCase() + " bill(s)");
    }
    
    /**
     * Refresh the bills list from the service
     */
    private void refreshBillsList() {
        // Update bills
        billsData.clear();
        billsData.addAll(billingService.getAllBills());
        
        // Update patient list in the combo box to show any new patients
        patientFilterComboBox.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
        
        // Reset filters
        patientFilterComboBox.setValue(null);
        statusFilterComboBox.setValue("All");
        updateStatusLabel("Showing all " + billsData.size() + " bill(s)");
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
        boolean hasSelection = selectedBill != null;
        viewDetailsButton.setDisable(!hasSelection);
        processPaymentButton.setDisable(!hasSelection || (hasSelection && selectedBill.isPaid()));
        addItemButton.setDisable(!hasSelection);
    }
    
    /**
     * Display dialog to create a new bill
     */
    private void showCreateBillDialog() {
        // Create dialog
        Dialog<Bill> dialog = new Dialog<>();
        dialog.setTitle("Create New Bill");
        dialog.setHeaderText("Enter bill details");
        
        // Set button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Patient selection (dropdown)
        ComboBox<Patient> patientComboBox = new ComboBox<>();
        patientComboBox.setPromptText("Select Patient");
        // Get the most recent patient list
        List<Patient> currentPatients = patientService.getAllPatients();
        patientComboBox.setItems(FXCollections.observableArrayList(currentPatients));
        patientComboBox.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : "[" + patient.getId() + "] " + patient.getFullName();
            }
            
            @Override
            public Patient fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Description field
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Enter Description");
        
        // Due date picker
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Due Date");
        dueDatePicker.setValue(LocalDate.now().plusMonths(1)); // Default to one month from now
        
        // Add fields to grid
        grid.add(new Label("Patient:"), 0, 0);
        grid.add(patientComboBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Due Date:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    Patient patient = patientComboBox.getValue();
                    String description = descriptionField.getText();
                    LocalDate dueDate = dueDatePicker.getValue();
                    
                    // Validate inputs
                    if (patient == null) {
                        showErrorAlert("Invalid Input", "Please select a patient.");
                        return null;
                    }
                    
                    if (description == null || description.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please enter a description.");
                        return null;
                    }
                    
                    if (dueDate == null) {
                        showErrorAlert("Invalid Input", "Please select a due date.");
                        return null;
                    }
                    
                    // Create the bill
                    return billingService.createBill(patient.getId(), description, dueDate);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Bill> result = dialog.showAndWait();
        result.ifPresent(bill -> {
            refreshBillsList();
            updateStatusLabel("Created new bill for " + bill.getPatient().getFullName());
            
            // Select the newly created bill
            billsTable.getSelectionModel().select(bill);
        });
    }
    
    /**
     * Display dialog to add an item to a bill
     */
    private void showAddItemDialog() {
        if (selectedBill == null) {
            return;
        }
        
        // Create dialog
        Dialog<Bill> dialog = new Dialog<>();
        dialog.setTitle("Add Item to Bill");
        dialog.setHeaderText("Add item to bill for " + selectedBill.getPatient().getFullName());
        
        // Set button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Description field
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Enter Item Description");
        
        // Amount field
        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount (e.g., 100.00)");
        
        // Add fields to grid
        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String description = descriptionField.getText();
                    String amountText = amountField.getText();
                    
                    // Validate inputs
                    if (description == null || description.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please enter a description.");
                        return null;
                    }
                    
                    if (amountText == null || amountText.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please enter an amount.");
                        return null;
                    }
                    
                    try {
                        BigDecimal amount = new BigDecimal(amountText);
                        
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            showErrorAlert("Invalid Input", "Amount must be greater than zero.");
                            return null;
                        }
                        
                        // Add the item to the bill
                        return billingService.addItemToBill(selectedBill.getId(), description, amount);
                    } catch (NumberFormatException e) {
                        showErrorAlert("Invalid Input", "Please enter a valid numeric amount.");
                        return null;
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Bill> result = dialog.showAndWait();
        result.ifPresent(bill -> {
            refreshBillsList();
            updateStatusLabel("Added item to bill for " + bill.getPatient().getFullName());
            
            // Select the updated bill
            billsTable.getSelectionModel().select(bill);
        });
    }
    
    /**
     * Display dialog to process a payment for a bill
     */
    private void showProcessPaymentDialog() {
        if (selectedBill == null) {
            return;
        }
        
        // Don't allow payment for already paid bills
        if (selectedBill.isPaid()) {
            showErrorAlert("Cannot Process Payment", "This bill is already fully paid.");
            return;
        }
        
        // Create dialog
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle("Process Payment");
        dialog.setHeaderText("Process payment for bill #" + selectedBill.getId());
        
        // Set button types
        ButtonType payButtonType = new ButtonType("Process Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Current balance information
        Label currentBalanceLabel = new Label(String.format("$%.2f", selectedBill.getTotalAmount()));
        
        // Amount field
        TextField amountField = new TextField();
        amountField.setPromptText("Enter Payment Amount");
        // Set default payment amount to total amount
        amountField.setText(String.format("%.2f", selectedBill.getTotalAmount()));
        
        // Payment method dropdown
        ComboBox<String> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.setPromptText("Select Payment Method");
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(PAYMENT_METHODS));
        
        // Add fields to grid
        grid.add(new Label("Bill ID:"), 0, 0);
        grid.add(new Label(selectedBill.getId()), 1, 0);
        grid.add(new Label("Patient:"), 0, 1);
        grid.add(new Label(selectedBill.getPatient().getFullName()), 1, 1);
        grid.add(new Label("Current Balance:"), 0, 2);
        grid.add(currentBalanceLabel, 1, 2);
        grid.add(new Label("Payment Amount:"), 0, 3);
        grid.add(amountField, 1, 3);
        grid.add(new Label("Payment Method:"), 0, 4);
        grid.add(paymentMethodComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                try {
                    String amountText = amountField.getText();
                    String paymentMethod = paymentMethodComboBox.getValue();
                    
                    // Validate inputs
                    if (amountText == null || amountText.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please enter a payment amount.");
                        return null;
                    }
                    
                    if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please select a payment method.");
                        return null;
                    }
                    
                    try {
                        BigDecimal amount = new BigDecimal(amountText);
                        
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            showErrorAlert("Invalid Input", "Payment amount must be greater than zero.");
                            return null;
                        }
                        
                        // Process the payment
                        return billingService.processPayment(selectedBill.getId(), amount, paymentMethod);
                    } catch (NumberFormatException e) {
                        showErrorAlert("Invalid Input", "Please enter a valid numeric amount.");
                        return null;
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Payment> result = dialog.showAndWait();
        result.ifPresent(payment -> {
            refreshBillsList();
            updateStatusLabel("Processed payment of $" + payment.getAmount() + " for bill #" + payment.getBillId());
            
            // Find and select the updated bill
            billingService.findBillById(payment.getBillId()).ifPresent(bill -> 
                billsTable.getSelectionModel().select(bill));
        });
    }
    
    /**
     * Display dialog to view bill details
     */
    private void showBillDetailsDialog() {
        if (selectedBill == null) {
            return;
        }
        
        // Create dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Bill Details");
        dialog.setHeaderText("Details for Bill #" + selectedBill.getId());
        
        // Set button types
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Basic bill information
        GridPane basicInfoGrid = new GridPane();
        basicInfoGrid.setHgap(15);
        basicInfoGrid.setVgap(8);
        basicInfoGrid.setPadding(new Insets(10));
        
        int row = 0;
        
        basicInfoGrid.add(new Label("Bill ID:"), 0, row);
        basicInfoGrid.add(new Label(selectedBill.getId()), 1, row++);
        
        basicInfoGrid.add(new Label("Patient ID:"), 0, row);
        Label patientIdLabel = new Label(selectedBill.getPatient().getId());
        patientIdLabel.setStyle("-fx-font-weight: bold;"); // Make the ID stand out
        basicInfoGrid.add(patientIdLabel, 1, row++);
        
        basicInfoGrid.add(new Label("Patient Name:"), 0, row);
        basicInfoGrid.add(new Label(selectedBill.getPatient().getFullName()), 1, row++);
        
        basicInfoGrid.add(new Label("Issue Date:"), 0, row);
        basicInfoGrid.add(new Label(dateFormatter.format(selectedBill.getIssueDate())), 1, row++);
        
        basicInfoGrid.add(new Label("Status:"), 0, row);
        Label statusLabel = new Label(selectedBill.getStatus());
        switch (selectedBill.getStatus()) {
            case "PAID":
                statusLabel.setTextFill(Color.GREEN);
                break;
            case "PARTIAL":
                statusLabel.setTextFill(Color.ORANGE);
                break;
            case "UNPAID":
                statusLabel.setTextFill(Color.RED);
                break;
        }
        basicInfoGrid.add(statusLabel, 1, row++);
        
        basicInfoGrid.add(new Label("Total Amount:"), 0, row);
        basicInfoGrid.add(new Label(String.format("$%.2f", selectedBill.getTotalAmount())), 1, row++);
        
        // Create a section for bill items
        Label itemsLabel = new Label("Items:");
        itemsLabel.setFont(new Font("Arial", 14));
        
        // Create table for bill items
        TableView<Bill.BillItem> itemsTable = new TableView<>();
        itemsTable.setPrefHeight(150);
        
        TableColumn<Bill.BillItem, String> itemDescCol = new TableColumn<>("Description");
        itemDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        itemDescCol.setPrefWidth(200);
        
        TableColumn<Bill.BillItem, Double> itemAmountCol = new TableColumn<>("Amount");
        itemAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        itemAmountCol.setPrefWidth(100);
        itemAmountCol.setCellFactory(col -> new TableCell<Bill.BillItem, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        itemsTable.getColumns().addAll(itemDescCol, itemAmountCol);
        itemsTable.setItems(FXCollections.observableArrayList(selectedBill.getItems()));
        
        // Add all components to the content
        content.getChildren().addAll(
                basicInfoGrid,
                new Separator(),
                itemsLabel,
                itemsTable
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        
        // Show the dialog
        dialog.showAndWait();
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
     * Returns the root container for the billing screen
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
    public TableView<Bill> getBillsTable() {
        return billsTable;
    }
    
    public TextField getSearchField() {
        return searchField;
    }
    
    public Button getSearchButton() {
        return searchButton;
    }
    
    public Button getCreateBillButton() {
        return createBillButton;
    }
    
    public Button getAddItemButton() {
        return addItemButton;
    }
    
    public Button getViewDetailsButton() {
        return viewDetailsButton;
    }
    
    public Button getProcessPaymentButton() {
        return processPaymentButton;
    }
    
    public Button getRefreshButton() {
        return refreshButton;
    }
    
    public Button getBackButton() {
        return backButton;
    }
    
    public Label getStatusLabel() {
        return statusLabel;
    }
    
    public ComboBox<String> getStatusFilterComboBox() {
        return statusFilterComboBox;
    }
    
    public ComboBox<Patient> getPatientFilterComboBox() {
        return patientFilterComboBox;
    }
} 