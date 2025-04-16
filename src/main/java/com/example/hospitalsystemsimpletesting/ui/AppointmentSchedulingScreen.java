package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.AppointmentService;
import com.example.hospitalsystemsimpletesting.service.AppointmentServiceImpl;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Screen for appointment scheduling functionality.
 * Allows scheduling new appointments, viewing existing appointments, 
 * and managing appointment status.
 */
public class AppointmentSchedulingScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<Appointment> appointmentsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button scheduleButton;
    private final Button rescheduleButton;
    private final Button cancelButton;
    private final Button completeButton;
    private final Button refreshButton;
    private final Button backButton;
    private final Label titleLabel;
    private final Label statusLabel;
    private final DatePicker datePicker;
    private final ComboBox<String> statusFilterComboBox;
    
    // Services for operations
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    
    // Observable list to display in the table
    private final ObservableList<Appointment> appointmentsData = FXCollections.observableArrayList();
    
    // Currently selected appointment
    private Appointment selectedAppointment;
    
    // Date and time formatters for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    
    // Standard appointment types
    private final String[] APPOINTMENT_TYPES = {
        "General Check-up", "Specialist Consultation", "Vaccination", 
        "Surgery", "Follow-up", "Dental", "Eye Examination", "Physical Therapy"
    };
    
    // Standard appointment times
    private final LocalTime[] APPOINTMENT_TIMES = {
        LocalTime.of(9, 0), LocalTime.of(9, 30), 
        LocalTime.of(10, 0), LocalTime.of(10, 30),
        LocalTime.of(11, 0), LocalTime.of(11, 30),
        LocalTime.of(13, 0), LocalTime.of(13, 30),
        LocalTime.of(14, 0), LocalTime.of(14, 30),
        LocalTime.of(15, 0), LocalTime.of(15, 30),
        LocalTime.of(16, 0), LocalTime.of(16, 30)
    };
    
    public AppointmentSchedulingScreen() {
        // Initialize services
        this.patientService = HospitalApplication.getPatientService();
        this.appointmentService = HospitalApplication.getAppointmentService();
        
        // Create title
        titleLabel = new Label("Appointment Scheduling");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create the table for appointments
        appointmentsTable = new TableView<>();
        appointmentsTable.setId("appointmentsTable");
        appointmentsTable.setPlaceholder(new Label("No appointments scheduled"));
        
        // Define table columns
        TableColumn<Appointment, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);
        
        // Patient ID column as the primary identifier
        TableColumn<Appointment, Patient> patientIdColumn = new TableColumn<>("ID");
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientIdColumn.setPrefWidth(80);
        patientIdColumn.setCellFactory(column -> new TableCell<Appointment, Patient>() {
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
        
        // Patient column that shows patient's full name
        TableColumn<Appointment, Patient> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientColumn.setPrefWidth(150);
        patientColumn.setCellFactory(column -> new TableCell<Appointment, Patient>() {
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
        
        TableColumn<Appointment, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(150);
        
        // Date column that displays only the date part
        TableColumn<Appointment, LocalDateTime> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        dateColumn.setPrefWidth(100);
        dateColumn.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(dateTime));
                }
            }
        });
        
        // Time column that displays only the time part
        TableColumn<Appointment, LocalDateTime> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        timeColumn.setPrefWidth(100);
        timeColumn.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(timeFormatter.format(dateTime));
                }
            }
        });
        
        // Status column with color coding
        TableColumn<Appointment, Appointment.Status> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(100);
        statusColumn.setCellFactory(column -> new TableCell<Appointment, Appointment.Status>() {
            @Override
            protected void updateItem(Appointment.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    switch (status) {
                        case SCHEDULED:
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green
                            break;
                        case COMPLETED:
                            setStyle("-fx-text-fill: #3498db;"); // Blue
                            break;
                        case CANCELLED:
                            setStyle("-fx-text-fill: #e74c3c;"); // Red
                            break;
                        case RESCHEDULED:
                            setStyle("-fx-text-fill: #f39c12;"); // Orange
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Add columns to the table
        appointmentsTable.getColumns().addAll(
                patientIdColumn, patientColumn, typeColumn, dateColumn, timeColumn, statusColumn
        );
        
        // Set up table selection listener
        appointmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAppointment = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        appointmentsTable.setItems(appointmentsData);
        
        // Create search and filter controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Search by patient name");
        searchField.setPrefWidth(250);
        
        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> searchAppointments());
        
        // Create date picker for filtering by date
        datePicker = new DatePicker();
        datePicker.setId("datePicker");
        datePicker.setPromptText("Filter by date");
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                filterAppointmentsByDate(newDate);
            } else {
                refreshAppointmentsList();
            }
        });
        
        // Create dropdown for filtering by status
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.setId("statusFilterComboBox");
        statusFilterComboBox.setPromptText("Filter by status");
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All",
                Appointment.Status.SCHEDULED.toString(),
                Appointment.Status.COMPLETED.toString(),
                Appointment.Status.CANCELLED.toString(),
                Appointment.Status.RESCHEDULED.toString()
        ));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("All")) {
                filterAppointmentsByStatus(newValue);
            } else {
                refreshAppointmentsList();
            }
        });
        
        // Reset search when text field is cleared
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshAppointmentsList();
            }
        });
        
        searchBox.getChildren().addAll(searchField, searchButton, datePicker, statusFilterComboBox);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        scheduleButton = new Button("Schedule New Appointment");
        scheduleButton.setId("scheduleButton");
        scheduleButton.setOnAction(e -> showScheduleAppointmentDialog());
        
        rescheduleButton = new Button("Reschedule");
        rescheduleButton.setId("rescheduleButton");
        rescheduleButton.setOnAction(e -> showRescheduleAppointmentDialog());
        rescheduleButton.setDisable(true);
        
        cancelButton = new Button("Cancel Appointment");
        cancelButton.setId("cancelButton");
        cancelButton.setOnAction(e -> cancelSelectedAppointment());
        cancelButton.setDisable(true);
        
        completeButton = new Button("Mark Completed");
        completeButton.setId("completeButton");
        completeButton.setOnAction(e -> completeSelectedAppointment());
        completeButton.setDisable(true);
        
        refreshButton = new Button("Refresh");
        refreshButton.setId("refreshButton");
        refreshButton.setOnAction(e -> refreshAppointmentsList());
        
        buttonBox.getChildren().addAll(scheduleButton, rescheduleButton, cancelButton, completeButton, refreshButton);
        
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
        tableContainer.getChildren().addAll(appointmentsTable);
        tableContainer.setVgrow(appointmentsTable, Priority.ALWAYS);
        
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
        refreshAppointmentsList();
        
        // Add some sample data for demonstration
        if (appointmentsData.isEmpty()) {
            addSampleData();
        }
    }
    
    /**
     * Add sample appointments for demonstration
     */
    private void addSampleData() {
        try {
            // First, ensure we have some patients to work with
            if (patientService.getAllPatients().isEmpty()) {
                // Create sample patients with proper data
                patientService.registerPatient("John Doe", LocalDate.of(1980, 5, 15), "Male", "555-1234", "123 Main St");
                patientService.registerPatient("Jane Smith", LocalDate.of(1992, 8, 22), "Female", "555-5678", "456 Oak Ave");
                patientService.registerPatient("Robert Johnson", LocalDate.of(1975, 12, 3), "Male", "555-9012", "789 Pine Blvd");
            }
            
            // Get the patients for our appointments
            List<Patient> patients = patientService.getAllPatients();
            if (!patients.isEmpty()) {
                // Create sample appointments
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime tomorrow = now.plusDays(1);
                LocalDateTime nextWeek = now.plusDays(7);
                
                // Schedule variety of appointments
                appointmentService.scheduleAppointment(
                        patients.get(0), 
                        "General Check-up", 
                        tomorrow.withHour(10).withMinute(30).withSecond(0)
                );
                
                appointmentService.scheduleAppointment(
                        patients.get(1), 
                        "Specialist Consultation", 
                        tomorrow.plusDays(2).withHour(14).withMinute(0).withSecond(0)
                );
                
                appointmentService.scheduleAppointment(
                        patients.get(patients.size() > 2 ? 2 : 0), 
                        "Vaccination", 
                        nextWeek.withHour(9).withMinute(0).withSecond(0)
                );
                
                // Get the appointments and update one of them
                List<Appointment> appointments = appointmentService.getAllAppointments();
                if (appointments.size() > 0) {
                    appointmentService.completeAppointment(appointments.get(0).getId());
                }
                
                refreshAppointmentsList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error Adding Sample Data", "Could not add sample appointments: " + e.getMessage());
        }
    }
    
    /**
     * Search for appointments by patient name
     */
    private void searchAppointments() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshAppointmentsList();
            return;
        }
        
        // Create a new variable for the lowercase version instead of modifying the original
        final String queryLower = query.toLowerCase();
        
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        List<Appointment> filteredAppointments = allAppointments.stream()
                .filter(apt -> {
                    Patient patient = apt.getPatient();
                    if (patient == null) {
                        return false;
                    }
                    
                    String firstName = patient.getFirstName();
                    String lastName = patient.getLastName();
                    
                    return (firstName != null && firstName.toLowerCase().contains(queryLower)) ||
                           (lastName != null && lastName.toLowerCase().contains(queryLower)) ||
                           (patient.getFullName() != null && patient.getFullName().toLowerCase().contains(queryLower));
                })
                .collect(Collectors.toList());
        
        appointmentsData.clear();
        appointmentsData.addAll(filteredAppointments);
        updateStatusLabel("Found " + appointmentsData.size() + " appointment(s) matching '" + query + "'");
    }
    
    /**
     * Filter appointments by date
     */
    private void filterAppointmentsByDate(LocalDate date) {
        if (date == null) {
            refreshAppointmentsList();
            return;
        }
        
        // Convert LocalDate to LocalDateTime for service method
        LocalDateTime dateTime = date.atStartOfDay();
        
        List<Appointment> appointmentsOnDate = appointmentService.getAppointmentsByDate(dateTime);
        appointmentsData.clear();
        appointmentsData.addAll(appointmentsOnDate);
        updateStatusLabel("Showing " + appointmentsData.size() + " appointment(s) on " + dateFormatter.format(date));
    }
    
    /**
     * Filter appointments by status
     */
    private void filterAppointmentsByStatus(String status) {
        if (status == null || status.equals("All")) {
            refreshAppointmentsList();
            return;
        }
        
        List<Appointment> appointmentsByStatus = appointmentService.getAppointmentsByStatus(status);
        appointmentsData.clear();
        appointmentsData.addAll(appointmentsByStatus);
        updateStatusLabel("Showing " + appointmentsData.size() + " " + status + " appointment(s)");
    }
    
    /**
     * Refresh the appointments list from the service
     */
    private void refreshAppointmentsList() {
        appointmentsData.clear();
        appointmentsData.addAll(appointmentService.getAllAppointments());
        datePicker.setValue(null);
        statusFilterComboBox.setValue("All");
        updateStatusLabel("Showing all " + appointmentsData.size() + " appointment(s)");
    }
    
    /**
     * Update status label with a message
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Update the button states based on selection and appointment status
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedAppointment != null;
        rescheduleButton.setDisable(!hasSelection);
        cancelButton.setDisable(!hasSelection);
        completeButton.setDisable(!hasSelection);
        
        if (hasSelection) {
            // Disable certain buttons based on appointment status
            Appointment.Status status = selectedAppointment.getStatus();
            
            // Can't reschedule completed or cancelled appointments
            rescheduleButton.setDisable(status == Appointment.Status.COMPLETED || 
                                       status == Appointment.Status.CANCELLED);
            
            // Can't cancel completed appointments
            cancelButton.setDisable(status == Appointment.Status.COMPLETED || 
                                   status == Appointment.Status.CANCELLED);
            
            // Can't complete cancelled appointments
            completeButton.setDisable(status == Appointment.Status.COMPLETED || 
                                     status == Appointment.Status.CANCELLED);
        }
    }
    
    /**
     * Cancel the selected appointment
     */
    private void cancelSelectedAppointment() {
        if (selectedAppointment == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Appointment");
        alert.setHeaderText("Cancel Appointment");
        alert.setContentText("Are you sure you want to cancel this appointment?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean cancelled = appointmentService.cancelAppointment(selectedAppointment.getId());
            if (cancelled) {
                refreshAppointmentsList();
                updateStatusLabel("Appointment cancelled successfully");
            } else {
                showErrorAlert("Cancel Failed", "Could not cancel the selected appointment.");
            }
        }
    }
    
    /**
     * Mark the selected appointment as completed
     */
    private void completeSelectedAppointment() {
        if (selectedAppointment == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Complete Appointment");
        alert.setHeaderText("Mark Appointment as Completed");
        alert.setContentText("Are you sure you want to mark this appointment as completed?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean completed = appointmentService.completeAppointment(selectedAppointment.getId());
            if (completed) {
                refreshAppointmentsList();
                updateStatusLabel("Appointment marked as completed");
            } else {
                showErrorAlert("Operation Failed", "Could not mark the appointment as completed.");
            }
        }
    }
    
    /**
     * Display dialog to schedule a new appointment
     */
    private void showScheduleAppointmentDialog() {
        // Create dialog
        Dialog<Appointment> dialog = new Dialog<>();
        dialog.setTitle("Schedule New Appointment");
        dialog.setHeaderText("Enter appointment details");
        
        // Set button types
        ButtonType scheduleButtonType = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scheduleButtonType, ButtonType.CANCEL);
        
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
                return patient == null ? "" : patient.getFullName() + " (ID: " + patient.getId() + ")";
            }
            
            @Override
            public Patient fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Type selection (dropdown)
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.setPromptText("Select Appointment Type");
        typeComboBox.setItems(FXCollections.observableArrayList(APPOINTMENT_TYPES));
        
        // Date picker
        DatePicker appointmentDatePicker = new DatePicker();
        appointmentDatePicker.setPromptText("Appointment Date");
        appointmentDatePicker.setValue(LocalDate.now().plusDays(1)); // Default to tomorrow
        
        // Time selection (dropdown)
        ComboBox<LocalTime> timeComboBox = new ComboBox<>();
        timeComboBox.setPromptText("Select Time");
        timeComboBox.setItems(FXCollections.observableArrayList(APPOINTMENT_TIMES));
        timeComboBox.setValue(LocalTime.of(9, 0)); // Default to 9 AM
        timeComboBox.setConverter(new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                return time == null ? "" : timeFormatter.format(time);
            }
            
            @Override
            public LocalTime fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Add fields to grid
        grid.add(new Label("Patient:"), 0, 0);
        grid.add(patientComboBox, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(appointmentDatePicker, 1, 2);
        grid.add(new Label("Time:"), 0, 3);
        grid.add(timeComboBox, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == scheduleButtonType) {
                try {
                    Patient patient = patientComboBox.getValue();
                    String type = typeComboBox.getValue();
                    LocalDate date = appointmentDatePicker.getValue();
                    LocalTime time = timeComboBox.getValue();
                    
                    // Validate inputs
                    if (patient == null) {
                        showErrorAlert("Invalid Input", "Please select a patient.");
                        return null;
                    }
                    
                    if (type == null || type.trim().isEmpty()) {
                        showErrorAlert("Invalid Input", "Please select an appointment type.");
                        return null;
                    }
                    
                    if (date == null) {
                        showErrorAlert("Invalid Input", "Please select a date.");
                        return null;
                    }
                    
                    if (time == null) {
                        showErrorAlert("Invalid Input", "Please select a time.");
                        return null;
                    }
                    
                    // Create appointment date time
                    LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
                    
                    // Schedule the appointment
                    return appointmentService.scheduleAppointment(patient, type, appointmentDateTime);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Appointment> result = dialog.showAndWait();
        result.ifPresent(appointment -> {
            refreshAppointmentsList();
            updateStatusLabel("Scheduled new appointment for " + appointment.getPatient().getFullName());
            
            // Select the newly created appointment
            appointmentsTable.getSelectionModel().select(appointment);
        });
    }
    
    /**
     * Display dialog to reschedule an existing appointment
     */
    private void showRescheduleAppointmentDialog() {
        if (selectedAppointment == null) {
            return;
        }
        
        // Create dialog
        Dialog<Appointment> dialog = new Dialog<>();
        dialog.setTitle("Reschedule Appointment");
        dialog.setHeaderText("Reschedule appointment for " + selectedAppointment.getPatient().getFullName());
        
        // Set button types
        ButtonType rescheduleButtonType = new ButtonType("Reschedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rescheduleButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Current date and time (informational)
        Label currentDateTimeLabel = new Label(dateTimeFormatter.format(selectedAppointment.getDateTime()));
        
        // Date picker
        DatePicker appointmentDatePicker = new DatePicker();
        appointmentDatePicker.setPromptText("New Date");
        appointmentDatePicker.setValue(selectedAppointment.getDateTime().toLocalDate());
        
        // Time selection (dropdown)
        ComboBox<LocalTime> timeComboBox = new ComboBox<>();
        timeComboBox.setPromptText("New Time");
        timeComboBox.setItems(FXCollections.observableArrayList(APPOINTMENT_TIMES));
        timeComboBox.setValue(selectedAppointment.getDateTime().toLocalTime());
        timeComboBox.setConverter(new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                return time == null ? "" : timeFormatter.format(time);
            }
            
            @Override
            public LocalTime fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Add fields to grid
        grid.add(new Label("Current Date/Time:"), 0, 0);
        grid.add(currentDateTimeLabel, 1, 0);
        grid.add(new Label("New Date:"), 0, 1);
        grid.add(appointmentDatePicker, 1, 1);
        grid.add(new Label("New Time:"), 0, 2);
        grid.add(timeComboBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == rescheduleButtonType) {
                try {
                    LocalDate date = appointmentDatePicker.getValue();
                    LocalTime time = timeComboBox.getValue();
                    
                    // Validate inputs
                    if (date == null) {
                        showErrorAlert("Invalid Input", "Please select a date.");
                        return null;
                    }
                    
                    if (time == null) {
                        showErrorAlert("Invalid Input", "Please select a time.");
                        return null;
                    }
                    
                    // Create appointment date time
                    LocalDateTime newDateTime = LocalDateTime.of(date, time);
                    
                    // Reschedule the appointment
                    Optional<Appointment> rescheduledAppointment = 
                            appointmentService.rescheduleAppointment(selectedAppointment.getId(), newDateTime);
                    
                    return rescheduledAppointment.orElse(null);
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Appointment> result = dialog.showAndWait();
        result.ifPresent(appointment -> {
            refreshAppointmentsList();
            updateStatusLabel("Rescheduled appointment for " + appointment.getPatient().getFullName());
            
            // Keep the selection on the rescheduled appointment
            appointmentsTable.getSelectionModel().select(appointment);
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
     * Returns the root container for the appointment scheduling screen
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
    public TableView<Appointment> getAppointmentsTable() {
        return appointmentsTable;
    }
    
    public TextField getSearchField() {
        return searchField;
    }
    
    public Button getSearchButton() {
        return searchButton;
    }
    
    public Button getScheduleButton() {
        return scheduleButton;
    }
    
    public Button getRescheduleButton() {
        return rescheduleButton;
    }
    
    public Button getCancelButton() {
        return cancelButton;
    }
    
    public Button getCompleteButton() {
        return completeButton;
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
    
    public ComboBox<String> getStatusFilterComboBox() {
        return statusFilterComboBox;
    }
} 