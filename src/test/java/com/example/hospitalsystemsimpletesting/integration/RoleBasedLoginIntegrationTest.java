package com.example.hospitalsystemsimpletesting.integration;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.ui.JavaFXInitializer;
import com.example.hospitalsystemsimpletesting.ui.LoginScreen;
import com.example.hospitalsystemsimpletesting.ui.MainMenuScreen;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the role-based login flow
 */
public class RoleBasedLoginIntegrationTest {
    
    private LoginScreen loginScreen;
    
    @BeforeAll
    public static void setupJavaFX() throws Exception {
        // Initialize JavaFX toolkit
        JavaFXInitializer.setupJavaFX();
    }
    
    @BeforeEach
    void setUp() {
        // Create login screen for each test
        loginScreen = new LoginScreen();
    }
    
    @AfterEach
    void resetRole() {
        // Reset the role after each test
        HospitalApplication.CURRENT_USER_ROLE = "NONE";
    }
    
    @Test
    void testDoctorLoginFlow() {
        // Get the doctor radio button and select it
        RadioButton doctorRadio = (RadioButton) findButtonById(loginScreen.getRoot(), "doctorRadio");
        assertNotNull(doctorRadio, "Doctor radio button should exist");
        doctorRadio.setSelected(true);
        
        // Get login button
        Button loginButton = (Button) findButtonById(loginScreen.getRoot(), "loginButton");
        assertNotNull(loginButton, "Login button should exist");
        
        // Simulate login button click by directly setting the role
        // (we can't actually click it in a headless test as it would try to change the scene)
        HospitalApplication.CURRENT_USER_ROLE = "DOCTOR";
        
        // Create main menu as would happen after login
        MainMenuScreen mainMenu = new MainMenuScreen();
        
        // Verify correct buttons are visible for doctor role
        assertTrue(mainMenu.getPatientManagementBtn().isVisible(), "Patient Management button should be visible");
        assertTrue(mainMenu.getAppointmentManagementBtn().isVisible(), "Appointment Management button should be visible");
        assertTrue(mainMenu.getMedicalRecordsBtn().isVisible(), "Medical Records button should be visible");
        assertFalse(mainMenu.getBillingBtn().isVisible(), "Billing button should be hidden");
        assertFalse(mainMenu.getInventoryBtn().isVisible(), "Inventory button should be hidden");
    }
    
    @Test
    void testAdminLoginFlow() {
        // Get the admin radio button and select it
        RadioButton adminRadio = (RadioButton) findButtonById(loginScreen.getRoot(), "adminRadio");
        assertNotNull(adminRadio, "Admin radio button should exist");
        adminRadio.setSelected(true);
        
        // Get login button
        Button loginButton = (Button) findButtonById(loginScreen.getRoot(), "loginButton");
        assertNotNull(loginButton, "Login button should exist");
        
        // Simulate login button click by directly setting the role
        HospitalApplication.CURRENT_USER_ROLE = "ADMIN";
        
        // Create main menu as would happen after login
        MainMenuScreen mainMenu = new MainMenuScreen();
        
        // Verify correct buttons are visible for admin role
        assertFalse(mainMenu.getPatientManagementBtn().isVisible(), "Patient Management button should be hidden");
        assertFalse(mainMenu.getAppointmentManagementBtn().isVisible(), "Appointment Management button should be hidden");
        assertFalse(mainMenu.getMedicalRecordsBtn().isVisible(), "Medical Records button should be hidden");
        assertTrue(mainMenu.getBillingBtn().isVisible(), "Billing button should be visible");
        assertTrue(mainMenu.getInventoryBtn().isVisible(), "Inventory button should be visible");
    }
    
    /**
     * Helper method to find a button by ID in a scene
     */
    private javafx.scene.Node findButtonById(javafx.scene.Parent root, String id) {
        for (javafx.scene.Node node : root.getChildrenUnmodifiable()) {
            if (id.equals(node.getId())) {
                return node;
            }
            if (node instanceof javafx.scene.Parent) {
                javafx.scene.Node found = findButtonById((javafx.scene.Parent) node, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
} 