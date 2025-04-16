package com.example.hospitalsystemsimpletesting.integration;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Payment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end test for the entire patient billing workflow.
 * This test verifies the complete lifecycle from patient registration,
 * through appointment scheduling, bill creation, and payment.
 */
public class PatientBillingEndToEndTest {

    /**
     * In-memory implementation of PatientService for testing
     */
    private static class TestPatientService implements PatientService {
        private final Map<String, Patient> patientsById = new HashMap<>();
        private int nextId = 1001;
        
        @Override
        public Patient registerPatient(String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
            String patientId = "P" + nextId++;
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
            return patientsById.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(namePart.toLowerCase()))
                .toList();
        }
        
        @Override
        public List<Patient> getAllPatients() {
            return patientsById.values().stream().toList();
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
            return patientsById.values().stream()
                .filter(Patient::isAdmitted)
                .toList();
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
    
    @BeforeEach
    void setUp() {
        // Initialize the services with in-memory test implementations
        patientService = new TestPatientService();
        appointmentService = new AppointmentServiceImpl(patientService);
        billingService = new BillingServiceImpl(patientService);
    }
    
    @Test
    @DisplayName("End-to-end test for patient registration, appointment, and billing")
    void testCompletePatientBillingWorkflow() {
        // 1. Register a new patient
        Patient patient = patientService.registerPatient(
            "Sarah Johnson", 
            LocalDate.of(1992, 5, 15),
            "Female",
            "555-123-4567",
            "123 Pine Street"
        );
        
        assertNotNull(patient, "Patient should be registered");
        assertNotNull(patient.getId(), "Patient should have an ID");
        
        // 2. Verify patient exists in the system
        Optional<Patient> retrievedPatient = patientService.findPatientById(patient.getId());
        assertTrue(retrievedPatient.isPresent(), "Patient should exist in the system");
        assertEquals("Sarah Johnson", retrievedPatient.get().getName(), "Patient name should match");
        
        // 3. Schedule an appointment for the patient
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusDays(7);
        Appointment appointment = appointmentService.scheduleAppointmentByPatientId(
            patient.getId(),
            "Initial Consultation",
            appointmentDateTime
        );
        
        assertNotNull(appointment, "Appointment should be created");
        assertEquals(Appointment.Status.SCHEDULED, appointment.getStatus(), "Appointment should be in SCHEDULED status");
        
        // 4. Patient attends the appointment - mark as completed
        appointmentService.completeAppointment(appointment.getId());
        
        // 5. Verify appointment was completed
        Optional<Appointment> completedAppointment = appointmentService.findAppointmentById(appointment.getId());
        assertTrue(completedAppointment.isPresent(), "Appointment should exist");
        assertEquals(Appointment.Status.COMPLETED, completedAppointment.get().getStatus(), "Appointment should be in COMPLETED status");
        
        // 6. Create a bill for the appointment services
        Bill bill = billingService.createBill(
            patient.getId(),
            "Bill for Initial Consultation",
            LocalDate.now().plusDays(30)
        );
        
        assertNotNull(bill, "Bill should be created");
        assertEquals(patient.getId(), bill.getPatient().getId(), "Bill should be associated with the patient");
        assertEquals("UNPAID", bill.getStatus(), "Bill should be in UNPAID status");
        
        // 7. Add services to the bill
        billingService.addItemToBill(bill.getId(), "Consultation Fee", new BigDecimal("150.00"));
        billingService.addItemToBill(bill.getId(), "Lab Tests", new BigDecimal("200.00"));
        billingService.addItemToBill(bill.getId(), "Medication", new BigDecimal("75.50"));
        
        // 8. Verify bill total
        Optional<Bill> updatedBill = billingService.findBillById(bill.getId());
        assertTrue(updatedBill.isPresent(), "Bill should exist");
        assertEquals(425.50, updatedBill.get().getTotalAmount(), 0.01, "Bill total should be correct");
        
        // 9. Make a partial payment
        Payment partialPayment = billingService.processPayment(bill.getId(), new BigDecimal("200.00"), "CREDIT_CARD");
        
        assertNotNull(partialPayment, "Payment should be created");
        assertEquals(new BigDecimal("200.00"), partialPayment.getAmount(), "Payment amount should match");
        assertEquals("CREDIT_CARD", partialPayment.getPaymentMethod(), "Payment method should match");
        
        // 10. Verify bill status after partial payment
        updatedBill = billingService.findBillById(bill.getId());
        assertTrue(updatedBill.isPresent(), "Bill should exist after partial payment");
        assertEquals("PARTIAL", updatedBill.get().getStatus(), "Bill should be in PARTIAL status");
        
        // 11. Make final payment
        BigDecimal remainingAmount = new BigDecimal("225.50");
        Payment finalPayment = billingService.processPayment(bill.getId(), remainingAmount, "INSURANCE");
        
        assertNotNull(finalPayment, "Final payment should be created");
        assertEquals(remainingAmount, finalPayment.getAmount(), "Final payment amount should match");
        
        // 12. Verify bill is now paid
        updatedBill = billingService.findBillById(bill.getId());
        assertTrue(updatedBill.isPresent(), "Bill should exist after final payment");
        assertEquals("PAID", updatedBill.get().getStatus(), "Bill should be in PAID status");
        assertTrue(updatedBill.get().isPaid(), "Bill isPaid() should return true");
        
        // 13. Verify the patient's billing history
        List<Bill> patientBills = billingService.findBillsByPatientId(patient.getId());
        assertEquals(1, patientBills.size(), "Patient should have 1 bill");
        assertTrue(patientBills.get(0).isPaid(), "Patient's bill should be paid");
    }
    
    @Test
    @DisplayName("Test billing workflow for admitted and discharged patient")
    void testAdmittedPatientBillingWorkflow() {
        // 1. Register a new patient
        Patient patient = patientService.registerPatient(
            "Robert Smith", 
            LocalDate.of(1975, 10, 20),
            "Male",
            "555-987-6543",
            "456 Oak Avenue"
        );
        
        // 2. Admit the patient
        patientService.admitPatient(patient.getId());
        
        // Verify patient is admitted
        Optional<Patient> retrievedPatient = patientService.findPatientById(patient.getId());
        assertTrue(retrievedPatient.isPresent(), "Patient should exist");
        assertTrue(retrievedPatient.get().isAdmitted(), "Patient should be admitted");
        
        // 3. Create a bill for the hospital stay
        Bill bill = billingService.createBill(
            patient.getId(),
            "Hospital Admission",
            LocalDate.now().plusDays(45)
        );
        
        // 4. Add items to the bill for multi-day stay
        billingService.addItemToBill(bill.getId(), "Room Charges (3 days)", new BigDecimal("1500.00"));
        billingService.addItemToBill(bill.getId(), "Nursing Care", new BigDecimal("800.00"));
        billingService.addItemToBill(bill.getId(), "Medications", new BigDecimal("350.50"));
        billingService.addItemToBill(bill.getId(), "Diagnostic Tests", new BigDecimal("1200.00"));
        
        // 5. Discharge the patient
        patientService.dischargePatient(patient.getId());
        
        // Verify patient is no longer admitted
        retrievedPatient = patientService.findPatientById(patient.getId());
        assertTrue(retrievedPatient.isPresent(), "Patient should exist");
        assertFalse(retrievedPatient.get().isAdmitted(), "Patient should not be admitted");
        
        // 6. Process insurance payment
        Payment insurancePayment = billingService.processPayment(
            bill.getId(), 
            new BigDecimal("3500.00"), 
            "INSURANCE"
        );
        
        // 7. Process patient co-pay
        Payment patientPayment = billingService.processPayment(
            bill.getId(),
            new BigDecimal("350.50"),
            "CREDIT_CARD"
        );
        
        // 8. Verify bill is now paid
        Optional<Bill> finalBill = billingService.findBillById(bill.getId());
        assertTrue(finalBill.isPresent(), "Bill should exist");
        assertEquals("PAID", finalBill.get().getStatus(), "Bill should be in PAID status");
        
        // 9. Verify in the list of bills by status
        List<Bill> paidBills = billingService.getBillsByStatus(true);
        assertTrue(paidBills.contains(finalBill.get()), "Paid bills list should contain this bill");
    }
} 