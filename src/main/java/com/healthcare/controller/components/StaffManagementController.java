package com.healthcare.controller.components;

import com.healthcare.model.Staff;
import com.healthcare.model.ActionLog;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Component Controller for Staff Management
 * Handles all staff CRUD operations
 */
public class StaffManagementController implements Initializable {
    
    // FXML Elements
    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, String> nameColumn;
    @FXML private TableColumn<Staff, String> usernameColumn;
    @FXML private TableColumn<Staff, Staff.Role> roleColumn;
    @FXML private TableColumn<Staff, Void> actionsColumn;
    @FXML private VBox staffFormContainer;
    @FXML private Label formTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Staff.Role> roleComboBox;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    
    // New UI Elements
    @FXML private Label managersCountLabel;
    @FXML private Label doctorsCountLabel;
    @FXML private Label nursesCountLabel;
    @FXML private TextField searchField;
    
    
    // Data
    private ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private FilteredList<Staff> filteredStaffList;
    private Staff editingStaff = null;
    private Staff currentStaff; // Current logged-in staff member
    
    // Services
    private StaffService staffService = new StaffService();
    private ActionLogService actionLogService = new ActionLogService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupSearch();
        loadStaffData();
    }
    
    /**
     * Set the current logged-in staff member for action logging
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }
    
    private void setupTable() {
        System.out.println("Setting up staff management component...");
        
        // Setup table columns with equal width distribution
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameColumn.prefWidthProperty().bind(staffTable.widthProperty().multiply(0.30));
        
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.prefWidthProperty().bind(staffTable.widthProperty().multiply(0.20));
        
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.prefWidthProperty().bind(staffTable.widthProperty().multiply(0.20));
        
        // Setup actions column with space-around layout
        actionsColumn.setCellFactory(col -> new TableCell<Staff, Void>() {
            private final Button editButton = new Button("✏️ Edit");
            private final Button deleteButton = new Button("🗑️ Delete");
            private final HBox buttonBox = new HBox(10, editButton, deleteButton);
            
            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; " +
                                  "-fx-background-radius: 6; -fx-padding: 6 12; " +
                                  "-fx-font-weight: bold; -fx-font-size: 12px;");
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                                    "-fx-background-radius: 6; -fx-padding: 6 12; " +
                                    "-fx-font-weight: bold; -fx-font-size: 12px;");
                
                // Set HBox to space buttons around
                buttonBox.setStyle("-fx-alignment: center; -fx-spacing: 10;");
                
                editButton.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    editStaff(staff);
                });
                
                deleteButton.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    deleteStaff(staff);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });
        actionsColumn.prefWidthProperty().bind(staffTable.widthProperty().multiply(0.25));
        
        // Setup role combo box
        roleComboBox.setItems(FXCollections.observableArrayList(Staff.Role.values()));
        
        System.out.println("Staff management component setup complete");
    }
    
    private void setupSearch() {
        // Create filtered list
        filteredStaffList = new FilteredList<>(staffList, p -> true);
        
        // Bind table to filtered list
        staffTable.setItems(filteredStaffList);
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredStaffList.setPredicate(staff -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                return staff.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                       staff.getRole().name().toLowerCase().contains(lowerCaseFilter) ||
                       (staff.getFullName() != null && staff.getFullName().toLowerCase().contains(lowerCaseFilter)) ||
                       (staff.getEmail() != null && staff.getEmail().toLowerCase().contains(lowerCaseFilter));
            });
        });
    }
    
    @FXML
    private void showAddStaffForm() {
        editingStaff = null;
        formTitleLabel.setText("Add New Staff Member");
        clearForm();
        staffFormContainer.setVisible(true);
        staffFormContainer.setManaged(true);
    }
    
    @FXML
    private void refreshStaffList() {
        loadStaffData();
        showSuccess("Staff list refreshed successfully!");
    }
    
    @FXML
    private void saveStaff() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            Staff.Role role = roleComboBox.getValue();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty() || role == null) {
                showError("Please fill in required fields (Username, Password, Role)");
                return;
            }
            
            if (editingStaff == null) {
                // Add new staff
                Staff newStaff = new Staff(username, password, role, firstName, lastName, email, phone);
                Staff savedStaff = staffService.save(newStaff);
                staffList.add(savedStaff);
                
                // Log the action
                ActionLog actionLog = new ActionLog(
                    currentStaff != null ? currentStaff.getStaffId() : null,
                    ActionLog.ActionType.Add_Staff,
                    "Added new staff: " + newStaff.getFullName(),
                    "Role: " + role.name() + ", Username: " + username
                );
                actionLogService.save(actionLog);
                
                showSuccess("Staff member added successfully!");
            } else {
                // Update existing staff
                editingStaff.setUsername(username);
                editingStaff.setPassword(password);
                editingStaff.setRole(role);
                editingStaff.setFirstName(firstName.isEmpty() ? null : firstName);
                editingStaff.setLastName(lastName.isEmpty() ? null : lastName);
                editingStaff.setEmail(email.isEmpty() ? null : email);
                editingStaff.setPhone(phone.isEmpty() ? null : phone);
                try {
                    staffService.update(editingStaff);
                    
                    // Log the action
                    ActionLog actionLog = new ActionLog(
                        currentStaff != null ? currentStaff.getStaffId() : null,
                        ActionLog.ActionType.Update,
                        "Updated staff: " + editingStaff.getFullName(),
                        "Staff details modified"
                    );
                    actionLogService.save(actionLog);
                    
                    showSuccess("Staff member updated successfully!");
                } catch (Exception e) {
                    showError("Failed to update staff: " + e.getMessage());
                }
            }
            
            cancelStaffForm();
            loadStaffData();
            
        } catch (Exception e) {
            showError("Failed to save staff: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancelStaffForm() {
        editingStaff = null;
        clearForm();
        staffFormContainer.setVisible(false);
        staffFormContainer.setManaged(false);
    }
    
    private void editStaff(Staff staff) {
        editingStaff = staff;
        formTitleLabel.setText("Edit Staff Member");
        
        usernameField.setText(staff.getUsername());
        passwordField.setText(staff.getPassword());
        roleComboBox.setValue(staff.getRole());
        firstNameField.setText(staff.getFirstName() != null ? staff.getFirstName() : "");
        lastNameField.setText(staff.getLastName() != null ? staff.getLastName() : "");
        emailField.setText(staff.getEmail() != null ? staff.getEmail() : "");
        phoneField.setText(staff.getPhone() != null ? staff.getPhone() : "");
        
        staffFormContainer.setVisible(true);
        staffFormContainer.setManaged(true);
    }
    
    private void deleteStaff(Staff staff) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Staff Member");
        confirmAlert.setContentText("Are you sure you want to delete " + staff.getUsername() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    staffService.deleteById(staff.getStaffId());
                    staffList.remove(staff);
                    
                    // Log the action
                    ActionLog actionLog = new ActionLog(
                        currentStaff != null ? currentStaff.getStaffId() : null,
                        ActionLog.ActionType.Delete_Staff,
                        "Deleted staff: " + staff.getFullName(),
                        "Username: " + staff.getUsername() + ", Role: " + staff.getRole().name()
                    );
                    actionLogService.save(actionLog);
                    
                    showSuccess("Staff member deleted successfully!");
                } catch (Exception e) {
                    showError("Failed to delete staff: " + e.getMessage());
                }
            }
        });
    }
    
    private void loadStaffData() {
        try {
            System.out.println("Loading staff data in component...");
            staffList.clear();
            List<Staff> allStaff = staffService.findAll();
            System.out.println("Found " + allStaff.size() + " staff members");
            
            // Filter out the current logged-in staff member
            List<Staff> filteredStaff = allStaff.stream()
                .filter(staff -> currentStaff == null || !staff.getStaffId().equals(currentStaff.getStaffId()))
                .collect(java.util.stream.Collectors.toList());
            
            for (Staff staff : allStaff) {
                System.out.println("Staff: ID=" + staff.getStaffId() + 
                                 ", Username=" + staff.getUsername() + 
                                 ", Role=" + staff.getRole());
            }
            System.out.println("Filtered staff count: " + filteredStaff.size() + " (excluding current user)");
            
            staffList.addAll(filteredStaff);
            staffTable.refresh();
            updateStaffCounts();
            System.out.println("Staff table updated with " + staffList.size() + " items");
            
        } catch (Exception e) {
            System.err.println("Error loading staff data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStaffCounts() {
        long managersCount = staffList.stream()
            .filter(staff -> staff.getRole() == Staff.Role.Manager)
            .count();
        
        long doctorsCount = staffList.stream()
            .filter(staff -> staff.getRole() == Staff.Role.Doctor)
            .count();
        
        long nursesCount = staffList.stream()
            .filter(staff -> staff.getRole() == Staff.Role.Nurse)
            .count();
        
        managersCountLabel.setText(String.valueOf(managersCount));
        doctorsCountLabel.setText(String.valueOf(doctorsCount));
        nursesCountLabel.setText(String.valueOf(nursesCount));
    }
    
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
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
    
}
