# Enhanced GUI Testing with TestFX

This document provides detailed guidance on implementing GUI testing for the Hospital Management System using TestFX, focusing on asynchronous operations and comprehensive test documentation.

## Setting Up TestFX

### Dependencies

Add these dependencies to your `pom.xml`:

```xml
<!-- TestFX Core -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-core</artifactId>
    <version>4.0.16-alpha</version>
    <scope>test</scope>
</dependency>

<!-- TestFX JUnit 5 -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.16-alpha</version>
    <scope>test</scope>
</dependency>

<!-- For headless testing (optional) -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>openjfx-monocle</artifactId>
    <version>jdk-12.0.1+2</version>
    <scope>test</scope>
</dependency>
```

### Basic Test Structure

Create a base test class to handle common setup:

```java
package com.example.hospitalsystemsimpletesting.view;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.TimeoutException;

@ExtendWith(ApplicationExtension.class)
public abstract class BaseGuiTest {
    
    protected FxRobot robot;
    
    @Start
    public void start(Stage stage) throws Exception {
        stage.show();
    }
    
    @BeforeEach
    public void setUp() {
        robot = new FxRobot();
    }
    
    @AfterEach
    public void tearDown() throws TimeoutException {
        FxToolkit.hideStage();
        robot.release(new KeyCode[]{});
        robot.release(new MouseButton[]{});
    }
    
    // Utility methods for all tests
    protected <T extends Node> T find(String query) {
        return robot.lookup(query).query();
    }
}
```

## Handling Asynchronous Operations

GUI testing often involves asynchronous operations that can lead to flaky tests if not handled properly.

### Waiting for UI Updates

```java
// Method to wait for a node to appear
protected void waitForNode(String query) {
    robot.waitFor(query, node -> node.isVisible(), 10);
}

// Method to wait for a specific condition
protected <T extends Node> void waitFor(T node, Predicate<T> condition) {
    robot.waitFor(node, condition::test, 10);
}

// Example usage in a test
@Test
void testPatientRegistration() {
    // Navigate to registration screen
    clickOn("#registerButton");
    
    // Wait for the form to appear
    waitForNode("#patientRegistrationForm");
    
    // Fill out form
    clickOn("#idField").write("P001");
    clickOn("#firstNameField").write("John");
    clickOn("#lastNameField").write("Doe");
    
    // Click submit and wait for success message
    clickOn("#submitButton");
    waitForNode("#successMessage");
    
    // Verify message
    verifyThat("#successMessage", hasText("Patient registered successfully"));
}
```

### Handling JavaFX Platform Thread

TestFX runs on the JavaFX Platform thread, but sometimes you need to ensure operations are completed:

```java
// Method to execute on JavaFX thread and wait for completion
protected void runAndWait(Runnable action) {
    try {
        FxToolkit.setupFixture(action::run);
    } catch (TimeoutException e) {
        throw new RuntimeException(e);
    }
}

// Example usage
@Test
void testDatabaseOperation() {
    // Navigate to screen
    clickOn("#patientsButton");
    
    // Perform action that triggers async database operation
    clickOn("#loadPatientsButton");
    
    // Wait for completion
    runAndWait(() -> {
        // This will run on the JavaFX thread and wait for completion
        controller.finishLoadingPatients();
    });
    
    // Now verify results
    verifyThat("#patientList", hasItems(1));
}
```

### Using TestFX's Await Methods

For more complex waiting scenarios:

```java
// Wait for a specific condition with custom timeout
WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> 
    !lookup("#patientList").queryListView().getItems().isEmpty()
);

// Wait for background tasks
WaitForAsyncUtils.waitForFxEvents();
```

## Advanced FSM Testing

Implement comprehensive FSM testing based on our defined states and transitions:

```java
@Test
void testCompleteUserJourney() {
    // STATE: Main Menu
    verifyThat("#mainMenu", isVisible());
    
    // TRANSITION: Main Menu -> Patient Registration
    clickOn("#registerButton");
    verifyThat("#patientRegistrationForm", isVisible());
    
    // Fill registration form
    clickOn("#idField").write("P001");
    clickOn("#firstNameField").write("John");
    clickOn("#lastNameField").write("Doe");
    
    // TRANSITION: Submit form -> Return to Main Menu
    clickOn("#submitButton");
    waitForNode("#successMessage");
    clickOn("#okButton");
    
    // STATE: Back at Main Menu
    verifyThat("#mainMenu", isVisible());
    
    // TRANSITION: Main Menu -> Appointment Scheduling
    clickOn("#scheduleButton");
    verifyThat("#appointmentForm", isVisible());
    
    // Fill appointment form
    clickOn("#patientIdField").write("P001");
    clickOn("#appointmentTypeField").write("Checkup");
    clickOn("#datePicker").write("01/15/2025");
    
    // TRANSITION: Submit appointment -> Return to Main Menu
    clickOn("#scheduleAppointmentButton");
    waitForNode("#appointmentConfirmation");
    clickOn("#okButton");
    
    // STATE: Back at Main Menu
    verifyThat("#mainMenu", isVisible());
    
    // Test complete user journey through FSM
}
```

## Capturing and Documenting Test Results

### Screenshot Capture

Create a method to capture screenshots during test execution:

