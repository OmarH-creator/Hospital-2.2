package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AppointmentSchedulingScreen UI class.
 * These tests verify that the basic functionality of the UI works correctly.
 */
public class AppointmentSchedulingScreenTest {
    
    private AppointmentSchedulingScreen appointmentScreen;
    
    @BeforeAll
    public static void setupJavaFX() throws Exception {
        // Initialize the JavaFX toolkit
        JavaFXInitializer.setupJavaFX();
    }
    
    @BeforeEach
    void setUp() {
        // Create the screen
        appointmentScreen = new AppointmentSchedulingScreen();
    }
    
    @Test
    void testInitialComponentsState() {
        // Verify all necessary components are created
        assertNotNull(appointmentScreen.getAppointmentsTable());
        assertNotNull(appointmentScreen.getSearchField());
        assertNotNull(appointmentScreen.getSearchButton());
        assertNotNull(appointmentScreen.getScheduleButton());
        assertNotNull(appointmentScreen.getRescheduleButton());
        assertNotNull(appointmentScreen.getCancelButton());
        assertNotNull(appointmentScreen.getCompleteButton());
        assertNotNull(appointmentScreen.getRefreshButton());
        assertNotNull(appointmentScreen.getBackButton());
        assertNotNull(appointmentScreen.getStatusLabel());
        assertNotNull(appointmentScreen.getDatePicker());
        assertNotNull(appointmentScreen.getStatusFilterComboBox());
        
        // Verify that button states are correct at initialization
        assertFalse(appointmentScreen.getScheduleButton().isDisabled());
        assertTrue(appointmentScreen.getRescheduleButton().isDisabled());
        assertTrue(appointmentScreen.getCancelButton().isDisabled());
        assertTrue(appointmentScreen.getCompleteButton().isDisabled());
        assertFalse(appointmentScreen.getRefreshButton().isDisabled());
        assertFalse(appointmentScreen.getBackButton().isDisabled());
    }
    
    @Test
    void testComponentsHaveCorrectIds() {
        // Verify components have the correct IDs for CSS styling and testing
        assertEquals("appointmentsTable", appointmentScreen.getAppointmentsTable().getId());
        assertEquals("searchField", appointmentScreen.getSearchField().getId());
        assertEquals("searchButton", appointmentScreen.getSearchButton().getId());
        assertEquals("scheduleButton", appointmentScreen.getScheduleButton().getId());
        assertEquals("rescheduleButton", appointmentScreen.getRescheduleButton().getId());
        assertEquals("cancelButton", appointmentScreen.getCancelButton().getId());
        assertEquals("completeButton", appointmentScreen.getCompleteButton().getId());
        assertEquals("refreshButton", appointmentScreen.getRefreshButton().getId());
        assertEquals("backButton", appointmentScreen.getBackButton().getId());
        assertEquals("statusLabel", appointmentScreen.getStatusLabel().getId());
        assertEquals("datePicker", appointmentScreen.getDatePicker().getId());
        assertEquals("statusFilterComboBox", appointmentScreen.getStatusFilterComboBox().getId());
    }
    
    @Test
    void testTableColumnsSetupCorrectly() {
        TableView<Appointment> table = appointmentScreen.getAppointmentsTable();
        
        // Verify the table has the correct number of columns
        assertEquals(6, table.getColumns().size());
        
        // Verify column headers
        assertEquals("ID", table.getColumns().get(0).getText());
        assertEquals("Patient", table.getColumns().get(1).getText());
        assertEquals("Type", table.getColumns().get(2).getText());
        assertEquals("Date", table.getColumns().get(3).getText());
        assertEquals("Time", table.getColumns().get(4).getText());
        assertEquals("Status", table.getColumns().get(5).getText());
    }
    
    @Test
    void testStatusFilterComboBoxValues() {
        // Verify the status filter combo box has the correct options
        ComboBox<String> comboBox = appointmentScreen.getStatusFilterComboBox();
        assertEquals(5, comboBox.getItems().size());
        assertTrue(comboBox.getItems().contains("All"));
        assertTrue(comboBox.getItems().contains("SCHEDULED"));
        assertTrue(comboBox.getItems().contains("COMPLETED"));
        assertTrue(comboBox.getItems().contains("CANCELLED"));
        assertTrue(comboBox.getItems().contains("RESCHEDULED"));
        assertEquals("All", comboBox.getValue()); // Default value should be "All"
    }
    
    @Test
    void testBackButtonActionSetter() {
        // Test that the back button action can be set
        boolean[] actionCalled = new boolean[1];
        appointmentScreen.setBackButtonAction(() -> actionCalled[0] = true);
        
        // Simulate clicking the back button
        ((Button)appointmentScreen.getBackButton()).fire();
        
        // Verify the action was called
        assertTrue(actionCalled[0]);
    }
} 