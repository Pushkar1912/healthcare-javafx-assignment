package com.healthcare.controller.components;

import com.healthcare.model.ActionLog;
import com.healthcare.model.Staff;
import com.healthcare.services.ActionLogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Component Controller for Action Logs
 * Handles viewing and filtering of action logs with pagination
 */
public class ActionLogsController implements Initializable {
    
    // FXML Elements
    @FXML private Label totalLogsLabel;
    @FXML private TableView<ActionLog> logsTable;
    @FXML private TableColumn<ActionLog, String> actionTimeColumn;
    @FXML private TableColumn<ActionLog, String> staffColumn;
    @FXML private TableColumn<ActionLog, ActionLog.ActionType> actionTypeColumn;
    @FXML private TableColumn<ActionLog, String> descriptionColumn;
    @FXML private TableColumn<ActionLog, String> detailsColumn;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> itemsPerPageComboBox;
    
    // Data
    private ObservableList<ActionLog> allLogs = FXCollections.observableArrayList();
    
    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 20;
    private int totalPages = 1;
    
    // Services
    private ActionLogService actionLogService = new ActionLogService();
    
    // Current staff for context
    private Staff currentStaff;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupPagination();
        loadData();
    }
    
    /**
     * Set the current logged-in staff member for context
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }
    
    private void setupTable() {
        System.out.println("Setting up action logs component...");
        
        // Setup table columns
        actionTimeColumn.setCellValueFactory(cellData -> {
            ActionLog log = cellData.getValue();
            if (log.getActionTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    log.getActionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        staffColumn.setCellValueFactory(cellData -> {
            ActionLog log = cellData.getValue();
            // Get staff name from the joined data
            return new javafx.beans.property.SimpleStringProperty(getStaffName(log));
        });
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("actionDescription"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        
        // Set column widths for simplified layout (without ID column)
        actionTimeColumn.prefWidthProperty().bind(logsTable.widthProperty().multiply(0.20));
        staffColumn.prefWidthProperty().bind(logsTable.widthProperty().multiply(0.18));
        actionTypeColumn.prefWidthProperty().bind(logsTable.widthProperty().multiply(0.15));
        descriptionColumn.prefWidthProperty().bind(logsTable.widthProperty().multiply(0.30));
        detailsColumn.prefWidthProperty().bind(logsTable.widthProperty().multiply(0.17));
        
        System.out.println("Action logs component setup complete");
    }
    
    /**
     * Get staff name from the joined data
     */
    private String getStaffName(ActionLog log) {
        // For now, we'll show the staff ID since we don't have the joined staff data
        // In a real implementation, you'd store the staff name in the ActionLog or join it
        return log.getStaffId() != null ? "Staff ID: " + log.getStaffId() : "Unknown Staff";
    }
    
    private void setupFilters() {
        // Setup items per page combo box
        itemsPerPageComboBox.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        itemsPerPageComboBox.setValue(20);
        itemsPerPageComboBox.setOnAction(e -> {
            itemsPerPage = itemsPerPageComboBox.getValue();
            currentPage = 1;
            updatePagination();
        });
    }
    
    private void setupPagination() {
        updatePagination();
    }
    
    private void loadData() {
        try {
            // Load all action logs
            allLogs.clear();
            List<ActionLog> logs = actionLogService.findAll();
            allLogs.addAll(logs);
            
            // Update table and pagination
            updatePagination();
            
            System.out.println("Loaded " + logs.size() + " action logs");
            System.out.println("Action logs component loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading action logs: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load action logs: " + e.getMessage());
        }
    }
    
    
    
    private void updatePagination() {
        totalPages = (int) Math.ceil((double) allLogs.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        
        // Update button states
        firstPageButton.setDisable(currentPage == 1);
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage == totalPages);
        lastPageButton.setDisable(currentPage == totalPages);
        
        // Update page info
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        totalLogsLabel.setText("Total: " + allLogs.size());
        
        updateTable();
    }
    
    private void updateTable() {
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allLogs.size());
        
        ObservableList<ActionLog> pageData = FXCollections.observableArrayList();
        for (int i = startIndex; i < endIndex; i++) {
            pageData.add(allLogs.get(i));
        }
        
        logsTable.setItems(pageData);
    }
    
    @FXML
    private void goToFirstPage() {
        currentPage = 1;
        updatePagination();
    }
    
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }
    
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }
    
    @FXML
    private void goToLastPage() {
        currentPage = totalPages;
        updatePagination();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
