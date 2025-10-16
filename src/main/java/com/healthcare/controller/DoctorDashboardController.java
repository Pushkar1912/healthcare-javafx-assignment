package com.healthcare.controller;

import com.healthcare.model.Staff;
import com.healthcare.model.Resident;
import com.healthcare.model.Prescription;
import com.healthcare.services.StaffService;
import com.healthcare.services.ResidentService;
import com.healthcare.services.PrescriptionService;
import com.healthcare.controller.components.MyPatientsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Doctor Dashboard
 * Simple implementation with component-wise structure
 */
public class DoctorDashboardController extends BaseDashboardController {
    
    // Dashboard elements
    @FXML
    private Label totalResidentsLabel;
    @FXML
    private Label myPatientsLabel;
    @FXML
    private Label todaysAppointmentsLabel;
    @FXML
    private Label pendingPrescriptionsLabel;
    @FXML
    private ListView<String> recentActivityList;
    
    // Navigation buttons
    @FXML
    private Button dashboardButton;
    @FXML
    private Button patientManagementButton;
    @FXML
    private Button prescriptionsButton;
    @FXML
    private Button reportsButton;
    @FXML
    private Button medicinesButton;
    
    // Content areas
    @FXML
    private VBox mainContentArea;
    @FXML
    private VBox dashboardContent;
    @FXML
    private VBox patientManagementContent;
    @FXML
    private VBox prescriptionsContent;
    @FXML
    private VBox reportsContent;
    @FXML
    private VBox medicinesContent;
    
    // Services
    private StaffService staffService = new StaffService();
    private ResidentService residentService = new ResidentService();
    private PrescriptionService prescriptionService = new PrescriptionService();

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        System.out.println("DoctorDashboardController: Initializing...");
        super.initialize(location, resources);
        setupNavigation();
        loadDashboardData();
        System.out.println("DoctorDashboardController: Initialization complete");
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
    private void showPatientManagement() {
        setActiveButton(patientManagementButton);
        showContent(patientManagementContent);
        loadPatientManagementData();
    }
    
    @FXML
    private void showPrescriptions() {
        setActiveButton(prescriptionsButton);
        showContent(prescriptionsContent);
        loadPrescriptionsData();
    }
    
    @FXML
    private void showReports() {
        setActiveButton(reportsButton);
        showContent(reportsContent);
        loadReportsData();
    }
    
    @FXML
    private void showMedicines() {
        setActiveButton(medicinesButton);
        showContent(medicinesContent);
        loadMedicinesData();
    }
    
