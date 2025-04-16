package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.AppointmentService;
import com.example.hospitalsystemsimpletesting.service.AppointmentServiceImpl;
import com.example.hospitalsystemsimpletesting.service.MedicalRecordService;
import com.example.hospitalsystemsimpletesting.service.MedicalRecordServiceImpl;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.PatientServiceImpl;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Screen for medical records management functionality.
 * Allows creating, viewing, and updating medical records for patients.
 */
public class MedicalRecordsScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<MedicalRecord> recordsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button createButton;
    private final Button updateButton;
    private final Button viewButton;
    private final Button deleteButton;
    private final Button refreshButton;
    private final Button backButton;
    private final Label titleLabel;
    private final Label statusLabel;
    private final DatePicker datePicker;
    private final ComboBox<Patient> patientFilterComboBox;
    
    // Services for operations
    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    
    // Observable list to display in the table
    private final ObservableList<MedicalRecord> recordsData = FXCollections.observableArrayList();
    
    // Currently selected medical record
    private MedicalRecord selectedRecord;
    
    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    public MedicalRecordsScreen() {
        // Initialize services
        this.patientService = HospitalApplication.getPatientService();
        this.appointmentService = HospitalApplication.getAppointmentService();
        this.medicalRecordService = HospitalApplication.getMedicalRecordService();
        
        // Create title
        titleLabel = new Label("Medical Records Management");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create the table for medical records
        recordsTable = new TableView<>();
        recordsTable.setId("recordsTable");
        recordsTable.setPlaceholder(new Label("No medical records found"));
        
        // Define table columns
        // Patient column that shows patient's full name
        TableColumn<MedicalRecord, Patient> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientColumn.setPrefWidth(150);
        patientColumn.setCellFactory(column -> new TableCell<MedicalRecord, Patient>() {
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
        
        // Patient ID column as the primary identifier column
        TableColumn<MedicalRecord, Patient> patientIdColumn = new TableColumn<>("ID");
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientIdColumn.setPrefWidth(80);
        patientIdColumn.setCellFactory(column -> new TableCell<MedicalRecord, Patient>() {
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
        
        TableColumn<MedicalRecord, String> diagnosisColumn = new TableColumn<>("Diagnosis");
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        diagnosisColumn.setPrefWidth(200);
        
        // Date column with formatted date
        TableColumn<MedicalRecord, LocalDate> dateColumn = new TableColumn<>("Record Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("recordDate"));
        dateColumn.setPrefWidth(120);
        dateColumn.setCellFactory(column -> new TableCell<MedicalRecord, LocalDate>() {
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
        
        // Related appointment column
        TableColumn<MedicalRecord, Appointment> appointmentColumn = new TableColumn<>("Related Appointment");
        appointmentColumn.setCellValueFactory(new PropertyValueFactory<>("relatedAppointment"));
        appointmentColumn.setPrefWidth(150);
        appointmentColumn.setCellFactory(column -> new TableCell<MedicalRecord, Appointment>() {
            @Override
            protected void updateItem(Appointment appointment, boolean empty) {
                super.updateItem(appointment, empty);
                if (empty || appointment == null) {
                    setText(null);
                } else {
                    setText(appointment.getId() + " (" + appointment.getType() + ")");
                }
            }
        });
        
        // Notes column (truncated)
        TableColumn<MedicalRecord, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesColumn.setPrefWidth(200);
        notesColumn.setCellFactory(column -> new TableCell<MedicalRecord, String>() {
            @Override
            protected void updateItem(String notes, boolean empty) {
                super.updateItem(notes, empty);
                if (empty || notes == null) {
                    setText(null);
                } else {
                    // Truncate long notes for display
                    setText(notes.length() > 30 ? notes.substring(0, 27) + "..." : notes);
                }
            }
        });
        
        // Add columns to the table
        recordsTable.getColumns().addAll(
                patientIdColumn, patientColumn, diagnosisColumn, dateColumn, notesColumn, appointmentColumn
        );
        
        // Set up table selection listener
        recordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedRecord = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        recordsTable.setItems(recordsData);
        
        // Create search and filter controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Search by diagnosis or notes");
        searchField.setPrefWidth(250);
        
        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> searchRecords());
        
        // Create date picker for filtering by date
        datePicker = new DatePicker();
        datePicker.setId("datePicker");
        datePicker.setPromptText("Filter by date");
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                filterRecordsByDate(newDate);
            } else {
                refreshRecordsList();
            }
        });
        
        // Create dropdown for filtering by patient
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
                filterRecordsByPatient(newValue);
            } else {
                refreshRecordsList();
            }
        });
        
        // Reset search when text field is cleared
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshRecordsList();
            }
        });
        
        searchBox.getChildren().addAll(searchField, searchButton, datePicker, patientFilterComboBox);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        createButton = new Button("Create New Record");
        createButton.setId("createButton");
        createButton.setOnAction(e -> showCreateRecordDialog());
        
        updateButton = new Button("Update Record");
        updateButton.setId("updateButton");
        updateButton.setOnAction(e -> showUpdateRecordDialog());
        updateButton.setDisable(true);
        
        viewButton = new Button("View Details");
        viewButton.setId("viewButton");
        viewButton.setOnAction(e -> showRecordDetailsDialog());
        viewButton.setDisable(true);
        
        deleteButton = new Button("Delete Record");
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(e -> deleteSelectedRecord());
        deleteButton.setDisable(true);
        
        refreshButton = new Button("Refresh");
        refreshButton.setId("refreshButton");
        refreshButton.setOnAction(e -> refreshRecordsList());
        
        buttonBox.getChildren().addAll(createButton, updateButton, viewButton, deleteButton, refreshButton);
        
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
        tableContainer.getChildren().addAll(recordsTable);
        tableContainer.setVgrow(recordsTable, Priority.ALWAYS);
        
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
        refreshRecordsList();
        
        // Add some sample data for demonstration
        if (recordsData.isEmpty()) {
            addSampleData();
        }
    }
    
    /**
     * Add sample medical records for demonstration
     */
    private void addSampleData() {
        try {
            // First, ensure we have some patients to work with
            if (patientService.getAllPatients().isEmpty()) {
                // Create sample patients
                patientService.registerPatient("John Doe", LocalDate.of(1980, 5, 15), "Male", "555-1234", "123 Main St");
                patientService.registerPatient("Jane Smith", LocalDate.of(1992, 8, 22), "Female", "555-5678", "456 Oak Ave");
                patientService.registerPatient("Robert Johnson", LocalDate.of(1975, 12, 3), "Male", "555-9012", "789 Pine Blvd");
                
                // Get the patients we just created to create medical records for them
                List<Patient> patients = patientService.getAllPatients();
                if (patients.size() >= 3) {
                    // Create sample medical records
                    LocalDate today = LocalDate.now();
                    LocalDate yesterday = today.minusDays(1);
                    LocalDate lastWeek = today.minusWeeks(1);
                    
                    // Create medical records
                    MedicalRecord record1 = medicalRecordService.createMedicalRecord(
                            "MR001", 
                            patients.get(0), 
                            "Influenza Type A", 
                            yesterday
                    );
                    medicalRecordService.addNotes(record1.getId(), "Patient presented with fever, cough, and body aches. Prescribed Tamiflu and rest.");
                    
                    MedicalRecord record2 = medicalRecordService.createMedicalRecord(
                            "MR002", 
                            patients.get(1), 
                            "Annual Physical Examination", 
                            today
                    );
                    medicalRecordService.addNotes(record2.getId(), "All vitals within normal range. Recommended lifestyle changes including increased exercise.");
                    
                    MedicalRecord record3 = medicalRecordService.createMedicalRecord(
                            "MR003", 
                            patients.get(patients.size() > 2 ? 2 : 0), 
                            "Hypertension Follow-up", 
                            lastWeek
                    );
                    medicalRecordService.addNotes(record3.getId(), "Blood pressure slightly elevated at 140/90. Adjusted medication dosage and scheduled follow-up in 2 weeks.");
                    
                    refreshRecordsList();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error Adding Sample Data", "Could not add sample medical records: " + e.getMessage());
        }
    }
    
    /**
     * Search for medical records by diagnosis or notes
     */
    private void searchRecords() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshRecordsList();
            return;
        }
        
        // Create a new variable for the lowercase version
        final String queryLower = query.toLowerCase();
        
        List<MedicalRecord> allRecords = medicalRecordService.getAllMedicalRecords();
        List<MedicalRecord> filteredRecords = allRecords.stream()
                .filter(record -> {
                    String diagnosis = record.getDiagnosis();
                    String notes = record.getNotes();
                    
                    return (diagnosis != null && diagnosis.toLowerCase().contains(queryLower)) ||
                           (notes != null && notes.toLowerCase().contains(queryLower));
                })
                .collect(Collectors.toList());
        
        recordsData.clear();
        recordsData.addAll(filteredRecords);
        updateStatusLabel("Found " + recordsData.size() + " record(s) matching '" + query + "'");
    }
    
    /**
     * Filter medical records by date
     */
    private void filterRecordsByDate(LocalDate date) {
        if (date == null) {
            refreshRecordsList();
            return;
        }
        
        List<MedicalRecord> recordsOnDate = medicalRecordService.findByRecordDate(date);
        recordsData.clear();
        recordsData.addAll(recordsOnDate);
        updateStatusLabel("Showing " + recordsData.size() + " record(s) on " + dateFormatter.format(date));
    }
    
    /**
     * Filter medical records by patient
     */
    private void filterRecordsByPatient(Patient patient) {
        if (patient == null) {
            refreshRecordsList();
            return;
        }
        
        List<MedicalRecord> patientRecords = medicalRecordService.findByPatientId(patient.getId());
        recordsData.clear();
        recordsData.addAll(patientRecords);
        updateStatusLabel("Showing " + recordsData.size() + " record(s) for " + patient.getFullName());
    }
    
    /**
     * Refresh the medical records list from the service
     */
    private void refreshRecordsList() {
        recordsData.clear();
        recordsData.addAll(medicalRecordService.getAllMedicalRecords());
        datePicker.setValue(null);
        patientFilterComboBox.setValue(null);
        updateStatusLabel("Showing all " + recordsData.size() + " medical record(s)");
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
        boolean hasSelection = selectedRecord != null;
        updateButton.setDisable(!hasSelection);
        viewButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }
    
    /**
     * Delete the selected medical record
     */
    private void deleteSelectedRecord() {
        if (selectedRecord == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Medical Record");
        alert.setHeaderText("Delete Medical Record");
        alert.setContentText("Are you sure you want to delete this medical record? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = medicalRecordService.deleteMedicalRecord(selectedRecord.getId());
            if (deleted) {
                refreshRecordsList();
                updateStatusLabel("Medical record deleted successfully");
            } else {
                showErrorAlert("Delete Failed", "Could not delete the selected medical record.");
            }
        }
    }
    
    /**
     * Display dialog to create a new medical record
     */
    private void showCreateRecordDialog() {
        // Create dialog
        Dialog<MedicalRecord> dialog = new Dialog<>();
        dialog.setTitle("Create New Medical Record");
        dialog.setHeaderText("Enter medical record details");
        
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
        patientComboBox.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
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
        
        // Diagnosis field
        TextField diagnosisField = new TextField();
        diagnosisField.setPromptText("Enter Diagnosis");
        
        // Date picker
        DatePicker recordDatePicker = new DatePicker();
        recordDatePicker.setPromptText("Record Date");
        recordDatePicker.setValue(LocalDate.now()); // Default to today
        
        // Notes field
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter Notes (Optional)");
        notesArea.setPrefRowCount(4);
        
        // Related appointment selection (optional)
        ComboBox<Appointment> appointmentComboBox = new ComboBox<>();
        appointmentComboBox.setPromptText("Select Related Appointment (Optional)");
        appointmentComboBox.setConverter(new StringConverter<Appointment>() {
            @Override
            public String toString(Appointment appointment) {
                if (appointment == null) {
                    return "";
                }
                return appointment.getType() + " on " + 
                      dateFormatter.format(appointment.getDateTime().toLocalDate()) + 
                      " (ID: " + appointment.getId() + ")";
            }
            
            @Override
            public Appointment fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Update appointment options when patient changes
        patientComboBox.valueProperty().addListener((obs, oldPatient, newPatient) -> {
            if (newPatient != null) {
                // Load appointments for the selected patient
                List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(newPatient.getId());
                appointmentComboBox.setItems(FXCollections.observableArrayList(appointments));
            } else {
                appointmentComboBox.getItems().clear();
            }
        });
        
        // Add fields to grid
        grid.add(new Label("Patient:"), 0, 0);
        grid.add(patientComboBox, 1, 0);
        grid.add(new Label("Diagnosis:"), 0, 1);
        grid.add(diagnosisField, 1, 1);
        grid.add(new Label("Record Date:"), 0, 2);
        grid.add(recordDatePicker, 1, 2);
        grid.add(new Label("Related Appointment:"), 0, 3);
        grid.add(appointmentComboBox, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    Patient patient = patientComboBox.getValue();
                    String diagnosis = diagnosisField.getText();
                    LocalDate recordDate = recordDatePicker.getValue();
                    Appointment appointment = appointmentComboBox.getValue();
                    String notes = notesArea.getText();
                    
                    // Validate inputs
                    if (patient == null) {
                        showErrorAlert("Invalid Input", "Please select a patient.");
                        return null;
                    }
                    
                    if (diagnosis == null || diagnosis.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please enter a diagnosis.");
                        return null;
                    }
                    
                    if (recordDate == null) {
                        showErrorAlert("Invalid Input", "Please select a record date.");
                        return null;
                    }
                    
                    // Generate a unique ID
                    String recordId = "MR-" + UUID.randomUUID().toString().substring(0, 8);
                    
                    // Create the medical record
                    MedicalRecord record = medicalRecordService.createMedicalRecord(recordId, patient, diagnosis, recordDate);
                    
                    // Add notes if provided
                    if (notes != null && !notes.trim().isEmpty()) {
                        medicalRecordService.addNotes(record.getId(), notes);
                    }
                    
                    // Link to appointment if selected
                    if (appointment != null) {
                        medicalRecordService.linkToAppointment(record.getId(), appointment.getId());
                    }
                    
                    return record;
                } catch (IllegalArgumentException | IllegalStateException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<MedicalRecord> result = dialog.showAndWait();
        result.ifPresent(record -> {
            refreshRecordsList();
            updateStatusLabel("Created new medical record for " + record.getPatient().getFullName());
            
            // Select the newly created record
            recordsTable.getSelectionModel().select(record);
        });
    }
    
    /**
     * Display dialog to update an existing medical record
     */
    private void showUpdateRecordDialog() {
        if (selectedRecord == null) {
            return;
        }
        
        // Create dialog
        Dialog<MedicalRecord> dialog = new Dialog<>();
        dialog.setTitle("Update Medical Record");
        dialog.setHeaderText("Update medical record for " + selectedRecord.getPatient().getFullName());
        
        // Set button types
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Diagnosis field
        TextField diagnosisField = new TextField(selectedRecord.getDiagnosis());
        diagnosisField.setPromptText("Enter Diagnosis");
        
        // Notes field
        TextArea notesArea = new TextArea(selectedRecord.getNotes());
        notesArea.setPromptText("Enter Notes");
        notesArea.setPrefRowCount(4);
        
        // Related appointment selection (optional)
        ComboBox<Appointment> appointmentComboBox = new ComboBox<>();
        appointmentComboBox.setPromptText("Select Related Appointment (Optional)");
        
        // Load appointments for the patient
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(selectedRecord.getPatient().getId());
        appointmentComboBox.setItems(FXCollections.observableArrayList(appointments));
        
        // Set the current appointment if any
        if (selectedRecord.getRelatedAppointment() != null) {
            appointmentComboBox.setValue(selectedRecord.getRelatedAppointment());
        }
        
        appointmentComboBox.setConverter(new StringConverter<Appointment>() {
            @Override
            public String toString(Appointment appointment) {
                if (appointment == null) {
                    return "";
                }
                return appointment.getType() + " on " + 
                      dateFormatter.format(appointment.getDateTime().toLocalDate()) + 
                      " (ID: " + appointment.getId() + ")";
            }
            
            @Override
            public Appointment fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Add fields to grid
        grid.add(new Label("Patient:"), 0, 0);
        grid.add(new Label(selectedRecord.getPatient().getFullName()), 1, 0);
        grid.add(new Label("Record Date:"), 0, 1);
        grid.add(new Label(dateFormatter.format(selectedRecord.getRecordDate())), 1, 1);
        grid.add(new Label("Diagnosis:"), 0, 2);
        grid.add(diagnosisField, 1, 2);
        grid.add(new Label("Related Appointment:"), 0, 3);
        grid.add(appointmentComboBox, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    String diagnosis = diagnosisField.getText();
                    Appointment appointment = appointmentComboBox.getValue();
                    String notes = notesArea.getText();
                    
                    // Validate inputs
                    if (diagnosis == null || diagnosis.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please enter a diagnosis.");
                        return null;
                    }
                    
                    // Update the diagnosis if changed
                    if (!diagnosis.equals(selectedRecord.getDiagnosis())) {
                        medicalRecordService.updateDiagnosis(selectedRecord.getId(), diagnosis);
                    }
                    
                    // Update notes if provided
                    if (notes != null && !notes.equals(selectedRecord.getNotes())) {
                        // Clear existing notes by creating a new record with same properties
                        MedicalRecord updatedRecord = new MedicalRecord(
                                selectedRecord.getId(),
                                selectedRecord.getPatient(),
                                diagnosis,
                                selectedRecord.getRecordDate()
                        );
                        updatedRecord.setRelatedAppointment(selectedRecord.getRelatedAppointment());
                        
                        // Save the record and add new notes if not empty
                        medicalRecordService.saveMedicalRecord(updatedRecord);
                        if (!notes.trim().isEmpty()) {
                            medicalRecordService.addNotes(selectedRecord.getId(), notes);
                        }
                    }
                    
                    // Link to appointment if selected and different from current
                    if (appointment != null && (selectedRecord.getRelatedAppointment() == null || 
                            !appointment.getId().equals(selectedRecord.getRelatedAppointment().getId()))) {
                        medicalRecordService.linkToAppointment(selectedRecord.getId(), appointment.getId());
                    }
                    
                    // Return updated record
                    return medicalRecordService.findById(selectedRecord.getId()).orElse(null);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<MedicalRecord> result = dialog.showAndWait();
        result.ifPresent(record -> {
            refreshRecordsList();
            updateStatusLabel("Updated medical record for " + record.getPatient().getFullName());
            
            // Keep the selection on the updated record
            recordsTable.getSelectionModel().select(record);
        });
    }
    
    /**
     * Display dialog to view medical record details
     */
    private void showRecordDetailsDialog() {
        if (selectedRecord == null) {
            return;
        }
        
        // Create dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Medical Record Details");
        dialog.setHeaderText("Details for Medical Record #" + selectedRecord.getId());
        
        // Set button types
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Add fields
        int row = 0;
        
        grid.add(new Label("Patient ID:"), 0, row);
        grid.add(new Label(selectedRecord.getPatient().getId()), 1, row++);
        
        grid.add(new Label("Patient Name:"), 0, row);
        grid.add(new Label(selectedRecord.getPatient().getFullName()), 1, row++);
        
        grid.add(new Label("Record Date:"), 0, row);
        grid.add(new Label(dateFormatter.format(selectedRecord.getRecordDate())), 1, row++);
        
        grid.add(new Label("Diagnosis:"), 0, row);
        grid.add(new Label(selectedRecord.getDiagnosis()), 1, row++);
        
        if (selectedRecord.getRelatedAppointment() != null) {
            Appointment appointment = selectedRecord.getRelatedAppointment();
            
            grid.add(new Label("Related Appointment:"), 0, row);
            grid.add(new Label(appointment.getId()), 1, row++);
            
            grid.add(new Label("Appointment Type:"), 0, row);
            grid.add(new Label(appointment.getType()), 1, row++);
            
            grid.add(new Label("Appointment Date:"), 0, row);
            grid.add(new Label(dateFormatter.format(appointment.getDateTime().toLocalDate())), 1, row++);
            
            grid.add(new Label("Appointment Status:"), 0, row);
            grid.add(new Label(appointment.getStatus().toString()), 1, row++);
        }
        
        grid.add(new Label("Notes:"), 0, row);
        
        TextArea notesArea = new TextArea(selectedRecord.getNotes());
        notesArea.setEditable(false);
        notesArea.setPrefRowCount(5);
        notesArea.setPrefColumnCount(30);
        notesArea.setWrapText(true);
        
        grid.add(notesArea, 0, row + 1, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
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
     * Returns the root container for the medical records screen
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
    public TableView<MedicalRecord> getRecordsTable() {
        return recordsTable;
    }
    
    public TextField getSearchField() {
        return searchField;
    }
    
    public Button getSearchButton() {
        return searchButton;
    }
    
    public Button getCreateButton() {
        return createButton;
    }
    
    public Button getUpdateButton() {
        return updateButton;
    }
    
    public Button getViewButton() {
        return viewButton;
    }
    
    public Button getDeleteButton() {
        return deleteButton;
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
    
    public DatePicker getDatePicker() {
        return datePicker;
    }
    
    public ComboBox<Patient> getPatientFilterComboBox() {
        return patientFilterComboBox;
    }
} 