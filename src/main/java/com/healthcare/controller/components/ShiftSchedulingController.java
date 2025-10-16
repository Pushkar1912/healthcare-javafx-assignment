package com.healthcare.controller.components;

import com.healthcare.model.Shift;
import com.healthcare.model.ShiftSchedule;
import com.healthcare.model.Staff;
import com.healthcare.model.ActionLog;
import com.healthcare.services.ShiftManagementService;
import com.healthcare.services.StaffService;
import com.healthcare.services.ActionLogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Simplified Shift Scheduling Controller - MVP implementation
 * Handles shift scheduling with simple table view and form
 */
public class ShiftSchedulingController implements Initializable {
    
    // FXML Elements - Table
    @FXML private TableView<ShiftSchedule> shiftsTable;
    @FXML private TableColumn<ShiftSchedule, String> staffColumn;
    @FXML private TableColumn<ShiftSchedule, LocalDate> dateColumn;
    @FXML private TableColumn<ShiftSchedule, String> shiftTypeColumn;
    @FXML private TableColumn<ShiftSchedule, String> startTimeColumn;
    @FXML private TableColumn<ShiftSchedule, String> endTimeColumn;
    @FXML private TableColumn<ShiftSchedule, String> wardColumn;
    @FXML private TableColumn<ShiftSchedule, String> statusColumn;
    @FXML private TableColumn<ShiftSchedule, Void> actionsColumn;
    
    // FXML Elements - Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label complianceStatusLabel;
    
    
    // FXML Elements - Form
    @FXML private VBox shiftFormContainer;
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Staff> staffComboBox;
    @FXML private DatePicker shiftDatePicker;
    @FXML private ComboBox<Shift.ShiftType> shiftTypeComboBox;
    @FXML private ComboBox<String> wardComboBox;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private ComboBox<String> endTimeComboBox;
    
    // Data
    private ObservableList<ShiftSchedule> shiftsList = FXCollections.observableArrayList();
    private ObservableList<Staff> allStaff = FXCollections.observableArrayList();
    private ShiftSchedule editingShift = null;
    private Staff currentStaff; // Current logged-in staff member
    
