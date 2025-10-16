package com.healthcare.controller.components;

import com.healthcare.model.Bed;
import com.healthcare.model.Resident;
import com.healthcare.model.ActionLog;
import com.healthcare.model.Staff;
import com.healthcare.services.BedManagementService;
import com.healthcare.services.ResidentService;
import com.healthcare.services.StaffService;
import com.healthcare.services.ActionLogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Component Controller for Resident Management
 * Handles all resident CRUD operations, bed assignments, and discharge
 */
public class ResidentManagementController implements Initializable {
    
    // FXML Elements - Table
    @FXML private TableView<Resident> residentsTable;
    @FXML private TableColumn<Resident, String> nameColumn;
    @FXML private TableColumn<Resident, Resident.Gender> genderColumn;
    @FXML private TableColumn<Resident, Integer> ageColumn;
    @FXML private TableColumn<Resident, LocalDate> admissionDateColumn;
    @FXML private TableColumn<Resident, String> bedColumn;
    @FXML private TableColumn<Resident, String> statusColumn;
    @FXML private TableColumn<Resident, Void> actionsColumn;
    
    // FXML Elements - Form
    @FXML private VBox residentFormContainer;
    @FXML private Label formTitleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<Resident.Gender> genderComboBox;
    @FXML private DatePicker birthDatePicker;
    @FXML private DatePicker admissionDatePicker;
    @FXML private ComboBox<Bed> bedComboBox;
    
    // FXML Elements - Count Cards
    @FXML private Label activeResidentsCountLabel;
    @FXML private Label dischargedResidentsCountLabel;
    @FXML private Label availableBedsCountLabel;
    
    // FXML Elements - Assign Doctor
    @FXML private ComboBox<Staff> doctorComboBox;
    @FXML private ComboBox<String> patientComboBox;
    @FXML private Button assignDoctorButton;
    
    // FXML Elements - Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    
    // Data
    private ObservableList<Resident> residentsList = FXCollections.observableArrayList();
    private FilteredList<Resident> filteredResidentsList;
    private ObservableList<Bed> availableBedsList = FXCollections.observableArrayList();
    private Resident editingResident = null;
    
    // Services
    private ResidentService residentService = new ResidentService();
    private BedManagementService bedService = new BedManagementService();
    private StaffService staffService = new StaffService();
    private ActionLogService actionLogService = new ActionLogService();
    
