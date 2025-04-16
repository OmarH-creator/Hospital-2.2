package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.PatientService;

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
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Screen for patient registration and management functionality.
 * Allows registering new patients, editing patient information, and managing admissions.
 */
public class PatientRegistrationScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<Patient> patientsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button registerButton;
    private final Button editButton;
    private final Button admitDischargeButton;
    private final Button deleteButton;
    private final Button backButton;
    private final Label titleLabel;
    private final Label statusLabel;
    private final ComboBox<String> searchTypeComboBox;
    
    // Service for patient operations
    private final PatientService patientService;
    
    // Observable list to display in the table
    private final ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    
    // Currently selected patient
    private Patient selectedPatient;
    
    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    // Blood type options
    private final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};
    
    // Flag to track if we've registered a shutdown hook already
    private static boolean shutdownHookRegistered = false;
    
    public PatientRegistrationScreen() {
        // Initialize the patient service
        this.patientService = HospitalApplication.getPatientService();
        
        // Create title
        titleLabel = new Label("Patient Registration");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create the table for patients
        patientsTable = new TableView<>();
        patientsTable.setId("patientsTable");
        patientsTable.setPlaceholder(new Label("No patients registered"));
        
        // Define table columns
        TableColumn<Patient, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        idColumn.setCellFactory(column -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(id);
                    setStyle("-fx-font-weight: bold;"); // Make ID stand out visually
                }
            }
        });
        
        TableColumn<Patient, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameColumn.setPrefWidth(150);
        
        TableColumn<Patient, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameColumn.setPrefWidth(150);
        
        // Create a column for date of birth that displays it as a formatted string
        TableColumn<Patient, LocalDate> dobColumn = new TableColumn<>("Date of Birth");
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dobColumn.setPrefWidth(120);
        dobColumn.setCellFactory(column -> new TableCell<Patient, LocalDate>() {
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
        
        // Create a column for age (calculated property)
        TableColumn<Patient, Integer> ageColumn = new TableColumn<>("Age");
        ageColumn.setCellValueFactory(cellData -> {
            Patient patient = cellData.getValue();
            // Use a wrapper to convert int to IntegerProperty
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(patient.getAge());
        });
        ageColumn.setPrefWidth(60);
        
        // Create a column for blood type
        TableColumn<Patient, String> bloodTypeColumn = new TableColumn<>("Blood Type");
        bloodTypeColumn.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        bloodTypeColumn.setPrefWidth(100);
        
        // Create a column for admission status
        TableColumn<Patient, Boolean> admissionColumn = new TableColumn<>("Status");
        admissionColumn.setCellValueFactory(new PropertyValueFactory<>("admitted"));
        admissionColumn.setPrefWidth(100);
        admissionColumn.setCellFactory(column -> new TableCell<Patient, Boolean>() {
            @Override
            protected void updateItem(Boolean admitted, boolean empty) {
                super.updateItem(admitted, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (admitted != null && admitted) {
                    setText("ADMITTED");
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    setText("OUTPATIENT");
                    setStyle("-fx-text-fill: #2ecc71;");
                }
            }
        });
        
        // Add columns to the table
        patientsTable.getColumns().addAll(
                idColumn, 
                firstNameColumn, 
                lastNameColumn, 
                dobColumn,
                ageColumn,
                bloodTypeColumn,
                admissionColumn
        );
        
        // Set up table selection listener
        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedPatient = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        patientsTable.setItems(patientsData);
        
        // Create search controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchTypeComboBox = new ComboBox<>();
        searchTypeComboBox.setItems(FXCollections.observableArrayList("ID", "Name"));
        searchTypeComboBox.setValue("Name");
        searchTypeComboBox.setId("searchTypeComboBox");
        
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Search patients");
        searchField.setPrefWidth(250);
        
        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> searchPatients());
        
        // Reset search when text field is cleared
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshPatientsList();
            }
        });
        
        searchBox.getChildren().addAll(searchTypeComboBox, searchField, searchButton);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        registerButton = new Button("Register Patient");
        registerButton.setId("registerButton");
        registerButton.setOnAction(e -> showRegisterPatientDialog());
        
        editButton = new Button("Edit Patient");
        editButton.setId("editButton");
        editButton.setOnAction(e -> showEditPatientDialog());
        editButton.setDisable(true);
        
        admitDischargeButton = new Button("Admit/Discharge");
        admitDischargeButton.setId("admitDischargeButton");
        admitDischargeButton.setOnAction(e -> togglePatientAdmission());
        admitDischargeButton.setDisable(true);
        
        deleteButton = new Button("Delete Patient");
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(e -> deleteSelectedPatient());
        deleteButton.setDisable(true);
        
        backButton = new Button("Back to Main Menu");
        backButton.setId("backButton");
        
        buttonBox.getChildren().addAll(registerButton, editButton, admitDischargeButton, deleteButton, backButton);
        
        // Status label for feedback
        statusLabel = new Label("");
        statusLabel.setId("statusLabel");
        
        // Arrange components in containers
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(titleLabel, searchBox);
        
        VBox tableContainer = new VBox(15);
        tableContainer.getChildren().addAll(patientsTable);
        tableContainer.setVgrow(patientsTable, Priority.ALWAYS);
        
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(buttonBox, statusLabel);
        
        // Main container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.getChildren().addAll(headerBox, tableContainer, bottomBox);
        mainContent.setVgrow(tableContainer, Priority.ALWAYS);
        
        // Root container
        rootContainer = new BorderPane();
        rootContainer.setCenter(mainContent);
        
        // Load initial data
        refreshPatientsList();
        
        // Add some sample data for demonstration
        if (patientsData.isEmpty()) {
            addSampleData();
        }
    }
    
    /**
     * Add sample patients for demonstration
     */
    private void addSampleData() {
        // Create and register sample patients with different properties
        
        // Register John Doe
        Patient p1 = patientService.registerPatient(
            "John Doe", 
            LocalDate.of(1980, 5, 15),
            "Male",
            "555-123-4567",
            "123 Main St, Springfield"
        );
        p1.setBloodType("A+");
        
        // Register Jane Smith
        Patient p2 = patientService.registerPatient(
            "Jane Smith", 
            LocalDate.of(1992, 8, 22),
            "Female",
            "555-987-6543",
            "456 Oak Ave, Springfield"
        );
        p2.setBloodType("O-");
        p2.admit(); // This patient is admitted
        
        // Register Robert Johnson
        Patient p3 = patientService.registerPatient(
            "Robert Johnson", 
            LocalDate.of(1975, 12, 3),
            "Male",
            "555-456-7890",
            "789 Pine St, Springfield"
        );
        p3.setBloodType("B+");
        
        refreshPatientsList();
    }
    
    /**
     * Search for patients by ID or name
     */
    private void searchPatients() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshPatientsList();
            return;
        }
        
        List<Patient> allPatients = patientService.getAllPatients();
        List<Patient> filteredPatients;
        
        // Filter based on search type
        if (searchTypeComboBox.getValue().equals("ID")) {
            // Search by ID
            filteredPatients = allPatients.stream()
                    .filter(p -> p.getId().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // Search by name (first or last)
            filteredPatients = allPatients.stream()
                    .filter(p -> 
                            p.getFirstName().toLowerCase().contains(query.toLowerCase()) || 
                            p.getLastName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        patientsData.clear();
        patientsData.addAll(filteredPatients);
        updateStatusLabel("Found " + patientsData.size() + " patient(s) matching '" + query + "'");
    }
    
    /**
     * Refresh the patients list from the service
     */
    private void refreshPatientsList() {
        patientsData.clear();
        patientsData.addAll(patientService.getAllPatients());
        updateStatusLabel("Showing all " + patientsData.size() + " patient(s)");
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
        boolean hasSelection = selectedPatient != null;
        editButton.setDisable(!hasSelection);
        admitDischargeButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        
        if (hasSelection) {
            admitDischargeButton.setText(selectedPatient.isAdmitted() ? "Discharge Patient" : "Admit Patient");
        }
    }
    
    /**
     * Toggle a patient's admission status
     */
    private void togglePatientAdmission() {
        if (selectedPatient == null) {
            return;
        }
        
        String message;
        
        try {
            if (selectedPatient.isAdmitted()) {
                patientService.dischargePatient(selectedPatient.getId());
                message = "Discharged patient: " + selectedPatient.getFullName();
            } else {
                patientService.admitPatient(selectedPatient.getId());
                message = "Admitted patient: " + selectedPatient.getFullName();
            }
            
            refreshPatientsList();
            updateStatusLabel(message);
            
            // Keep the same patient selected after refresh
            for (Patient p : patientsData) {
                if (p.getId().equals(selectedPatient.getId())) {
                    patientsTable.getSelectionModel().select(p);
                    break;
                }
            }
        } catch (Exception e) {
            showErrorAlert("Operation Failed", "Unable to update patient admission status: " + e.getMessage());
        }
    }
    
    /**
     * Delete the selected patient after confirmation
     */
    private void deleteSelectedPatient() {
        if (selectedPatient == null) {
            System.out.println("DEBUG: No patient selected for deletion");
            return;
        }
        
        System.out.println("DEBUG: Attempting to delete patient: " + selectedPatient.getId() + " (" + selectedPatient.getFullName() + ")");
        
        // Create confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Patient");
        confirmDialog.setHeaderText("Delete Patient: " + selectedPatient.getFullName());
        confirmDialog.setContentText("Are you sure you want to delete this patient? This action cannot be undone.");
        
        // Show dialog and wait for response
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        // If confirmed, delete the patient
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("DEBUG: User confirmed deletion of patient: " + selectedPatient.getId());
            try {
                // First check if the patient actually exists in the service
                Optional<Patient> patientExists = patientService.findPatientById(selectedPatient.getId());
                if (!patientExists.isPresent()) {
                    System.out.println("DEBUG: Patient ID " + selectedPatient.getId() + " not found in patient service");
                    
                    // Show specific error about patient not found
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Delete Failed");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Could not delete the patient: ID " + selectedPatient.getId() + 
                            " does not exist in the system. This may indicate a data synchronization issue.");
                    errorAlert.showAndWait();
                    
                    // Refresh the UI to get current data
                    refreshPatientsList();
                    return;
                }
                
                // Call delete method directly now that it's implemented
                System.out.println("DEBUG: Calling patientService.deletePatient for ID: " + selectedPatient.getId());
                boolean deleted = patientService.deletePatient(selectedPatient.getId());
                System.out.println("DEBUG: Delete operation returned: " + deleted);
                
                if (deleted) {
                    // Store patient name before nullifying the reference
                    String patientName = selectedPatient.getFullName();
                    
                    // Update UI - refresh list and show status
                    System.out.println("DEBUG: Deletion successful, refreshing UI");
                    refreshPatientsList();
                    selectedPatient = null;
                    updateButtonStates();
                    updateStatusLabel("Patient deleted successfully: " + patientName);
                } else {
                    // The deletion might fail if the patient has dependencies
                    System.out.println("DEBUG: Deletion failed, showing error alert");
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Delete Failed");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Could not delete the selected patient. The patient may have appointments, bills, or medical records that must be deleted first.");
                    errorAlert.showAndWait();
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception during deletion: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Delete Failed", "Error deleting patient: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG: User cancelled deletion");
        }
    }
    
    /**
     * Display dialog to register a new patient
     */
    private void showRegisterPatientDialog() {
        // Create dialog
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Register New Patient");
        dialog.setHeaderText("Enter patient information");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField idField = new TextField();
        idField.setPromptText("Patient ID");
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        
        // Date picker for birth date
        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPromptText("Date of Birth");
        dateOfBirthPicker.setValue(LocalDate.now().minusYears(30)); // Default to 30 years ago
        
        // Blood type dropdown
        ComboBox<String> bloodTypeComboBox = new ComboBox<>();
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(BLOOD_TYPES));
        bloodTypeComboBox.setValue("Unknown");
        
        // Add fields to grid
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3);
        grid.add(dateOfBirthPicker, 1, 3);
        grid.add(new Label("Blood Type:"), 0, 4);
        grid.add(bloodTypeComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on ID field
        idField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String id = idField.getText().trim();
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate dob = dateOfBirthPicker.getValue();
                    String bloodType = bloodTypeComboBox.getValue();
                    
                    // Validate inputs
                    if (id.isEmpty()) {
                        showErrorAlert("Invalid Input", "Patient ID cannot be empty.");
                        return null;
                    }
                    
                    if (firstName.isEmpty()) {
                        showErrorAlert("Invalid Input", "First name cannot be empty.");
                        return null;
                    }
                    
                    if (lastName.isEmpty()) {
                        showErrorAlert("Invalid Input", "Last name cannot be empty.");
                        return null;
                    }
                    
                    if (dob == null) {
                        showErrorAlert("Invalid Input", "Date of birth is required.");
                        return null;
                    }
                    
                    // Check if ID already exists
                    Optional<Patient> existingPatient = patientService.findPatientById(id);
                    if (existingPatient.isPresent()) {
                        showErrorAlert("Duplicate ID", "A patient with ID " + id + " already exists.");
                        return null;
                    }
                    
                    // Default values for required fields that aren't collected in the dialog
                    String fullName = firstName + " " + lastName;
                    String gender = "Unknown"; // Default value
                    String contactNumber = ""; // Default value
                    String address = ""; // Default value
                    
                    // Register patient using the new method
                    Patient patient = patientService.registerPatient(fullName, dob, gender, contactNumber, address);
                    
                    // Set additional properties not covered by register
                    patient.setId(id); // Set the custom ID
                    patient.setBloodType(bloodType);
                    
                    // Force a save with the updated blood type
                    try {
                        System.out.println("DEBUG: Explicitly saving patient with blood type: " + bloodType);
                        
                        // Try direct blood type update first
                        try {
                            // Check if the direct blood type update method is available
                            java.lang.reflect.Method directUpdateMethod = 
                                patientService.getClass().getMethod("updatePatientBloodType", String.class, String.class);
                            
                            if (directUpdateMethod != null) {
                                System.out.println("DEBUG: Found direct blood type update method, using it for registration");
                                Boolean result = (Boolean)directUpdateMethod.invoke(patientService, patient.getId(), bloodType);
                                System.out.println("DEBUG: Direct blood type update result: " + result);
                                
                                if (result) {
                                    System.out.println("DEBUG: Direct blood type update was successful");
                                    diagnoseBloodTypePersistence(patient, "register-direct-update");
                                    return patient;
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            System.out.println("DEBUG: Direct blood type update method not available, continuing with regular update");
                        } catch (Exception e) {
                            System.out.println("DEBUG: Error using direct blood type update: " + e.getMessage());
                        }
                        
                        // Fallback to standard update
                        patientService.updatePatient(patient.getId(), 
                                                     patient.getName(), 
                                                     patient.getDateOfBirth(), 
                                                     patient.getGender(),
                                                     patient.getContactNumber(), 
                                                     patient.getAddress(),
                                                     bloodType);
                        
                        // Additional check to force persistence
                        diagnoseBloodTypePersistence(patient, "register");
                    } catch (Exception e) {
                        System.out.println("DEBUG: Error saving blood type on registration: " + e.getMessage());
                    }
                    
                    return patient;
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            refreshPatientsList();
            updateStatusLabel("Registered new patient: " + patient.getFullName());
            
            // Select the newly added patient
            patientsTable.getSelectionModel().select(patient);
        });
    }
    
    /**
     * Display dialog to edit an existing patient
     */
    private void showEditPatientDialog() {
        if (selectedPatient == null) {
            return;
        }
        
        // First check if the patient actually exists in the service
        Optional<Patient> patientExists = patientService.findPatientById(selectedPatient.getId());
        if (!patientExists.isPresent()) {
            System.out.println("DEBUG: Patient ID " + selectedPatient.getId() + " not found in patient service when editing");
            
            // Show specific error about patient not found
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Invalid Input");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Patient not found: " + selectedPatient.getId() + 
                    ". This may indicate a data synchronization issue.");
            errorAlert.showAndWait();
            
            // Refresh the UI to get current data
            refreshPatientsList();
            return;
        }
        
        // Create dialog
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Edit Patient");
        dialog.setHeaderText("Edit patient information");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Make the ID prominent
        Label idValueLabel = new Label(selectedPatient.getId());
        idValueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0056b3;");
        
        TextField firstNameField = new TextField(selectedPatient.getFirstName());
        
        TextField lastNameField = new TextField(selectedPatient.getLastName());
        
        // Date picker for birth date
        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setValue(selectedPatient.getDateOfBirth());
        
        // Blood type dropdown
        ComboBox<String> bloodTypeComboBox = new ComboBox<>();
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(BLOOD_TYPES));
        bloodTypeComboBox.setValue(selectedPatient.getBloodType() != null ? 
                selectedPatient.getBloodType() : "Unknown");
        
        // Add fields to grid
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idValueLabel, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3);
        grid.add(dateOfBirthPicker, 1, 3);
        grid.add(new Label("Blood Type:"), 0, 4);
        grid.add(bloodTypeComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on first name field
        firstNameField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate dob = dateOfBirthPicker.getValue();
                    String bloodType = bloodTypeComboBox.getValue();
                    
                    // Validate inputs
                    if (firstName.isEmpty()) {
                        showErrorAlert("Invalid Input", "First name cannot be empty.");
                        return null;
                    }
                    
                    if (lastName.isEmpty()) {
                        showErrorAlert("Invalid Input", "Last name cannot be empty.");
                        return null;
                    }
                    
                    if (dob == null) {
                        showErrorAlert("Invalid Input", "Date of birth is required.");
                        return null;
                    }
                    
                    try {
                        // Store original values for debugging
                        String oldBloodType = selectedPatient.getBloodType();
                        System.out.println("DEBUG: Attempting stronger fix for blood type persistence");
                        System.out.println("DEBUG: Current blood type: " + oldBloodType + ", New blood type: " + bloodType);
                        
                        // Get patient fields
                        String fullName = firstName + " " + lastName;
                        String gender = selectedPatient.getGender() != null ? selectedPatient.getGender() : "Unknown";
                        String contactNumber = selectedPatient.getContactNumber() != null ? selectedPatient.getContactNumber() : "";
                        String address = selectedPatient.getAddress() != null ? selectedPatient.getAddress() : "";
                        
                        // 1. Set blood type directly on selected patient first
                        System.out.println("DEBUG: Step 1 - Setting blood type on selected patient");
                        selectedPatient.setBloodType(bloodType);
                        
                        // Special case: Try to use direct blood type update if available
                        try {
                            // Check if the direct blood type update method is available
                            java.lang.reflect.Method directUpdateMethod = 
                                patientService.getClass().getMethod("updatePatientBloodType", String.class, String.class);
                            
                            if (directUpdateMethod != null) {
                                System.out.println("DEBUG: Found direct blood type update method, using it");
                                Boolean result = (Boolean)directUpdateMethod.invoke(patientService, selectedPatient.getId(), bloodType);
                                System.out.println("DEBUG: Direct blood type update result: " + result);
                                
                                if (result) {
                                    System.out.println("DEBUG: Direct blood type update was successful");
                                    // We can skip the normal update process
                                    diagnoseBloodTypePersistence(selectedPatient, "after-direct-update");
                                    return patientService.findPatientById(selectedPatient.getId()).orElse(selectedPatient);
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            System.out.println("DEBUG: Direct blood type update method not available, continuing with regular update");
                        } catch (Exception e) {
                            System.out.println("DEBUG: Error using direct blood type update: " + e.getMessage());
                        }
                        
                        // Continue with the regular update process if direct update wasn't available or failed
                        // 2. Update the patient and force save
                        System.out.println("DEBUG: Step 2 - Updating patient via service");
                        patientService.updatePatient(selectedPatient.getId(), fullName, dob, gender, contactNumber, address, bloodType);
                        
                        // 3. Get direct reference to patient in service
                        System.out.println("DEBUG: Step 3 - Getting reference to patient in service map");
                        Patient updatedPatient = patientService.findPatientById(selectedPatient.getId()).orElseThrow();
                        
                        // 4. Set blood type again on the service's copy
                        System.out.println("DEBUG: Step 4 - Setting blood type on service reference");
                        updatedPatient.setBloodType(bloodType);
                        
                        // 5. Use reflection to force save if needed
                        System.out.println("DEBUG: Step 5 - Attempting to force save via reflection");
                        try {
                            // Try to access the savePatients method via reflection
                            java.lang.reflect.Method saveMethod = patientService.getClass().getDeclaredMethod("savePatients");
                            saveMethod.setAccessible(true);
                            saveMethod.invoke(patientService);
                            System.out.println("DEBUG: Successfully called savePatients via reflection");
                        } catch (Exception e) {
                            System.out.println("DEBUG: Reflection approach failed: " + e.getMessage());
                            // Fallback - update patient again to trigger save
                            patientService.updatePatient(selectedPatient.getId(), fullName, dob, gender, contactNumber, address, bloodType);
                        }
                        
                        // 6. Verify the patient has the correct blood type
                        Patient verifyPatient = patientService.findPatientById(selectedPatient.getId()).orElse(null);
                        if (verifyPatient != null) {
                            System.out.println("DEBUG: Verification - Patient blood type is now: " + verifyPatient.getBloodType());
                        }
                        
                        // 7. Let's also try a direct save to original patient service
                        try {
                            // Use reflection to get access to private field persistenceService
                            java.lang.reflect.Field persistenceField = patientService.getClass().getDeclaredField("persistenceService");
                            persistenceField.setAccessible(true);
                            Object persistenceService = persistenceField.get(patientService);
                            
                            if (persistenceService != null) {
                                System.out.println("DEBUG: Found persistence service, forcing direct save");
                                java.lang.reflect.Method savePatients = persistenceService.getClass().getMethod("savePatients", java.util.List.class);
                                java.util.List<Patient> patients = patientService.getAllPatients();
                                savePatients.invoke(persistenceService, patients);
                                System.out.println("DEBUG: Direct save to persistence service completed");
                                
                                // Diagnose after direct persistence save
                                diagnoseBloodTypePersistence(updatedPatient, "after-direct-save");
                            }
                        } catch (Exception e) {
                            System.out.println("DEBUG: Advanced persistence approach failed: " + e.getMessage());
                        }
                        
                        // Now register a shutdown hook to save on exit - only once
                        if (!shutdownHookRegistered) {
                            try {
                                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                    System.out.println("DEBUG: Shutdown hook triggered - final save attempt");
                                    try {
                                        java.lang.reflect.Method saveMethod = patientService.getClass().getDeclaredMethod("savePatients");
                                        saveMethod.setAccessible(true);
                                        saveMethod.invoke(patientService);
                                        System.out.println("DEBUG: Final save completed");
                                    } catch (Exception ex) {
                                        System.out.println("DEBUG: Final save failed: " + ex.getMessage());
                                    }
                                }));
                                shutdownHookRegistered = true;
                                System.out.println("DEBUG: Registered shutdown hook for final save");
                            } catch (Exception e) {
                                System.out.println("DEBUG: Could not register shutdown hook: " + e.getMessage());
                            }
                        } else {
                            System.out.println("DEBUG: Shutdown hook already registered");
                        }
                        
                        return updatedPatient;
                    } catch (Exception e) {
                        showErrorAlert("Update Failed", "Failed to update patient: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            refreshPatientsList();
            updateStatusLabel("Updated patient: " + patient.getFullName());
            
            // Keep the same patient selected after refresh
            for (Patient p : patientsData) {
                if (p.getId().equals(patient.getId())) {
                    patientsTable.getSelectionModel().select(p);
                    break;
                }
            }
        });
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
     * Returns the root container for the patient registration screen
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
    public TableView<Patient> getPatientsTable() {
        return patientsTable;
    }
    
    public TextField getSearchField() {
        return searchField;
    }
    
    public Button getSearchButton() {
        return searchButton;
    }
    
    public Button getRegisterButton() {
        return registerButton;
    }
    
    public Button getEditButton() {
        return editButton;
    }
    
    public Button getAdmitDischargeButton() {
        return admitDischargeButton;
    }
    
    public Button getDeleteButton() {
        return deleteButton;
    }
    
    public Button getBackButton() {
        return backButton;
    }
    
    public Label getStatusLabel() {
        return statusLabel;
    }
    
    public ComboBox<String> getSearchTypeComboBox() {
        return searchTypeComboBox;
    }
    
    /**
     * Diagnoses and verifies blood type persistence
     * @param patient The patient whose blood type we're verifying
     * @param action The action that triggered this check (e.g., "edit", "save")
     */
    private void diagnoseBloodTypePersistence(Patient patient, String action) {
        if (patient == null) {
            System.out.println("DEBUG: Cannot diagnose null patient");
            return;
        }
        
        System.out.println("--- BLOOD TYPE PERSISTENCE DIAGNOSIS [" + action + "] ---");
        System.out.println("Patient: " + patient.getFullName() + " (ID: " + patient.getId() + ")");
        System.out.println("Current blood type in memory: " + patient.getBloodType());
        System.out.println("Checking for data directory: " + new java.io.File("data").exists());
        
        // Add code to force create the data directory if it doesn't exist
        try {
            java.io.File dataDir = new java.io.File("data");
            if (!dataDir.exists()) {
                dataDir.mkdir();
                System.out.println("Created data directory: " + dataDir.getAbsolutePath());
            } else {
                System.out.println("Data directory exists at: " + dataDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Error checking/creating data directory: " + e.getMessage());
        }
        
        // Check if this patient exists in the database and compare values
        try {
            Optional<Patient> patientOptional = patientService.findPatientById(patient.getId());
            if (patientOptional.isPresent()) {
                Patient dbPatient = patientOptional.get();
                System.out.println("Found patient in service layer: " + dbPatient.getFullName());
                System.out.println("Blood type in service layer: " + dbPatient.getBloodType());
                
                // Are they the same object in memory?
                System.out.println("Same object reference? " + (patient == dbPatient));
                
                // Check if values are consistent
                if (!Objects.equals(patient.getBloodType(), dbPatient.getBloodType())) {
                    System.out.println("WARNING: Blood type mismatch between UI and service layer!");
                    
                    // Force a save directly to try to align the values
                    patientService.updatePatient(
                        patient.getId(), 
                        patient.getName(), 
                        patient.getDateOfBirth(), 
                        patient.getGender(), 
                        patient.getContactNumber(), 
                        patient.getAddress(),
                        patient.getBloodType()
                    );
                    System.out.println("Forced updatePatient call to align values");
                }
            } else {
                System.out.println("Patient not found in service layer!");
            }
            
            // Check direct persistence layer if possible
            try {
                java.lang.reflect.Field persistenceField = patientService.getClass().getDeclaredField("persistenceService");
                persistenceField.setAccessible(true);
                Object persistenceService = persistenceField.get(patientService);
                
                if (persistenceService != null) {
                    // Try to get the raw data from the persistence layer
                    java.lang.reflect.Method loadMethod = persistenceService.getClass().getMethod("loadPatients");
                    loadMethod.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<Patient> persistedPatients = (List<Patient>) loadMethod.invoke(persistenceService);
                    
                    Optional<Patient> persistedPatient = persistedPatients.stream()
                        .filter(p -> p.getId().equals(patient.getId()))
                        .findFirst();
                    
                    if (persistedPatient.isPresent()) {
                        System.out.println("Direct persistence check - found patient");
                        System.out.println("Blood type in persistence: " + persistedPatient.get().getBloodType());
                    } else {
                        System.out.println("Direct persistence check - patient not found!");
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not perform direct persistence check: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Error during diagnosis: " + e.getMessage());
        }
        
        System.out.println("--- END DIAGNOSIS ---");
    }
} 