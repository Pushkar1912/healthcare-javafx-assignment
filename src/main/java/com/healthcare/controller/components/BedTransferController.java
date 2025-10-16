package com.healthcare.controller.components;

import com.healthcare.model.Bed;
import com.healthcare.model.Resident;
import com.healthcare.model.Staff;
import com.healthcare.model.BedTransfer;
import com.healthcare.services.BedTransferService;
import com.healthcare.services.BedTransferService.TransferStats;
import com.healthcare.services.BedTransferService.TransferValidation;
import com.healthcare.services.ResidentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Bed Transfer component
 * Allows nurses to transfer residents between beds
 */
public class BedTransferController implements Initializable {
    
    @FXML
    private VBox mainContainer;
    
    @FXML
    private Label transfersTodayLabel;
    @FXML
    private Label transfersThisWeekLabel;
    @FXML
    private Label pendingTransfersLabel;
    
    @FXML
    private ComboBox<Resident> residentComboBox;
    @FXML
    private TextField fromBedTextField;
    @FXML
    private ComboBox<Bed> toBedComboBox;
    @FXML
    private TextArea reasonTextArea;
    @FXML
    private Button transferButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button validateButton;
    
    @FXML
    private TableView<BedTransfer> transferHistoryTable;
    @FXML
    private TableColumn<BedTransfer, String> residentColumn;
    @FXML
    private TableColumn<BedTransfer, String> fromBedColumn;
    @FXML
    private TableColumn<BedTransfer, String> toBedColumn;
    @FXML
    private TableColumn<BedTransfer, String> nurseColumn;
    @FXML
    private TableColumn<BedTransfer, String> timeColumn;
    @FXML
    private TableColumn<BedTransfer, String> reasonColumn;
    
    @FXML
    private Label validationLabel;
    
    // Services
    private BedTransferService bedTransferService = new BedTransferService();
    private ResidentService residentService = new ResidentService();
    private Staff currentNurse;
    
