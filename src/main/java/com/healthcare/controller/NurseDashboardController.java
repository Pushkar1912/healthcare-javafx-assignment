package com.healthcare.controller;

import com.healthcare.model.Staff;
import com.healthcare.model.Resident;
import com.healthcare.services.ResidentService;
import com.healthcare.services.MedicationAdministrationService;
import com.healthcare.services.BedTransferService;
import com.healthcare.controller.components.SimplifiedMedicationController;
import com.healthcare.controller.components.BedTransferController;
import com.healthcare.controller.components.NursePatientCareController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for the Nurse Dashboard
 * Enhanced implementation with nurse-specific functionality
 */
public class NurseDashboardController extends BaseDashboardController {
    
    // Dashboard elements
    @FXML
    private Label totalResidentsLabel;
    @FXML
    private Label myPatientsLabel;
    @FXML
    private Label todaysTasksLabel;
    @FXML
    private Label pendingMedicationsLabel;
    @FXML
    private ListView<String> recentActivityList;
    
    // Navigation buttons
    @FXML
    private Button dashboardButton;
    @FXML
    private Button patientCareButton;
    @FXML
    private Button medicationsButton;
    @FXML
    private Button bedTransfersButton;
    
    // Content areas
    @FXML
    private VBox mainContentArea;
    @FXML
    private VBox dashboardContent;
    @FXML
    private VBox patientCareContent;
    @FXML
    private VBox medicationsContent;
    @FXML
    private VBox bedTransfersContent;
    
    // Services
    private ResidentService residentService = new ResidentService();
    private MedicationAdministrationService medicationService = new MedicationAdministrationService();
    private BedTransferService bedTransferService = new BedTransferService();

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        System.out.println("NurseDashboardController: Initializing...");
        super.initialize(location, resources);
        setupNavigation();
        loadDashboardData();
        System.out.println("NurseDashboardController: Initialization complete");
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
    private void showPatientCare() {
        setActiveButton(patientCareButton);
        showContent(patientCareContent);
        loadPatientCareData();
    }
    
    @FXML
    private void showMedications() {
        setActiveButton(medicationsButton);
        showContent(medicationsContent);
        loadMedicationData();
    }
    
    @FXML
    private void showBedTransfers() {
        setActiveButton(bedTransfersButton);
        showContent(bedTransfersContent);
        loadBedTransfersData();
    }
    
    
    @FXML
    
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
            
            // Load nurse's assigned patients count (nurses can care for all residents in their ward)
            // For simplicity, we'll show residents who need medication administration
            var residentsNeedingCare = allResidents.stream()
                .filter(resident -> resident.getCurrentBedId() != null)
                .count();
            myPatientsLabel.setText(String.valueOf(residentsNeedingCare));
            
            // Load today's medication tasks
            var medicationStats = medicationService.getMedicationStats();
            todaysTasksLabel.setText(String.valueOf(medicationStats.getTotalScheduled()));
            pendingMedicationsLabel.setText(String.valueOf(medicationStats.getPending()));
            
            // Load recent activity
            loadRecentActivity();
            
