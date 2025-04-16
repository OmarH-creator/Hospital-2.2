package com.example.hospitalsystemsimpletesting;

import com.example.hospitalsystemsimpletesting.service.*;
import com.example.hospitalsystemsimpletesting.service.impl.BillingServiceImpl;
import com.example.hospitalsystemsimpletesting.service.impl.CSVDataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.AppointmentServiceImpl;
import com.example.hospitalsystemsimpletesting.service.MedicalRecordServiceImpl;
import com.example.hospitalsystemsimpletesting.service.PatientServiceImpl;
import com.example.hospitalsystemsimpletesting.ui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the Hospital Management System.
 * Acts as the entry point for the JavaFX application.
 */
public class HospitalApplication extends Application {
    // Static role variable - visible to all classes
    public static String CURRENT_USER_ROLE = "NONE";
    
    // Shared services for data persistence
    private static PatientService patientService;
    private static DataPersistenceService dataPersistenceService;
    private static MedicalRecordService medicalRecordService;
    private static AppointmentService appointmentService;
    private static BillingService billingService;
    
    // Initialize services
    static {
        // Create services with circular dependency resolution
        PatientServiceImpl tempPatientService = new PatientServiceImpl();
        dataPersistenceService = new CSVDataPersistenceService(tempPatientService);
        patientService = new PatientServiceImpl(dataPersistenceService);
        
        // Update the reference in the persistence service
        ((CSVDataPersistenceService) dataPersistenceService).setPatientService(patientService);
        
        // Initialize appointment service
        appointmentService = new AppointmentServiceImpl(patientService, dataPersistenceService);
        
        // Initialize billing service
        billingService = new BillingServiceImpl(patientService, dataPersistenceService);
        
        // Wire up the dependencies for patient deletion
        if (patientService instanceof PatientServiceImpl) {
            PatientServiceImpl patientServiceImpl = (PatientServiceImpl) patientService;
            patientServiceImpl.setAppointmentService(appointmentService);
            patientServiceImpl.setBillingService(billingService);
        }
        
        // Initialize medical record service with proper persistence
        medicalRecordService = new MedicalRecordServiceImpl(patientService, appointmentService, dataPersistenceService);
        
        // Log service initialization status for debugging
        System.out.println("DEBUG: Services initialized:");
        System.out.println("DEBUG: PatientService: " + (patientService != null ? "OK" : "NULL"));
        System.out.println("DEBUG: AppointmentService: " + (appointmentService != null ? "OK" : "NULL"));
        System.out.println("DEBUG: BillingService: " + (billingService != null ? "OK" : "NULL"));
        System.out.println("DEBUG: MedicalRecordService: " + (medicalRecordService != null ? "OK" : "NULL"));

        // Check if the wiring was successful
        if (patientService instanceof PatientServiceImpl) {
            PatientServiceImpl impl = (PatientServiceImpl) patientService;
            System.out.println("DEBUG: PatientServiceImpl dependencies:");
            System.out.println("DEBUG: - AppointmentService: " + (impl.getAppointmentService() != null ? "OK" : "NULL"));
            System.out.println("DEBUG: - BillingService: " + (impl.getBillingService() != null ? "OK" : "NULL"));
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            // Start with login screen instead of main menu
            LoginScreen loginScreen = new LoginScreen();
            Scene scene = new Scene(loginScreen.getRoot(), 600, 450);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            stage.setTitle("Hospital Management System - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the shared patient service instance
     */
    public static PatientService getPatientService() {
        return patientService;
    }
    
    /**
     * Get the shared data persistence service instance
     */
    public static DataPersistenceService getDataPersistenceService() {
        return dataPersistenceService;
    }
    
    /**
     * Get the shared medical record service instance
     */
    public static MedicalRecordService getMedicalRecordService() {
        return medicalRecordService;
    }
    
    /**
     * Get the shared appointment service instance
     */
    public static AppointmentService getAppointmentService() {
        return appointmentService;
    }

    /**
     * Get the shared billing service instance
     */
    public static BillingService getBillingService() {
        return billingService;
    }

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
} 