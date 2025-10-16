package com.healthcare.controller.components;

import com.healthcare.model.Resident;
import com.healthcare.model.Staff;
import com.healthcare.services.ResidentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for My Patients component in Doctor Dashboard
 */
public class MyPatientsController implements Initializable {
    
    // FXML Elements
    @FXML private Label totalPatientsLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private TableView<Resident> patientsTable;
    @FXML private TableColumn<Resident, String> patientNameColumn;
    @FXML private TableColumn<Resident, Integer> ageColumn;
    @FXML private TableColumn<Resident, String> genderColumn;
    @FXML private TableColumn<Resident, String> admissionDateColumn;
    @FXML private TableColumn<Resident, String> medicalConditionColumn;
    @FXML private TableColumn<Resident, String> bedColumn;
    @FXML private TableColumn<Resident, String> statusColumn;
    @FXML private TableColumn<Resident, String> actionsColumn;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> itemsPerPageComboBox;
    
    // Data
    private ObservableList<Resident> allPatients = FXCollections.observableArrayList();
    private ObservableList<Resident> filteredPatients = FXCollections.observableArrayList();
    
    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 20;
    private int totalPages = 1;
    
    // Services
    private ResidentService residentService = new ResidentService();
    
    // Current doctor for filtering
    private Staff currentDoctor;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        setupPagination();
        loadData();
    }
    
    public void setCurrentDoctor(Staff doctor) {
        this.currentDoctor = doctor;
        System.out.println("MyPatientsController: Setting current doctor to: " + (doctor != null ? doctor.getFullName() : "null"));
        loadData();
    }
    
    private void setupTable() {
        System.out.println("Setting up my patients component...");
        
        // Setup table columns
        patientNameColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(resident.getFullName());
        });
        
        ageColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(resident.getAge()).asObject();
        });
        
        genderColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(resident.getGender().name());
        });
        
        admissionDateColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            if (resident.getAdmissionDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    resident.getAdmissionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        medicalConditionColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                resident.getMedicalCondition() != null ? resident.getMedicalCondition() : "N/A"
            );
        });
        
        bedColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            // Get bed code from database using service
            String bedCode = residentService.getBedCodeForResident(resident);
            return new javafx.beans.property.SimpleStringProperty(bedCode);
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                resident.isDischarged() ? "Discharged" : "Active"
            );
        });
        
        // Actions column with view button only
        actionsColumn.setCellFactory(column -> new TableCell<Resident, String>() {
            private final Button viewButton = new Button("👁️ View Details");
            
            {
                viewButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 5 10;");
                
                viewButton.setOnAction(e -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    viewPatientDetails(resident);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        
        // Set column widths
        patientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientNameColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.20));
        ageColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.08));
        genderColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.08));
        admissionDateColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.12));
        medicalConditionColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.25));
        bedColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.12));
        statusColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.10));
        actionsColumn.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.15));
        
        System.out.println("My patients component setup complete");
    }
    
    private void setupFilters() {
        // Setup status filter
        statusFilterComboBox.getItems().addAll("All Status", "Active", "Discharged");
        statusFilterComboBox.setValue("All Status");
        
        // Setup items per page
        itemsPerPageComboBox.getItems().addAll(10, 20, 50, 100);
        itemsPerPageComboBox.setValue(20);
        itemsPerPageComboBox.setOnAction(e -> {
            itemsPerPage = itemsPerPageComboBox.getValue();
            currentPage = 1;
            updatePagination();
            updateTable();
        });
    }
    
    private void setupPagination() {
        // Pagination button styles
        String buttonStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;";
        
        firstPageButton.setStyle(buttonStyle);
        prevPageButton.setStyle(buttonStyle);
        nextPageButton.setStyle(buttonStyle);
        lastPageButton.setStyle(buttonStyle);
    }
    
    private void loadData() {
        try {
            if (currentDoctor == null) {
                System.out.println("No current doctor set, cannot load patients");
                return;
            }
            
            System.out.println("Loading patients for doctor: " + currentDoctor.getFullName() + " (ID: " + currentDoctor.getStaffId() + ")");
            
            // Load all patients assigned to this doctor
            List<Resident> allResidents = residentService.findAll();
            List<Resident> myPatients = allResidents.stream()
                .filter(resident -> resident.getAssignedDoctorId() != null && 
                        resident.getAssignedDoctorId().equals(currentDoctor.getStaffId()))
                .collect(Collectors.toList());
            
            allPatients.clear();
            allPatients.addAll(myPatients);
            
            System.out.println("Found " + myPatients.size() + " patients for doctor " + currentDoctor.getFullName());
            
            // Apply current filters
            applyFilters();
            
        } catch (Exception e) {
            System.err.println("Error loading patients data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void searchPatients() {
        applyFilters();
    }
    
    @FXML
    private void clearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("All Status");
        applyFilters();
    }
    
    private void applyFilters() {
        String searchTerm = searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();
        
        filteredPatients.clear();
        
        for (Resident patient : allPatients) {
            boolean matchesSearch = searchTerm.isEmpty() || 
                patient.getFullName().toLowerCase().contains(searchTerm) ||
                (patient.getMedicalCondition() != null && 
                 patient.getMedicalCondition().toLowerCase().contains(searchTerm));
            
            boolean matchesStatus = statusFilter.equals("All Status") ||
                (statusFilter.equals("Active") && !patient.isDischarged()) ||
                (statusFilter.equals("Discharged") && patient.isDischarged());
            
            if (matchesSearch && matchesStatus) {
                filteredPatients.add(patient);
            }
        }
        
        currentPage = 1;
        updatePagination();
        updateTable();
    }
    
    private void updatePagination() {
        totalPages = Math.max(1, (int) Math.ceil((double) filteredPatients.size() / itemsPerPage));
        
        // Update pagination buttons
        firstPageButton.setDisable(currentPage <= 1);
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
        lastPageButton.setDisable(currentPage >= totalPages);
        
        // Update page info
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        
        // Update total count
        totalPatientsLabel.setText("Total: " + filteredPatients.size());
    }
    
    private void updateTable() {
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredPatients.size());
        
        List<Resident> pageData = filteredPatients.subList(startIndex, endIndex);
        
        patientsTable.getItems().clear();
        patientsTable.getItems().addAll(pageData);
    }
    
    // Pagination methods
    @FXML
    private void goToFirstPage() {
        currentPage = 1;
        updatePagination();
        updateTable();
    }
    
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            updateTable();
        }
    }
    
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
            updateTable();
        }
    }
    
    @FXML
    private void goToLastPage() {
        currentPage = totalPages;
        updatePagination();
        updateTable();
    }
    
    // Action methods
    private void viewPatientDetails(Resident patient) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Patient Details");
        alert.setHeaderText("Patient Information");
        
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(patient.getFullName()).append("\n");
        details.append("Age: ").append(patient.getAge()).append("\n");
        details.append("Gender: ").append(patient.getGender()).append("\n");
        details.append("Admission Date: ").append(patient.getAdmissionDate()).append("\n");
        details.append("Medical Condition: ").append(patient.getMedicalCondition() != null ? patient.getMedicalCondition() : "N/A").append("\n");
        String bedCode = residentService.getBedCodeForResident(patient);
        details.append("Bed: ").append(bedCode).append("\n");
        details.append("Status: ").append(patient.isDischarged() ? "Discharged" : "Active").append("\n");
        details.append("Emergency Contact: ").append(patient.getEmergencyContact() != null ? patient.getEmergencyContact() : "N/A");
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
}
