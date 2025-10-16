package com.healthcare.controller;

import com.healthcare.model.Staff;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base controller for all dashboard screens
 * Provides common functionality for role-based dashboards
 */
public abstract class BaseDashboardController implements Initializable {
    
    @FXML
    protected Label welcomeLabel;
    
    @FXML
    protected Label userRoleLabel;
    
    protected Staff currentStaff;
    protected Stage primaryStage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Common initialization for all dashboards
    }
    
    public void setCurrentStaff(Staff currentStaff) {
        this.currentStaff = currentStaff;
        updateUserInfo();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    private void updateUserInfo() {
        if (currentStaff != null) {
            if (welcomeLabel != null) {
                String displayName = currentStaff.getFullName() != null && !currentStaff.getFullName().trim().isEmpty() 
                    ? currentStaff.getFullName() 
                    : currentStaff.getUsername();
                welcomeLabel.setText("Welcome, " + displayName);
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText("Role: " + currentStaff.getRole());
            }
        }
    }
    
    @FXML
    protected void handleLogout() {
        // Navigate back to login screen
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 800, 600);
            
            LoginController loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);
            
            primaryStage.setTitle("Healthcare Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected Staff getCurrentStaff() {
        return currentStaff;
    }
    
    protected Stage getPrimaryStage() {
        return primaryStage;
    }
}
