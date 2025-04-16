package com.example.hospitalsystemsimpletesting.controller;

import com.example.hospitalsystemsimpletesting.model.*;
import com.example.hospitalsystemsimpletesting.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for HospitalController using Mockito to mock dependencies
 */
class HospitalControllerTest {

    private HospitalController hospitalController;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private AppointmentService appointmentService;
    
    @Mock
    private MedicalRecordService medicalRecordService;
    
    @Mock
    private BillingService billingService;
    
    @Mock
    private InventoryService inventoryService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        hospitalController = new HospitalController(
            patientService,
            appointmentService,
            medicalRecordService,
            billingService,
            inventoryService
        );
    }
    
    // Patient Management Tests
    
    @Test
    @DisplayName("Test registerPatient with valid data")
    void testRegisterPatient() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        when(patientService.findPatientById("P001")).thenReturn(Optional.empty());
        when(patientService.savePatient(any(Patient.class))).thenReturn(patient);
        
        // Act
        Patient savedPatient = hospitalController.registerPatient(
            "P001", "John", "Doe", LocalDate.of(1990, 5, 15), null
        );
        
        // Assert
        assertNotNull(savedPatient);
        assertEquals("P001", savedPatient.getId());
        assertEquals("John", savedPatient.getFirstName());
        assertEquals("Doe", savedPatient.getLastName());
        
        verify(patientService).findPatientById("P001");
        verify(patientService).savePatient(any(Patient.class));
    }
    
    // Appointment Management Tests
    
    @Test
    @DisplayName("Test scheduleAppointment with valid data")
    void testScheduleAppointment() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);
        Appointment appointment = new Appointment("A001", patient, "Check-up", appointmentTime);
        
        when(appointmentService.scheduleAppointmentByPatientId(eq("P001"), eq("Check-up"), any(LocalDateTime.class)))
            .thenReturn(appointment);
        
        // Act
        Appointment result = hospitalController.scheduleAppointment("P001", "Check-up", appointmentTime);
        
        // Assert
        assertNotNull(result);
        assertEquals("A001", result.getId());
        assertEquals("Check-up", result.getType());
        assertEquals(patient, result.getPatient());
        
        verify(appointmentService).scheduleAppointmentByPatientId(eq("P001"), eq("Check-up"), any(LocalDateTime.class));
    }
    
    @Test
    @DisplayName("Test getPatientAppointments")
    void testGetPatientAppointments() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(new Appointment("A001", patient, "Check-up", LocalDateTime.now().plusDays(7)));
        appointments.add(new Appointment("A002", patient, "Follow-up", LocalDateTime.now().plusDays(14)));
        
        when(appointmentService.getAppointmentsByPatientId("P001")).thenReturn(appointments);
        
        // Act
        List<Appointment> result = hospitalController.getPatientAppointments("P001");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A001", result.get(0).getId());
        assertEquals("A002", result.get(1).getId());
        
        verify(appointmentService).getAppointmentsByPatientId("P001");
    }
    
    // Medical Record Tests
    
    @Test
    @DisplayName("Test createMedicalRecord with valid data")
    void testCreateMedicalRecord() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        MedicalRecord medicalRecord = new MedicalRecord("MR001", patient, "Fever", LocalDate.now());
        
        when(medicalRecordService.createMedicalRecordByPatientId(
            eq("MR001"), eq("P001"), eq("Fever"), any(LocalDate.class)
        )).thenReturn(medicalRecord);
        
        // Act
        MedicalRecord result = hospitalController.createMedicalRecord(
            "MR001", "P001", "Fever", LocalDate.now()
        );
        
        // Assert
        assertNotNull(result);
        assertEquals("MR001", result.getId());
        assertEquals("Fever", result.getDiagnosis());
        assertEquals(patient, result.getPatient());
        
        verify(medicalRecordService).createMedicalRecordByPatientId(
            eq("MR001"), eq("P001"), eq("Fever"), any(LocalDate.class)
        );
    }
    
    // Billing Tests
    
    @Test
    @DisplayName("Test createBill with valid data")
    void testCreateBill() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        Bill bill = new Bill("B001", patient, LocalDate.now());
        LocalDate dueDate = LocalDate.now().plusDays(30);
        
        when(billingService.createBill(eq("P001"), eq("Hospital Stay"), any(LocalDate.class)))
            .thenReturn(bill);
        
        // Act
        Bill result = hospitalController.createBill("P001", "Hospital Stay", dueDate);
        
        // Assert
        assertNotNull(result);
        assertEquals("B001", result.getId());
        assertEquals(patient, result.getPatient());
        
        verify(billingService).createBill(eq("P001"), eq("Hospital Stay"), any(LocalDate.class));
    }
    
    @Test
    @DisplayName("Test addItemToBill")
    void testAddItemToBill() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        Bill bill = new Bill("B001", patient, LocalDate.now());
        bill.addItem("Room charge", 500.0);
        
        when(billingService.addItemToBill(eq("B001"), eq("Medication"), any(BigDecimal.class)))
            .thenReturn(bill);
        
        // Act
        Bill result = hospitalController.addItemToBill(
            "B001", "Medication", new BigDecimal("250.00")
        );
        
        // Assert
        assertNotNull(result);
        assertEquals("B001", result.getId());
        
        verify(billingService).addItemToBill(eq("B001"), eq("Medication"), any(BigDecimal.class));
    }
    
    // Inventory Tests
    
    @Test
    @DisplayName("Test addInventoryItem with valid data")
    void testAddInventoryItem() {
        // Arrange
        InventoryItem item = new InventoryItem("I001", "Aspirin", 100, 15.99);
        
        when(inventoryService.addInventoryItem(
            eq("I001"), eq("Aspirin"), eq(100), any(BigDecimal.class)
        )).thenReturn(item);
        
        // Act
        InventoryItem result = hospitalController.addInventoryItem(
            "I001", "Aspirin", 100, new BigDecimal("15.99")
        );
        
        // Assert
        assertNotNull(result);
        assertEquals("I001", result.getId());
        assertEquals("Aspirin", result.getName());
        assertEquals(100, result.getQuantity());
        assertEquals(15.99, result.getUnitPrice());
        
        verify(inventoryService).addInventoryItem(
            eq("I001"), eq("Aspirin"), eq(100), any(BigDecimal.class)
        );
    }
    
    // Cross-Service Workflow Tests
    
    @Test
    @DisplayName("Test completeAppointmentWithRecord")
    void testCompleteAppointmentWithRecord() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        Appointment appointment = new Appointment("A001", patient, "Check-up", LocalDateTime.now());
        MedicalRecord medicalRecord = new MedicalRecord("MR001", patient, "Healthy", LocalDate.now());
        medicalRecord.setRelatedAppointment(appointment);
        
        when(appointmentService.completeAppointment("A001")).thenReturn(true);
        when(medicalRecordService.createMedicalRecordFromAppointment(
            eq("MR001"), eq("A001"), eq("Healthy"), eq("Regular checkup completed")
        )).thenReturn(medicalRecord);
        
        // Act
        MedicalRecord result = hospitalController.completeAppointmentWithRecord(
            "A001", "MR001", "Healthy", "Regular checkup completed"
        );
        
        // Assert
        assertNotNull(result);
        assertEquals("MR001", result.getId());
        assertEquals("Healthy", result.getDiagnosis());
        assertEquals(appointment, result.getRelatedAppointment());
        
        verify(appointmentService).completeAppointment("A001");
        verify(medicalRecordService).createMedicalRecordFromAppointment(
            eq("MR001"), eq("A001"), eq("Healthy"), eq("Regular checkup completed")
        );
    }
    
    @Test
    @DisplayName("Test createBillFromAppointment")
    void testCreateBillFromAppointment() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        Appointment appointment = new Appointment("A001", patient, "Check-up", LocalDateTime.now());
        appointment.complete(); // Mark as completed
        
        Bill bill = new Bill("B001", patient, LocalDate.now());
        LocalDate dueDate = LocalDate.now().plusDays(30);
        
        when(appointmentService.findAppointmentById("A001")).thenReturn(Optional.of(appointment));
        when(billingService.createBill(eq("P001"), eq("Billing for Check-up"), any(LocalDate.class)))
            .thenReturn(bill);
        when(billingService.addItemToBill(eq("B001"), contains("Consultation fee"), any(BigDecimal.class)))
            .thenReturn(bill);
        
        // Act
        Bill result = hospitalController.createBillFromAppointment(
            "A001", "Billing for Check-up", dueDate, new BigDecimal("150.00")
        );
        
        // Assert
        assertNotNull(result);
        assertEquals("B001", result.getId());
        assertEquals(patient, result.getPatient());
        
        verify(appointmentService).findAppointmentById("A001");
        verify(billingService).createBill(eq("P001"), eq("Billing for Check-up"), any(LocalDate.class));
        verify(billingService).addItemToBill(eq("B001"), contains("Consultation fee"), any(BigDecimal.class));
    }
    
    @Test
    @DisplayName("Test dispenseMedication")
    void testDispenseMedication() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        MedicalRecord medicalRecord = new MedicalRecord("MR001", patient, "Fever", LocalDate.now());
        InventoryItem medication = new InventoryItem("I001", "Aspirin", 100, 15.99);
        
        when(medicalRecordService.findById("MR001")).thenReturn(Optional.of(medicalRecord));
        when(inventoryService.findById("I001")).thenReturn(Optional.of(medication));
        when(inventoryService.removeStock(eq("I001"), eq(5))).thenReturn(medication);
        when(medicalRecordService.addNotes(eq("MR001"), anyString())).thenReturn(Optional.of(medicalRecord));
        
        // Act
        boolean result = hospitalController.dispenseMedication("MR001", "I001", 5);
        
        // Assert
        assertTrue(result);
        
        verify(medicalRecordService).findById("MR001");
        verify(inventoryService).findById("I001");
        verify(inventoryService).removeStock(eq("I001"), eq(5));
        verify(medicalRecordService).addNotes(eq("MR001"), contains("Dispensed 5 units"));
    }
    
    @Test
    @DisplayName("Test registerPatientWithAppointment")
    void testRegisterPatientWithAppointment() {
        // Arrange
        Patient patient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);
        Appointment appointment = new Appointment("A001", patient, "Initial Check-up", appointmentTime);
        
        when(patientService.findPatientById("P001")).thenReturn(Optional.empty());
        when(patientService.savePatient(any(Patient.class))).thenReturn(patient);
        when(appointmentService.scheduleAppointmentByPatientId(
            eq("P001"), eq("Initial Check-up"), any(LocalDateTime.class)
        )).thenReturn(appointment);
        
        // Act
        Appointment result = hospitalController.registerPatientWithAppointment(
            "P001", "John", "Doe", LocalDate.of(1990, 5, 15),
            "Initial Check-up", appointmentTime
        );
        
        // Assert
        assertNotNull(result);
        assertEquals("A001", result.getId());
        assertEquals("Initial Check-up", result.getType());
        assertEquals(patient, result.getPatient());
        
        verify(patientService).findPatientById("P001");
        verify(patientService).savePatient(any(Patient.class));
        verify(appointmentService).scheduleAppointmentByPatientId(
            eq("P001"), eq("Initial Check-up"), any(LocalDateTime.class)
        );
    }
    
    // Error Handling Tests
    
    @Test
    @DisplayName("Test scheduleAppointment with null service")
    void testServiceUnavailableException() {
        // Arrange
        HospitalController controllerWithNullServices = new HospitalController(patientService);
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);
        
        // Act & Assert
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            controllerWithNullServices.scheduleAppointment("P001", "Check-up", appointmentTime);
        });
        
        assertTrue(exception.getMessage().contains("Cannot schedule appointments"));
    }
} 