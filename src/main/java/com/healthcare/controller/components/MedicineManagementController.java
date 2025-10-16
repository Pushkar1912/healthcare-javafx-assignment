package com.healthcare.controller.components;

import com.healthcare.model.Medicine;
import com.healthcare.model.Staff;
import com.healthcare.services.MedicineService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Component Controller for Medicine Management
 * Handles medicine CRUD operations for doctors
 */
public class MedicineManagementController implements Initializable {
    
    // FXML Elements
    @FXML private Label totalMedicinesLabel;
    @FXML private Label activeMedicinesLabel;
    @FXML private Label categoriesLabel;
    @FXML private TableView<Medicine> medicinesTable;
    @FXML private TableColumn<Medicine, String> nameColumn;
    @FXML private TableColumn<Medicine, String> categoryColumn;
    @FXML private TableColumn<Medicine, String> classificationColumn;
    @FXML private TableColumn<Medicine, String> dosageUnitColumn;
    @FXML private TableColumn<Medicine, String> descriptionColumn;
    @FXML private TableColumn<Medicine, String> statusColumn;
    @FXML private TableColumn<Medicine, Void> actionsColumn;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> classificationFilterComboBox;
    @FXML private TextField searchField;
    
    // Form Elements
    @FXML private VBox medicineFormContainer;
    @FXML private Label formTitleLabel;
    @FXML private TextField medicineNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> classificationComboBox;
    @FXML private TextField dosageUnitField;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button saveMedicineButton;
    @FXML private Button cancelMedicineButton;
    
    // Data
    private ObservableList<Medicine> medicinesList = FXCollections.observableArrayList();
    private ObservableList<Medicine> filteredMedicines = FXCollections.observableArrayList();
    private Staff currentDoctor;
    
