package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Main menu screen for the Hospital Management System.
 * Provides access to all major features through a simple button interface.
 */
public class MainMenuScreen {
    
    // Keep UI components as fields for easy access in tests
    private final BorderPane rootContainer;
    private final VBox menuContainer;
    private final Label titleLabel;
    private final Text welcomeText;
    private final Button patientManagementBtn;
    private final Button appointmentManagementBtn;
    private final Button medicalRecordsBtn;
    private final Button billingBtn;
    private final Button inventoryBtn;
    private final Button signOutBtn;
    private final Button exitBtn;
    
    public MainMenuScreen() {
        // Create title
        titleLabel = new Label("Hospital Management System");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel"); // ID for testing
        
        // Welcome message
        welcomeText = new Text(
            "Welcome to the Hospital Management System. This application provides tools " +
            "to manage patients, appointments, medical records, billing, and inventory. " +
            "Please select an option below to get started."
        );
        welcomeText.setWrappingWidth(400);
        welcomeText.setTextAlignment(TextAlignment.CENTER);
        welcomeText.setId("welcomeText"); // ID for testing
        
        // Create menu buttons with IDs for testing
        patientManagementBtn = createMenuButton("Patient Management", "patientManagementBtn");
        appointmentManagementBtn = createMenuButton("Appointment Management", "appointmentManagementBtn");
        medicalRecordsBtn = createMenuButton("Medical Records", "medicalRecordsBtn");
        billingBtn = createMenuButton("Billing", "billingBtn");
        inventoryBtn = createMenuButton("Inventory Management", "inventoryBtn");
        signOutBtn = createMenuButton("Sign Out", "signOutBtn");
        exitBtn = createMenuButton("Exit", "exitBtn");
        
        // Style the sign out button differently to distinguish it
        signOutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        // User role label
        Label roleLabel = new Label("Current Role: " + HospitalApplication.CURRENT_USER_ROLE);
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0056b3;");
        roleLabel.setId("roleLabel");
        
        // Set button visibility based on role
        if (HospitalApplication.CURRENT_USER_ROLE.equals("DOCTOR")) {
            // Doctor can see: Patient Management, Appointment Management, Medical Records
            patientManagementBtn.setVisible(true);
            appointmentManagementBtn.setVisible(true);
            medicalRecordsBtn.setVisible(true);
            billingBtn.setVisible(false);
            inventoryBtn.setVisible(false);
        } else if (HospitalApplication.CURRENT_USER_ROLE.equals("ADMIN")) {
            // Admin can see: Billing, Inventory
            patientManagementBtn.setVisible(false);
            appointmentManagementBtn.setVisible(false);
            medicalRecordsBtn.setVisible(false);
            billingBtn.setVisible(true);
            inventoryBtn.setVisible(true);
        }
        
        // Set button actions (placeholder for now)
        patientManagementBtn.setOnAction(e -> openPatientRegistrationScreen());
        appointmentManagementBtn.setOnAction(e -> openAppointmentSchedulingScreen());
        medicalRecordsBtn.setOnAction(e -> openMedicalRecordsScreen());
        billingBtn.setOnAction(event -> {
            openBillingScreen();
        });
        inventoryBtn.setOnAction(e -> openInventoryScreen());
        signOutBtn.setOnAction(e -> signOut());
        exitBtn.setOnAction(e -> System.exit(0));
        
        // Create menu container
        menuContainer = new VBox(15); // 15px spacing between elements
        menuContainer.setPadding(new Insets(30));
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setId("mainMenuRoot"); // ID for testing
        
        // Add components to menu container
        menuContainer.getChildren().addAll(
                titleLabel,
                welcomeText,
                roleLabel, // Add role label
                patientManagementBtn,
                appointmentManagementBtn,
                medicalRecordsBtn,
                billingBtn,
                inventoryBtn,
                signOutBtn,
                exitBtn
        );
        
        // Add version label at bottom
        Label versionLabel = new Label("Hospital Management System v1.0");
        versionLabel.setId("versionLabel");
        versionLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");
        
        // Create main root container
        rootContainer = new BorderPane();
        rootContainer.setCenter(menuContainer);
        rootContainer.setBottom(versionLabel);
        BorderPane.setAlignment(versionLabel, Pos.CENTER);
        BorderPane.setMargin(versionLabel, new Insets(10));
    }
    
    /**
     * Signs out the current user and returns to the login screen
     */
    private void signOut() {
        try {
            // Reset the current user role
            HospitalApplication.CURRENT_USER_ROLE = "NONE";
            
            // Get the current stage
            Stage stage = (Stage) signOutBtn.getScene().getWindow();
            
            // Create a new login screen
            LoginScreen loginScreen = new LoginScreen();
            Scene loginScene = new Scene(loginScreen.getRoot(), 600, 450);
            loginScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            // Set the login scene
            stage.setTitle("Hospital Management System - Login");
            stage.setScene(loginScene);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error signing out: " + e.getMessage());
        }
    }
    
