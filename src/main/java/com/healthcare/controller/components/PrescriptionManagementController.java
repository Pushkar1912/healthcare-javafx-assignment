package com.healthcare.controller.components;

import com.healthcare.model.Prescription;
import com.healthcare.model.Resident;
import com.healthcare.model.Medicine;
import com.healthcare.model.Staff;
import com.healthcare.services.PrescriptionService;
import com.healthcare.services.ResidentService;
import com.healthcare.services.MedicineService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Component Controller for Prescription Management
 * Handles prescription CRUD operations for doctors
 */
public class PrescriptionManagementController implements Initializable {
    
    // FXML Elements
    @FXML private Label activePrescriptionsLabel;
    @FXML private Label pendingReviewsLabel;
    @FXML private Label completedTodayLabel;
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> patientNameColumn;
    @FXML private TableColumn<Prescription, LocalDate> prescriptionDateColumn;
    @FXML private TableColumn<Prescription, String> medicinesColumn;
    @FXML private TableColumn<Prescription, String> statusColumn;
    @FXML private TableColumn<Prescription, String> reviewStatusColumn;
    @FXML private TableColumn<Prescription, String> actionsColumn;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField searchField;
    
    // Form Elements
    @FXML private VBox prescriptionFormContainer;
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Resident> patientComboBox;
    @FXML private DatePicker prescriptionDatePicker;
    @FXML private ComboBox<Medicine> medicineComboBox;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private ListView<String> medicinesListView;
    @FXML private TextArea notesTextArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button removeMedicineButton;
    @FXML private Label medicinesCountLabel;
    @FXML private Button viewPrescriptionButton;
    @FXML private Button editPrescriptionButton;
    @FXML private Button deletePrescriptionButton;
    @FXML private Label selectedPrescriptionLabel;
    
    // Data
    private ObservableList<Prescription> prescriptionsList = FXCollections.observableArrayList();
    private ObservableList<String> medicinesList = FXCollections.observableArrayList();
    private Staff currentDoctor;
    
    // Services
    private PrescriptionService prescriptionService = new PrescriptionService();
    private ResidentService residentService = new ResidentService();
    private MedicineService medicineService = new MedicineService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupForm();
        loadData();
        
