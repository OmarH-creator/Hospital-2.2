package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Simple login screen with role selection
 */
public class LoginScreen {
    private final VBox container;
    
    public LoginScreen() {
        // Create container
        container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        container.setId("loginContainer");
        
        // Title
        Label titleLabel = new Label("Hospital Management System");
        titleLabel.setFont(new Font("Arial", 24));
        
        // Role selection
        Label selectRoleLabel = new Label("Select Your Role:");
        selectRoleLabel.setFont(new Font("Arial", 16));
        
        // Create radio buttons
        ToggleGroup roleGroup = new ToggleGroup();
        
        RadioButton doctorBtn = new RadioButton("Doctor");
        doctorBtn.setToggleGroup(roleGroup);
        doctorBtn.setSelected(true);
        doctorBtn.setId("doctorRadio");
        
        RadioButton adminBtn = new RadioButton("Administrator");
        adminBtn.setToggleGroup(roleGroup);
        adminBtn.setId("adminRadio");
        
        // Login button
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(200);
        loginButton.setId("loginButton");
        
        loginButton.setOnAction(e -> {
            if (doctorBtn.isSelected()) {
                HospitalApplication.CURRENT_USER_ROLE = "DOCTOR";
            } else {
                HospitalApplication.CURRENT_USER_ROLE = "ADMIN";
            }
            
            // Open the main menu
            Stage stage = (Stage) container.getScene().getWindow();
            MainMenuScreen mainMenu = new MainMenuScreen();
            Scene mainMenuScene = new Scene(mainMenu.getRoot(), 800, 600);
            stage.setScene(mainMenuScene);
            stage.setTitle("Hospital Management System");
        });
        
        // Add everything to the container
        container.getChildren().addAll(
                titleLabel,
                selectRoleLabel,
                doctorBtn,
                adminBtn,
                loginButton
        );
    }
    
    /**
     * Get the root container
     */
    public Parent getRoot() {
        return container;
    }
} 