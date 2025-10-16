package com.healthcare.controller;

import java.util.List;

import com.healthcare.controller.components.ActionLogsController;
import com.healthcare.services.StaffService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

/**
 * Controller for the Manager Dashboard
 * Simple implementation with component-wise structure
 */
public class ManagerDashboardController extends BaseDashboardController {
    
    // Dashboard elements
    @FXML
    private Label totalResidentsLabel;
    @FXML
    private Label totalStaffLabel;
    @FXML
    private Label occupiedBedsLabel;
    @FXML
    private Label todaysActionsLabel;
    @FXML
    private ListView<String> recentActivityList;
    
    // Navigation buttons
    @FXML
    private Button dashboardButton;
    @FXML
    private Button staffManagementButton;
    @FXML
    private Button residentManagementButton;
    @FXML
    private Button shiftSchedulingButton;
    @FXML
    private Button actionLogsButton;
    @FXML
    private Button reportsArchivesButton;
    @FXML
    private Button systemSettingsButton;
    
    // Content areas
    @FXML
    private VBox mainContentArea;
    @FXML
    private VBox dashboardContent;
    @FXML
    private VBox staffManagementContent;
    @FXML
    private VBox residentManagementContent;
    @FXML
    private VBox shiftSchedulingContent;
    @FXML
    private VBox actionLogsContent;
    @FXML
    private VBox reportsArchivesContent;
    @FXML
    private VBox systemSettingsContent;
    