    // Current staff for action logging
    private Staff currentStaff;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupForm();
        setupSearch();
        setupAssignDoctor();
        loadResidentsData();
    }
    
    /**
     * Set the current logged-in staff member for action logging
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }
    
    private void setupTable() {
        System.out.println("Setting up resident management component...");
        
        // Setup table columns with equal width distribution
        nameColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(resident.getFullName());
        });
        nameColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.22));
        
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.10));
        
        ageColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(resident.getAge()).asObject();
        });
        ageColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.08));
        
        admissionDateColumn.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
        admissionDateColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.15));
        
        bedColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            if (resident.getCurrentBedId() != null) {
                try {
                    // Get bed information to display bed code
                    Optional<Bed> bed = bedService.findById(resident.getCurrentBedId());
                    if (bed.isPresent()) {
                        return new javafx.beans.property.SimpleStringProperty(bed.get().getBedCode());
                    }
                } catch (Exception e) {
                    System.err.println("Error getting bed info: " + e.getMessage());
                }
                return new javafx.beans.property.SimpleStringProperty("Bed " + resident.getCurrentBedId());
            }
            return new javafx.beans.property.SimpleStringProperty("No Bed");
        });
        bedColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.12));
        
        statusColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            String status = resident.isDischarged() ? "Discharged" : "Active";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.12));
        
        // Setup actions column with space-around layout
        actionsColumn.setCellFactory(col -> new TableCell<Resident, Void>() {
            private final Button editButton = new Button("✏️ Edit");
            private final Button dischargeButton = new Button("🏥 Discharge");
            private final Button assignBedButton = new Button("🛏️ Assign Bed");
            private final HBox buttonBox = new HBox(5, editButton, dischargeButton, assignBedButton);
            
            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; " +
                                  "-fx-background-radius: 4; -fx-padding: 4 8; " +
                                  "-fx-font-weight: bold; -fx-font-size: 10px;");
                dischargeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                                      "-fx-background-radius: 4; -fx-padding: 4 8; " +
                                      "-fx-font-weight: bold; -fx-font-size: 10px;");
                assignBedButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                                      "-fx-background-radius: 4; -fx-padding: 4 8; " +
                                      "-fx-font-weight: bold; -fx-font-size: 10px;");
                
                // Set HBox to space buttons around
                buttonBox.setStyle("-fx-alignment: center; -fx-spacing: 5;");
                
                editButton.setOnAction(e -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    editResident(resident);
                });
                
                dischargeButton.setOnAction(e -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    dischargeResident(resident);
                });
                
                assignBedButton.setOnAction(e -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    showBedAssignmentDialog(resident);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Resident resident = getTableView().getItems().get(getIndex());
                    // Show different buttons based on resident status
                    if (resident.isDischarged()) {
                        buttonBox.getChildren().clear();
                        buttonBox.getChildren().add(editButton);
                    } else {
                        buttonBox.getChildren().clear();
                        buttonBox.getChildren().addAll(editButton, dischargeButton, assignBedButton);
                    }
                    setGraphic(buttonBox);
                }
            }
        });
        actionsColumn.prefWidthProperty().bind(residentsTable.widthProperty().multiply(0.22));
        
        System.out.println("Resident management component setup complete");
    }
    
    private void setupForm() {
        // Setup gender combo box
        genderComboBox.setItems(FXCollections.observableArrayList(Resident.Gender.values()));
        
        // Setup bed combo box
        bedComboBox.setItems(availableBedsList);
        
        // Set custom cell factory for bed combo box to display meaningful information
        bedComboBox.setCellFactory(listView -> new ListCell<Bed>() {
            @Override
            protected void updateItem(Bed bed, boolean empty) {
                super.updateItem(bed, empty);
                if (empty || bed == null) {
                    setText(null);
                } else {
                    String displayText = bed.getBedCode() != null ? bed.getBedCode() : "Room " + bed.getRoomId() + " Bed " + bed.getBedNumber();
                    if (bed.getBedType() != null) {
                        displayText += " (" + bed.getBedType() + ")";
                    }
                    setText(displayText);
                }
            }
        });
        
        // Set button cell for the combo box (what's shown when collapsed)
        bedComboBox.setButtonCell(new ListCell<Bed>() {
            @Override
            protected void updateItem(Bed bed, boolean empty) {
                super.updateItem(bed, empty);
                if (empty || bed == null) {
                    setText("Select a bed...");
                } else {
                    String displayText = bed.getBedCode() != null ? bed.getBedCode() : "Room " + bed.getRoomId() + " Bed " + bed.getBedNumber();
                    if (bed.getBedType() != null) {
                        displayText += " (" + bed.getBedType() + ")";
                    }
                    setText(displayText);
                }
            }
        });
        
        // Set default admission date to today
        admissionDatePicker.setValue(LocalDate.now());
    }
    
    private void setupSearch() {
        // Create filtered list
        filteredResidentsList = new FilteredList<>(residentsList, p -> true);
        
        // Bind table to filtered list
        residentsTable.setItems(filteredResidentsList);
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredResidentsList.setPredicate(resident -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                return resident.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                       resident.getGender().name().toLowerCase().contains(lowerCaseFilter);
            });
        });
        
        // Setup status filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList("All", "Active", "Discharged"));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredResidentsList.setPredicate(resident -> {
                if (newValue == null || newValue.equals("All")) {
                    return true;
                }
                
                if (newValue.equals("Active")) {
                    return !resident.isDischarged();
                } else if (newValue.equals("Discharged")) {
                    return resident.isDischarged();
                }
                
                return true;
            });
        });
    }
    
    @FXML
    private void showAdmitResidentForm() {
        editingResident = null;
        formTitleLabel.setText("Admit New Resident");
        clearForm();
        loadAvailableBeds();
        residentFormContainer.setVisible(true);
        residentFormContainer.setManaged(true);
    }
    
    @FXML
    private void refreshResidentsList() {
        loadResidentsData();
        showSuccess("Residents list refreshed successfully!");
    }
    
    @FXML
    private void showBedManagement() {
        showInfo("Bed Management", "Bed management functionality will be implemented here.");
    }
    
    @FXML
    private void saveResident() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            Resident.Gender gender = genderComboBox.getValue();
            LocalDate birthDate = birthDatePicker.getValue();
            LocalDate admissionDate = admissionDatePicker.getValue();
            Bed selectedBed = bedComboBox.getValue();
            
            if (firstName.isEmpty() || lastName.isEmpty() || gender == null || admissionDate == null) {
                showError("Please fill in all required fields (First Name, Last Name, Gender, Admission Date)");
                return;
            }
            
            if (editingResident == null) {
                // Add new resident
                Resident newResident = new Resident(firstName, lastName, gender, admissionDate);
                newResident.setBirthDate(birthDate);
                
                if (selectedBed != null) {
                    newResident.setCurrentBedId(selectedBed.getBedId());
                }
                
                Resident savedResident = residentService.save(newResident);
                if (savedResident != null && selectedBed != null) {
                    bedService.assignResidentToBed(selectedBed.getBedId(), savedResident.getResidentId());
                }
                
                // Log the action
                ActionLog actionLog = new ActionLog(
                    currentStaff != null ? currentStaff.getStaffId() : null,
                    ActionLog.ActionType.Admit,
                    "Admitted resident: " + newResident.getFullName(),
                    "Bed: " + (selectedBed != null ? selectedBed.getBedCode() : "Not assigned")
                );
                actionLogService.save(actionLog);
                
                showSuccess("Resident admitted successfully!");
            } else {
                // Update existing resident
                editingResident.setFirstName(firstName);
                editingResident.setLastName(lastName);
                editingResident.setGender(gender);
                editingResident.setBirthDate(birthDate);
                editingResident.setAdmissionDate(admissionDate);
                
                if (selectedBed != null) {
                    editingResident.setCurrentBedId(selectedBed.getBedId());
                }
                
                residentService.update(editingResident);
                
                // Log the action
                ActionLog actionLog = new ActionLog(
                    currentStaff != null ? currentStaff.getStaffId() : null,
                    ActionLog.ActionType.Update,
                    "Updated resident: " + editingResident.getFullName(),
                    "Resident details modified"
                );
                actionLogService.save(actionLog);
                
                showSuccess("Resident updated successfully!");
            }
            
            cancelResidentForm();
            loadResidentsData();
            
        } catch (Exception e) {
            showError("Failed to save resident: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancelResidentForm() {
        editingResident = null;
        clearForm();
        residentFormContainer.setVisible(false);
        residentFormContainer.setManaged(false);
    }
    
    private void editResident(Resident resident) {
        editingResident = resident;
        formTitleLabel.setText("Edit Resident");
        
        firstNameField.setText(resident.getFirstName());
        lastNameField.setText(resident.getLastName());
        genderComboBox.setValue(resident.getGender());
        birthDatePicker.setValue(resident.getBirthDate());
        admissionDatePicker.setValue(resident.getAdmissionDate());
        
        // Load available beds first
        loadAvailableBeds();
        
        // Set current bed if assigned - do this after loading available beds
        if (resident.getCurrentBedId() != null) {
            bedService.findById(resident.getCurrentBedId()).ifPresent(bed -> {
                // Add the current bed to the available beds list if it's not already there
                if (!availableBedsList.contains(bed)) {
                    availableBedsList.add(bed);
                }
                bedComboBox.setValue(bed);
            });
        }
        
        residentFormContainer.setVisible(true);
        residentFormContainer.setManaged(true);
    }
    
    private void dischargeResident(Resident resident) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Discharge");
        confirmAlert.setHeaderText("Discharge Resident");
        confirmAlert.setContentText("Are you sure you want to discharge " + resident.getFullName() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    residentService.dischargeResident(resident.getResidentId());
                    if (resident.getCurrentBedId() != null) {
                        bedService.unassignBed(resident.getCurrentBedId());
                    }
                    
                    // Log the action
                    ActionLog actionLog = new ActionLog(
                        currentStaff != null ? currentStaff.getStaffId() : null,
                        ActionLog.ActionType.Discharge,
                        "Discharged resident: " + resident.getFullName(),
                        "Resident discharged from facility"
                    );
                    actionLogService.save(actionLog);
                    
                    showSuccess("Resident discharged successfully!");
                    loadResidentsData();
                } catch (Exception e) {
                    showError("Failed to discharge resident: " + e.getMessage());
                }
            }
        });
    }
    
    private void showBedAssignmentDialog(Resident resident) {
        // Create a simple dialog for bed assignment
        List<Bed> availableBeds = bedService.findAvailableBeds();
        
        if (availableBeds.isEmpty()) {
            showError("No available beds found!");
            return;
        }
        
        // Create a custom dialog
        Dialog<Bed> dialog = new Dialog<>();
        dialog.setTitle("Assign Bed");
        dialog.setHeaderText("Select a bed for " + resident.getFullName());
        
        // Create a combo box with proper formatting
        ComboBox<Bed> bedComboBox = new ComboBox<>(FXCollections.observableArrayList(availableBeds));
        bedComboBox.setValue(availableBeds.get(0));
        
        // Set the same cell factory as the main bed combo box
        bedComboBox.setCellFactory(listView -> new ListCell<Bed>() {
            @Override
            protected void updateItem(Bed bed, boolean empty) {
                super.updateItem(bed, empty);
                if (empty || bed == null) {
                    setText(null);
                } else {
                    String displayText = bed.getBedCode() != null ? bed.getBedCode() : "Room " + bed.getRoomId() + " Bed " + bed.getBedNumber();
                    if (bed.getBedType() != null) {
                        displayText += " (" + bed.getBedType() + ")";
                    }
                    setText(displayText);
                }
            }
        });
        
        bedComboBox.setButtonCell(new ListCell<Bed>() {
            @Override
            protected void updateItem(Bed bed, boolean empty) {
                super.updateItem(bed, empty);
                if (empty || bed == null) {
                    setText("Select a bed...");
                } else {
                    String displayText = bed.getBedCode() != null ? bed.getBedCode() : "Room " + bed.getRoomId() + " Bed " + bed.getBedNumber();
                    if (bed.getBedType() != null) {
                        displayText += " (" + bed.getBedType() + ")";
                    }
                    setText(displayText);
                }
            }
        });
        
        // Add the combo box to the dialog
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Available beds:"),
            bedComboBox
        );
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);
        
        // Set result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return bedComboBox.getValue();
            }
            return null;
        });
        
        // Show dialog and handle result
        dialog.showAndWait().ifPresent(selectedBed -> {
            try {
                // Unassign current bed if any
                if (resident.getCurrentBedId() != null) {
                    bedService.unassignBed(resident.getCurrentBedId());
                }
                
                // Assign new bed
                resident.setCurrentBedId(selectedBed.getBedId());
                residentService.update(resident);
                bedService.assignResidentToBed(selectedBed.getBedId(), resident.getResidentId());
                
                showSuccess("Bed assigned successfully!");
                loadResidentsData();
            } catch (Exception e) {
                showError("Failed to assign bed: " + e.getMessage());
            }
        });
    }
    
    private void loadResidentsData() {
        try {
            System.out.println("Loading residents data in component...");
            residentsList.clear();
            List<Resident> allResidents = residentService.findAll();
            System.out.println("Found " + allResidents.size() + " residents");
            
            residentsList.addAll(allResidents);
            residentsTable.refresh();
            updateResidentCounts();
            System.out.println("Residents table updated with " + residentsList.size() + " items");
            
        } catch (Exception e) {
            System.err.println("Error loading residents data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAvailableBeds() {
        try {
            availableBedsList.clear();
            List<Bed> vacantBeds = bedService.findAvailableBeds();
            availableBedsList.addAll(vacantBeds);
            
            // If editing a resident with an assigned bed, include that bed in the list
            if (editingResident != null && editingResident.getCurrentBedId() != null) {
                bedService.findById(editingResident.getCurrentBedId()).ifPresent(bed -> {
                    if (!availableBedsList.contains(bed)) {
                        availableBedsList.add(bed);
                    }
                });
            }
            
            bedComboBox.setItems(availableBedsList);
        } catch (Exception e) {
            System.err.println("Error loading available beds: " + e.getMessage());
        }
    }
    
    private void updateResidentCounts() {
        long activeCount = residentsList.stream()
            .filter(resident -> !resident.isDischarged())
            .count();
        
        long dischargedCount = residentsList.stream()
            .filter(Resident::isDischarged)
            .count();
        
        try {
            long availableBedsCount = bedService.findAvailableBeds().size();
            availableBedsCountLabel.setText(String.valueOf(availableBedsCount));
        } catch (Exception e) {
            availableBedsCountLabel.setText("0");
        }
        
        activeResidentsCountLabel.setText(String.valueOf(activeCount));
        dischargedResidentsCountLabel.setText(String.valueOf(dischargedCount));
    }
    
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        genderComboBox.setValue(null);
        birthDatePicker.setValue(null);
        admissionDatePicker.setValue(LocalDate.now());
        bedComboBox.setValue(null);
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
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Assign Doctor functionality
    private void setupAssignDoctor() {
        System.out.println("Setting up assign doctor functionality...");
        
        // Load doctors into combo box
        loadDoctors();
        
        // Load patients into combo box
        loadPatients();
    }
    
    private void loadDoctors() {
        try {
            List<Staff> allStaff = staffService.findAll();
            List<Staff> doctors = allStaff.stream()
                .filter(staff -> staff.getRole() == Staff.Role.Doctor)
                .collect(java.util.stream.Collectors.toList());
            
            doctorComboBox.setItems(FXCollections.observableArrayList(doctors));
            doctorComboBox.setCellFactory(listView -> new ListCell<Staff>() {
                @Override
                protected void updateItem(Staff staff, boolean empty) {
                    super.updateItem(staff, empty);
                    if (empty || staff == null) {
                        setText(null);
                    } else {
                        setText(staff.getFullName() + " (" + staff.getUsername() + ")");
                    }
                }
            });
            
            doctorComboBox.setButtonCell(new ListCell<Staff>() {
                @Override
                protected void updateItem(Staff staff, boolean empty) {
                    super.updateItem(staff, empty);
                    if (empty || staff == null) {
                        setText(null);
                    } else {
                        setText(staff.getFullName() + " (" + staff.getUsername() + ")");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error loading doctors: " + e.getMessage());
            showError("Failed to load doctors");
        }
    }
    
    private void loadPatients() {
        try {
            List<Resident> residents = residentService.findAll();
            List<String> patientNames = residents.stream()
                .map(resident -> resident.getFirstName() + " " + resident.getLastName())
                .collect(java.util.stream.Collectors.toList());
            
            patientComboBox.setItems(FXCollections.observableArrayList(patientNames));
            
        } catch (Exception e) {
            System.err.println("Error loading patients: " + e.getMessage());
            showError("Failed to load patients");
        }
    }
    
    @FXML
    private void assignDoctorToPatient() {
        Staff selectedDoctor = doctorComboBox.getValue();
        String selectedPatient = patientComboBox.getValue();
        
        if (selectedDoctor == null || selectedPatient == null) {
            showError("Please select both a doctor and a patient");
            return;
        }
        
        try {
            // Find the resident by name
            List<Resident> residents = residentService.findAll();
            Resident targetResident = residents.stream()
                .filter(resident -> (resident.getFirstName() + " " + resident.getLastName()).equals(selectedPatient))
                .findFirst()
                .orElse(null);
            
            if (targetResident == null) {
                showError("Patient not found");
                return;
            }
            
            // Update the resident's assigned doctor
            targetResident.setAssignedDoctorId(selectedDoctor.getStaffId());
            residentService.update(targetResident);
            
            // Log the action
            if (currentStaff != null) {
                actionLogService.save(new ActionLog(
                    currentStaff.getStaffId(),
                    ActionLog.ActionType.Update,
                    "Assigned doctor to patient",
                    "Assigned Dr. " + selectedDoctor.getFullName() + " to " + selectedPatient
                ));
            }
            
            showSuccess("Successfully assigned Dr. " + selectedDoctor.getFullName() + " to " + selectedPatient);
            
            // Clear selections
            doctorComboBox.setValue(null);
            patientComboBox.setValue(null);
            
            // Refresh the residents list to show updated assignments
            loadResidentsData();
            
        } catch (Exception e) {
            System.err.println("Error assigning doctor: " + e.getMessage());
            showError("Failed to assign doctor: " + e.getMessage());
        }
    }
    
}
