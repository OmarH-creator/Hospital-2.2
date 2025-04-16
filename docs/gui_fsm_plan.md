# GUI Testing with Finite State Machine (FSM)

## Overview

This document outlines the Finite State Machine (FSM) approach for testing the Hospital Management System GUI. We'll use a minimalist approach focusing on the core functionality.

## States and Transitions

### Main States

1. **Main Menu** - The starting point of the application
2. **Patient Registration** - Form for registering new patients
3. **Appointment Scheduling** - Interface for scheduling appointments
4. **Medical Records** - View for accessing patient medical records
5. **Billing** - Screen for billing operations
6. **Inventory** - Screen for inventory management

### FSM Diagram

```
         ┌─────────────────────────────────────────┐
         │                                         │
         │             MAIN MENU                   │
         │                                         │
         └─────────────────────────────────────────┘
           │           │           │           │
           ▼           ▼           ▼           ▼
┌─────────────┐ ┌─────────────┐ ┌─────────┐ ┌─────────┐
│   Patient   │ │ Appointment │ │ Medical │ │ Billing │
│ Registration│ │ Scheduling  │ │ Records │ │         │
└─────────────┘ └─────────────┘ └─────────┘ └─────────┘
       │                │             │           │
       └────────────────┴─────────────┴───────────┘
                        │
                        ▼
               ┌─────────────────┐
               │    Inventory    │
               └─────────────────┘
```

## Transition Table

| Current State       | Event                  | Next State           | Output                  |
|---------------------|------------------------|----------------------|-------------------------|
| Main Menu           | Click Register         | Patient Registration | Registration form shown |
| Main Menu           | Click Schedule         | Appointment          | Scheduling form shown   |
| Main Menu           | Click Records          | Medical Records      | Records view shown      |
| Main Menu           | Click Billing          | Billing              | Billing screen shown    |
| Main Menu           | Click Inventory        | Inventory            | Inventory screen shown  |
| Patient Registration| Submit                 | Main Menu            | Patient saved           |
| Patient Registration| Cancel                 | Main Menu            | No changes              |
| Appointment         | Submit                 | Main Menu            | Appointment scheduled   |
| Appointment         | Cancel                 | Main Menu            | No changes              |
| Medical Records     | View Patient           | Medical Records      | Patient details shown   |
| Medical Records     | Back                   | Main Menu            | Returns to main menu    |
| Billing             | Process Payment        | Billing              | Payment processed       |
| Billing             | Back                   | Main Menu            | Returns to main menu    |
| Inventory           | Update Item            | Inventory            | Item updated            |
| Inventory           | Back                   | Main Menu            | Returns to main menu    |

## Test Scenarios

### Scenario 1: Patient Registration Flow
1. Start at Main Menu
2. Click on Patient Registration button
3. Fill out patient information
4. Click Submit
5. Verify return to Main Menu
6. Verify patient was saved

### Scenario 2: Appointment Scheduling Flow
1. Start at Main Menu
2. Click on Appointment Scheduling
3. Select patient and appointment details
4. Click Schedule
5. Verify return to Main Menu
6. Verify appointment was saved

### Scenario 3: Navigation Flow
1. Start at Main Menu
2. Navigate to each screen
3. Verify correct screen is displayed
4. Return to Main Menu
5. Verify Main Menu is displayed

## TestFX Implementation

Basic structure for TestFX tests:

```java
@ExtendWith(ApplicationExtension.class)
class GuiTest {
    
    private Stage stage;
    
    @Start
    private void start(Stage stage) {
        this.stage = stage;
        // Start the application
        new HospitalApplication().start(stage);
    }
    
    @Test
    void testNavigationToPatientRegistration() {
        // Click on the patient registration button
        clickOn("#patientButton");
        
        // Verify we're on the registration screen
        verifyThat("#patientRegistrationForm", isVisible());
        
        // Go back to main menu
        clickOn("#backButton");
        
        // Verify we're back at the main menu
        verifyThat("#mainMenu", isVisible());
    }
    
    // Additional tests for other scenarios
}
```

## Test Implementation Plan

1. Create basic TestFX setup
2. Implement tests for main navigation flows
3. Implement tests for data entry forms
4. Implement tests for full workflows (e.g., register patient then schedule appointment)

## Conclusion

This minimal FSM approach provides a structured way to test the Hospital Management System GUI, focusing on the core functionality while keeping the testing scope manageable. 