    // Services
    private MedicineService medicineService = new MedicineService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupForm();
        setupFilters();
        loadData();
    }
    
    /**
     * Set the current doctor for medicine management
     */
    public void setCurrentDoctor(Staff doctor) {
        this.currentDoctor = doctor;
        System.out.println("MedicineManagementController: Setting current doctor to: " + (doctor != null ? doctor.getFullName() : "null"));
        loadData();
    }
    
    private void setupTable() {
        System.out.println("Setting up medicine management component...");
        
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        classificationColumn.setCellValueFactory(new PropertyValueFactory<>("classification"));
        dosageUnitColumn.setCellValueFactory(new PropertyValueFactory<>("dosageUnit"));
        descriptionColumn.setCellValueFactory(cellData -> {
            Medicine medicine = cellData.getValue();
            String description = medicine.getDescription();
            if (description != null && description.length() > 50) {
                description = description.substring(0, 47) + "...";
            }
            return new javafx.beans.property.SimpleStringProperty(description != null ? description : "");
        });
        statusColumn.setCellValueFactory(cellData -> {
            Medicine medicine = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                medicine.isActive() ? "Active" : "Inactive"
            );
        });
        
        // Setup actions column - only view button since delete is not allowed due to foreign key constraints
        actionsColumn.setCellFactory(col -> new TableCell<Medicine, Void>() {
            private final Button viewButton = new Button("👁️ View");
            
            {
                viewButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8;");
                viewButton.setOnAction(e -> viewMedicine(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        
        // Setup search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMedicines());
        
        System.out.println("Medicine management component setup complete");
    }
    
    private void setupForm() {
        // Setup category options
        categoryComboBox.getItems().addAll(
            "Pain Relief", "Antibiotic", "Cardiovascular", "Endocrine", 
            "Respiratory", "Gastrointestinal", "Neurological", "Other"
        );
        
        // Setup classification options
        classificationComboBox.getItems().addAll(
            "Prescription", "Over-the-counter", "Controlled", "Herbal", "Other"
        );
        
        // Setup dosage unit options
        dosageUnitField.setText("mg"); // Default value
    }
    
    private void setupFilters() {
        // Setup category filter
        categoryFilterComboBox.getItems().add("All Categories");
        try {
            List<String> categories = medicineService.getAllCategories();
            categoryFilterComboBox.getItems().addAll(categories);
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
        categoryFilterComboBox.setValue("All Categories");
        categoryFilterComboBox.setOnAction(e -> filterMedicines());
        
        // Setup classification filter
        classificationFilterComboBox.getItems().add("All Classifications");
        try {
            List<String> classifications = medicineService.getAllClassifications();
            classificationFilterComboBox.getItems().addAll(classifications);
        } catch (Exception e) {
            System.err.println("Error loading classifications: " + e.getMessage());
        }
        classificationFilterComboBox.setValue("All Classifications");
        classificationFilterComboBox.setOnAction(e -> filterMedicines());
    }
    
    private void loadData() {
        try {
            List<Medicine> medicines = medicineService.findAll();
            medicinesList.setAll(medicines);
            
            // Update statistics
            long totalCount = medicines.size();
            long activeCount = medicines.stream()
                .filter(Medicine::isActive)
                .count();
            long categoriesCount = medicines.stream()
                .map(Medicine::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .count();
            
            totalMedicinesLabel.setText(String.valueOf(totalCount));
            activeMedicinesLabel.setText(String.valueOf(activeCount));
            categoriesLabel.setText(String.valueOf(categoriesCount));
            
            // Apply current filters
            filterMedicines();
            
            System.out.println("Medicine management component loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading medicine data: " + e.getMessage());
            showError("Failed to load medicine data");
        }
    }
    
    private void filterMedicines() {
        String searchText = searchField.getText().toLowerCase();
        String categoryFilter = categoryFilterComboBox.getValue();
        String classificationFilter = classificationFilterComboBox.getValue();
        
        filteredMedicines.clear();
        
        for (Medicine medicine : medicinesList) {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() || 
                medicine.getName().toLowerCase().contains(searchText) ||
                (medicine.getDescription() != null && 
                 medicine.getDescription().toLowerCase().contains(searchText)) ||
                (medicine.getCategory() != null && 
                 medicine.getCategory().toLowerCase().contains(searchText));
            
            // Category filter
            boolean matchesCategory = categoryFilter.equals("All Categories") ||
                (medicine.getCategory() != null && medicine.getCategory().equals(categoryFilter));
            
            // Classification filter
            boolean matchesClassification = classificationFilter.equals("All Classifications") ||
                (medicine.getClassification() != null && medicine.getClassification().equals(classificationFilter));
            
            if (matchesSearch && matchesCategory && matchesClassification) {
                filteredMedicines.add(medicine);
            }
        }
        
        medicinesTable.setItems(filteredMedicines);
    }
    
    // Action methods
    @FXML
    private void showAddMedicineForm() {
        formTitleLabel.setText("Add New Medicine");
        medicineFormContainer.setVisible(true);
        medicineFormContainer.setManaged(true);
        clearForm();
    }
    
    @FXML
    private void refreshMedicinesList() {
        loadData();
        showSuccess("Medicine list refreshed");
    }
    
    @FXML
    private void showCategoriesView() {
        categoryFilterComboBox.setValue("All Categories");
        classificationFilterComboBox.setValue("All Classifications");
        filterMedicines();
    }
    
    @FXML
    private void saveMedicine() {
        String name = medicineNameField.getText().trim();
        String category = categoryComboBox.getValue();
        String classification = classificationComboBox.getValue();
        String dosageUnit = dosageUnitField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        
        if (name.isEmpty()) {
            showError("Please enter a medicine name");
            return;
        }
        
        if (category == null) {
            showError("Please select a category");
            return;
        }
        
        if (classification == null) {
            showError("Please select a classification");
            return;
        }
        
        if (dosageUnit.isEmpty()) {
            showError("Please enter a dosage unit");
            return;
        }
        
        try {
            Medicine medicine = new Medicine();
            medicine.setName(name);
            medicine.setCategory(category);
            medicine.setClassification(classification);
            medicine.setDosageUnit(dosageUnit);
            medicine.setDescription(description.isEmpty() ? null : description);
            medicine.setActive(true);
            
            medicineService.save(medicine);
            
            showSuccess("Medicine saved successfully");
            medicineFormContainer.setVisible(false);
            medicineFormContainer.setManaged(false);
            loadData();
            
        } catch (Exception e) {
            System.err.println("Error saving medicine: " + e.getMessage());
            showError("Failed to save medicine: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancelMedicineForm() {
        medicineFormContainer.setVisible(false);
        medicineFormContainer.setManaged(false);
        clearForm();
    }
    
    private void clearForm() {
        medicineNameField.clear();
        categoryComboBox.setValue(null);
        classificationComboBox.setValue(null);
        dosageUnitField.setText("mg");
        descriptionTextArea.clear();
    }
    
    private void viewMedicine(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Medicine Details");
        alert.setHeaderText("Medicine Information");
        
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(medicine.getName()).append("\n");
        details.append("Category: ").append(medicine.getCategory() != null ? medicine.getCategory() : "N/A").append("\n");
        details.append("Classification: ").append(medicine.getClassification() != null ? medicine.getClassification() : "N/A").append("\n");
        details.append("Dosage Unit: ").append(medicine.getDosageUnit()).append("\n");
        details.append("Description: ").append(medicine.getDescription() != null ? medicine.getDescription() : "N/A").append("\n");
        details.append("Status: ").append(medicine.isActive() ? "Active" : "Inactive");
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    // Edit and delete medicine functionality removed as requested
    // Delete is not allowed due to foreign key constraints with prescription medicines
    
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