            System.out.println("Dashboard data loaded successfully for Nurse " + currentStaff.getFullName());
            System.out.println("Total patients: " + allResidents.size() + 
                             ", Today's tasks: " + medicationStats.getTotalScheduled() + 
                             ", Pending medications: " + medicationStats.getPending());
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void loadRecentActivity() {
        recentActivityList.getItems().clear();
        
        if (currentStaff != null) {
            try {
                // Load real activity based on nurse's recent actions
                var recentTransfers = bedTransferService.getRecentTransfersByNurse(currentStaff.getStaffId(), 3);
                var todaysAdministrations = medicationService.getTodaysAdministrations(currentStaff.getStaffId());
                
                // Add recent transfer activities
                recentTransfers.stream()
                    .limit(3)
                    .forEach(transfer -> {
                        String activity = "Transferred resident ID " + transfer.getResidentId() + 
                                        " to bed " + transfer.getToBedId();
                        recentActivityList.getItems().add(activity);
                    });
                
                // Add recent medication activities
                todaysAdministrations.stream()
                    .limit(2)
                    .forEach(admin -> {
                        String activity = "Administered medication - " + admin.getStatus();
                        recentActivityList.getItems().add(activity);
                    });
                
                // Add login activity
                recentActivityList.getItems().add("Nurse " + currentStaff.getFullName() + " logged in");
                recentActivityList.getItems().add("Dashboard loaded successfully");
                
            } catch (Exception e) {
                System.err.println("Error loading recent activity: " + e.getMessage());
                // Fallback to default activities
                recentActivityList.getItems().addAll(
                    "Nurse " + currentStaff.getFullName() + " logged in",
                    "Dashboard loaded",
                    "System ready"
                );
            }
        } else {
            recentActivityList.getItems().addAll(
                "System started successfully",
                "Nurse logged in",
                "Dashboard loaded"
            );
        }
    }
    
    private void loadPatientCareData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping Patient Care component loading");
                return;
            }
            
            // Clear existing content first
            patientCareContent.getChildren().clear();
            
            // Load Patient Care component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/nurse-patient-care.fxml"));
            VBox patientCareComponent = loader.load();
            
            // Set current nurse in the component
            NursePatientCareController controller = loader.getController();
            controller.setCurrentNurse(currentStaff);
            
            patientCareContent.getChildren().add(patientCareComponent);
            
            System.out.println("Patient Care component loaded successfully for Nurse " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading Patient Care component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading patient care data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            patientCareContent.getChildren().clear();
            patientCareContent.getChildren().add(errorLabel);
        }
    }
    
    private void loadMedicationData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping Medication Administration component loading");
                return;
            }
            
            // Clear existing content first
            medicationsContent.getChildren().clear();
            
            // Load Simplified Medication Administration component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/simplified-medication-administration.fxml"));
            VBox medicationComponent = loader.load();
            
            // Set current nurse in the component
            SimplifiedMedicationController controller = loader.getController();
            controller.setCurrentNurse(currentStaff);
            
            medicationsContent.getChildren().add(medicationComponent);
            
            System.out.println("Medication Administration component loaded successfully for Nurse " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading Medication Administration component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading medication data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            medicationsContent.getChildren().clear();
            medicationsContent.getChildren().add(errorLabel);
        }
    }
    
    private void loadBedTransfersData() {
        try {
            if (currentStaff == null) {
                System.out.println("Current staff not set yet, skipping Bed Transfers component loading");
                return;
            }
            
            // Clear existing content first
            bedTransfersContent.getChildren().clear();
            
            // Load Bed Transfers component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/bed-transfer.fxml"));
            VBox bedTransferComponent = loader.load();
            
            // Set current nurse in the component
            BedTransferController controller = loader.getController();
            controller.setCurrentNurse(currentStaff);
            
            bedTransfersContent.getChildren().add(bedTransferComponent);
            
            System.out.println("Bed Transfers component loaded successfully for Nurse " + currentStaff.getFullName());
        } catch (Exception e) {
            System.err.println("Error loading Bed Transfers component: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in the content area
            Label errorLabel = new Label("Error loading bed transfer data: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            bedTransfersContent.getChildren().clear();
            bedTransfersContent.getChildren().add(errorLabel);
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
            dashboardButton, patientCareButton, medicationsButton, bedTransfersButton
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
            dashboardContent, patientCareContent, medicationsContent, bedTransfersContent
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
        System.out.println("NurseDashboardController: Setting current staff to: " + (currentStaff != null ? currentStaff.getUsername() : "null"));
        super.setCurrentStaff(currentStaff);
        
        // Reload dashboard data when staff is set
        if (currentStaff != null) {
            loadDashboardData();
        }
    }
}
