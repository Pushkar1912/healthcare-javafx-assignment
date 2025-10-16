package com.healthcare.controller.components;

import com.healthcare.model.Resident;
import com.healthcare.model.Staff;
import com.healthcare.services.ResidentService;
import com.healthcare.services.PrescriptionService;
import com.healthcare.services.MedicationAdministrationService;
import com.healthcare.services.MedicationAdministrationService.MedicationSchedule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Nurse Patient Care component
 * Allows nurses to view patient details and care information
 */
public class NursePatientCareController implements Initializable {
    
    @FXML
    private VBox mainContainer;
    
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label myPatientsLabel;
    @FXML
    private Label todaysMedicationsLabel;
    @FXML
    private Label pendingTasksLabel;
    
    @FXML
    private TableView<Resident> patientsTable;
    @FXML
    private TableColumn<Resident, String> patientNameColumn;
    @FXML
    private TableColumn<Resident, String> bedColumn;
    @FXML
    private TableColumn<Resident, String> doctorColumn;
    @FXML
    private TableColumn<Resident, String> conditionColumn;
    @FXML
    private TableColumn<Resident, String> admissionDateColumn;
    
    @FXML
    private TableView<MedicationSchedule> medicationsTable;
    @FXML
    private TableColumn<MedicationSchedule, String> medPatientColumn;
    @FXML
    private TableColumn<MedicationSchedule, String> medMedicineColumn;
    @FXML
    private TableColumn<MedicationSchedule, String> medDosageColumn;
    @FXML
    private TableColumn<MedicationSchedule, String> medFrequencyColumn;
    @FXML
    private TableColumn<MedicationSchedule, String> medStatusColumn;
    
    @FXML
    private Button refreshButton;
    @FXML
    private Button viewPatientDetailsButton;
    @FXML
    private Button viewMedicationsButton;
    
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> wardFilterComboBox;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    // Services
    private ResidentService residentService = new ResidentService();
    private PrescriptionService prescriptionService = new PrescriptionService();
    private MedicationAdministrationService medicationService = new MedicationAdministrationService();
    private Staff currentNurse;
    