```java
// Add to BaseGuiTest class
protected void captureScreenshot(String name) {
    try {
        robot.capture(robot.rootNode(robot.window(0)))
            .save("target/screenshots/" + name + ".png");
    } catch (Exception e) {
        System.err.println("Failed to capture screenshot: " + e.getMessage());
    }
}

// Use in tests
@Test
void testPatientRegistration() {
    clickOn("#registerButton");
    captureScreenshot("01-registration-form");
    
    // Fill form...
    captureScreenshot("02-filled-form");
    
    clickOn("#submitButton");
    captureScreenshot("03-submission-result");
}
```

### Creating Comprehensive Test Reports

Create a structured output format for GUI test results:

```java
// Add reporting capabilities to BaseGuiTest
private final List<String> testSteps = new ArrayList<>();
private final Map<String, String> screenshots = new HashMap<>();

protected void recordStep(String description) {
    testSteps.add(description);
    String screenshotName = testSteps.size() + "-" + description.toLowerCase().replace(' ', '-');
    captureScreenshot(screenshotName);
    screenshots.put(description, screenshotName + ".png");
}

@AfterEach
public void generateReport() {
    // Create simple HTML report
    try (PrintWriter writer = new PrintWriter(new FileWriter(
            "target/gui-test-reports/" + getClass().getSimpleName() + 
            "-" + testInfo.getDisplayName() + ".html"))) {
        
        writer.println("<html><head><title>GUI Test Report</title></head><body>");
        writer.println("<h1>Test: " + testInfo.getDisplayName() + "</h1>");
        writer.println("<div class='test-steps'>");
        
        for (String step : testSteps) {
            writer.println("<div class='step'>");
            writer.println("  <p>" + step + "</p>");
            if (screenshots.containsKey(step)) {
                writer.println("  <img src='../screenshots/" + screenshots.get(step) + "' />");
            }
            writer.println("</div>");
        }
        
        writer.println("</div></body></html>");
    } catch (IOException e) {
        System.err.println("Failed to generate report: " + e.getMessage());
    }
}

// Example test using reporting
@Test
void testCompleteRegistrationFlow() {
    recordStep("Starting at main menu");
    
    clickOn("#registerButton");
    recordStep("Navigated to registration form");
    
    clickOn("#idField").write("P001");
    clickOn("#firstNameField").write("John");
    clickOn("#lastNameField").write("Doe");
    recordStep("Filled patient information");
    
    clickOn("#submitButton");
    waitForNode("#successMessage");
    recordStep("Submitted form and received confirmation");
}
```

## FSM Test Documentation Template

For each FSM test, document the following:

```
# GUI FSM Test Report: [Test Name]

## States and Transitions Tested
| From State | Action | To State | Verification | Result |
|------------|--------|----------|--------------|--------|
| Main Menu  | Click Register | Registration Form | Form visible | ✅ |
| Registration Form | Submit valid data | Success Dialog | Success message visible | ✅ |
| Success Dialog | Click OK | Main Menu | Main menu visible | ✅ |

## Critical Path Analysis
- Path fully traversed: ✅
- Alternative paths tested: ✅/❌
- Boundary conditions tested: ✅/❌

## Issues Identified
1. [Issue description, if any]
2. [Issue description, if any]

## Screenshots
[Links to key screenshots]

## Recommendations
1. [Recommendation for UI improvements]
2. [Recommendation for error handling]
```

## Testing Multi-Screen Workflows

Create comprehensive tests that validate complete user workflows across multiple screens:

```java
@Test
void testPatientRegistrationToAppointmentWorkflow() {
    // Record start state
    recordStep("Starting at main menu");
    
    // First workflow segment: Patient Registration
    clickOn("#registerButton");
    waitForNode("#patientRegistrationForm");
    recordStep("Navigated to registration form");
    
    String uniqueId = "P" + System.currentTimeMillis();
    clickOn("#idField").write(uniqueId);
    clickOn("#firstNameField").write("Jane");
    clickOn("#lastNameField").write("Smith");
    recordStep("Filled patient information with ID: " + uniqueId);
    
    clickOn("#submitButton");
    waitForNode("#successMessage");
    clickOn("#okButton");
    recordStep("Patient registered successfully");
    
    // Verify return to main menu
    waitForNode("#mainMenu");
    
    // Second workflow segment: Appointment Scheduling
    clickOn("#scheduleButton");
    waitForNode("#appointmentForm");
    recordStep("Navigated to appointment scheduling");
    
    // Search for the patient we just created
    clickOn("#searchPatientField").write(uniqueId);
    clickOn("#searchButton");
    waitFor("#searchResults", results -> !results.isEmpty());
    recordStep("Found patient in search results");
    
    // Select patient and create appointment
    clickOn(hasText(uniqueId));
    clickOn("#appointmentTypeField").write("Initial Consultation");
    clickOn("#datePicker").write("01/15/2025");
    recordStep("Filled appointment details");
    
    clickOn("#scheduleAppointmentButton");
    waitForNode("#appointmentConfirmation");
    recordStep("Appointment scheduled successfully");
    
    // Verify complete end-to-end workflow
    clickOn("#viewDetailsButton");
    waitForNode("#appointmentDetails");
    verifyThat("#patientIdLabel", hasText(uniqueId));
    verifyThat("#appointmentTypeLabel", hasText("Initial Consultation"));
    recordStep("Verified appointment details match input data");
}
```

## Conclusion

By incorporating these enhanced GUI testing techniques, we can:

1. Create robust tests that handle asynchronous operations
2. Document test results with screenshots and structured reports
3. Validate the FSM transitions thoroughly
4. Identify UI issues early in the development process
5. Ensure complete workflows function correctly

This approach ensures our Hospital Management System's UI is reliable, user-friendly, and correctly implements the intended user flows. 