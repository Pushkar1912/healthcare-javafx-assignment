package com.healthcare.controller.components;

import com.healthcare.model.Resident;
import com.healthcare.model.Medicine;
import com.healthcare.model.AdministeredMedication;
import com.healthcare.services.MedicationAdministrationService;
import com.healthcare.services.ResidentService;
import com.healthcare.services.MedicineService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Simplified Medication Administration Controller
 * Focuses on core nurse tasks: view today's medications and administer them
 */
public class SimplifiedMedicationController implements Initializable {
    
    // Summary Labels
    @FXML private Label totalPatientsLabel;
    @FXML private Label medicationsDueLabel;
    @FXML private Label completedLabel;
    @FXML private Label overdueLabel;
    
    // Action Buttons
    @FXML private Button refreshButton;
    @FXML private Button markGivenButton;
    @FXML private Button markMissedButton;
    
    // Medication Table
    @FXML private TableView<MedicationItem> medicationTable;
    @FXML private TableColumn<MedicationItem, String> patientColumn;
    @FXML private TableColumn<MedicationItem, String> medicineColumn;
    @FXML private TableColumn<MedicationItem, String> dosageColumn;
    @FXML private TableColumn<MedicationItem, String> timeColumn;
    @FXML private TableColumn<MedicationItem, String> statusColumn;
    @FXML private TableColumn<MedicationItem, String> actionColumn;
    
    // Quick Form
    @FXML private ComboBox<Resident> patientComboBox;
    @FXML private ComboBox<Medicine> medicineComboBox;
    @FXML private TextField dosageTextField;
    @FXML private TextField notesTextField;
    @FXML private Button administerButton;
    @FXML private Button clearButton;
    
    // Status
    @FXML private VBox statusContainer;
    @FXML private Label statusLabel;
    
    // Data
    private ObservableList<MedicationItem> medicationData = FXCollections.observableArrayList();
    private ObservableList<Resident> patientsData = FXCollections.observableArrayList();
    private ObservableList<Medicine> medicinesData = FXCollections.observableArrayList();
    
    // Services
    private MedicationAdministrationService medicationService = new MedicationAdministrationService();
    private ResidentService residentService = new ResidentService();
    private MedicineService medicineService = new MedicineService();
    
    // Current nurse (set by parent controller)
    private com.healthcare.model.Staff currentNurse;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SimplifiedMedicationController: Initializing...");
        
        setupTableColumns();
        setupEventHandlers();
        loadData();
        