    // Data loading methods
    private void loadDashboardData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set, cannot load dashboard data");
                return;
            }
            
            // Load total residents count
            List<Resident> allResidents = residentService.findActiveResidents();
            totalResidentsLabel.setText(String.valueOf(allResidents.size()));
            
            // Load doctor's assigned patients count
            List<Resident> myPatients = allResidents.stream()
                .filter(resident -> resident.getAssignedDoctorId() != null && 
                        resident.getAssignedDoctorId().equals(currentStaff.getStaffId()))
                .collect(Collectors.toList());
            myPatientsLabel.setText(String.valueOf(myPatients.size()));
            
            // Load today's appointments (using today's prescriptions as proxy)
            List<Prescription> todaysPrescriptions = prescriptionService.findTodaysByDoctorId(currentStaff.getStaffId());
            todaysAppointmentsLabel.setText(String.valueOf(todaysPrescriptions.size()));
            
            // Load pending prescriptions for this doctor
            List<Prescription> pendingPrescriptions = prescriptionService.findPendingByDoctorId(currentStaff.getStaffId());
            pendingPrescriptionsLabel.setText(String.valueOf(pendingPrescriptions.size()));
            
            // Load recent activity
            loadRecentActivity();
            
            System.out.println("Dashboard data loaded successfully for Dr. " + currentStaff.getFullName());
            System.out.println("Total patients: " + allResidents.size() + ", My patients: " + myPatients.size() + 
                             ", Today's appointments: " + todaysPrescriptions.size() + ", Pending prescriptions: " + pendingPrescriptions.size());
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void loadRecentActivity() {
        recentActivityList.getItems().clear();
        
        if (currentStaff != null) {
            // Load real activity based on doctor's recent actions
            try {
                List<Prescription> recentPrescriptions = prescriptionService.findByDoctorId(currentStaff.getStaffId());
                
                // Add recent prescription activities
                recentPrescriptions.stream()
                    .limit(5)
                    .forEach(prescription -> {
                        String activity = "Prescribed medication for patient ID: " + prescription.getResidentId() + 
                                        " on " + prescription.getPrescriptionDate();
                        recentActivityList.getItems().add(activity);
                    });
                
                // Add login activity
                recentActivityList.getItems().add("Dr. " + currentStaff.getFullName() + " logged in");
                recentActivityList.getItems().add("Dashboard loaded successfully");
                
            } catch (Exception e) {
                System.err.println("Error loading recent activity: " + e.getMessage());
                // Fallback to default activities
                recentActivityList.getItems().addAll(
                    "Dr. " + currentStaff.getFullName() + " logged in",
                    "Dashboard loaded",
                    "System ready"
                );
            }
        } else {
            recentActivityList.getItems().addAll(
                "System started successfully",
                "Doctor logged in",
                "Dashboard loaded"
            );
        }
    }
    
    private void loadPatientManagementData() {
        try {
            // Check if currentStaff is set
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping My Patients component loading");
                return;
            }
            
            // Clear existing content first
            patientManagementContent.getChildren().clear();
            
            // Load My Patients component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/my-patients.fxml"));
            VBox myPatientsComponent = loader.load();
            
            // Set current doctor in the component
            MyPatientsController controller = loader.getController();
            controller.setCurrentDoctor(currentStaff);
            
            patientManagementContent.getChildren().add(myPatientsComponent);
            
            System.out.println("My Patients component loaded successfully for Dr. " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading My Patients component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading patient data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            patientManagementContent.getChildren().clear();
            patientManagementContent.getChildren().add(errorLabel);
        }
    }
    
    private void loadPrescriptionsData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping Prescriptions component loading");
                return;
            }
            
            // Clear existing content first
            prescriptionsContent.getChildren().clear();
            
            // Load Prescriptions component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/prescription-management.fxml"));
            VBox prescriptionsComponent = loader.load();
            
            // Set current doctor in the component
            com.healthcare.controller.components.PrescriptionManagementController controller = loader.getController();
            controller.setCurrentDoctor(currentStaff);
            
            prescriptionsContent.getChildren().add(prescriptionsComponent);
            
            System.out.println("Prescriptions component loaded successfully for Dr. " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading Prescriptions component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading prescription data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            prescriptionsContent.getChildren().clear();
            prescriptionsContent.getChildren().add(errorLabel);
        }
    }
    
    private void loadReportsData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping Reports component loading");
                return;
            }
            
            // Clear existing content first
            reportsContent.getChildren().clear();
            
            // Load Reports component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/doctor-reports.fxml"));
            VBox reportsComponent = loader.load();
            
            // Set current doctor in the component
            com.healthcare.controller.components.DoctorReportsController controller = loader.getController();
            controller.setCurrentDoctor(currentStaff);
            
            reportsContent.getChildren().add(reportsComponent);
            
            System.out.println("Reports component loaded successfully for Dr. " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading Reports component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading reports data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            reportsContent.getChildren().clear();
            reportsContent.getChildren().add(errorLabel);
        }
    }
    
    private void loadMedicinesData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping Medicines component loading");
                return;
            }
            
            // Clear existing content first
            medicinesContent.getChildren().clear();
            
            // Load Medicines component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/medicine-management.fxml"));
            VBox medicinesComponent = loader.load();
            
            // Set current doctor in the component
            com.healthcare.controller.components.MedicineManagementController controller = loader.getController();
            controller.setCurrentDoctor(currentStaff);
            
            medicinesContent.getChildren().add(medicinesComponent);
            
            System.out.println("Medicines component loaded successfully for Dr. " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading Medicines component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading medicine data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            medicinesContent.getChildren().clear();
            medicinesContent.getChildren().add(errorLabel);
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
            dashboardButton, patientManagementButton, prescriptionsButton, reportsButton, medicinesButton
        );
        
        for (Button button : buttons) {
            if (button == activeButton) {
                // Active button - darker color with subtle shadow
                button.setStyle("-fx-background-color: " + ACTIVE_COLOR + "; " + BASE_STYLE + 
                               " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
            } else {
                // Default button - consistent blue color
                button.setStyle("-fx-background-color: " + DEFAULT_COLOR + "; " + BASE_STYLE);
            }
            
            // Add hover effects
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
    
    private void showContent(VBox content) {
        // Hide all content areas
        List<VBox> contentAreas = List.of(
            dashboardContent, patientManagementContent, prescriptionsContent, reportsContent, medicinesContent
        );
        
        for (VBox area : contentAreas) {
            area.setVisible(false);
            area.setManaged(false);
        }
        
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
    
    @Override
    public void setCurrentStaff(Staff currentStaff) {
        System.out.println("DoctorDashboardController: Setting current staff to: " + (currentStaff != null ? currentStaff.getUsername() : "null"));
        super.setCurrentStaff(currentStaff);
        
        // Reload dashboard data when staff is set
        if (currentStaff != null) {
            loadDashboardData();
        }
    }
}
