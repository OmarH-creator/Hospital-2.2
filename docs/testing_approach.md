# Hospital Management System Testing Approach

## Introduction
This document outlines the testing approach for our minimal Hospital Management System. We will implement a simple but functional system with core hospital operations using Java and JavaFX, and focus on effective testing strategies.

## Current State Analysis

### Required Components
- **Models**: Patient, Appointment, MedicalRecord, Bill, InventoryItem
- **Services**: PatientService, AppointmentService, MedicalRecordService, BillingService, InventoryService
- **Controller**: HospitalController
- **UI**: Minimal JavaFX screens for each operation

### Testing Requirements
1. JUnit testing approaches with test suites
2. White box testing on at least 3 classes
3. GUI testing with FSM for various scenarios
4. Integration testing of the whole application

## 1. JUnit Testing Approach

We will implement a streamlined testing approach with well-organized test suites.

### Test Organization

```
src/test/
├── java/
    └── com/example/hospitalsystemsimpletesting/
        ├── model/
        │   ├── PatientTest.java
        │   ├── AppointmentTest.java
        │   ├── MedicalRecordTest.java
        │   ├── BillTest.java
        │   └── InventoryItemTest.java
        ├── service/
        │   ├── PatientServiceTest.java
        │   ├── AppointmentServiceTest.java
        │   ├── MedicalRecordServiceTest.java
        │   ├── BillingServiceTest.java
        │   └── InventoryServiceTest.java
        ├── controller/
        │   └── HospitalControllerTest.java
        ├── view/
        │   └── [Basic GUI tests]
        ├── integration/
        │   └── [Simple integration tests]
        └── suites/
            ├── ModelTestSuite.java
            ├── ServiceTestSuite.java
            └── AllTestsSuite.java
```

### Test Suite Example

```java
@Suite
@SelectClasses({
    PatientTest.class,
    AppointmentTest.class,
    MedicalRecordTest.class,
    BillTest.class,
    InventoryItemTest.class
})
public class ModelTestSuite {
    // This class holds suite annotations
}
```

### Testing Techniques

For simplicity, we'll focus on:
- Core functionality testing
- Basic validation testing
- Simple error handling

## 2. White Box Testing

We'll perform streamlined white box testing on three key classes:

### Patient Class (Model)
Simple example test case:
```java
@Test
void testPatientCreation() {
    Patient patient = new Patient("P1", "John", "Doe", LocalDate.of(1990, 1, 1));
    assertEquals("P1", patient.getId());
    assertEquals("John Doe", patient.getFullName());
}
```

### AppointmentService Class (Service)
Simple example test case:
```java
@Test
void testAppointmentScheduling() {
    Patient patient = new Patient("P1", "John", "Doe", LocalDate.now());
    LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
    
    Appointment appointment = appointmentService.scheduleAppointment(
        patient, "General Checkup", appointmentTime);
    
    assertNotNull(appointment);
    assertEquals(patient, appointment.getPatient());
    assertEquals("General Checkup", appointment.getType());
}
```

### HospitalController Class (Controller)
Simple example test case:
```java
@Test
void testPatientRegistrationFlow() {
    // Mock service
    when(patientService.savePatient(any(Patient.class))).thenReturn(new Patient("P1", "John", "Doe", null));
    
    // Test controller
    Patient result = hospitalController.registerPatient("P1", "John", "Doe", LocalDate.now());
    
    // Verify
    assertNotNull(result);
    verify(patientService).savePatient(any(Patient.class));
}
```

## 3. GUI Testing with FSM

We'll create a simple Finite State Machine for our UI:

### Basic FSM

```
[Main Menu]
  ├──> [Patient Registration]
  ├──> [Appointment Scheduling]
  ├──> [Medical Records View]
  ├──> [Billing Screen]
  └──> [Inventory Management]
```

### Basic TestFX Example

```java
@Test
void testNavigationToPatientRegistration() {
    // Click on the patient registration button from main menu
    clickOn("#patientRegistrationButton");
    
    // Verify we're on the registration screen
    verifyThat("#patientRegistrationForm", isVisible());
}
```

## 4. Integration Testing

We'll focus on testing key workflows that span multiple components:

### Example Integration Test

```java
@Test
void testPatientRegistrationAndAppointmentBooking() {
    // 1. Register patient
    PatientService patientService = new PatientServiceImpl();
    AppointmentService appointmentService = new AppointmentServiceImpl();
    HospitalController controller = new HospitalController(patientService, appointmentService);
    
    Patient patient = controller.registerPatient("P1", "John", "Doe", LocalDate.now());
    
    // 2. Book appointment for the patient
    LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
    Appointment appointment = controller.scheduleAppointment(
        patient.getId(), "General Checkup", appointmentTime);
    
    // 3. Verify
    assertNotNull(appointment);
    assertEquals(patient.getId(), appointment.getPatient().getId());
}
```

## Implementation Steps

1. **Create Basic Models**
   - Simple Patient, Appointment, MedicalRecord, Bill, and InventoryItem classes

2. **Implement Core Services**
   - Basic operations for each service

3. **Build Minimal Controller**
   - Essential business logic only

4. **Create Simple UI**
   - Minimal screens for each operation

5. **Implement Tests**
   - Start with unit tests
   - Add simple whitebox tests
   - Create basic UI tests
   - Finish with integration tests

## Conclusion

This minimalist approach ensures we can:
- Test all required aspects of the system
- Keep implementation simple and maintainable
- Focus on quality over quantity
- Meet all testing requirements efficiently 