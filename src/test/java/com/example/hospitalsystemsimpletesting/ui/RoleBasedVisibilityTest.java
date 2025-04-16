package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test for the role-based UI visibility functionality
 */
public class RoleBasedVisibilityTest {
    
    @BeforeAll
    public static void setupJavaFX() throws Exception {
        // Initialize JavaFX toolkit
        JavaFXInitializer.setupJavaFX();
    }
    
    @AfterEach
    void resetRole() {
        // Reset the role after each test
        HospitalApplication.CURRENT_USER_ROLE = "NONE";
    }
    
    @Test
    void testDoctorRoleButtonVisibility() {
        // Set role to DOCTOR
        HospitalApplication.CURRENT_USER_ROLE = "DOCTOR";
        
        // Create main menu
        MainMenuScreen mainMenu = new MainMenuScreen();
        
        // Verify button visibility for doctor role:
        // Medical buttons should be visible
        assertTrue(mainMenu.getPatientManagementBtn().isVisible(), "Patient Management button should be visible for doctor");
        assertTrue(mainMenu.getAppointmentManagementBtn().isVisible(), "Appointment Management button should be visible for doctor");
        assertTrue(mainMenu.getMedicalRecordsBtn().isVisible(), "Medical Records button should be visible for doctor");
        
        // Admin buttons should be hidden
        assertFalse(mainMenu.getBillingBtn().isVisible(), "Billing button should be hidden for doctor");
        assertFalse(mainMenu.getInventoryBtn().isVisible(), "Inventory button should be hidden for doctor");
        
        // Sign-out button should always be visible
        assertTrue(mainMenu.getSignOutBtn().isVisible(), "Sign-out button should be visible for all roles");
        
        // Exit button should always be visible
        assertTrue(mainMenu.getExitBtn().isVisible(), "Exit button should be visible for all roles");
    }
    
    @Test
    void testAdminRoleButtonVisibility() {
        // Set role to ADMIN
        HospitalApplication.CURRENT_USER_ROLE = "ADMIN";
        
        // Create main menu
        MainMenuScreen mainMenu = new MainMenuScreen();
        
        // Verify button visibility for admin role:
        // Admin buttons should be visible
        assertTrue(mainMenu.getBillingBtn().isVisible(), "Billing button should be visible for admin");
        assertTrue(mainMenu.getInventoryBtn().isVisible(), "Inventory button should be visible for admin");
        
        // Medical buttons should be hidden
        assertFalse(mainMenu.getPatientManagementBtn().isVisible(), "Patient Management button should be hidden for admin");
        assertFalse(mainMenu.getAppointmentManagementBtn().isVisible(), "Appointment Management button should be hidden for admin");
        assertFalse(mainMenu.getMedicalRecordsBtn().isVisible(), "Medical Records button should be hidden for admin");
        
        // Sign-out button should always be visible
        assertTrue(mainMenu.getSignOutBtn().isVisible(), "Sign-out button should be visible for all roles");
        
        // Exit button should always be visible
        assertTrue(mainMenu.getExitBtn().isVisible(), "Exit button should be visible for all roles");
    }
} 