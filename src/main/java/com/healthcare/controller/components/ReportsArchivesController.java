package com.healthcare.controller.components;

import com.healthcare.model.*;
import com.healthcare.services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Reports and Archives page
 * Handles CSV export functionality and archived residents view
 */
public class ReportsArchivesController implements Initializable {
    
    // FXML Elements
    @FXML private Button exportStaffButton;
    @FXML private Button exportResidentButton;
    @FXML private Button exportAuditButton;
    @FXML private Button exportShiftButton;
    @FXML private Label archiveCountLabel;
    @FXML private TableView<Resident> archivesTable;
    @FXML private TableColumn<Resident, String> archivedNameColumn;
    @FXML private TableColumn<Resident, Resident.Gender> archivedGenderColumn;
    @FXML private TableColumn<Resident, Integer> archivedAgeColumn;
    @FXML private TableColumn<Resident, LocalDate> archivedAdmissionDateColumn;
    @FXML private TableColumn<Resident, LocalDate> archivedDischargeDateColumn;
    @FXML private TableColumn<Resident, String> archivedBedColumn;
    @FXML private TableColumn<Resident, String> archivedReasonColumn;
    
    // Data
    private ObservableList<Resident> archivedResidents = FXCollections.observableArrayList();
    
    // Services
    private StaffService staffService = new StaffService();
    private ResidentService residentService = new ResidentService();
    private ActionLogService actionLogService = new ActionLogService();
    private ShiftService shiftService = new ShiftService();
    