        System.out.println("SimplifiedMedicationController: Initialization complete");
    }
    
    private void setupTableColumns() {
        // Configure table columns
        patientColumn.setCellValueFactory(cellData -> cellData.getValue().patientNameProperty());
        medicineColumn.setCellValueFactory(cellData -> cellData.getValue().medicineNameProperty());
        dosageColumn.setCellValueFactory(cellData -> cellData.getValue().dosageProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        
        // Set table data
        medicationTable.setItems(medicationData);
    }
    
    private void setupEventHandlers() {
        // Refresh button
        refreshButton.setOnAction(e -> loadData());
        
        // Quick form buttons
        administerButton.setOnAction(e -> administerMedication());
        clearButton.setOnAction(e -> clearForm());
        
        // Mark buttons
        markGivenButton.setOnAction(e -> markSelectedAsGiven());
        markMissedButton.setOnAction(e -> markSelectedAsMissed());
    }
    
    private void loadData() {
        try {
            // Load patients with custom display format
            List<Resident> patients = residentService.findAll();
            patientsData.clear();
            patientsData.addAll(patients);
            patientComboBox.setItems(patientsData);
            setupPatientComboBoxFormat();
            
            // Set up patient selection handler to filter medicines
            patientComboBox.setOnAction(e -> {
                Resident selectedPatient = patientComboBox.getSelectionModel().getSelectedItem();
                if (selectedPatient != null) {
                    loadMedicinesForPatient(selectedPatient.getResidentId());
                } else {
                    // Clear medicine dropdown if no patient selected
                    medicineComboBox.getItems().clear();
                }
            });
            
            // Load all medicines initially (will be filtered when patient is selected)
            List<Medicine> medicines = medicineService.findAll();
            medicinesData.clear();
            medicinesData.addAll(medicines);
            setupMedicineComboBoxFormat();
            
            // Load today's medications (simplified)
            loadTodaysMedications();
            
            // Update summary
            updateSummary();
            
            System.out.println("Loaded " + patients.size() + " patients, " + medicines.size() + " medicines");
            
        } catch (Exception e) {
            System.err.println("Error loading medication data: " + e.getMessage());
            showStatus("Error loading data: " + e.getMessage(), "error");
        }
    }
    
    private void loadTodaysMedications() {
        medicationData.clear();
        
        // Create sample medication items for demonstration
        medicationData.add(new MedicationItem(
            "John Doe", "Aspirin", "100mg", "9:00 AM", "Due", "Give"
        ));
        medicationData.add(new MedicationItem(
            "Jane Smith", "Pain Relief", "50mg", "10:30 AM", "Due", "Give"
        ));
        medicationData.add(new MedicationItem(
            "John Doe", "Insulin", "10 units", "12:00 PM", "Given", "Completed"
        ));
        medicationData.add(new MedicationItem(
            "Bob Wilson", "Antibiotics", "500mg", "2:00 PM", "Overdue", "Give Now"
        ));
    }
    
    private void updateSummary() {
        // Update summary labels
        totalPatientsLabel.setText(String.valueOf(patientsData.size()));
        
        long dueCount = medicationData.stream()
            .filter(item -> "Due".equals(item.getStatus()) || "Overdue".equals(item.getStatus()))
            .count();
        medicationsDueLabel.setText(String.valueOf(dueCount));
        
        long completedCount = medicationData.stream()
            .filter(item -> "Given".equals(item.getStatus()))
            .count();
        completedLabel.setText(String.valueOf(completedCount));
        
        long overdueCount = medicationData.stream()
            .filter(item -> "Overdue".equals(item.getStatus()))
            .count();
        overdueLabel.setText(String.valueOf(overdueCount));
    }
    
    private void administerMedication() {
        Resident selectedPatient = patientComboBox.getSelectionModel().getSelectedItem();
        Medicine selectedMedicine = medicineComboBox.getSelectionModel().getSelectedItem();
        String dosage = dosageTextField.getText().trim();
        String notes = notesTextField.getText().trim();
        
        if (selectedPatient == null || selectedMedicine == null || dosage.isEmpty()) {
            showStatus("Please select patient, medicine, and enter dosage", "error");
            return;
        }
        
        try {
            // For now, we'll use a simplified approach since we need prescription_medicine_id
            // Find a valid prescription_medicine_id for this patient and medicine
            Long prescriptionMedicineId = findPrescriptionMedicineId(selectedPatient.getResidentId(), selectedMedicine.getMedicineId());
            
            if (prescriptionMedicineId == null) {
                showStatus("No prescription found for this patient and medicine", "error");
                return;
            }
            
            // Mark medication as administered using the service method
            boolean success = medicationService.markMedicationAsAdministered(
                prescriptionMedicineId,
                currentNurse.getStaffId(),
                dosage,
                notes
            );
            
            if (!success) {
                throw new Exception("Failed to record medication administration");
            }
            
            showStatus("Medication administered successfully for " + selectedPatient.getFirstName() + " " + selectedPatient.getLastName(), "success");
            clearForm();
            loadData(); // Refresh the data
            
        } catch (Exception e) {
            System.err.println("Error administering medication: " + e.getMessage());
            showStatus("Error administering medication: " + e.getMessage(), "error");
        }
    }
    
    private void markSelectedAsGiven() {
        MedicationItem selected = medicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a medication to mark as given", "error");
            return;
        }
        
        selected.setStatus("Given");
        selected.setAction("Completed");
        medicationTable.refresh();
        updateSummary();
        showStatus("Medication marked as given", "success");
    }
    
    private void markSelectedAsMissed() {
        MedicationItem selected = medicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a medication to mark as missed", "error");
            return;
        }
        
        selected.setStatus("Missed");
        selected.setAction("Missed");
        medicationTable.refresh();
        updateSummary();
        showStatus("Medication marked as missed", "success");
    }
    
    private void clearForm() {
        patientComboBox.getSelectionModel().clearSelection();
        medicineComboBox.getSelectionModel().clearSelection();
        dosageTextField.clear();
        notesTextField.clear();
        hideStatus();
    }
    
    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        statusContainer.setVisible(true);
        
        if ("error".equals(type)) {
            statusLabel.setStyle("-fx-text-fill: #DC3545; -fx-font-size: 12px;");
        } else if ("success".equals(type)) {
            statusLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 12px;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #2E86AB; -fx-font-size: 12px;");
        }
    }
    
    private void hideStatus() {
        statusContainer.setVisible(false);
    }
    
    private void setupPatientComboBoxFormat() {
        // Set custom cell factory to display "ID - Name"
        patientComboBox.setCellFactory(listView -> new ListCell<Resident>() {
            @Override
            protected void updateItem(Resident resident, boolean empty) {
                super.updateItem(resident, empty);
                if (empty || resident == null) {
                    setText(null);
                } else {
                    setText(resident.getResidentId() + " - " + resident.getFirstName() + " " + resident.getLastName());
                }
            }
        });
        
        // Set custom button cell to show the same format when selected
        patientComboBox.setButtonCell(new ListCell<Resident>() {
            @Override
            protected void updateItem(Resident resident, boolean empty) {
                super.updateItem(resident, empty);
                if (empty || resident == null) {
                    setText("Select patient...");
                } else {
                    setText(resident.getResidentId() + " - " + resident.getFirstName() + " " + resident.getLastName());
                }
            }
        });
    }
    
    private void setupMedicineComboBoxFormat() {
        // Set custom cell factory to display "ID - Name"
        medicineComboBox.setCellFactory(listView -> new ListCell<Medicine>() {
            @Override
            protected void updateItem(Medicine medicine, boolean empty) {
                super.updateItem(medicine, empty);
                if (empty || medicine == null) {
                    setText(null);
                } else {
                    setText(medicine.getMedicineId() + " - " + medicine.getName());
                }
            }
        });
        
        // Set custom button cell to show the same format when selected
        medicineComboBox.setButtonCell(new ListCell<Medicine>() {
            @Override
            protected void updateItem(Medicine medicine, boolean empty) {
                super.updateItem(medicine, empty);
                if (empty || medicine == null) {
                    setText("Select medicine...");
                } else {
                    setText(medicine.getMedicineId() + " - " + medicine.getName());
                }
            }
        });
    }
    
    private void loadMedicinesForPatient(Long residentId) {
        try {
            // Get medicines prescribed to this specific patient
            List<Medicine> prescribedMedicines = medicationService.getMedicinesForPatient(residentId);
            
            // Clear and populate medicine dropdown with prescribed medicines only
            medicineComboBox.getItems().clear();
            medicineComboBox.getItems().addAll(prescribedMedicines);
            
            // Clear any previous selection
            medicineComboBox.getSelectionModel().clearSelection();
            
            System.out.println("Loaded " + prescribedMedicines.size() + " prescribed medicines for patient ID: " + residentId);
            
        } catch (Exception e) {
            System.err.println("Error loading medicines for patient: " + e.getMessage());
            e.printStackTrace();
            // Fallback to showing all medicines if there's an error
            medicineComboBox.getItems().clear();
            medicineComboBox.getItems().addAll(medicinesData);
        }
    }
    
    private Long findPrescriptionMedicineId(Long residentId, Long medicineId) {
        try {
            // Query to find a prescription_medicine_id for this patient and medicine
            String sql = """
                SELECT pm.id 
                FROM Prescription_Medicines pm 
                JOIN Prescriptions p ON pm.prescription_id = p.prescription_id 
                WHERE p.resident_id = ? AND pm.medicine_id = ? AND pm.is_active = TRUE 
                LIMIT 1
                """;
            
            return medicationService.findPrescriptionMedicineId(residentId, medicineId);
        } catch (Exception e) {
            System.err.println("Error finding prescription medicine ID: " + e.getMessage());
            return null;
        }
    }
    
    public void setCurrentNurse(com.healthcare.model.Staff nurse) {
        this.currentNurse = nurse;
        System.out.println("SimplifiedMedicationController: Set current nurse to " + nurse.getFullName());
    }
    
    /**
     * Simple data class for medication table items
     */
    public static class MedicationItem {
        private String patientName;
        private String medicineName;
        private String dosage;
        private String time;
        private String status;
        private String action;
        
        public MedicationItem(String patientName, String medicineName, String dosage, String time, String status, String action) {
            this.patientName = patientName;
            this.medicineName = medicineName;
            this.dosage = dosage;
            this.time = time;
            this.status = status;
            this.action = action;
        }
        
        // Getters and setters
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        // Property methods for TableView
        public javafx.beans.property.StringProperty patientNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(patientName);
        }
        
        public javafx.beans.property.StringProperty medicineNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(medicineName);
        }
        
        public javafx.beans.property.StringProperty dosageProperty() {
            return new javafx.beans.property.SimpleStringProperty(dosage);
        }
        
        public javafx.beans.property.StringProperty timeProperty() {
            return new javafx.beans.property.SimpleStringProperty(time);
        }
        
        public javafx.beans.property.StringProperty statusProperty() {
            return new javafx.beans.property.SimpleStringProperty(status);
        }
        
        public javafx.beans.property.StringProperty actionProperty() {
            return new javafx.beans.property.SimpleStringProperty(action);
        }
    }
}