    // Data
    private ObservableList<Resident> residentsData = FXCollections.observableArrayList();
    private ObservableList<Bed> availableBedsData = FXCollections.observableArrayList();
    private ObservableList<BedTransfer> transferHistoryData = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("BedTransferController: Initializing...");
        setupTableColumns();
        setupEventHandlers();
        loadData();
        System.out.println("BedTransferController: Initialization complete");
    }
    
    private void setupTableColumns() {
        residentColumn.setCellValueFactory(cellData -> {
            BedTransfer transfer = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("Resident " + transfer.getResidentId());
        });
        fromBedColumn.setCellValueFactory(cellData -> {
            BedTransfer transfer = cellData.getValue();
            if (transfer.getFromBedId() == null) {
                return new javafx.beans.property.SimpleStringProperty("New Admission");
            }
            return new javafx.beans.property.SimpleStringProperty("Bed " + transfer.getFromBedId());
        });
        toBedColumn.setCellValueFactory(cellData -> {
            BedTransfer transfer = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("Bed " + transfer.getToBedId());
        });
        nurseColumn.setCellValueFactory(cellData -> {
            BedTransfer transfer = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("Nurse " + transfer.getNurseId());
        });
        timeColumn.setCellValueFactory(cellData -> {
            LocalDateTime time = cellData.getValue().getTransferTime();
            return new javafx.beans.property.SimpleStringProperty(
                time.format(DateTimeFormatter.ofPattern("HH:mm"))
            );
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
    }
    
    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadData());
        
        transferButton.setOnAction(e -> performTransfer());
        validateButton.setOnAction(e -> validateTransfer());
        
        // Clear validation when inputs change and update bed information
        residentComboBox.setOnAction(e -> {
            clearValidation();
            updateAvailableBeds(); // This will call updateFromBedField
        });
        toBedComboBox.setOnAction(e -> clearValidation());
        reasonTextArea.textProperty().addListener((obs, oldVal, newVal) -> clearValidation());
        
        // Setup custom display format for resident ComboBox
        setupResidentComboBoxFormat();
        
        // Setup custom display format for bed ComboBoxes
        setupBedComboBoxFormat();
    }
    
    private void setupResidentComboBoxFormat() {
        // Set custom cell factory to display "ID + Full Name"
        residentComboBox.setCellFactory(listView -> new ListCell<Resident>() {
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
        residentComboBox.setButtonCell(new ListCell<Resident>() {
            @Override
            protected void updateItem(Resident resident, boolean empty) {
                super.updateItem(resident, empty);
                if (empty || resident == null) {
                    setText("Choose resident...");
                } else {
                    setText(resident.getResidentId() + " - " + resident.getFirstName() + " " + resident.getLastName());
                }
            }
        });
    }
    
    private void loadData() {
        try {
            // Load statistics
            TransferStats stats = bedTransferService.getTransferStats();
            transfersTodayLabel.setText(String.valueOf(stats.getTransfersToday()));
            transfersThisWeekLabel.setText(String.valueOf(stats.getTransfersThisWeek()));
            pendingTransfersLabel.setText(String.valueOf(stats.getPendingTransfers()));
            
            // Load active residents
            List<Resident> residents = residentService.findActiveResidents();
            residentsData.clear();
            residentsData.addAll(residents);
            residentComboBox.setItems(residentsData);
            
            // Load available beds
            List<Bed> availableBeds = bedTransferService.getAvailableBeds();
            availableBedsData.clear();
            availableBedsData.addAll(availableBeds);
            toBedComboBox.setItems(availableBedsData);
            
            // Load transfer history
            loadTransferHistory();
            
            System.out.println("Loaded " + residents.size() + " residents, " + availableBeds.size() + " available beds");
            
        } catch (Exception e) {
            System.err.println("Error loading bed transfer data: " + e.getMessage());
            showError("Failed to load bed transfer data: " + e.getMessage());
        }
    }
    
    private void loadTransferHistory() {
        try {
            if (currentNurse != null) {
                List<BedTransfer> transfers = bedTransferService.getRecentTransfersByNurse(currentNurse.getStaffId(), 20);
                transferHistoryData.clear();
                transferHistoryData.addAll(transfers);
                transferHistoryTable.setItems(transferHistoryData);
            }
        } catch (Exception e) {
            System.err.println("Error loading transfer history: " + e.getMessage());
        }
    }
    
    private void updateAvailableBeds() {
        Resident selectedResident = residentComboBox.getSelectionModel().getSelectedItem();
        System.out.println("updateAvailableBeds called with resident: " + (selectedResident != null ? selectedResident.getFirstName() + " " + selectedResident.getLastName() : "null"));
        
        if (selectedResident != null) {
            try {
                // Update "To Bed" options with suitable beds
                List<Bed> suitableBeds = bedTransferService.getSuitableBedsForResident(selectedResident.getResidentId());
                availableBedsData.clear();
                availableBedsData.addAll(suitableBeds);
                toBedComboBox.setItems(availableBedsData);
                
                // Update "From Bed" field based on current bed assignment
                updateFromBedField(selectedResident);
                
            } catch (Exception e) {
                System.err.println("Error updating available beds: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void updateFromBedField(Resident resident) {
        System.out.println("Updating From Bed field for resident: " + resident.getFirstName() + " " + resident.getLastName());
        System.out.println("Resident current bed ID: " + resident.getCurrentBedId());
        
        if (resident.getCurrentBedId() != null) {
            try {
                // Resident has a current bed - fetch bed details and display
                Bed currentBed = bedTransferService.getBedById(resident.getCurrentBedId());
                System.out.println("Fetched current bed: " + (currentBed != null ? currentBed.getBedNumber() : "null"));
                
                if (currentBed != null) {
                    System.out.println("Bed details - Number: " + currentBed.getBedNumber() + ", Code: " + currentBed.getBedCode());
                    String bedInfo = getBedDisplayText(currentBed);
                    fromBedTextField.setText(bedInfo);
                    fromBedTextField.setDisable(true); // Make it read-only
                    System.out.println("Set current bed display: " + bedInfo);
                } else {
                    // Bed not found - show error state
                    fromBedTextField.setText("Bed not found (ID: " + resident.getCurrentBedId() + ")");
                    fromBedTextField.setDisable(true);
                    System.out.println("Bed not found for ID: " + resident.getCurrentBedId());
                }
            } catch (Exception e) {
                System.err.println("Error fetching current bed: " + e.getMessage());
                fromBedTextField.setText("Error loading current bed");
                fromBedTextField.setDisable(true);
            }
        } else {
            // No current bed assigned - show new admission state
            fromBedTextField.setText("No bed assigned - New admission");
            fromBedTextField.setDisable(true);
            System.out.println("No bed assigned for resident: " + resident.getFirstName() + " " + resident.getLastName());
        }
    }
    
    private String getBedDisplayText(Bed bed) {
        if (bed == null) return "Unknown";
        
        // Show bed code if available, otherwise show bed number
        if (bed.getBedCode() != null && !bed.getBedCode().trim().isEmpty()) {
            return bed.getBedCode();
        } else {
            return bed.getBedNumber();
        }
    }
    
    private void setupBedComboBoxFormat() {
        // Setup "To Bed" ComboBox format only (From Bed is now a TextField)
        toBedComboBox.setCellFactory(listView -> new ListCell<Bed>() {
            @Override
            protected void updateItem(Bed bed, boolean empty) {
                super.updateItem(bed, empty);
                if (empty || bed == null) {
                    setText(null);
                } else {
                    setText(getBedDisplayText(bed));
                }
            }
        });
        
        toBedComboBox.setButtonCell(new ListCell<Bed>() {
            @Override
            protected void updateItem(Bed bed, boolean empty) {
                super.updateItem(bed, empty);
                if (empty || bed == null) {
                    setText("Choose destination bed...");
                } else {
                    setText(getBedDisplayText(bed));
                }
            }
        });
    }
    
    private void validateTransfer() {
        Resident selectedResident = residentComboBox.getSelectionModel().getSelectedItem();
        Bed selectedToBed = toBedComboBox.getSelectionModel().getSelectedItem();
        
        if (selectedResident == null || selectedToBed == null) {
            showValidationMessage("Please select both resident and destination bed.", false);
            return;
        }
        
        try {
            TransferValidation validation = bedTransferService.validateTransfer(
                selectedResident.getResidentId(), 
                selectedToBed.getBedId()
            );
            
            if (validation.isValid()) {
                showValidationMessage("Transfer is valid and can proceed.", true);
                transferButton.setDisable(false);
            } else {
                showValidationMessage("Transfer validation failed: " + validation.getErrorMessage(), false);
                transferButton.setDisable(true);
            }
            
        } catch (Exception e) {
            showValidationMessage("Error validating transfer: " + e.getMessage(), false);
            transferButton.setDisable(true);
        }
    }
    
    private void performTransfer() {
        Resident selectedResident = residentComboBox.getSelectionModel().getSelectedItem();
        Bed selectedToBed = toBedComboBox.getSelectionModel().getSelectedItem();
        String reason = reasonTextArea.getText().trim();
        
        if (selectedResident == null || selectedToBed == null) {
            showAlert("Please select both resident and destination bed.");
            return;
        }
        
        if (reason.isEmpty()) {
            reason = "Bed transfer by nurse";
        }
        
        try {
            boolean success = bedTransferService.transferResident(
                selectedResident.getResidentId(),
                selectedToBed.getBedId(),
                currentNurse.getStaffId(),
                reason
            );
            
            if (success) {
                showAlert("Resident transferred successfully!");
                clearForm();
                loadData();
            } else {
                showAlert("Failed to transfer resident. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Error performing transfer: " + e.getMessage());
            showAlert("Error performing transfer: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        residentComboBox.getSelectionModel().clearSelection();
        toBedComboBox.getSelectionModel().clearSelection();
        reasonTextArea.clear();
        clearValidation();
    }
    
    private void clearValidation() {
        validationLabel.setText("");
        validationLabel.setStyle("");
        transferButton.setDisable(true);
    }
    
    private void showValidationMessage(String message, boolean isValid) {
        validationLabel.setText(message);
        if (isValid) {
            validationLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
        } else {
            validationLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bed Transfer");
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
    
    public void setCurrentNurse(Staff nurse) {
        this.currentNurse = nurse;
        System.out.println("BedTransferController: Set current nurse to " + (nurse != null ? nurse.getFullName() : "null"));
        loadTransferHistory();
    }
}
