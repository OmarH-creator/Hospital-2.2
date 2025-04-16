# Enhanced JUnit Testing Approaches

## Test Documentation and Reporting

### Documenting Test Cases

For each test class, include a clear documentation header:

```java
/**
 * Test class for Patient model.
 * 
 * Tests cover:
 * - Basic object creation and validation
 * - Patient information retrieval
 * - Age calculation logic
 * - Status management (admit/discharge)
 */
public class PatientTest {
    // Tests follow...
}
```

For individual test methods, use descriptive names and include documentation:

```java
/**
 * Tests that age is calculated correctly for various scenarios:
 * - Normal case (past birthdate)
 * - Edge case (birthday today)
 * - Special case (null birthdate)
 */
@Test
@DisplayName("Patient age calculation should handle various scenarios")
void testAgeCalculation() {
    // Test implementation...
}
```

### Test Reporting

Configure JUnit to generate detailed HTML reports with Maven Surefire:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Suite.java</include>
        </includes>
        <reportFormat>html</reportFormat>
        <reportNameSuffix>test-report</reportNameSuffix>
    </configuration>
</plugin>
```

Create a simple test summary template:

```
# Test Summary Report

## Overview
- Total Tests: [NUMBER]
- Passed: [NUMBER]
- Failed: [NUMBER]
- Skipped: [NUMBER]

## Test Coverage
- Model Layer: [PERCENTAGE]%
- Service Layer: [PERCENTAGE]%
- Controller Layer: [PERCENTAGE]%

## Failed Tests Summary
1. [TEST_NAME] - [BRIEF_DESCRIPTION_OF_FAILURE]
2. [TEST_NAME] - [BRIEF_DESCRIPTION_OF_FAILURE]

## Action Items
1. [ACTION_ITEM]
2. [ACTION_ITEM]
```

## Parameterized Tests

Use parameterized tests for more efficient testing of similar scenarios:

### Example: Testing Patient Validation with Different Inputs

Here's an example from our implemented PatientValidationTest class:

```java
@ParameterizedTest(name = "Age calculation with birth date = {0}")
@MethodSource("birthdateParameters")
@DisplayName("Test age calculation for various birth dates")
void testAgeCalculation(LocalDate dateOfBirth, int expectedAge) {
    Patient patient = new Patient("P001", "John", "Doe", dateOfBirth);
    assertEquals(expectedAge, patient.getAge(), "Age should be calculated correctly");
}

/**
 * Test patient with null birth date
 */
@ParameterizedTest
@NullSource
@DisplayName("Test patient with null birth date")
void testNullBirthDate(LocalDate dateOfBirth) {
    Patient patient = new Patient("P001", "John", "Doe", dateOfBirth);
    assertEquals(0, patient.getAge(), "Age should be 0 when birth date is null");
}

/**
 * Test blood type validation with various values
 */
@ParameterizedTest(name = "Blood type validation with value = {0}")
@ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
@DisplayName("Test valid blood type assignment")
void testValidBloodTypeAssignment(String bloodType) {
    Patient patient = new Patient("P001", "John", "Doe", LocalDate.now());
    patient.setBloodType(bloodType);
    assertEquals(bloodType, patient.getBloodType(), "Blood type should match the assigned value");
}

// MethodSource for parameterized tests
private static Stream<Arguments> birthdateParameters() {
    LocalDate today = LocalDate.now();
    
    return Stream.of(
        // Today's date from 20 years ago
        Arguments.of(today.minusYears(20), 20),
        
        // Today's date minus 1 day from 30 years ago (30 years old)
        Arguments.of(today.minusYears(30).minusDays(1), 30),
        
        // Today's date plus 1 day from 40 years ago (39 years old as birthday hasn't occurred yet this year)
        Arguments.of(today.minusYears(40).plusDays(1), 39),
        
        // Today's date from 10 years ago (exactly 10 years old today)
        Arguments.of(today.minusYears(10), 10),
        
        // Future date (not born yet) - should handle this case
        Arguments.of(today.plusYears(1), 0)
    );
}
```

### Example: Testing Bill Operations with Multiple Parameters

From our BillingOperationsTest class:

```java
/**
 * Test adding items to bill with various descriptions and amounts
 */
