package com.example.hospitalsystemsimpletesting.controller;

import com.example.hospitalsystemsimpletesting.model.*;
import com.example.hospitalsystemsimpletesting.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
 * Test class for cross-service workflows in HospitalController
 * This class focuses on testing interactions between multiple services
 */
public class CrossServiceWorkflowTest {

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
    
    // Common test data
    private Patient testPatient;
    private Appointment testAppointment;
    private MedicalRecord testMedicalRecord;
    private Bill testBill;
    private InventoryItem testMedication;
    
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
        
        // Initialize common test data
        testPatient = new Patient("P001", "John", "Doe", LocalDate.of(1990, 5, 15));
        testPatient.setBloodType("O+");
        
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusDays(7);
        testAppointment = new Appointment("A001", testPatient, "Check-up", appointmentDateTime);
        
        testMedicalRecord = new MedicalRecord("MR001", testPatient, "Common Cold", LocalDate.now());
        
        testBill = new Bill("B001", testPatient, LocalDate.now());
        
        testMedication = new InventoryItem("MED001", "Ibuprofen", 100, 5.99);
    }
    
    @Test
    @DisplayName("Test complete patient visit workflow - consultation to billing")
    void testCompletePatientVisitWorkflow() {
        // Setup mocks for the appointment completion part
        when(appointmentService.findAppointmentById("A001")).thenReturn(Optional.of(testAppointment));
        when(appointmentService.completeAppointment("A001")).thenReturn(true);
        
        // Create medical record from appointment
        when(medicalRecordService.createMedicalRecordFromAppointment(
            eq("MR001"), eq("A001"), eq("Common Cold"), eq("Patient has fever and cough")))
            .thenReturn(testMedicalRecord);
            
        // For bill creation, the appointment needs to be COMPLETED
        testAppointment.complete(); // Set status to COMPLETED
        when(appointmentService.findAppointmentById("A001")).thenReturn(Optional.of(testAppointment));
        
        // Setup bill creation
        LocalDate dueDate = LocalDate.now().plusDays(30);
        when(billingService.createBill(eq("P001"), eq("Consultation charges"), eq(dueDate)))
            .thenReturn(testBill);
            
        // Setup adding item to bill
        Bill updatedBill = new Bill("B001", testPatient, LocalDate.now());
        updatedBill.addItem("Consultation fee for Check-up", 100.0);
        when(billingService.addItemToBill(eq("B001"), eq("Consultation fee for Check-up"), any(BigDecimal.class)))
            .thenReturn(updatedBill);
            
        // Execute the workflow
        // 1. Complete appointment and create medical record
        MedicalRecord record = hospitalController.completeAppointmentWithRecord(
            "A001", "MR001", "Common Cold", "Patient has fever and cough");
            
        // 2. Create bill from the appointment
        Bill bill = hospitalController.createBillFromAppointment(
            "A001", "Consultation charges", dueDate, new BigDecimal("100.00"));
            
        // Verify all interactions happened in the correct order
        // First complete the appointment
        verify(appointmentService).completeAppointment("A001");
        
        // Then create the medical record from the appointment
        verify(medicalRecordService).createMedicalRecordFromAppointment(
            eq("MR001"), eq("A001"), eq("Common Cold"), eq("Patient has fever and cough"));
            
        // Verify the appointment is checked before creating the bill
        verify(appointmentService, times(1)).findAppointmentById("A001");
        
        // Create the bill for the patient
        verify(billingService).createBill(eq("P001"), eq("Consultation charges"), eq(dueDate));
        
        // Add the consultation fee as a line item
        verify(billingService).addItemToBill(eq("B001"), eq("Consultation fee for Check-up"), any(BigDecimal.class));
        
        // Verify the returned values
        assertNotNull(record);
        assertEquals("MR001", record.getId());
        assertEquals("Common Cold", record.getDiagnosis());
        
        assertNotNull(bill);
        assertEquals("B001", bill.getId());
    }
    
    @Test
    @DisplayName("Test patient registration and appointment scheduling workflow")
    void testPatientRegistrationAndAppointmentWorkflow() {
        // Setup mocks for patient registration
        when(patientService.findPatientById("P001")).thenReturn(Optional.empty());
        when(patientService.savePatient(any(Patient.class))).thenReturn(testPatient);
        
        // Setup mocks for appointment scheduling
        when(appointmentService.scheduleAppointmentByPatientId(
            eq("P001"), eq("Initial Consultation"), any(LocalDateTime.class)))
            .thenReturn(testAppointment);
            
        // Execute the workflow
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusDays(7);
        Appointment appointment = hospitalController.registerPatientWithAppointment(
            "P001", "John", "Doe", LocalDate.of(1990, 5, 15),
            "Initial Consultation", appointmentDateTime);
            
        // Verify all interactions happened in the correct order
        // First check if patient exists
        verify(patientService).findPatientById("P001");
        
        // Then save the new patient
        verify(patientService).savePatient(any(Patient.class));
        
        // Then schedule the appointment for the patient
        verify(appointmentService).scheduleAppointmentByPatientId(
            eq("P001"), eq("Initial Consultation"), any(LocalDateTime.class));
            
        // Verify the returned appointment
        assertNotNull(appointment);
        assertEquals("A001", appointment.getId());
        assertEquals("Check-up", appointment.getType());
        assertEquals(testPatient, appointment.getPatient());
    }
    
    @Test
    @DisplayName("Test medication dispensing workflow with inventory update")
    void testMedicationDispensingWorkflow() {
        // Setup mocks for medical record - use the correct method name findById
        when(medicalRecordService.findById("MR001")).thenReturn(Optional.of(testMedicalRecord));
        
        // Setup mocks for inventory item - use the correct method name findById
        when(inventoryService.findById("MED001")).thenReturn(Optional.of(testMedication));
        
        // Setup mocks for inventory update
        InventoryItem updatedMedication = new InventoryItem("MED001", "Ibuprofen", 95, 5.99);
        when(inventoryService.removeStock("MED001", 5)).thenReturn(updatedMedication);
        
        // Setup mocks for updating medical record notes
        MedicalRecord updatedRecord = new MedicalRecord("MR001", testPatient, "Common Cold", LocalDate.now());
        updatedRecord.setNotes("Dispensed 5 units of Ibuprofen");
        when(medicalRecordService.addNotes(eq("MR001"), contains("Dispensed 5 units of Ibuprofen")))
            .thenReturn(Optional.of(updatedRecord));
            
        // Execute the workflow
        boolean result = hospitalController.dispenseMedication("MR001", "MED001", 5);
        
        // Verify all interactions happened in the correct order
        // First check if the medical record exists
        verify(medicalRecordService).findById("MR001");
        
        // Then check if the medication exists
        verify(inventoryService).findById("MED001");
        
        // Then remove the stock
        verify(inventoryService).removeStock("MED001", 5);
        
        // Then update the medical record notes
        verify(medicalRecordService).addNotes(eq("MR001"), contains("Dispensed 5 units of Ibuprofen"));
        
        // Verify the result
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Test complete medical cycle workflow - from registration to billing")
    void testCompleteMedicalCycleWorkflow() {
        // Step 1: Register a new patient
        when(patientService.findPatientById("P001")).thenReturn(Optional.empty());
        when(patientService.savePatient(any(Patient.class))).thenReturn(testPatient);
        
        // Step 2: Schedule an appointment
        when(appointmentService.scheduleAppointmentByPatientId(
            eq("P001"), eq("Initial Consultation"), any(LocalDateTime.class)))
            .thenReturn(testAppointment);
            
        // Step 3: Complete the appointment
        when(appointmentService.findAppointmentById("A001")).thenReturn(Optional.of(testAppointment));
        when(appointmentService.completeAppointment("A001")).thenReturn(true);
        
        // Step 4: Create medical record
        when(medicalRecordService.createMedicalRecordFromAppointment(
            eq("MR001"), eq("A001"), eq("Common Cold"), eq("Patient has fever and cough")))
            .thenReturn(testMedicalRecord);
            
        // Step 5: Create bill
        testAppointment.complete(); // Set status to COMPLETED
        when(appointmentService.findAppointmentById("A001")).thenReturn(Optional.of(testAppointment));
        
        when(billingService.createBill(eq("P001"), eq("Consultation charges"), any(LocalDate.class)))
            .thenReturn(testBill);
            
        Bill updatedBill = new Bill("B001", testPatient, LocalDate.now());
        updatedBill.addItem("Consultation fee for Initial Consultation", 100.0);
        when(billingService.addItemToBill(eq("B001"), contains("Consultation fee"), any(BigDecimal.class)))
            .thenReturn(updatedBill);
            
        // Execute the complete workflow
        // Register patient and schedule appointment
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusDays(1);
        Appointment appointment = hospitalController.registerPatientWithAppointment(
            "P001", "John", "Doe", LocalDate.of(1990, 5, 15),
            "Initial Consultation", appointmentDateTime);
            
        // Complete appointment and create medical record
        MedicalRecord record = hospitalController.completeAppointmentWithRecord(
            "A001", "MR001", "Common Cold", "Patient has fever and cough");
            
        // Create bill from the appointment
        LocalDate dueDate = LocalDate.now().plusDays(30);
        Bill bill = hospitalController.createBillFromAppointment(
            "A001", "Consultation charges", dueDate, new BigDecimal("100.00"));
            
        // Verify all steps were executed in the correct order
        verify(patientService).findPatientById("P001");
        verify(patientService).savePatient(any(Patient.class));
        verify(appointmentService).scheduleAppointmentByPatientId(
            eq("P001"), eq("Initial Consultation"), any(LocalDateTime.class));
        verify(appointmentService).completeAppointment("A001");
        verify(medicalRecordService).createMedicalRecordFromAppointment(
            eq("MR001"), eq("A001"), eq("Common Cold"), eq("Patient has fever and cough"));
        verify(billingService).createBill(eq("P001"), eq("Consultation charges"), any(LocalDate.class));
        verify(billingService).addItemToBill(eq("B001"), contains("Consultation fee"), any(BigDecimal.class));
        
        // Verify the returned values
        assertNotNull(appointment);
        assertNotNull(record);
        assertNotNull(bill);
        
        assertEquals("A001", appointment.getId());
        assertEquals("MR001", record.getId());
        assertEquals("B001", bill.getId());
    }
} 