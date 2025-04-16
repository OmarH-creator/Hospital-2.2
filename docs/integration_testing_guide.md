# Comprehensive Integration Testing Guide

This document provides a detailed approach to integration testing for the Hospital Management System, ensuring all components work together as expected.

## Integration Testing Approach

Integration testing validates that different components of the system work together correctly. For our Hospital Management System, we'll use a pragmatic approach that combines:

1. **Bottom-up testing**: Starting with low-level components and progressively integrating higher-level ones
2. **Key workflow testing**: Testing complete workflows that span multiple system components
3. **Boundary integration testing**: Focusing on integration points where data flows between components

## Setting Up Integration Tests

Create a dedicated package for integration tests:

```
src/test/java/com/example/hospitalsystemsimpletesting/integration/
```

Use a consistent naming convention:
- `*IntegrationTest.java` for general integration tests
- `*WorkflowTest.java` for end-to-end workflow tests

### Base Integration Test Class

```java
package com.example.hospitalsystemsimpletesting.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("integration")
public abstract class BaseIntegrationTest {
    
    // Common setup for integration tests
    @BeforeEach
    void setupIntegrationTest() {
        // Setup code common to all integration tests
    }
    
    // Helper methods for integration tests
}
```

## Multi-Layer Integration Tests

These tests verify the interaction between multiple layers of the application.

### Example: Patient Registration Workflow Test

```java
@Test
void testPatientRegistrationWorkflow() {
    // 1. Create real implementations (not mocks)
    PatientService patientService = new PatientServiceImpl();
    HospitalController controller = new HospitalController(patientService);
    
    // 2. Execute the workflow
    String patientId = "P" + System.currentTimeMillis(); // Ensure unique ID
    String firstName = "John";
    String lastName = "Smith";
    LocalDate birthDate = LocalDate.of(1980, 5, 15);
    
    // 3. Call controller method which integrates with service
    Patient registeredPatient = controller.registerPatient(
        patientId, firstName, lastName, birthDate, "O+");
    
    // 4. Verify patient was registered correctly
    assertNotNull(registeredPatient, "Patient should be registered");
    assertEquals(patientId, registeredPatient.getId());
    
    // 5. Verify patient can be retrieved (tests service-to-service integration)
    Optional<Patient> retrievedPatient = patientService.findPatientById(patientId);
    assertTrue(retrievedPatient.isPresent(), "Patient should be found in service");
    assertEquals(firstName, retrievedPatient.get().getFirstName());
    assertEquals(lastName, retrievedPatient.get().getLastName());
    assertEquals("O+", retrievedPatient.get().getBloodType());
}
```

### Example: Appointment and Medical Record Integration

```java
@Test
void testAppointmentAndMedicalRecordIntegration() {
    // 1. Setup services
    PatientService patientService = new PatientServiceImpl();
    AppointmentService appointmentService = new AppointmentServiceImpl();
    MedicalRecordService medicalRecordService = new MedicalRecordServiceImpl();
    
    // 2. Create a controller that integrates all services
    HospitalController controller = new HospitalController(
        patientService, appointmentService, medicalRecordService);
    
    // 3. Register a patient
    String patientId = "P" + System.currentTimeMillis();
    Patient patient = controller.registerPatient(
        patientId, "Alice", "Johnson", LocalDate.of(1975, 3, 10), "B+");
    
    // 4. Schedule an appointment
    LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
    Appointment appointment = controller.scheduleAppointment(
        patientId, "Annual Checkup", appointmentTime);
    assertNotNull(appointment, "Appointment should be scheduled");
    
    // 5. Create medical record from appointment
    MedicalRecord medicalRecord = controller.createMedicalRecord(
        appointment.getId(), "Annual physical examination", "All vitals normal");
    assertNotNull(medicalRecord, "Medical record should be created");
    
    // 6. Verify integration between all components
    assertEquals(patientId, medicalRecord.getPatient().getId(), 
                "Medical record should reference correct patient");
    assertEquals(appointment.getId(), medicalRecord.getAppointmentId(), 
                "Medical record should reference correct appointment");
    
    // 7. Verify retrieval through different paths
    MedicalRecord retrievedRecord = medicalRecordService.findById(medicalRecord.getId()).get();
    assertEquals("All vitals normal", retrievedRecord.getNotes());
    
    // 8. Verify bidirectional relationships
    List<MedicalRecord> patientRecords = medicalRecordService.findByPatientId(patientId);
    assertEquals(1, patientRecords.size());
    assertEquals(medicalRecord.getId(), patientRecords.get(0).getId());
}
```

## Mocking External Dependencies

In some cases, you may need to mock external dependencies while testing internal integration.

### Example: Integration Test with External Billing System