@ParameterizedTest(name = "Add item: {0}, ${1}")
@CsvSource({
    "Consultation, 100.00",
    "X-Ray, 250.50",
    "Blood Test, 75.25",
    "Surgery, 5000.00",
    "Prescription, 45.99"
})
@DisplayName("Test adding items to bill")
void testAddItemToBill(String description, double amount) {
    Bill bill = new Bill("B001", testPatient, issueDate);
    bill.addItem(description, amount);
    
    assertEquals(1, bill.getItems().size(), "Bill should have one item");
    Bill.BillItem item = bill.getItems().get(0);
    assertEquals(description, item.getDescription(), "Item description should match");
    assertEquals(amount, item.getAmount(), "Item amount should match");
    assertEquals(amount, bill.getTotalAmount(), "Bill total should be updated correctly");
}

/**
 * Test adding multiple items to bill
 */
@ParameterizedTest(name = "Multiple items test")
@MethodSource("multipleItemsParameters")
@DisplayName("Test adding multiple items to bill")
void testAddMultipleItemsToBill(String[] descriptions, double[] amounts, double expectedTotal) {
    Bill bill = new Bill("B001", testPatient, issueDate);
    
    for (int i = 0; i < descriptions.length; i++) {
        bill.addItem(descriptions[i], amounts[i]);
    }
    
    assertEquals(descriptions.length, bill.getItems().size(), "Bill should have correct number of items");
    assertEquals(expectedTotal, bill.getTotalAmount(), 0.001, "Bill total should be calculated correctly");
}

private static Stream<Arguments> multipleItemsParameters() {
    return Stream.of(
        // Three items
        Arguments.of(
            new String[]{"Consultation", "X-Ray", "Medication"},
            new double[]{100.0, 250.0, 75.0},
            425.0
        ),
        // Five items
        Arguments.of(
            new String[]{"Room charge", "Surgery", "Anesthesia", "Recovery", "Medication"},
            new double[]{500.0, 3000.0, 1000.0, 800.0, 200.0},
            5500.0
        ),
        // Single item
        Arguments.of(
            new String[]{"Emergency visit"},
            new double[]{350.0},
            350.0
        ),
        // Zero items (empty arrays)
        Arguments.of(
            new String[]{},
            new double[]{},
            0.0
        )
    );
}
```

### Example: Testing Appointment Status Transitions

From our AppointmentValidationTest class:

```java
/**
 * Test appointment status changes
 */
@ParameterizedTest(name = "Status change: {0}")
@MethodSource("statusChangeParameters")
@DisplayName("Test appointment status changes")
void testStatusChanges(String action, String expectedStatus) {
    Appointment appointment = new Appointment("A001", testPatient, "Checkup", LocalDateTime.now());
    
    // Apply the action
    switch (action) {
        case "complete":
            appointment.complete();
            break;
        case "cancel":
            appointment.cancel();
            break;
        case "reschedule":
            appointment.reschedule(LocalDateTime.now().plusDays(1));
            break;
    }
    
    assertEquals(expectedStatus, appointment.getStatus(), 
                 "Appointment status should be updated correctly after " + action);
}

private static Stream<Arguments> statusChangeParameters() {
    return Stream.of(
        Arguments.of("complete", "COMPLETED"),
        Arguments.of("cancel", "CANCELLED"),
        Arguments.of("reschedule", "RESCHEDULED")
    );
}
```

## Cross-Service Workflow Testing

A new approach implemented in the project is cross-service workflow testing, which validates the interactions between multiple services through the HospitalController:

```java
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
}
```

This comprehensive approach ensures that not only do individual components work correctly in isolation, but they also interact properly as part of real-world workflows.

## Measuring Test Coverage

### IntelliJ IDEA Coverage Configuration

1. **Setup IntelliJ for Coverage**:
   - Go to Run → Edit Configurations
   - Select your test configuration
   - Check "With Coverage" option
   - Select JaCoCo as the coverage runner

2. **Running Tests with Coverage**:
   - Right-click on a test class/package
   - Select "Run with Coverage"
   - View results in the Coverage tool window

### JaCoCo Maven Configuration

Add JaCoCo plugin to your pom.xml:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run with: `mvn clean test`

### Coverage Goals

| Layer       | Minimum Coverage Target |
|-------------|-------------------------|
| Model       | 90%                     |
| Service     | 85%                     |
| Controller  | 80%                     |

### Coverage Metrics to Track

1. **Statement Coverage**: Percentage of code statements executed
2. **Branch Coverage**: Percentage of branches (if/else paths) executed
3. **Method Coverage**: Percentage of methods called

### Interpreting Coverage Reports

- **Green**: Covered code
- **Red**: Uncovered code
- **Yellow**: Partially covered branches

Focus improvements on:
1. Critical business logic paths
2. Error handling code
3. Boundary conditions 