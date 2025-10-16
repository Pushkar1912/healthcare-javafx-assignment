package com.healthcare.controller.components;

import com.healthcare.model.Staff;
import com.healthcare.services.ShiftManagementService;
import com.healthcare.model.ShiftSchedule;
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
 * Controller for Nurse Shift Schedule component
 * Allows nurses to view their shift schedules
 */
public class NurseShiftScheduleController implements Initializable {
    
    @FXML
    private VBox mainContainer;
    
    @FXML
    private Label totalShiftsLabel;
    @FXML
    private Label thisWeekShiftsLabel;
    @FXML
    private Label nextWeekShiftsLabel;
    @FXML
    private Label completedShiftsLabel;
    
    @FXML
    private TableView<ShiftSchedule> shiftsTable;
    @FXML
    private TableColumn<ShiftSchedule, String> dateColumn;
    @FXML
    private TableColumn<ShiftSchedule, String> shiftTypeColumn;
    @FXML
    private TableColumn<ShiftSchedule, String> startTimeColumn;
    @FXML
    private TableColumn<ShiftSchedule, String> endTimeColumn;
    @FXML
    private TableColumn<ShiftSchedule, String> wardColumn;
    @FXML
    private TableColumn<ShiftSchedule, String> statusColumn;
    
    @FXML
    private Button refreshButton;
    @FXML
    private Button viewThisWeekButton;
    @FXML
    private Button viewNextWeekButton;
    @FXML
    private Button viewAllButton;
    
    @FXML
    private ComboBox<String> weekFilterComboBox;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    // Services
    private ShiftManagementService shiftService = new ShiftManagementService();
    private Staff currentNurse;
    
    // Data
    private ObservableList<ShiftSchedule> shiftsData = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("NurseShiftScheduleController: Initializing...");
        setupTableColumns();
        setupEventHandlers();
        setupFilters();
        loadData();
        System.out.println("NurseShiftScheduleController: Initialization complete");
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            ShiftSchedule shift = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                shift.getShiftDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
        });
        
        shiftTypeColumn.setCellValueFactory(new PropertyValueFactory<>("shiftType"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        
        wardColumn.setCellValueFactory(cellData -> {
            ShiftSchedule shift = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                shift.getWardId() != null ? "Ward " + shift.getWardId() : "N/A"
            );
        });
        
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }
    
    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadData());
        
        viewThisWeekButton.setOnAction(e -> viewThisWeek());
        viewNextWeekButton.setOnAction(e -> viewNextWeek());
        viewAllButton.setOnAction(e -> viewAllShifts());
        
        // Filter functionality
        weekFilterComboBox.setOnAction(e -> filterShifts());
        statusFilterComboBox.setOnAction(e -> filterShifts());
    }
    
    private void setupFilters() {
        weekFilterComboBox.getItems().addAll("All Weeks", "This Week", "Next Week", "Last Week");
        weekFilterComboBox.setValue("All Weeks");
        
        statusFilterComboBox.getItems().addAll("All Status", "Scheduled", "Completed", "Cancelled");
        statusFilterComboBox.setValue("All Status");
    }
    
    private void loadData() {
        try {
            if (currentNurse != null) {
                // Load shifts for current nurse
                List<ShiftSchedule> shifts = shiftService.findByStaffId(currentNurse.getStaffId());
                shiftsData.clear();
                shiftsData.addAll(shifts);
                shiftsTable.setItems(shiftsData);
                
                // Update statistics
                updateStatistics();
                
                System.out.println("Loaded " + shifts.size() + " shifts for nurse " + currentNurse.getFullName());
            } else {
                System.out.println("No current nurse set, cannot load shifts");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading shift schedule data: " + e.getMessage());
            showError("Failed to load shift schedule data: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            if (currentNurse != null) {
                List<ShiftSchedule> allShifts = shiftService.findByStaffId(currentNurse.getStaffId());
                
                // Total shifts
                totalShiftsLabel.setText(String.valueOf(allShifts.size()));
                
                // This week shifts
                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                long thisWeekCount = allShifts.stream()
                    .filter(shift -> !shift.getShiftDate().isBefore(weekStart) && !shift.getShiftDate().isAfter(weekEnd))
                    .count();
                thisWeekShiftsLabel.setText(String.valueOf(thisWeekCount));
                
                // Next week shifts
                LocalDate nextWeekStart = weekStart.plusDays(7);
                LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
                
                long nextWeekCount = allShifts.stream()
                    .filter(shift -> !shift.getShiftDate().isBefore(nextWeekStart) && !shift.getShiftDate().isAfter(nextWeekEnd))
                    .count();
                nextWeekShiftsLabel.setText(String.valueOf(nextWeekCount));
                
                // Completed shifts
                long completedCount = allShifts.stream()
                    .filter(shift -> "Completed".equals(shift.getStatus().name()))
                    .count();
                completedShiftsLabel.setText(String.valueOf(completedCount));
                
            }
            
        } catch (Exception e) {
            System.err.println("Error updating shift statistics: " + e.getMessage());
        }
    }
    
    private void viewThisWeek() {
        weekFilterComboBox.setValue("This Week");
        filterShifts();
    }
    
    private void viewNextWeek() {
        weekFilterComboBox.setValue("Next Week");
        filterShifts();
    }
    
    private void viewAllShifts() {
        weekFilterComboBox.setValue("All Weeks");
        statusFilterComboBox.setValue("All Status");
        filterShifts();
    }
    
    private void filterShifts() {
        String weekFilter = weekFilterComboBox.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        
        shiftsData.clear();
        
        try {
            if (currentNurse != null) {
                List<ShiftSchedule> allShifts = shiftService.findByStaffId(currentNurse.getStaffId());
                
                for (ShiftSchedule shift : allShifts) {
                    // Week filter
                    if (!weekFilter.equals("All Weeks")) {
                        LocalDate today = LocalDate.now();
                        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                        LocalDate weekEnd = weekStart.plusDays(6);
                        
                        if (weekFilter.equals("This Week")) {
                            if (shift.getShiftDate().isBefore(weekStart) || shift.getShiftDate().isAfter(weekEnd)) {
                                continue;
                            }
                        } else if (weekFilter.equals("Next Week")) {
                            LocalDate nextWeekStart = weekStart.plusDays(7);
                            LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
                            if (shift.getShiftDate().isBefore(nextWeekStart) || shift.getShiftDate().isAfter(nextWeekEnd)) {
                                continue;
                            }
                        } else if (weekFilter.equals("Last Week")) {
                            LocalDate lastWeekStart = weekStart.minusDays(7);
                            LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
                            if (shift.getShiftDate().isBefore(lastWeekStart) || shift.getShiftDate().isAfter(lastWeekEnd)) {
                                continue;
                            }
                        }
                    }
                    
                    // Status filter
                    if (!statusFilter.equals("All Status")) {
                        if (!shift.getStatus().name().equals(statusFilter)) {
                            continue;
                        }
                    }
                    
                    shiftsData.add(shift);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error filtering shifts: " + e.getMessage());
        }
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
        System.out.println("NurseShiftScheduleController: Set current nurse to " + (nurse != null ? nurse.getFullName() : "null"));
        loadData();
    }
}