```java
@Test
void testBillingIntegrationWithMockedExternalSystem() {
    // 1. Setup real services
    PatientService patientService = new PatientServiceImpl();
    AppointmentService appointmentService = new AppointmentServiceImpl();
    
    // 2. Create mock for external billing system
    ExternalBillingSystem mockBillingSystem = mock(ExternalBillingSystem.class);
    when(mockBillingSystem.generateInvoice(any(Patient.class), any(Appointment.class)))
        .thenReturn(new Invoice("INV001", BigDecimal.valueOf(150.00)));
    
    // 3. Create billing service with mock external system
    BillingService billingService = new BillingServiceImpl(mockBillingSystem);
    
    // 4. Create controller with real and mocked services
    HospitalController controller = new HospitalController(
        patientService, appointmentService, billingService);
    
    // 5. Execute workflow
    String patientId = "P" + System.currentTimeMillis();
    Patient patient = controller.registerPatient(
        patientId, "Robert", "Smith", LocalDate.of(1965, 7, 22), "A-");
    
    Appointment appointment = controller.scheduleAppointment(
        patientId, "Specialist Consultation", LocalDateTime.now().plusDays(3));
    
    // 6. Test integration with billing
    Bill bill = controller.generateBill(appointment.getId());
    
    // 7. Verify integration worked correctly
    assertNotNull(bill, "Bill should be generated");
    assertEquals(patientId, bill.getPatientId());
    assertEquals(appointment.getId(), bill.getAppointmentId());
    assertEquals(BigDecimal.valueOf(150.00), bill.getAmount());
    
    // 8. Verify mock interaction
    verify(mockBillingSystem).generateInvoice(
        argThat(p -> p.getId().equals(patientId)), 
        argThat(a -> a.getId().equals(appointment.getId()))
    );
}
```

## Testing Error Scenarios in Integration

Error handling is critical in integration tests, as failures in one component may affect others.

### Example: Testing Service Failures

```java
@Test
void testErrorPropagationBetweenComponents() {
    // 1. Setup real service that will generate data
    PatientService patientService = new PatientServiceImpl();
    
    // 2. Setup mock service that will fail
    AppointmentService mockAppointmentService = mock(AppointmentService.class);
    when(mockAppointmentService.scheduleAppointment(any(Patient.class), anyString(), any(LocalDateTime.class)))
        .thenThrow(new ServiceException("Database connection failed"));
    
    // 3. Create controller with mixed services
    HospitalController controller = new HospitalController(patientService, mockAppointmentService);
    
    // 4. Setup data
    String patientId = "P" + System.currentTimeMillis();
    Patient patient = controller.registerPatient(
        patientId, "Karen", "Miller", LocalDate.of(1990, 2, 15), "AB+");
    
    // 5. Test error propagation
    try {
        controller.scheduleAppointment(patientId, "Checkup", LocalDateTime.now().plusDays(1));
        fail("Should have thrown exception");
    } catch (Exception e) {
        // 6. Verify controller properly wraps the error
        assertTrue(e instanceof ControllerException);
        assertTrue(e.getMessage().contains("Failed to schedule appointment"));
        assertTrue(e.getCause() instanceof ServiceException);
        assertEquals("Database connection failed", e.getCause().getMessage());
    }
    
    // 7. Verify system remains in consistent state after error
    Optional<Patient> retrievedPatient = patientService.findPatientById(patientId);
    assertTrue(retrievedPatient.isPresent(), "Patient should still exist after error");
}
```

### Example: Testing Transaction Boundaries

```java
@Test
void testTransactionRollbackOnError() {
    // 1. Setup services with transaction support
    PatientService patientService = new PatientServiceImpl();
    MedicalRecordService medicalRecordService = new MedicalRecordServiceImpl();
    
    // 2. Create a faulty billing service that always fails
    BillingService faultyBillingService = new BillingService() {
        @Override
        public Bill generateBill(Patient patient, Appointment appointment) {
            throw new RuntimeException("Billing system unavailable");
        }
        
        // Other methods implemented...
    };
    
    // 3. Create controller with transaction support
    TransactionalHospitalController controller = new TransactionalHospitalController(
        patientService, medicalRecordService, faultyBillingService);
    
    // 4. Setup test data
    String patientId = "P" + System.currentTimeMillis();
    Patient patient = patientService.savePatient(
        new Patient(patientId, "Tom", "Wilson", LocalDate.of(1972, 11, 5)));
    
    MedicalRecord medicalRecord = new MedicalRecord();
    medicalRecord.setPatient(patient);
    medicalRecord.setDiagnosis("Common cold");
    
    // 5. Attempt operation that should fail and rollback
    try {
        controller.createMedicalRecordAndBill(medicalRecord);
        fail("Should have thrown exception");
    } catch (Exception e) {
        // Expected exception
    }
    
    // 6. Verify transaction was rolled back
    List<MedicalRecord> records = medicalRecordService.findByPatientId(patientId);
    assertEquals(0, records.size(), "No medical records should exist after rollback");
}
```

## End-to-End Integration Workflow Testing

These tests validate complete workflows that span all system components.

### Example: Complete Patient Journey

