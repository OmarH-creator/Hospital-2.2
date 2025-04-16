# White Box Testing Guide

This guide provides detailed instructions on how to perform effective white box testing on three key classes in our Hospital Management System.

## Introduction to White Box Testing

White box testing examines the internal structure of the software, including:
- Code paths
- Conditional statements
- Loops
- Exception handling

For our project, we will focus on these key aspects:
1. Statement coverage
2. Branch coverage 
3. Path coverage
4. Boundary and edge case testing

## Measuring Code Coverage with IntelliJ IDEA

### Setting Up Coverage Analysis

1. **Configure IntelliJ for Coverage**:
   - Open IntelliJ IDEA
   - Go to `Run → Edit Configurations`
   - Select the test configuration you want to run
   - Click the `Code Coverage` tab
   - Select `JaCoCo` as the coverage runner
   - Check `Track per test coverage` option for detailed analysis

2. **Running Coverage Analysis**:
   - Right-click on a test class
   - Select `Run [TestClass] with Coverage`
   - IntelliJ will execute the tests and display coverage results

3. **Interpreting Results**:
   - Green: Covered code
   - Red: Uncovered code
   - Yellow: Partially covered branches
   - Numbers on the left gutter show how many times each line executed

4. **Coverage Reports**:
   - Click the `Generate Coverage Report` icon in the Coverage tool window
   - Save as HTML for documentation

### Example: Coverage View for Patient Class

```
Patient.java                  Coverage: 87%
---------------------------------------------
 9  | public class Patient {
10  |     private String id;             ✓
11  |     private String firstName;      ✓
12  |     private String lastName;       ✓
13  |     private LocalDate dateOfBirth; ✓
14  |     private boolean admitted;      ✓
15  |     private String bloodType;      ✓
16  | 
17  |     public Patient(...) {          ✓
18  |         if (id == null || id.trim().isEmpty()) {       ✓
19  |             throw new IllegalArgumentException(...);   ✓
20  |         }
...
47  |     public int getAge() {          ✓
48  |         if (dateOfBirth == null) { ✓
49  |             return 0;              ✓
50  |         }
51  |         
52  |         LocalDate now = LocalDate.now();               ✓
53  |         return Period.between(dateOfBirth, now).getYears(); ✓
54  |     }
```

## Identifying Untested Branches

1. **Locating Branches in Code**:
   - Look for conditional statements (`if`, `else`, `switch`)
   - Look for loops (`for`, `while`)
   - Look for exception handling (`try/catch`)

2. **Using IntelliJ's Branch Coverage**:
   - In coverage results, yellow highlights indicate partially covered branches
   - Hover over yellow highlights to see which branches weren't covered

3. **Branch Listing Example**:
   ```
   Method: Patient.getAge()
   Branches:
   - if (dateOfBirth == null): true ✓, false ✓
   
   Method: PatientService.admitPatient()
   Branches:
   - if (patient == null): true ✓, false ✓
   - if (patient.isAdmitted()): true ✓, false ✓
   ```

## White Box Testing for Patient Class

### Critical Paths to Test

1. **Constructor Validation**:
   - Test all branch conditions in validation logic
   - Check exceptions thrown for invalid inputs

2. **Age Calculation Logic**:
   - Test with null birth date
   - Test with future birth date
   - Test with birth date today
   - Test with birth date in the past

### Example Test Cases

```java
@Test
void testConstructorValidationBranches() {
    // Branch 1: null ID throws exception
    assertThrows(IllegalArgumentException.class, () -> 
        new Patient(null, "John", "Doe", LocalDate.now()));
    
    // Branch 2: empty ID throws exception
    assertThrows(IllegalArgumentException.class, () -> 
        new Patient("", "John", "Doe", LocalDate.now()));
    
    // Branch 3: valid ID passes
    Patient patient = new Patient("P1", "John", "Doe", LocalDate.now());
    assertEquals("P1", patient.getId());
}

@Test
void testAgeCalculationBranches() {
    // Branch 1: null birth date
    Patient patient1 = new Patient("P1", "John", "Doe", null);
    assertEquals(0, patient1.getAge());
    
    // Branch 2: future birth date
    Patient patient2 = new Patient("P2", "Jane", "Doe", 
                                  LocalDate.now().plusYears(1));
    assertEquals(0, patient2.getAge());
    
    // Branch 3: past birth date
    Patient patient3 = new Patient("P3", "Jim", "Doe", 
                                  LocalDate.now().minusYears(30));
    assertEquals(30, patient3.getAge());
}
```

