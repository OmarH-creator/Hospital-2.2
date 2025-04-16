package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PatientRegistrationScreen UI class.
 * These tests verify that the basic functionality of the UI works correctly.
 */
public class PatientRegistrationScreenTest {
    
    private PatientRegistrationScreen patientScreen;
    private PatientService patientService;
    
    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        // Initialize JavaFX toolkit before any tests run
        JavaFXInitializer.setupJavaFX();
    }
    
    @BeforeEach
    void setUp() throws InterruptedException {
        // Create components on JavaFX thread
        final CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create the screen
                patientScreen = new PatientRegistrationScreen();
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for UI creation to complete
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Failed to initialize UI components");
        }
    }
    
    @Test
    void testInitialComponentsState() {
        // Run assertions on JavaFX thread
        runOnFXThreadAndWait(() -> {
            // Verify all necessary components are created
            assertNotNull(patientScreen.getPatientsTable());
            assertNotNull(patientScreen.getSearchField());
            assertNotNull(patientScreen.getSearchButton());
            assertNotNull(patientScreen.getRegisterButton());
            assertNotNull(patientScreen.getEditButton());
            assertNotNull(patientScreen.getAdmitDischargeButton());
            assertNotNull(patientScreen.getBackButton());
            assertNotNull(patientScreen.getStatusLabel());
            assertNotNull(patientScreen.getSearchTypeComboBox());
            
            // Verify that button states are correct at initialization
            assertFalse(patientScreen.getRegisterButton().isDisabled());
            assertTrue(patientScreen.getEditButton().isDisabled());
            assertTrue(patientScreen.getAdmitDischargeButton().isDisabled());
            assertFalse(patientScreen.getBackButton().isDisabled());
        });
    }
    
    @Test
    void testComponentsHaveCorrectIds() {
        runOnFXThreadAndWait(() -> {
            // Verify components have the correct IDs for CSS styling and testing
            assertEquals("patientsTable", patientScreen.getPatientsTable().getId());
            assertEquals("searchField", patientScreen.getSearchField().getId());
            assertEquals("searchButton", patientScreen.getSearchButton().getId());
            assertEquals("registerButton", patientScreen.getRegisterButton().getId());
            assertEquals("editButton", patientScreen.getEditButton().getId());
            assertEquals("admitDischargeButton", patientScreen.getAdmitDischargeButton().getId());
            assertEquals("backButton", patientScreen.getBackButton().getId());
            assertEquals("statusLabel", patientScreen.getStatusLabel().getId());
            assertEquals("searchTypeComboBox", patientScreen.getSearchTypeComboBox().getId());
        });
    }
    
    @Test
    void testTableColumnsSetupCorrectly() {
        runOnFXThreadAndWait(() -> {
            TableView<Patient> table = patientScreen.getPatientsTable();
            
            // Verify the table has the correct number of columns
            assertEquals(7, table.getColumns().size());
            
            // Verify column headers
            assertEquals("ID", table.getColumns().get(0).getText());
            assertEquals("First Name", table.getColumns().get(1).getText());
            assertEquals("Last Name", table.getColumns().get(2).getText());
            assertEquals("Date of Birth", table.getColumns().get(3).getText());
            assertEquals("Age", table.getColumns().get(4).getText());
            assertEquals("Blood Type", table.getColumns().get(5).getText());
            assertEquals("Status", table.getColumns().get(6).getText());
        });
    }
    
    @Test
    void testSearchTypeComboBoxValues() {
        runOnFXThreadAndWait(() -> {
            // Verify that the search type combo box has the correct options
            ComboBox<String> comboBox = patientScreen.getSearchTypeComboBox();
            assertEquals(2, comboBox.getItems().size());
            assertTrue(comboBox.getItems().contains("ID"));
            assertTrue(comboBox.getItems().contains("Name"));
            assertEquals("Name", comboBox.getValue()); // Default value should be "Name"
        });
    }
    
    // Helper method to run code on JavaFX thread
    private void runOnFXThreadAndWait(Runnable action) {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("JavaFX thread operation timed out");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("JavaFX thread operation was interrupted", e);
        }
    }
} 