```java
@Test
void testCompletePatientJourney() {
    // 1. Setup all real services
    PatientService patientService = new PatientServiceImpl();
    AppointmentService appointmentService = new AppointmentServiceImpl();
    MedicalRecordService medicalRecordService = new MedicalRecordServiceImpl();
    BillingService billingService = new BillingServiceImpl();
    InventoryService inventoryService = new InventoryServiceImpl();
    
    // 2. Create controller with all services
    HospitalController controller = new HospitalController(
        patientService, appointmentService, medicalRecordService,
        billingService, inventoryService);
    
    // 3. Register a new patient
    String patientId = "P" + System.currentTimeMillis();
    Patient patient = controller.registerPatient(
        patientId, "Emily", "Davis", LocalDate.of(1983, 4, 12), "O-");
    
    // 4. Schedule an appointment
    LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);
    Appointment appointment = controller.scheduleAppointment(
        patientId, "Initial Consultation", appointmentTime);
    
    // 5. Create medical record
    MedicalRecord medicalRecord = controller.createMedicalRecord(
        appointment.getId(), "Patient reports frequent headaches", 
        "Prescribed pain medication and blood tests");
    
    // 6. Create prescription that uses inventory
    InventoryItem medication = inventoryService.addInventoryItem(
        "MED001", "Ibuprofen 400mg", 100);
    
    Prescription prescription = controller.createPrescription(
        medicalRecord.getId(), medication.getId(), "Take 1 tablet every 6 hours", 20);
    
    // 7. Generate bill
    Bill bill = controller.generateBill(appointment.getId());
    controller.addItemToBill(bill.getId(), "Consultation Fee", BigDecimal.valueOf(100.00));
    controller.addItemToBill(bill.getId(), "Medication: " + medication.getName(), 
                           BigDecimal.valueOf(medication.getUnitPrice().doubleValue() * 20));
    
    // 8. Process payment
    Payment payment = controller.processPayment(
        bill.getId(), BigDecimal.valueOf(200.00), "CREDIT_CARD");
    
    // 9. Verify complete workflow integration
    // Patient
    Optional<Patient> retrievedPatient = patientService.findPatientById(patientId);
    assertTrue(retrievedPatient.isPresent());
    
    // Appointment
    Optional<Appointment> retrievedAppointment = appointmentService.findById(appointment.getId());
    assertTrue(retrievedAppointment.isPresent());
    
    // Medical Record
    List<MedicalRecord> patientRecords = medicalRecordService.findByPatientId(patientId);
    assertEquals(1, patientRecords.size());
    
    // Inventory (verify stock reduction)
    InventoryItem updatedMedication = inventoryService.findById(medication.getId()).get();
    assertEquals(80, updatedMedication.getQuantity()); // 100 - 20 = 80
    
    // Bill with line items
    Bill retrievedBill = controller.getBill(bill.getId());
    assertEquals(2, retrievedBill.getLineItems().size());
    
    // Payment
    assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    
    // 10. Test relationship integrity across the whole system
    assertEquals(patientId, retrievedAppointment.get().getPatient().getId());
    assertEquals(appointment.getId(), patientRecords.get(0).getAppointmentId());
    assertEquals(patientId, retrievedBill.getPatientId());
}
```

## Integration Test Documentation Template

For each integration test, document:

```
# Integration Test Report: [Test Name]

## Components Integrated
- [Component 1]
- [Component 2]
- [Component 3]

## Integration Points Tested
1. [Description of integration point 1]
2. [Description of integration point 2]

## Test Scenarios
1. [Description of scenario 1]
   - Expected outcome: [Description]
   - Actual outcome: [Description]
   - Status: ✅/❌

2. [Description of scenario 2]
   - Expected outcome: [Description]
   - Actual outcome: [Description]
   - Status: ✅/❌

## Error Scenarios Tested
1. [Description of error scenario 1]
   - Expected error handling: [Description]
   - Actual error handling: [Description]
   - Status: ✅/❌

## Data Flow Verification
- [Description of data verification point 1]: ✅/❌
- [Description of data verification point 2]: ✅/❌

## Issues Identified
1. [Issue description, if any]
2. [Issue description, if any]

## Recommendations
1. [Recommendation]
2. [Recommendation]
```

## Running Integration Tests in IntelliJ

1. Configure a run configuration specifically for integration tests:
   - Open Run/Debug Configurations
   - Create a new JUnit configuration
   - Select "Tags" option and enter "integration"
   - Name it "Integration Tests"

2. Use Maven to run integration tests separately:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <groups>integration</groups>
    </configuration>
</plugin>
```

## Conclusion

Comprehensive integration testing ensures that all components of the Hospital Management System work together correctly. By testing both the happy paths and error scenarios, we can verify that the system remains robust and reliable under various conditions.

The approach outlined in this document allows for thorough testing of integration points while keeping tests manageable and focused on specific workflows and component interactions. 