## White Box Testing for AppointmentService

### Critical Paths to Test

1. **Appointment Scheduling Logic**:
   - Valid appointment creation
   - Invalid appointment parameters
   - Scheduling conflicts

2. **Appointment Retrieval Logic**:
   - Finding existing appointments
   - Handling non-existent appointments
   - Filtering appointments by criteria

### Example of Control Flow Testing

```java
@Test
void testAppointmentSchedulingControlFlow() {
    PatientService patientService = new PatientServiceImpl();
    AppointmentService appointmentService = new AppointmentServiceImpl();
    
    // Path 1: Schedule valid appointment
    Patient patient = new Patient("P1", "John", "Doe", LocalDate.now().minusYears(30));
    LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
    Appointment appointment = appointmentService.scheduleAppointment(patient, "Checkup", futureTime);
    assertNotNull(appointment);
    
    // Path 2: Schedule with past time (should fail)
    LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
    assertThrows(IllegalArgumentException.class, () -> 
        appointmentService.scheduleAppointment(patient, "Checkup", pastTime));
    
    // Path 3: Schedule with null patient (should fail)
    assertThrows(IllegalArgumentException.class, () -> 
        appointmentService.scheduleAppointment(null, "Checkup", futureTime));
}
```

## White Box Testing for HospitalController

### Critical Paths to Test

1. **Controller Method Validation**:
   - Parameter validation for all methods
   - Exception propagation from services
   - Error handling logic

2. **Business Logic Coordination**:
   - Integration of multiple service calls
   - Workflows spanning multiple operations

### Example of Testing Exception Handling

```java
@Test
void testControllerExceptionHandlingPaths() {
    // Mock service to throw exceptions
    PatientService mockService = mock(PatientService.class);
    when(mockService.findPatientById("P1"))
        .thenThrow(new RuntimeException("Database error"));
    
    HospitalController controller = new HospitalController(mockService);
    
    // Test exception handling path
    try {
        controller.getPatient("P1");
        fail("Should have thrown exception");
    } catch (RuntimeException e) {
        // Verify controller handled or wrapped the exception correctly
        assertTrue(e.getMessage().contains("Error retrieving patient"));
    }
}
```

## Boundary Testing

Boundary testing is a crucial aspect of white box testing that focuses on values at or near the boundaries of input domains.

### Example: Boundary Test for Appointment Scheduling

```java
@Test
void testAppointmentSchedulingBoundaries() {
    Patient patient = new Patient("P1", "John", "Doe", LocalDate.now().minusYears(30));
    LocalDateTime now = LocalDateTime.now();
    
    // Test exact boundary: Appointment exactly now
    Appointment appointment1 = appointmentService.scheduleAppointment(
        patient, "Emergency", now);
    assertNotNull(appointment1);
    
    // Test near boundary: Appointment 1 minute in the past
    LocalDateTime justPast = now.minusMinutes(1);
    assertThrows(IllegalArgumentException.class, () -> 
        appointmentService.scheduleAppointment(patient, "Checkup", justPast));
    
    // Test near boundary: Appointment 1 minute in the future
    LocalDateTime justFuture = now.plusMinutes(1);
    Appointment appointment2 = appointmentService.scheduleAppointment(
        patient, "Checkup", justFuture);
    assertNotNull(appointment2);
    
    // Test far boundary: Appointment at maximum allowed scheduling date
    LocalDateTime maxDate = now.plusYears(1);
    Appointment appointment3 = appointmentService.scheduleAppointment(
        patient, "Annual Checkup", maxDate);
    assertNotNull(appointment3);
    
    // Test beyond boundary: Appointment beyond maximum allowed scheduling date
    LocalDateTime beyondMaxDate = now.plusYears(1).plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> 
        appointmentService.scheduleAppointment(patient, "Checkup", beyondMaxDate));
}
```

