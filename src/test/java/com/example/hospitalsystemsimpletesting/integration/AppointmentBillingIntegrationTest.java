package com.example.hospitalsystemsimpletesting.integration;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.AppointmentService;
import com.example.hospitalsystemsimpletesting.service.BillingService;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.AppointmentServiceImpl;
import com.example.hospitalsystemsimpletesting.service.impl.BillingServiceImpl;
import com.example.hospitalsystemsimpletesting.service.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating the workflow between appointment scheduling and billing.
 * This is an example of how to test cross-service interactions.
 */
public class AppointmentBillingIntegrationTest {

    /**
     * Custom PatientService implementation for testing
     */
    private static class TestPatientService implements PatientService {
        private final Map<String, Patient> patientsById = new HashMap<>();
        
        @Override
        public Patient registerPatient(String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
            String patientId = "P" + (100 + patientsById.size());
            Patient patient = new Patient(patientId, name, dateOfBirth, gender, contactNumber, address);
            patientsById.put(patientId, patient);
            return patient;
        }
        
        @Override
        public Patient savePatient(Patient patient) {
            if (patient == null) {
                throw new IllegalArgumentException("Patient cannot be null");
            }
            patientsById.put(patient.getId(), patient);
            return patient;
        }
        
        @Override
        public void updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
            Patient patient = patientsById.get(patientId);
            if (patient != null) {
                patient.setName(name);
                patient.setDateOfBirth(dateOfBirth);
                patient.setGender(gender);
                patient.setContactNumber(contactNumber);
                patient.setAddress(address);
            } else {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
        }
        
        @Override
        public Optional<Patient> findPatientById(String patientId) {
            return Optional.ofNullable(patientsById.get(patientId));
        }
        
        @Override
        public List<Patient> findPatientsByName(String namePart) {
            List<Patient> results = new ArrayList<>();
            if (namePart == null) return results;
            
            String lowerNamePart = namePart.toLowerCase();
            for (Patient patient : patientsById.values()) {
                if (patient.getName().toLowerCase().contains(lowerNamePart)) {
                    results.add(patient);
                }
            }
            return results;
        }
        
        @Override
        public List<Patient> getAllPatients() {
            return new ArrayList<>(patientsById.values());
        }
        
        @Override
        public void admitPatient(String patientId) {
            Patient patient = patientsById.get(patientId);
            if (patient != null) {
                patient.admit();
            } else {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
        }
        
        @Override
        public void dischargePatient(String patientId) {
            Patient patient = patientsById.get(patientId);
            if (patient != null) {
                patient.discharge();
            } else {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
        }
        
        @Override
        public List<Patient> getAdmittedPatients() {
            List<Patient> admitted = new ArrayList<>();
            for (Patient patient : patientsById.values()) {
                if (patient.isAdmitted()) {
                    admitted.add(patient);
                }
            }
            return admitted;
        }
        
        @Override
        public boolean deletePatient(String patientId) {
            if (patientId == null || patientId.trim().isEmpty()) {
                return false;
            }
            
            Patient removedPatient = patientsById.remove(patientId);
            return removedPatient != null;
        }
    }

    private PatientService patientService;
    private AppointmentService appointmentService;
    private BillingService billingService;
    
    private Patient testPatient;
    private String testPatientId;
    
    @BeforeEach
    void setUp() {
        // Initialize the test service implementation
        patientService = new TestPatientService();
        appointmentService = new AppointmentServiceImpl(patientService);
        billingService = new BillingServiceImpl(patientService);
        
        // Create a test patient
        testPatient = new Patient(
            "P" + System.currentTimeMillis(),
            "John",
            "Doe",
            LocalDate.of(1990, 5, 15)
        );
        
        // Register the patient
        patientService.savePatient(testPatient);
        testPatientId = testPatient.getId();
    }
    
    @Test
    @DisplayName("Test complete appointment and billing workflow")
    void testAppointmentAndBillingWorkflow() {
        // 1. Schedule an appointment
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);
        Appointment appointment = appointmentService.scheduleAppointmentByPatientId(
            testPatientId,
            "Annual Check-up",
            appointmentTime
        );
        
        assertNotNull(appointment, "Appointment should be created");
        assertEquals(testPatientId, appointment.getPatient().getId(), "Appointment should be for the test patient");
        
        // 2. Complete the appointment
        appointmentService.completeAppointment(appointment.getId());
        Optional<Appointment> completedAppointment = appointmentService.findAppointmentById(appointment.getId());
        
        assertTrue(completedAppointment.isPresent(), "Appointment should exist");
        assertEquals(Appointment.Status.COMPLETED, completedAppointment.get().getStatus(), "Appointment status should be COMPLETED");
        
        // 3. Create a bill based on the appointment
        LocalDate dueDate = LocalDate.now().plusDays(30);
        Bill bill = billingService.createBill(
            testPatientId,
            "Bill for " + appointment.getType(),
            dueDate
        );
        
        assertNotNull(bill, "Bill should be created");
        assertEquals(testPatientId, bill.getPatient().getId(), "Bill should be for the test patient");
        
        // 4. Add items to the bill based on the appointment type
        billingService.addItemToBill(bill.getId(), "Consultation Fee", new BigDecimal("150.00"));
        billingService.addItemToBill(bill.getId(), "Lab Tests", new BigDecimal("75.50"));
        
        // 5. Retrieve the bill and verify items
        Optional<Bill> retrievedBill = billingService.findBillById(bill.getId());
        
        assertTrue(retrievedBill.isPresent(), "Bill should exist");
        assertEquals(3, retrievedBill.get().getItems().size(), "Bill should have 3 items");
        
        // Use BigDecimal.compareTo for proper comparison rather than equals
        BigDecimal expectedTotal = new BigDecimal("225.50");
        BigDecimal actualTotal = BigDecimal.valueOf(retrievedBill.get().getTotalAmount());
        assertEquals(0, expectedTotal.compareTo(actualTotal), "Bill total should be correct");
        
        // 6. Process a payment
        billingService.processPayment(bill.getId(), new BigDecimal("100.00"), "Partial payment");
        
        retrievedBill = billingService.findBillById(bill.getId());
        assertTrue(retrievedBill.isPresent(), "Bill should exist after payment");
        assertFalse(retrievedBill.get().isPaid(), "Bill should not be fully paid");
        
        // 7. Process remaining payment
        billingService.processPayment(bill.getId(), new BigDecimal("125.50"), "Final payment");
        
        retrievedBill = billingService.findBillById(bill.getId());
        assertTrue(retrievedBill.isPresent(), "Bill should exist after final payment");
        assertTrue(retrievedBill.get().isPaid(), "Bill should be fully paid");
    }
    
    // Additional integration tests could be added here:
    // - Test overdue bill notifications
    // - Test rescheduling appointments and adjusting bills
    // - Test appointment cancellation and refund workflow
} 