    // Services
    private ShiftManagementService shiftService = new ShiftManagementService();
    private StaffService staffService = new StaffService();
    private ActionLogService actionLogService = new ActionLogService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupForm();
        setupFilters();
        loadShiftsData();
    }
    
    /**
     * Set the current logged-in staff member for action logging
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }
    
    private void setupTable() {
        System.out.println("Setting up shift scheduling component...");
        
        // Setup table columns
        staffColumn.setCellValueFactory(cellData -> {
            ShiftSchedule schedule = cellData.getValue();
            if (schedule.getStaff() != null) {
                return new javafx.beans.property.SimpleStringProperty(schedule.getStaff().getFullName());
            }
            return new javafx.beans.property.SimpleStringProperty("Unknown");
        });
        staffColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.20));
        
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("shiftDate"));
        dateColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.12));
        
        shiftTypeColumn.setCellValueFactory(cellData -> {
            ShiftSchedule schedule = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(schedule.getShiftType().name());
        });
        shiftTypeColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.12));
        
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.10));
        
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.10));
        
        wardColumn.setCellValueFactory(cellData -> {
            ShiftSchedule schedule = cellData.getValue();
            Long wardId = schedule.getWardId();
            if (wardId != null) {
                return new javafx.beans.property.SimpleStringProperty("Ward " + wardId);
            }
            return new javafx.beans.property.SimpleStringProperty("Not assigned");
        });
        wardColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.12));
        
        statusColumn.setCellValueFactory(cellData -> {
            ShiftSchedule schedule = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(schedule.getStatus().name());
        });
        statusColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.12));
        
        // Setup actions column
        actionsColumn.setCellFactory(col -> new TableCell<ShiftSchedule, Void>() {
            private final Button editButton = new Button("✏️ Edit");
            private final Button deleteButton = new Button("🗑️ Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);
            
            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; " +
                                  "-fx-background-radius: 4; -fx-padding: 4 8; " +
                                  "-fx-font-weight: bold; -fx-font-size: 10px;");
                deleteButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                                    "-fx-background-radius: 4; -fx-padding: 4 8; " +
                                    "-fx-font-weight: bold; -fx-font-size: 10px;");
                
                buttonBox.setStyle("-fx-alignment: center; -fx-spacing: 5;");
                
                editButton.setOnAction(e -> {
                    ShiftSchedule schedule = getTableView().getItems().get(getIndex());
                    editShift(schedule);
                });
                
                deleteButton.setOnAction(e -> {
                    ShiftSchedule schedule = getTableView().getItems().get(getIndex());
                    deleteShift(schedule);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
        actionsColumn.prefWidthProperty().bind(shiftsTable.widthProperty().multiply(0.12));
        
        // Bind table to data
        shiftsTable.setItems(shiftsList);
        
        System.out.println("Shift scheduling component setup complete");
    }
    
    private void setupForm() {
        // Setup staff combo box
        staffComboBox.setItems(allStaff);
        staffComboBox.setCellFactory(listView -> new ListCell<Staff>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    setText(staff.getFullName() + " (" + staff.getRole() + ")");
                }
            }
        });
        
        // Also set the button cell to show the same format
        staffComboBox.setButtonCell(new ListCell<Staff>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    setText(staff.getFullName() + " (" + staff.getRole() + ")");
                }
            }
        });
        
        // Setup shift type combo box
        shiftTypeComboBox.setItems(FXCollections.observableArrayList(Shift.ShiftType.values()));
        shiftTypeComboBox.setCellFactory(listView -> new ListCell<Shift.ShiftType>() {
            @Override
            protected void updateItem(Shift.ShiftType shiftType, boolean empty) {
                super.updateItem(shiftType, empty);
                if (empty || shiftType == null) {
                    setText(null);
                } else {
                    setText(shiftType.name());
                }
            }
        });
        
        // Setup ward combo box
        wardComboBox.setItems(FXCollections.observableArrayList("Ward 1", "Ward 2"));
        
        // Setup time combo boxes with common shift times
        ObservableList<String> timeOptions = FXCollections.observableArrayList(
            "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
            "20:00", "21:00", "22:00", "23:00"
        );
        startTimeComboBox.setItems(timeOptions);
        endTimeComboBox.setItems(timeOptions);
        
        // Set default date to today
        shiftDatePicker.setValue(LocalDate.now());
        
        // Add real-time validation listeners
        setupRealTimeValidation();
        
        // Load staff data
        loadStaff();
    }
    
    private void setupFilters() {
        // Set default date range (current week)
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.minusDays(7));
        endDatePicker.setValue(today.plusDays(7));
    }
    
    private void setupRealTimeValidation() {
        // Add listeners to form fields for real-time validation
        staffComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && shiftDatePicker.getValue() != null && 
                startTimeComboBox.getValue() != null && endTimeComboBox.getValue() != null) {
                validateRealTime();
            }
        });
        
        shiftDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && staffComboBox.getValue() != null && 
                startTimeComboBox.getValue() != null && endTimeComboBox.getValue() != null) {
                validateRealTime();
            }
        });
        
        startTimeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && staffComboBox.getValue() != null && 
                shiftDatePicker.getValue() != null && endTimeComboBox.getValue() != null) {
                validateRealTime();
            }
        });
        
        endTimeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && staffComboBox.getValue() != null && 
                shiftDatePicker.getValue() != null && startTimeComboBox.getValue() != null) {
                validateRealTime();
            }
        });
    }
    
    private void validateRealTime() {
        if (editingShift != null) return; // Don't validate when editing existing shifts
        
        Staff selectedStaff = staffComboBox.getValue();
        LocalDate shiftDate = shiftDatePicker.getValue();
        String startTime = startTimeComboBox.getValue();
        String endTime = endTimeComboBox.getValue();
        
        if (selectedStaff != null && shiftDate != null && startTime != null && endTime != null) {
            String conflictMessage = checkStaffAvailability(selectedStaff, shiftDate, startTime, endTime);
            if (conflictMessage != null) {
                // Show a warning but don't block the form
                complianceStatusLabel.setText("⚠️ Scheduling conflict detected!");
                complianceStatusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            } else {
                complianceStatusLabel.setText("✅ Staff is available for this time slot");
                complianceStatusLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
            }
        }
    }
    
    private void loadStaff() {
        try {
            List<Staff> allStaffList = staffService.findAll();
            allStaff.clear();
            
            // Filter out managers and only include nurses and doctors
            for (Staff staff : allStaffList) {
                if (staff.getRole() == Staff.Role.Nurse || staff.getRole() == Staff.Role.Doctor) {
                    allStaff.add(staff);
                }
            }
        } catch (Exception e) {
            showError("Failed to load staff: " + e.getMessage());
        }
    }
    
    private void loadShiftsData() {
        try {
            System.out.println("Loading shifts data...");
            shiftsList.clear();
            
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                List<ShiftSchedule> shifts = shiftService.findByDateRange(startDate, endDate);
                shiftsList.addAll(shifts);
            } else {
                List<ShiftSchedule> allShifts = shiftService.findAll();
                shiftsList.addAll(allShifts);
            }
            
            shiftsTable.refresh();
            updateComplianceStatus();
            System.out.println("Shifts table updated with " + shiftsList.size() + " items");
            
        } catch (Exception e) {
            System.err.println("Error loading shifts data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateComplianceStatus() {
        try {
            // Simple compliance check - just show if we have any shifts
            if (shiftsList.isEmpty()) {
                complianceStatusLabel.setText("⚠️ No shifts scheduled");
                complianceStatusLabel.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
            } else {
                complianceStatusLabel.setText("✅ " + shiftsList.size() + " shifts scheduled");
                complianceStatusLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            complianceStatusLabel.setText("❌ Error checking compliance");
            complianceStatusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        }
    }
    
    // FXML Action Methods
    @FXML
    private void filterShifts() {
        loadShiftsData();
    }
    
    @FXML
    private void showAddShiftForm() {
        editingShift = null;
        formTitleLabel.setText("Add New Shift");
        clearForm();
        shiftFormContainer.setVisible(true);
        shiftFormContainer.setManaged(true);
    }
    
    @FXML
    private void saveShift() {
        try {
            Staff selectedStaff = staffComboBox.getValue();
            LocalDate shiftDate = shiftDatePicker.getValue();
            Shift.ShiftType shiftType = shiftTypeComboBox.getValue();
            String ward = wardComboBox.getValue();
            String startTime = startTimeComboBox.getValue();
            String endTime = endTimeComboBox.getValue();
            
            if (selectedStaff == null || shiftDate == null || shiftType == null || 
                startTime == null || endTime == null) {
                showError("Please fill in all required fields!");
                return;
            }
            
            // Check for scheduling conflicts
            if (editingShift == null) { // Only check for new shifts, not when editing
                String conflictMessage = checkStaffAvailability(selectedStaff, shiftDate, startTime, endTime);
                if (conflictMessage != null) {
                    showError(conflictMessage);
                    return;
                }
            }
            
            // Parse ward ID
            Long wardId = null;
            if (ward != null && ward.startsWith("Ward ")) {
                wardId = Long.parseLong(ward.substring(5));
            }
            
            if (editingShift == null) {
                // Add new shift
                ShiftSchedule newShift = new ShiftSchedule(
                    selectedStaff.getStaffId(),
                    shiftDate,
                    shiftType,
                    startTime,
                    endTime
                );
                newShift.setWardId(wardId);
                newShift.setStaff(selectedStaff);
                
                ShiftSchedule savedShift = shiftService.save(newShift);
                if (savedShift != null) {
                    // Log the action
                    ActionLog actionLog = new ActionLog(
                        currentStaff != null ? currentStaff.getStaffId() : null,
                        ActionLog.ActionType.Assign_Shift,
                        "Assigned shift to: " + selectedStaff.getFullName(),
                        shiftType.name() + " shift on " + shiftDate.toString()
                    );
                    actionLogService.save(actionLog);
                    showSuccess("Shift added successfully!");
                } else {
                    showError("Failed to add shift!");
                }
            } else {
                // Update existing shift
                editingShift.setStaffId(selectedStaff.getStaffId());
                editingShift.setShiftDate(shiftDate);
                editingShift.setShiftType(shiftType);
                editingShift.setStartTime(startTime);
                editingShift.setEndTime(endTime);
                editingShift.setWardId(wardId);
                editingShift.setStaff(selectedStaff);
                
                // Note: We don't have an update method in the service, so we'll delete and recreate
                shiftService.deleteById(editingShift.getScheduleId());
                ShiftSchedule savedShift = shiftService.save(editingShift);
                if (savedShift != null) {
                    // Log the action
                    ActionLog actionLog = new ActionLog(
                        currentStaff != null ? currentStaff.getStaffId() : null,
                        ActionLog.ActionType.Update,
                        "Updated shift for: " + selectedStaff.getFullName(),
                        shiftType.name() + " shift on " + shiftDate.toString()
                    );
                    actionLogService.save(actionLog);
                    showSuccess("Shift updated successfully!");
                } else {
                    showError("Failed to update shift!");
                }
            }
            
            cancelShiftForm();
            loadShiftsData();
            
        } catch (Exception e) {
            showError("Failed to save shift: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancelShiftForm() {
        editingShift = null;
        clearForm();
        shiftFormContainer.setVisible(false);
        shiftFormContainer.setManaged(false);
    }
    
    private void editShift(ShiftSchedule shift) {
        editingShift = shift;
        formTitleLabel.setText("Edit Shift");
        
        // Find and select the staff member
        for (Staff staff : allStaff) {
            if (staff.getStaffId().equals(shift.getStaffId())) {
                staffComboBox.setValue(staff);
                break;
            }
        }
        
        shiftDatePicker.setValue(shift.getShiftDate());
        shiftTypeComboBox.setValue(shift.getShiftType());
        
        if (shift.getWardId() != null) {
            wardComboBox.setValue("Ward " + shift.getWardId());
        }
        
        startTimeComboBox.setValue(shift.getStartTime());
        endTimeComboBox.setValue(shift.getEndTime());
        
        shiftFormContainer.setVisible(true);
        shiftFormContainer.setManaged(true);
    }
    
    private void deleteShift(ShiftSchedule shift) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Shift");
        confirmAlert.setContentText("Are you sure you want to delete this shift?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = shiftService.deleteById(shift.getScheduleId());
                    if (deleted) {
                        // Log the action
                        ActionLog actionLog = new ActionLog(
                            currentStaff != null ? currentStaff.getStaffId() : null,
                            ActionLog.ActionType.Delete_Shift,
                            "Deleted shift for: " + (shift.getStaff() != null ? shift.getStaff().getFullName() : "Unknown Staff"),
                            shift.getShiftType().name() + " shift on " + shift.getShiftDate().toString()
                        );
                        actionLogService.save(actionLog);
                        showSuccess("Shift deleted successfully!");
                        loadShiftsData();
                    } else {
                        showError("Failed to delete shift!");
                    }
                } catch (Exception e) {
                    showError("Failed to delete shift: " + e.getMessage());
                }
            }
        });
    }
    
    private void clearForm() {
        staffComboBox.setValue(null);
        shiftDatePicker.setValue(LocalDate.now());
        shiftTypeComboBox.setValue(null);
        wardComboBox.setValue(null);
        startTimeComboBox.setValue(null);
        endTimeComboBox.setValue(null);
    }
    
    private String checkStaffAvailability(Staff staff, LocalDate shiftDate, String startTime, String endTime) {
        try {
            // Get all existing shifts for this staff member on the same date
            List<ShiftSchedule> existingShifts = shiftService.findByStaffAndDate(staff.getStaffId(), shiftDate);
            
            for (ShiftSchedule existingShift : existingShifts) {
                // Skip if this is the same shift we're editing
                if (editingShift != null && existingShift.getScheduleId().equals(editingShift.getScheduleId())) {
                    continue;
                }
                
                // Check for time overlap
                if (isTimeOverlap(startTime, endTime, existingShift.getStartTime(), existingShift.getEndTime())) {
                    return String.format(
                        "⚠️ CONFLICT DETECTED!\n\n" +
                        "Staff: %s\n" +
                        "Date: %s\n\n" +
                        "❌ Requested Time: %s - %s\n" +
                        "❌ Already Scheduled: %s - %s\n" +
                        "📍 Ward: %s\n\n" +
                        "✅ Staff will be FREE at: %s",
                        staff.getFullName(),
                        shiftDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")),
                        startTime, endTime,
                        existingShift.getStartTime(), existingShift.getEndTime(),
                        existingShift.getWardId() != null ? "Ward " + existingShift.getWardId() : "Not assigned",
                        existingShift.getEndTime()
                    );
                }
            }
            
            return null; // No conflicts found
            
        } catch (Exception e) {
            System.err.println("Error checking staff availability: " + e.getMessage());
            return "Error checking availability. Please try again.";
        }
    }
    
    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        try {
            // Parse times (assuming HH:MM format)
            int start1Minutes = timeToMinutes(start1);
            int end1Minutes = timeToMinutes(end1);
            int start2Minutes = timeToMinutes(start2);
            int end2Minutes = timeToMinutes(end2);
            
            // Check for overlap: two time ranges overlap if one starts before the other ends
            return start1Minutes < end2Minutes && start2Minutes < end1Minutes;
            
        } catch (Exception e) {
            System.err.println("Error parsing times: " + e.getMessage());
            return false;
        }
    }
    
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
    
    // Helper methods
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