    // Data
    private ObservableList<Resident> patientsData = FXCollections.observableArrayList();
    private ObservableList<MedicationSchedule> medicationsData = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("NursePatientCareController: Initializing...");
        setupTableColumns();
        setupEventHandlers();
        setupFilters();
        loadData();
        System.out.println("NursePatientCareController: Initialization complete");
    }
    
    private void setupTableColumns() {
        // Patients table
        patientNameColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(resident.getFullName());
        });
        
        bedColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            String bedCode = residentService.getBedCodeForResident(resident);
            return new javafx.beans.property.SimpleStringProperty(bedCode);
        });
        
        doctorColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            // This would need to be implemented to get doctor name
            return new javafx.beans.property.SimpleStringProperty("Dr. Smith");
        });
        
        conditionColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            String condition = resident.getMedicalCondition();
            return new javafx.beans.property.SimpleStringProperty(condition != null ? condition : "N/A");
        });
        
        admissionDateColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                resident.getAdmissionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
        });
        
        // Medications table
        medPatientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        medMedicineColumn.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        medDosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        medFrequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        medStatusColumn.setCellValueFactory(cellData -> {
            // This would need to check if medication was administered today
            return new javafx.beans.property.SimpleStringProperty("Pending");
        });
    }
    
    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadData());
        
        viewPatientDetailsButton.setOnAction(e -> viewPatientDetails());
        viewMedicationsButton.setOnAction(e -> viewPatientMedications());
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterPatients());
        wardFilterComboBox.setOnAction(e -> filterPatients());
        statusFilterComboBox.setOnAction(e -> filterPatients());
        
        // Enable/disable buttons based on selection
        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            viewPatientDetailsButton.setDisable(!hasSelection);
            viewMedicationsButton.setDisable(!hasSelection);
        });
    }
    
    private void setupFilters() {
        wardFilterComboBox.getItems().addAll("All Wards", "Ward 1", "Ward 2");
        wardFilterComboBox.setValue("All Wards");
        
        statusFilterComboBox.getItems().addAll("All Patients", "Active", "Discharged");
        statusFilterComboBox.setValue("All Patients");
    }
    
    private void loadData() {
        try {
            // Load all active residents
            List<Resident> residents = residentService.findActiveResidents();
            patientsData.clear();
            patientsData.addAll(residents);
            patientsTable.setItems(patientsData);
            
            // Load today's medications
            List<MedicationSchedule> medications = medicationService.getTodaysMedicationSchedule();
            medicationsData.clear();
            medicationsData.addAll(medications);
            medicationsTable.setItems(medicationsData);
            
            // Update statistics
            updateStatistics();
            
            System.out.println("Loaded " + residents.size() + " patients, " + medications.size() + " medications");
            
        } catch (Exception e) {
            System.err.println("Error loading patient care data: " + e.getMessage());
            showError("Failed to load patient care data: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            // Total patients
            List<Resident> allResidents = residentService.findActiveResidents();
            totalPatientsLabel.setText(String.valueOf(allResidents.size()));
            
            // My patients (simplified - all patients for now)
            myPatientsLabel.setText(String.valueOf(allResidents.size()));
            
            // Today's medications
            List<MedicationSchedule> todaysMedications = medicationService.getTodaysMedicationSchedule();
            todaysMedicationsLabel.setText(String.valueOf(todaysMedications.size()));
            
            // Pending tasks (simplified)
            List<MedicationSchedule> pendingMedications = medicationService.getPendingMedications();
            pendingTasksLabel.setText(String.valueOf(pendingMedications.size()));
            
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
    
    private void filterPatients() {
        String searchText = searchField.getText().toLowerCase();
        String wardFilter = wardFilterComboBox.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        
        patientsData.clear();
        
        try {
            List<Resident> allResidents = residentService.findActiveResidents();
            
            for (Resident resident : allResidents) {
                // Search filter
                if (!searchText.isEmpty()) {
                    String fullName = resident.getFullName().toLowerCase();
                    if (!fullName.contains(searchText)) {
                        continue;
                    }
                }
                
                // Ward filter (simplified)
                if (!wardFilter.equals("All Wards")) {
                    // This would need to be implemented based on bed location
                    // For now, skip ward filtering
                }
                
                // Status filter
                if (statusFilter.equals("Active") && resident.getDischargeDate() != null) {
                    continue;
                }
                if (statusFilter.equals("Discharged") && resident.getDischargeDate() == null) {
                    continue;
                }
                
                patientsData.add(resident);
            }
            
        } catch (Exception e) {
            System.err.println("Error filtering patients: " + e.getMessage());
        }
    }
    
    private void viewPatientDetails() {
        Resident selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Please select a patient to view details.");
            return;
        }
        
        // Create patient details dialog
        Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
        detailsDialog.setTitle("Patient Details");
        detailsDialog.setHeaderText("Patient Information");
        
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(selectedPatient.getFullName()).append("\n");
        details.append("Gender: ").append(selectedPatient.getGender()).append("\n");
        details.append("Birth Date: ").append(selectedPatient.getBirthDate() != null ? 
            selectedPatient.getBirthDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A").append("\n");
        details.append("Admission Date: ").append(selectedPatient.getAdmissionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
        details.append("Current Bed: ").append(residentService.getBedCodeForResident(selectedPatient)).append("\n");
        details.append("Medical Condition: ").append(selectedPatient.getMedicalCondition() != null ? selectedPatient.getMedicalCondition() : "N/A").append("\n");
        details.append("Requires Isolation: ").append(selectedPatient.isRequiresIsolation() ? "Yes" : "No").append("\n");
        details.append("Emergency Contact: ").append(selectedPatient.getEmergencyContact() != null ? selectedPatient.getEmergencyContact() : "N/A");
        
        detailsDialog.setContentText(details.toString());
        detailsDialog.showAndWait();
    }
    
    private void viewPatientMedications() {
        Resident selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Please select a patient to view medications.");
            return;
        }
        
        // Filter medications for selected patient
        medicationsData.clear();
        
        try {
            List<MedicationSchedule> allMedications = medicationService.getTodaysMedicationSchedule();
            for (MedicationSchedule medication : allMedications) {
                if (medication.getResidentId().equals(selectedPatient.getResidentId())) {
                    medicationsData.add(medication);
                }
            }
            
            if (medicationsData.isEmpty()) {
                showAlert("No medications scheduled for this patient today.");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading patient medications: " + e.getMessage());
            showError("Failed to load patient medications: " + e.getMessage());
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Patient Care");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setCurrentNurse(Staff nurse) {
        this.currentNurse = nurse;
        System.out.println("NursePatientCareController: Set current nurse to " + (nurse != null ? nurse.getFullName() : "null"));
    }
}