    // Current staff for context
    private Staff currentStaff;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadArchivedResidents();
    }
    
    /**
     * Set the current logged-in staff member for context
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }
    
    private void setupTable() {
        System.out.println("Setting up reports and archives component...");
        
        // Setup table columns
        archivedNameColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(resident.getFullName());
        });
        archivedGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        archivedAgeColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(resident.getAge()).asObject();
        });
        archivedAdmissionDateColumn.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
        archivedDischargeDateColumn.setCellValueFactory(new PropertyValueFactory<>("dischargeDate"));
        archivedBedColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            // For archived residents, show the last bed they were in
            return new javafx.beans.property.SimpleStringProperty(
                resident.getCurrentBedId() != null ? "Bed " + resident.getCurrentBedId() : "N/A"
            );
        });
        archivedReasonColumn.setCellValueFactory(cellData -> {
            Resident resident = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("Discharged");
        });
        
        // Set column widths
        archivedNameColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.25));
        archivedGenderColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.10));
        archivedAgeColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.08));
        archivedAdmissionDateColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.15));
        archivedDischargeDateColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.15));
        archivedBedColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.12));
        archivedReasonColumn.prefWidthProperty().bind(archivesTable.widthProperty().multiply(0.15));
        
        archivesTable.setItems(archivedResidents);
        
        System.out.println("Reports and archives component setup complete");
    }
    
    private void loadArchivedResidents() {
        try {
            // Load archived residents (discharged residents)
            archivedResidents.clear();
            List<Resident> archived = residentService.findArchivedResidents();
            archivedResidents.addAll(archived);
            
            archiveCountLabel.setText("Total: " + archived.size());
            
            System.out.println("Loaded " + archived.size() + " archived residents");
        } catch (Exception e) {
            System.err.println("Error loading archived residents: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load archived residents: " + e.getMessage());
        }
    }
    
    @FXML
    private void exportStaffReport() {
        try {
            List<Staff> staffList = staffService.findAll();
            String fileName = "staff_report_" + getTimestamp() + ".csv";
            boolean exported = exportToCSV(fileName, generateStaffCSV(staffList));
            if (exported) {
                showSuccess("Staff report exported successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error exporting staff report: " + e.getMessage());
            showError("Failed to export staff report: " + e.getMessage());
        }
    }
    
    @FXML
    private void exportResidentReport() {
        try {
            List<Resident> residentList = residentService.findAll();
            String fileName = "resident_report_" + getTimestamp() + ".csv";
            boolean exported = exportToCSV(fileName, generateResidentCSV(residentList));
            if (exported) {
                showSuccess("Resident report exported successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error exporting resident report: " + e.getMessage());
            showError("Failed to export resident report: " + e.getMessage());
        }
    }
    
    @FXML
    private void exportAuditReport() {
        try {
            List<ActionLog> auditList = actionLogService.findAll();
            String fileName = "audit_report_" + getTimestamp() + ".csv";
            boolean exported = exportToCSV(fileName, generateAuditCSV(auditList));
            if (exported) {
                showSuccess("Audit report exported successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error exporting audit report: " + e.getMessage());
            showError("Failed to export audit report: " + e.getMessage());
        }
    }
    
    @FXML
    private void exportShiftReport() {
        try {
            List<ShiftSchedule> scheduleList = shiftService.findAll();
            String fileName = "shift_schedule_report_" + getTimestamp() + ".csv";
            boolean exported = exportToCSV(fileName, generateShiftScheduleCSV(scheduleList));
            if (exported) {
                showSuccess("Shift schedule report exported successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error exporting shift schedule report: " + e.getMessage());
            showError("Failed to export shift schedule report: " + e.getMessage());
        }
    }
    
    private boolean exportToCSV(String fileName, String csvContent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        Stage stage = (Stage) exportStaffButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csvContent);
                System.out.println("Report exported to: " + file.getAbsolutePath());
                return true;
            } catch (IOException e) {
                System.err.println("Error writing file: " + e.getMessage());
                throw new RuntimeException("Failed to write file", e);
            }
        }
        return false; // User cancelled
    }
    
    private String generateStaffCSV(List<Staff> staffList) {
        StringBuilder csv = new StringBuilder();
        csv.append("Staff ID,Username,First Name,Last Name,Role,Email,Phone,Is Active\n");
        
        for (Staff staff : staffList) {
            csv.append(staff.getStaffId()).append(",");
            csv.append(escapeCSV(staff.getUsername())).append(",");
            csv.append(escapeCSV(staff.getFirstName())).append(",");
            csv.append(escapeCSV(staff.getLastName())).append(",");
            csv.append(staff.getRole()).append(",");
            csv.append(escapeCSV(staff.getEmail())).append(",");
            csv.append(escapeCSV(staff.getPhone())).append(",");
            csv.append("Yes").append("\n");
        }
        
        return csv.toString();
    }
    
    private String generateResidentCSV(List<Resident> residentList) {
        StringBuilder csv = new StringBuilder();
        csv.append("Resident ID,Name,Gender,Age,Admission Date,Current Bed,Status,Discharge Date,Discharge Reason\n");
        
        for (Resident resident : residentList) {
            csv.append(resident.getResidentId()).append(",");
            csv.append(escapeCSV(resident.getFullName())).append(",");
            csv.append(resident.getGender()).append(",");
            csv.append(resident.getAge()).append(",");
            csv.append(resident.getAdmissionDate()).append(",");
            csv.append(resident.getCurrentBedId() != null ? resident.getCurrentBedId() : "N/A").append(",");
            csv.append(resident.getDischargeDate() != null ? "Discharged" : "Active").append(",");
            csv.append(resident.getDischargeDate() != null ? resident.getDischargeDate() : "N/A").append(",");
            csv.append("Discharged").append("\n");
        }
        
        return csv.toString();
    }
    
    private String generateAuditCSV(List<ActionLog> auditList) {
        StringBuilder csv = new StringBuilder();
        csv.append("Action ID,Staff ID,Action Type,Description,Action Time,Details\n");
        
        for (ActionLog log : auditList) {
            csv.append(log.getActionId()).append(",");
            csv.append(log.getStaffId()).append(",");
            csv.append(log.getActionType()).append(",");
            csv.append(escapeCSV(log.getActionDescription())).append(",");
            csv.append(log.getActionTime()).append(",");
            csv.append(escapeCSV(log.getDetails() != null ? log.getDetails() : "")).append("\n");
        }
        
        return csv.toString();
    }
    
    private String generateShiftScheduleCSV(List<ShiftSchedule> scheduleList) {
        StringBuilder csv = new StringBuilder();
        csv.append("Schedule ID,Staff ID,Shift Date,Shift Type,Start Time,End Time,Ward ID,Status\n");
        
        for (ShiftSchedule schedule : scheduleList) {
            csv.append(schedule.getScheduleId()).append(",");
            csv.append(schedule.getStaffId()).append(",");
            csv.append(schedule.getShiftDate()).append(",");
            csv.append(schedule.getShiftType()).append(",");
            csv.append(schedule.getStartTime()).append(",");
            csv.append(schedule.getEndTime()).append(",");
            csv.append(schedule.getWardId() != null ? schedule.getWardId() : "N/A").append(",");
            csv.append(schedule.getStatus()).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
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