## Documentation of White Box Testing

For each class tested, document:

1. **Statement Coverage**: Percentage of code statements executed
2. **Branch Coverage**: Percentage of decision branches executed
3. **Path Coverage**: Key paths through the code that were tested
4. **Uncovered Areas**: Note any code that remains untested with justification

### Documentation Template

```
## White Box Testing Report: [ClassName]

### Coverage Statistics
- Statement Coverage: [XX]%
- Branch Coverage: [XX]%
- Method Coverage: [XX]%

### Key Paths Tested
1. [Path description]
2. [Path description]
3. [Path description]

### Boundary Tests
1. [Boundary description]
2. [Boundary description]

### Uncovered Areas
1. [Description of uncovered code] - [Justification]
2. [Description of uncovered code] - [Justification]

### Recommendations
1. [Recommendation for improving coverage]
2. [Recommendation for improving code]
```

## Completed White Box Testing Implementation

We have successfully implemented white box testing for three key model classes in our system:

### 1. Patient Class White Box Testing

The `PatientWhiteBoxTest` class implements comprehensive white box testing for the `Patient` class with the following coverage:

- **Constructor Validation Paths**:
  - Null ID validation
  - Empty ID validation
  - Null firstName validation
  - Null lastName validation
  - Valid parameter validation

- **Age Calculation Logic**:
  - Null birth date handling
  - Future birth date handling
  - Past birth date calculations

- **Patient Status Management**:
  - Admit/discharge state transitions
  - Repeated admit/discharge handling

### 2. Appointment Class White Box Testing

The `AppointmentWhiteBoxTest` class implements detailed white box tests for the `Appointment` class:

- **Constructor Validation Paths**:
  - Null and empty ID validation
  - Null patient validation
  - Null and empty type validation 
  - Null dateTime validation

- **Status Transition Logic**:
  - Complete method branches
  - Cancel method branches
  - Reschedule method branches
  - Status transitions from various states

### 3. InventoryItem Class White Box Testing

The `InventoryItemWhiteBoxTest` class provides thorough white box testing for the `InventoryItem` class:

- **Constructor Validation Paths**:
  - Null and empty ID validation
  - Null and empty name validation
  - Negative quantity validation
  - Negative unitPrice validation

- **Stock Management Logic**:
  - Adding stock with valid amounts
  - Adding zero or negative amounts
  - Removing stock with valid amounts 
  - Removing with insufficient stock
  - Removing zero or negative amounts

- **Inventory Status Logic**:
  - Various threshold conditions for low stock detection
  - Total value calculations

### Test Implementation Notes

All white box tests were implemented with these key practices:

1. **Specific Error Message Testing**: Tests were updated to use `assertEquals` instead of `assertTrue(...contains())` to ensure exact error message matching.

2. **Parameterized Testing**: Used `@ParameterizedTest` with various sources (`@NullAndEmptySource`, `@ValueSource`, `@CsvSource`) to test multiple variations of inputs with the same test logic.

3. **Branch Coverage**: Each `if` statement branch was explicitly tested for both true and false paths.

4. **Boundary Testing**: Edge cases were explicitly tested (zero values, boundary conditions, etc.).

5. **State Verification**: After method calls, object state was verified to ensure expected changes (or no changes in error cases).

These improvements resulted in higher test coverage and exposed several bugs that were subsequently fixed, including:

- Missing validation in the `Patient` class constructor
- Incorrect error message assertions in test cases
- Conflicts between validation in constructors and services

The white box testing phase is now complete with all tests passing successfully.

## Conclusion

White box testing is essential for ensuring thorough code coverage and finding bugs in edge cases and boundary conditions. By systematically examining internal code structures, we can build more robust and reliable software. 