        // Initialize medicines list view
        medicinesListView.setItems(FXCollections.observableArrayList(medicinesList));
        System.out.println("Medicines ListView initialized with " + medicinesList.size() + " items");
    }
    
    /**
     * Set the current doctor for prescription management
     */
    public void setCurrentDoctor(Staff doctor) {
        this.currentDoctor = doctor;
        loadDoctorPatients();
        loadData();
    }
    
    private void setupTable() {
        System.out.println("Setting up prescription management component...");
        
        // Setup table columns
        patientNameColumn.setCellValueFactory(cellData -> {
            Prescription prescription = cellData.getValue();
            // Get patient name from database using prescription service
            try {
                String patientName = prescriptionService.getPatientNameByResidentId(prescription.getResidentId());
                return new javafx.beans.property.SimpleStringProperty(patientName);
            } catch (Exception e) {
                System.err.println("Error getting patient name: " + e.getMessage());
                return new javafx.beans.property.SimpleStringProperty("Unknown Patient");
            }
        });
        
        prescriptionDateColumn.setCellValueFactory(new PropertyValueFactory<>("prescriptionDate"));
        
        medicinesColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Multiple medicines");
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            Prescription prescription = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                prescription.getStatus() != null ? prescription.getStatus().toString() : "Unknown"
            );
        });
        
        reviewStatusColumn.setCellValueFactory(cellData -> {
            Prescription prescription = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                prescription.getReviewStatus() != null ? prescription.getReviewStatus().toString() : "Unknown"
            );
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
            private final Button viewButton = new Button("👁️");
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");
            private final HBox buttonBox = new HBox(5);
            
            {
                // Style buttons
                viewButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
                editButton.setStyle("-fx-background-color: #F18F01; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 12px;");
                
                // Set button actions
                viewButton.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    viewPrescription(prescription);
                });
                
                editButton.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    editPrescription(prescription);
                });
                
                deleteButton.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    deletePrescription(prescription);
                });
                
                buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
        
        // Remove old selection listener and action buttons since we now have inline buttons
        
        // Setup search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterPrescriptions());
        
        // Setup status filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Active", "Completed", "Cancelled", "Pending", "Reviewed", "Approved", "Rejected"
        ));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> filterPrescriptions());
    }
    
    private void setupForm() {
        // Setup date picker
        prescriptionDatePicker.setValue(LocalDate.now());
        
        // Setup medicines list view with custom cell factory
        medicinesListView.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("💊 " + item);
                    setStyle("-fx-padding: 8 12; -fx-background-color: #F8F9FA; -fx-border-color: #DEE2E6; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-margin: 2; -fx-font-size: 13px;");
                }
            }
        });
        
        // Setup medicine combo box
        try {
            List<Medicine> medicines = medicineService.findAll();
            medicineComboBox.setItems(FXCollections.observableArrayList(medicines));
            medicineComboBox.setCellFactory(listView -> new ListCell<Medicine>() {
                @Override
                protected void updateItem(Medicine medicine, boolean empty) {
                    super.updateItem(medicine, empty);
                    if (empty || medicine == null) {
                        setText(null);
                    } else {
                        setText(medicine.getName() + " (" + medicine.getDosageUnit() + ")");
                    }
                }
            });
            
            // Set the button cell to show the selected medicine name
            medicineComboBox.setButtonCell(new ListCell<Medicine>() {
                @Override
                protected void updateItem(Medicine medicine, boolean empty) {
                    super.updateItem(medicine, empty);
                    if (empty || medicine == null) {
                        setText(null);
                    } else {
                        setText(medicine.getName() + " (" + medicine.getDosageUnit() + ")");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading medicines: " + e.getMessage());
        }
        
        // Setup patient combo box - will be populated when doctor is set
        patientComboBox.setCellFactory(listView -> new ListCell<Resident>() {
            @Override
            protected void updateItem(Resident resident, boolean empty) {
                super.updateItem(resident, empty);
                if (empty || resident == null) {
                    setText(null);
                } else {
                    setText(resident.getFirstName() + " " + resident.getLastName());
                }
            }
        });
        
        // Set the button cell to show the selected patient name
        patientComboBox.setButtonCell(new ListCell<Resident>() {
            @Override
            protected void updateItem(Resident resident, boolean empty) {
                super.updateItem(resident, empty);
                if (empty || resident == null) {
                    setText(null);
                } else {
                    setText(resident.getFirstName() + " " + resident.getLastName());
                }
            }
        });
    }
    
    private void loadDoctorPatients() {
        try {
            if (currentDoctor == null) {
                System.out.println("No current doctor set, cannot load patients");
                patientComboBox.setItems(FXCollections.observableArrayList());
                return;
            }
            
            // Load only patients assigned to this doctor
            List<Resident> allResidents = residentService.findAll();
            List<Resident> doctorPatients = allResidents.stream()
                .filter(resident -> resident.getAssignedDoctorId() != null && 
                        resident.getAssignedDoctorId().equals(currentDoctor.getStaffId()))
                .collect(Collectors.toList());
            
            patientComboBox.setItems(FXCollections.observableArrayList(doctorPatients));
            
            if (doctorPatients.isEmpty()) {
                System.out.println("No patients assigned to Dr. " + currentDoctor.getFullName());
                // Add a placeholder item to show in the dropdown
                Resident placeholder = new Resident();
                placeholder.setFirstName("No patients assigned");
                placeholder.setLastName("");
                placeholder.setResidentId(-1L); // Use -1 as a special ID for placeholder
                patientComboBox.getItems().add(placeholder);
            } else {
                System.out.println("Loaded " + doctorPatients.size() + " patients for Dr. " + currentDoctor.getFullName());
            }
            
        } catch (Exception e) {
            System.err.println("Error loading doctor's patients: " + e.getMessage());
            patientComboBox.setItems(FXCollections.observableArrayList());
        }
    }
    
    private void loadData() {
        try {
            List<Prescription> allPrescriptions = prescriptionService.findAll();
            
            // Filter prescriptions to show only those created by the current doctor
            List<Prescription> doctorPrescriptions = allPrescriptions.stream()
                .filter(prescription -> currentDoctor != null && 
                        prescription.getDoctorId().equals(currentDoctor.getStaffId()))
                .collect(Collectors.toList());
            
            prescriptionsList.setAll(doctorPrescriptions);
            prescriptionsTable.setItems(prescriptionsList);
            
            // Update statistics based on doctor's prescriptions
            long activeCount = doctorPrescriptions.stream()
                .filter(p -> p.getStatus() == Prescription.PrescriptionStatus.Active)
                .count();
            long pendingCount = doctorPrescriptions.stream()
                .filter(p -> p.getReviewStatus() == Prescription.ReviewStatus.Pending)
                .count();
            long completedToday = doctorPrescriptions.stream()
                .filter(p -> p.getPrescriptionDate().equals(LocalDate.now()) && 
                           p.getStatus() == Prescription.PrescriptionStatus.Completed)
                .count();
            
            activePrescriptionsLabel.setText(String.valueOf(activeCount));
            pendingReviewsLabel.setText(String.valueOf(pendingCount));
            completedTodayLabel.setText(String.valueOf(completedToday));
            
            System.out.println("Prescription management component loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading prescription data: " + e.getMessage());
            showError("Failed to load prescription data");
        }
    }
    
    private void filterPrescriptions() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();
        
        ObservableList<Prescription> filteredList = FXCollections.observableArrayList();
        
        for (Prescription prescription : prescriptionsList) {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() || 
                (prescription.getNotes() != null && prescription.getNotes().toLowerCase().contains(searchText));
            
            // Status filter
            boolean matchesStatus = statusFilter.equals("All") ||
                (statusFilter.equals("Active") && prescription.getStatus() == Prescription.PrescriptionStatus.Active) ||
                (statusFilter.equals("Completed") && prescription.getStatus() == Prescription.PrescriptionStatus.Completed) ||
                (statusFilter.equals("Cancelled") && prescription.getStatus() == Prescription.PrescriptionStatus.Cancelled) ||
                (statusFilter.equals("Pending") && prescription.getReviewStatus() == Prescription.ReviewStatus.Pending) ||
                (statusFilter.equals("Reviewed") && prescription.getReviewStatus() == Prescription.ReviewStatus.Reviewed) ||
                (statusFilter.equals("Approved") && prescription.getReviewStatus() == Prescription.ReviewStatus.Approved) ||
                (statusFilter.equals("Rejected") && prescription.getReviewStatus() == Prescription.ReviewStatus.Rejected);
            
            if (matchesSearch && matchesStatus) {
                filteredList.add(prescription);
            }
        }
        
        prescriptionsTable.setItems(filteredList);
    }
    
    // Action methods
    @FXML
    private void showNewPrescriptionForm() {
        formTitleLabel.setText("New Prescription");
        prescriptionFormContainer.setVisible(true);
        prescriptionFormContainer.setManaged(true);
        clearForm();
        
        // Initialize medicines list view
        medicinesListView.setItems(FXCollections.observableArrayList(medicinesList));
        updateMedicinesCount();
    }
    
    @FXML
    private void refreshPrescriptionsList() {
        loadDoctorPatients();
        loadData();
        showSuccess("Prescription list refreshed");
    }
    
    @FXML
    private void showPendingReviews() {
        statusFilterComboBox.setValue("Pending");
        filterPrescriptions();
    }
    
    @FXML
    private void addMedicineToList() {
        Medicine selectedMedicine = medicineComboBox.getValue();
        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        
        if (selectedMedicine == null || dosage.isEmpty() || frequency.isEmpty()) {
            showError("Please select a medicine and enter dosage and frequency");
            return;
        }
        
        String medicineEntry = String.format("%s | %s | %s", selectedMedicine.getName(), dosage, frequency);
        medicinesList.add(medicineEntry);
        
        // Force refresh the ListView
        medicinesListView.setItems(FXCollections.observableArrayList(medicinesList));
        
        // Update medicines count
        updateMedicinesCount();
        
        // Clear fields
        medicineComboBox.setValue(null);
        dosageField.clear();
        frequencyField.clear();
        
        System.out.println("Added medicine: " + medicineEntry);
        System.out.println("Medicines list size: " + medicinesList.size());
        System.out.println("ListView items count: " + medicinesListView.getItems().size());
        
        // Show success message
        showSuccess("Medicine added to prescription");
    }
    
    @FXML
    private void removeSelectedMedicine() {
        String selectedMedicine = medicinesListView.getSelectionModel().getSelectedItem();
        if (selectedMedicine != null) {
            medicinesList.remove(selectedMedicine);
            // Force refresh the ListView
            medicinesListView.setItems(FXCollections.observableArrayList(medicinesList));
            updateMedicinesCount();
            showSuccess("Medicine removed from prescription");
        } else {
            showError("Please select a medicine to remove");
        }
    }
    
    @FXML
    private void savePrescription() {
        Resident selectedPatient = patientComboBox.getValue();
        LocalDate prescriptionDate = prescriptionDatePicker.getValue();
        String notes = notesTextArea.getText().trim();
        
        if (selectedPatient == null || prescriptionDate == null) {
            showError("Please select a patient and prescription date");
            return;
        }
        
        // Check if placeholder is selected
        if (selectedPatient.getResidentId() == -1L) {
            showError("No patients are assigned to you. Please contact the manager to assign patients.");
            return;
        }
        
        if (medicinesList.isEmpty()) {
            showError("Please add at least one medicine");
            return;
        }
        
        try {
            Prescription prescription = new Prescription(
                selectedPatient.getResidentId(),
                currentDoctor.getStaffId(),
                prescriptionDate,
                notes
            );
            
            // Save prescription first to get the prescription_id
            prescriptionService.save(prescription);
            
            // Now save each medicine to Prescription_Medicines table
            for (String medicineEntry : medicinesList) {
                // Parse the medicine entry: "Medicine Name | Dosage | Frequency"
                String[] parts = medicineEntry.split(" \\| ");
                if (parts.length == 3) {
                    String medicineName = parts[0].trim();
                    String dosage = parts[1].trim();
                    String frequency = parts[2].trim();
                    
                    // Find the medicine by name
                    List<Medicine> allMedicines = medicineService.findAll();
                    Medicine medicine = allMedicines.stream()
                        .filter(m -> m.getName().equals(medicineName))
                        .findFirst()
                        .orElse(null);
                    
                    if (medicine != null) {
                        // Save to Prescription_Medicines table
                        prescriptionService.savePrescriptionMedicine(
                            prescription.getPrescriptionId(),
                            medicine.getMedicineId(),
                            dosage,
                            frequency,
                            prescriptionDate, // start_date
                            null, // end_date (can be set later)
                            "" // instructions
                        );
                    }
                }
            }
            
            showSuccess("Prescription saved successfully with " + medicinesList.size() + " medicines");
            prescriptionFormContainer.setVisible(false);
            prescriptionFormContainer.setManaged(false);
            loadData();
            
        } catch (Exception e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            showError("Failed to save prescription: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancelForm() {
        prescriptionFormContainer.setVisible(false);
        prescriptionFormContainer.setManaged(false);
        clearForm();
    }
    
    private void clearForm() {
        patientComboBox.setValue(null);
        prescriptionDatePicker.setValue(LocalDate.now());
        medicineComboBox.setValue(null);
        dosageField.clear();
        frequencyField.clear();
        medicinesList.clear();
        // Force refresh the ListView
        medicinesListView.setItems(FXCollections.observableArrayList(medicinesList));
        notesTextArea.clear();
        
        updateMedicinesCount();
        System.out.println("Form cleared, medicines list size: " + medicinesList.size());
    }
    
    private void updateMedicinesCount() {
        if (medicinesCountLabel != null) {
            medicinesCountLabel.setText("(" + medicinesList.size() + " medicines)");
        }
    }
    
    // Old action button methods removed - now using inline table buttons
    
    private void viewPrescription(Prescription prescription) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prescription Details");
        alert.setHeaderText("Prescription #" + prescription.getPrescriptionId());
        
        // Get patient name from database
        String patientName = prescriptionService.getPatientNameByResidentId(prescription.getResidentId());
        
        StringBuilder content = new StringBuilder();
        content.append("Patient: ").append(patientName).append("\n");
        content.append("Date: ").append(prescription.getPrescriptionDate()).append("\n");
        content.append("Status: ").append(prescription.getStatus()).append("\n");
        content.append("Review Status: ").append(prescription.getReviewStatus()).append("\n");
        content.append("Notes: ").append(prescription.getNotes() != null ? prescription.getNotes() : "None");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void editPrescription(Prescription prescription) {
        showInfo("Edit prescription functionality will be implemented soon");
    }
    
    private void deletePrescription(Prescription prescription) {
        // Get patient name from database
        String patientName = prescriptionService.getPatientNameByResidentId(prescription.getResidentId());
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Prescription");
        confirmAlert.setHeaderText("Are you sure you want to delete this prescription?");
        confirmAlert.setContentText("Prescription #" + prescription.getPrescriptionId() + " for " + patientName);
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                prescriptionService.delete(prescription.getPrescriptionId());
                showSuccess("Prescription deleted successfully");
                loadData();
            } catch (Exception e) {
                showError("Failed to delete prescription: " + e.getMessage());
            }
        }
    }
    
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
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
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
