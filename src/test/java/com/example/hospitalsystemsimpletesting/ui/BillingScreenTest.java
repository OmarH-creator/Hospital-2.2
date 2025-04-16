package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.BillingService;
import com.example.hospitalsystemsimpletesting.service.PatientService;
import com.example.hospitalsystemsimpletesting.service.impl.BillingServiceImpl;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test for the BillingScreen UI.
 */
public class BillingScreenTest {

    @Mock
    private PatientService patientService;
    
    @Mock
    private BillingService billingService;
    
    private BillingScreen billingScreen;
    
    @BeforeEach
    void setUp() {
        // Initialize mocks and JavaFX toolkit
        try {
            JavaFXInitializer.setupJavaFX();
        } catch (Exception e) {
            // In case JavaFX initialization fails, log and continue
            System.err.println("JavaFX initialization failed: " + e.getMessage());
        }
        
        MockitoAnnotations.openMocks(this);
        
        // Create sample patients
        Patient patient1 = new Patient("P123", "John", "Doe", LocalDate.of(1980, 1, 1));
        Patient patient2 = new Patient("P456", "Jane", "Smith", LocalDate.of(1985, 5, 5));
        List<Patient> patients = Arrays.asList(patient1, patient2);
        
        // Create sample bills
        Bill bill1 = new Bill("B001", patient1, LocalDate.now());
        bill1.addItem("Consultation", 100.0);
        
        Bill bill2 = new Bill("B002", patient2, LocalDate.now());
        bill2.addItem("X-Ray", 200.0);
        
        // Configure mocks
        when(patientService.getAllPatients()).thenReturn(patients);
        when(patientService.findPatientById("P123")).thenReturn(Optional.of(patient1));
        when(patientService.findPatientById("P456")).thenReturn(Optional.of(patient2));
        
        when(billingService.getAllBills()).thenReturn(Arrays.asList(bill1, bill2));
        when(billingService.findBillById("B001")).thenReturn(Optional.of(bill1));
        when(billingService.findBillById("B002")).thenReturn(Optional.of(bill2));
        when(billingService.findBillsByPatientId("P123")).thenReturn(Arrays.asList(bill1));
        when(billingService.findBillsByPatientId("P456")).thenReturn(Arrays.asList(bill2));
        
        // Mock HospitalApplication.getPatientService to return our mock
        try {
            setMockPatientService(patientService);
            
            // Initialize the BillingScreen
            billingScreen = new BillingScreen();
            
            // Replace the real BillingService with our mock
            setBillingServiceField(billingScreen, billingService);
            
            // Refresh the data to use our mocked data
            billingScreen.getRefreshButton().fire();
        } catch (Exception e) {
            fail("Failed to set up test environment: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to set the mock PatientService in HospitalApplication
     */
    private void setMockPatientService(PatientService mock) throws Exception {
        // This uses reflection to set the patientService field in HospitalApplication
        // Note: This approach is used for testing purposes only and should be used with caution
        java.lang.reflect.Field field = HospitalApplication.class.getDeclaredField("patientService");
        field.setAccessible(true);
        field.set(null, mock);
    }
    
    /**
     * Helper method to set the mock BillingService in BillingScreen
     */
    private void setBillingServiceField(BillingScreen screen, BillingService mock) throws Exception {
        java.lang.reflect.Field field = BillingScreen.class.getDeclaredField("billingService");
        field.setAccessible(true);
        field.set(screen, mock);
    }
    
    @Test
    @DisplayName("Test initialization of UI components")
    void testInitComponents() {
        // Verify UI components are initialized correctly
        assertNotNull(billingScreen.getBillsTable(), "Table should be initialized");
        assertNotNull(billingScreen.getSearchField(), "Search field should be initialized");
        assertNotNull(billingScreen.getCreateBillButton(), "Create button should be initialized");
        assertNotNull(billingScreen.getViewDetailsButton(), "View details button should be initialized");
        assertNotNull(billingScreen.getProcessPaymentButton(), "Process payment button should be initialized");
        
        // Check that the buttons have correct IDs
        assertEquals("billsTable", billingScreen.getBillsTable().getId(), "Table should have correct ID");
        assertEquals("searchField", billingScreen.getSearchField().getId(), "Search field should have correct ID");
        assertEquals("createBillButton", billingScreen.getCreateBillButton().getId(), "Create button should have correct ID");
    }
    
    @Test
    @DisplayName("Test table structure")
    void testTableStructure() {
        TableView<Bill> table = billingScreen.getBillsTable();
        
        // Check number of columns
        assertEquals(7, table.getColumns().size(), "Table should have 7 columns");
        
        // Check column types and headers
        assertTrue(table.getColumns().get(0) instanceof TableColumn, "First column should be a TableColumn");
        assertTrue(table.getColumns().get(1) instanceof TableColumn, "Second column should be a TableColumn");
        
        // Get column titles
        List<String> columnTitles = new ArrayList<>();
        for (TableColumn column : table.getColumns()) {
            columnTitles.add(column.getText());
        }
        
        // Verify important columns exist
        assertTrue(columnTitles.contains("Patient"), "Table should have Patient column");
        assertTrue(columnTitles.contains("Total Amount"), "Table should have Total Amount column");
        assertTrue(columnTitles.contains("Status"), "Table should have Status column");
        assertTrue(columnTitles.contains("Bill ID"), "Table should have Bill ID column");
    }
    
    @Test
    @DisplayName("Test search functionality")
    void testSearchBills() {
        // Set search text
        billingScreen.getSearchField().setText("B001");
        
        // Click search button
        billingScreen.getSearchButton().fire();
        
        // Verify search called on service
        verify(billingService).findBillById("B001");
    }
    
    @Test
    @DisplayName("Test table selection changes button states")
    void testSelectionAffectsButtons() {
        // Initially, details and payment buttons should be disabled
        assertTrue(billingScreen.getViewDetailsButton().isDisabled(), "View details button should be disabled initially");
        assertTrue(billingScreen.getProcessPaymentButton().isDisabled(), "Process payment button should be disabled initially");
        
        // Select a bill
        TableView<Bill> table = billingScreen.getBillsTable();
        if (!table.getItems().isEmpty()) {
            table.getSelectionModel().select(0);
            
            // Verify buttons are enabled after selection
            assertFalse(billingScreen.getViewDetailsButton().isDisabled(), "View details button should be enabled after selection");
            // Process payment might still be disabled if bill is paid
        }
    }
    
    @Test
    @DisplayName("Test filter by status")
    void testFilterByStatus() {
        // Select filter
        billingScreen.getStatusFilterComboBox().setValue("UNPAID");
        
        // Verify correct service method called
        verify(billingService, atLeastOnce()).getBillsByStatus(false);
    }
} 