    /**
     * Open the inventory management screen
     */
    private void openInventoryScreen() {
        try {
            // Get the current scene
            Scene currentScene = inventoryBtn.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            
            // Create inventory screen
            InventoryScreen inventoryScreen = new InventoryScreen();
            
            // Set back button action to return to main menu
            inventoryScreen.setBackButtonAction(() -> {
                primaryStage.setScene(currentScene);
                primaryStage.setTitle("Hospital Management System");
            });
            
            // Switch to inventory screen
            Scene inventoryScene = new Scene(inventoryScreen.getRoot(), currentScene.getWidth(), currentScene.getHeight());
            inventoryScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            primaryStage.setTitle("Hospital Management System - Inventory");
            primaryStage.setScene(inventoryScene);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening inventory screen: " + e.getMessage());
        }
    }
    
    /**
     * Open the patient registration screen
     */
    private void openPatientRegistrationScreen() {
        try {
            // Get the current scene
            Scene currentScene = patientManagementBtn.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            
            // Create patient registration screen
            PatientRegistrationScreen patientScreen = new PatientRegistrationScreen();
            
            // Set back button action to return to main menu
            patientScreen.setBackButtonAction(() -> {
                primaryStage.setScene(currentScene);
                primaryStage.setTitle("Hospital Management System");
            });
            
            // Switch to patient registration screen
            Scene patientScene = new Scene(patientScreen.getRoot(), currentScene.getWidth(), currentScene.getHeight());
            patientScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            primaryStage.setTitle("Hospital Management System - Patient Registration");
            primaryStage.setScene(patientScene);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening patient registration screen: " + e.getMessage());
        }
    }
    
    /**
     * Open the appointment scheduling screen
     */
    private void openAppointmentSchedulingScreen() {
        try {
            // Get the current scene
            Scene currentScene = appointmentManagementBtn.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            
            // Create appointment scheduling screen
            AppointmentSchedulingScreen appointmentScreen = new AppointmentSchedulingScreen();
            
            // Set back button action to return to main menu
            appointmentScreen.setBackButtonAction(() -> {
                primaryStage.setScene(currentScene);
                primaryStage.setTitle("Hospital Management System");
            });
            
            // Switch to appointment scheduling screen
            Scene appointmentScene = new Scene(appointmentScreen.getRoot(), currentScene.getWidth(), currentScene.getHeight());
            appointmentScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            primaryStage.setTitle("Hospital Management System - Appointment Scheduling");
            primaryStage.setScene(appointmentScene);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening appointment scheduling screen: " + e.getMessage());
        }
    }
    
    /**
     * Open the medical records screen
     */
    private void openMedicalRecordsScreen() {
        try {
            // Get the current scene
            Scene currentScene = medicalRecordsBtn.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            
            // Create medical records screen
            MedicalRecordsScreen medicalRecordsScreen = new MedicalRecordsScreen();
            
            // Set back button action to return to main menu
            medicalRecordsScreen.setBackButtonAction(() -> {
                primaryStage.setScene(currentScene);
                primaryStage.setTitle("Hospital Management System");
            });
            
            // Switch to medical records screen
            Scene medicalRecordsScene = new Scene(medicalRecordsScreen.getRoot(), currentScene.getWidth(), currentScene.getHeight());
            medicalRecordsScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            primaryStage.setTitle("Hospital Management System - Medical Records");
            primaryStage.setScene(medicalRecordsScene);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening medical records screen: " + e.getMessage());
        }
    }
    
    /**
     * Open the billing screen
     */
    private void openBillingScreen() {
        try {
            Scene currentScene = ((Button) billingBtn).getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            
            BillingScreen billingScreen = new BillingScreen();
            billingScreen.setBackButtonAction(() -> {
                try {
                    MainMenuScreen mainMenuScreen = new MainMenuScreen();
                    Scene mainMenuScene = new Scene(mainMenuScreen.getRoot(), 800, 600);
                    mainMenuScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
                    primaryStage.setScene(mainMenuScene);
                    primaryStage.setTitle("Hospital Management System - Main Menu");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            Scene billingScene = new Scene(billingScreen.getRoot(), 800, 600);
            billingScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            primaryStage.setScene(billingScene);
            primaryStage.setTitle("Hospital Management System - Billing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to create consistently styled menu buttons
     */
    private Button createMenuButton(String text, String id) {
        Button button = new Button(text);
        button.setId(id); // Set ID for testing
        button.setMinWidth(200);
        button.setPrefHeight(40);
        button.setFont(new Font("Arial", 14));
        return button;
    }
    
    /**
     * Get the root container that holds all UI components
     */
    public Parent getRoot() {
        return rootContainer;
    }
    
    // Getters for all components to allow test access
    public Button getPatientManagementBtn() {
        return patientManagementBtn;
    }
    
    public Button getAppointmentManagementBtn() {
        return appointmentManagementBtn;
    }
    
    public Button getMedicalRecordsBtn() {
        return medicalRecordsBtn;
    }
    
    public Button getBillingBtn() {
        return billingBtn;
    }
    
    public Button getInventoryBtn() {
        return inventoryBtn;
    }
    
    public Button getSignOutBtn() {
        return signOutBtn;
    }
    
    public Button getExitBtn() {
        return exitBtn;
    }
} 