    // Services
    private StaffService staffService = new StaffService();

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        super.initialize(location, resources);
        setupNavigation();
        loadDashboardData();
    }
    
    private void setupNavigation() {
        // Set default active button
        setActiveButton(dashboardButton);
        showContent(dashboardContent);
    }
    
    // Navigation methods
    @FXML
    private void showDashboard() {
        setActiveButton(dashboardButton);
        showContent(dashboardContent);
        loadDashboardData();
    }
    
    @FXML
    private void showStaffManagement() {
        setActiveButton(staffManagementButton);
        showContent(staffManagementContent);
        loadStaffManagementData();
    }
    
    @FXML
    private void showResidentManagement() {
        setActiveButton(residentManagementButton);
        showContent(residentManagementContent);
        loadResidentManagementData();
    }
    
    @FXML
    private void showShiftScheduling() {
        setActiveButton(shiftSchedulingButton);
        showContent(shiftSchedulingContent);
        loadShiftSchedulingData();
    }
    
    @FXML
    private void showActionLogs() {
        setActiveButton(actionLogsButton);
        showContent(actionLogsContent);
        loadActionLogsData();
    }
    
    @FXML
    private void showReportsArchives() {
        setActiveButton(reportsArchivesButton);
        showContent(reportsArchivesContent);
        loadReportsArchivesData();
    }
    
    @FXML
    private void showSystemSettings() {
        setActiveButton(systemSettingsButton);
        showContent(systemSettingsContent);
        loadSystemSettingsData();
    }
    
    // Quick Actions
    @FXML
    private void addNewStaff() {
        showStaffManagement();
    }
    
    @FXML
    private void admitNewResident() {
        showResidentManagement();
    }
    
    @FXML
    private void scheduleShifts() {
        showShiftScheduling();
    }
    
    @FXML
    private void generateReport() {
        showReportsArchives();
    }
    
    // Data loading methods
    private void loadDashboardData() {
        try {
            // Load statistics using simple services
            long totalStaff = staffService.findAll().size();
            
            totalResidentsLabel.setText("0"); 
            totalStaffLabel.setText(String.valueOf(totalStaff));
            occupiedBedsLabel.setText("0"); 
            todaysActionsLabel.setText("0"); 
            
            // Load recent activity
            loadRecentActivity();
            
        } catch (Exception e) {
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void loadRecentActivity() {
        recentActivityList.getItems().clear();
        recentActivityList.getItems().addAll(
            "System started successfully",
            "Manager logged in",
            "Dashboard loaded"
        );
    }
    
    private void loadStaffManagementData() {
        try {
            // Load the Staff Management component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/staff-management.fxml"));
            VBox staffManagementComponent = loader.load();
            com.healthcare.controller.components.StaffManagementController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Clear existing content and add the component
            staffManagementContent.getChildren().clear();
            staffManagementContent.getChildren().add(staffManagementComponent);
            
            System.out.println("Staff management component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading staff management component: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadResidentManagementData() {
        try {
            // Load the Resident Management component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/resident-management.fxml"));
            VBox residentManagementComponent = loader.load();
            com.healthcare.controller.components.ResidentManagementController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Clear existing content and add the component
            residentManagementContent.getChildren().clear();
            residentManagementContent.getChildren().add(residentManagementComponent);
            
            System.out.println("Resident management component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading resident management component: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadShiftSchedulingData() {
        try {
            // Load the Shift Scheduling component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/shift-scheduling.fxml"));
            VBox shiftSchedulingComponent = loader.load();
            com.healthcare.controller.components.ShiftSchedulingController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Clear existing content and add the component
            shiftSchedulingContent.getChildren().clear();
            shiftSchedulingContent.getChildren().add(shiftSchedulingComponent);
            
            System.out.println("Shift scheduling component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading shift scheduling component: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadActionLogsData() {
        try {
            // Load the Action Logs component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/action-logs.fxml"));
            VBox actionLogsComponent = loader.load();
            ActionLogsController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Clear existing content and add the component
            actionLogsContent.getChildren().clear();
            actionLogsContent.getChildren().add(actionLogsComponent);
            
            System.out.println("Action logs component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading action logs component: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadReportsArchivesData() {
        try {
            // Load the Reports and Archives component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/reports-archives.fxml"));
            VBox reportsArchivesComponent = loader.load();
            com.healthcare.controller.components.ReportsArchivesController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Clear existing content and add the component
            reportsArchivesContent.getChildren().clear();
            reportsArchivesContent.getChildren().add(reportsArchivesComponent);
            
            System.out.println("Reports and archives component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading reports and archives component: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadSystemSettingsData() {
        try {
            // Load the System Settings component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/system-settings.fxml"));
            VBox systemSettingsComponent = loader.load();
            com.healthcare.controller.components.SystemSettingsController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Clear existing content and add the component
            systemSettingsContent.getChildren().clear();
            systemSettingsContent.getChildren().add(systemSettingsComponent);
            
            System.out.println("System settings component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading system settings component: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods
    private void setActiveButton(Button activeButton) {
        // Define consistent color scheme
        final String DEFAULT_COLOR = "#3498DB";      // Blue
        final String ACTIVE_COLOR = "#2C3E50";       // Dark blue-gray (active state)
        final String HOVER_COLOR = "#2980B9";        // Darker blue (hover state)
        
        // Base button style
        final String BASE_STYLE = "-fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12 20 12 20; -fx-font-weight: bold; -fx-cursor: hand;";
        
        // Reset all buttons
        List<Button> buttons = List.of(
            dashboardButton, staffManagementButton, residentManagementButton,
            shiftSchedulingButton, actionLogsButton, reportsArchivesButton, systemSettingsButton
        );
        
        for (Button button : buttons) {
            if (button == activeButton) {
                // Active button - darker color with subtle shadow
                button.setStyle("-fx-background-color: " + ACTIVE_COLOR + "; " + BASE_STYLE + 
                               " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
            } else {
                // Default button - consistent blue color
                button.setStyle("-fx-background-color: " + DEFAULT_COLOR + "; " + BASE_STYLE);
                
                // Add hover effect
                button.setOnMouseEntered(e -> {
                    if (button != activeButton) {
                        button.setStyle("-fx-background-color: " + HOVER_COLOR + "; " + BASE_STYLE);
                    }
                });
                
                button.setOnMouseExited(e -> {
                    if (button != activeButton) {
                        button.setStyle("-fx-background-color: " + DEFAULT_COLOR + "; " + BASE_STYLE);
                    }
                });
            }
        }
    }
    
    private void showContent(VBox content) {
        // Hide all content areas using both visible and managed properties
        // This prevents layout shifts and components from being pushed down
        dashboardContent.setVisible(false);
        dashboardContent.setManaged(false);
        
        staffManagementContent.setVisible(false);
        staffManagementContent.setManaged(false);
        
        residentManagementContent.setVisible(false);
        residentManagementContent.setManaged(false);
        
        shiftSchedulingContent.setVisible(false);
        shiftSchedulingContent.setManaged(false);
        
        actionLogsContent.setVisible(false);
        actionLogsContent.setManaged(false);
        
        reportsArchivesContent.setVisible(false);
        reportsArchivesContent.setManaged(false);
        
        systemSettingsContent.setVisible(false);
        systemSettingsContent.setManaged(false);
        
        // Show selected content
        content.setVisible(true);
        content.setManaged(true);
    }
    
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}