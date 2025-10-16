package com.healthcare.controller.components;

import com.healthcare.model.ActionLog;
import com.healthcare.model.Staff;
import com.healthcare.services.ActionLogService;
import com.healthcare.services.StaffService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Component Controller for System Settings
 * Handles profile management for the current logged-in user
 * Public component that can be used across all dashboards
 */
public class SystemSettingsController implements Initializable {

    // FXML Elements
    @FXML private Label currentUserLabel;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button updateProfileButton;
    @FXML private Button cancelProfileButton;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;

    // Services
    private StaffService staffService = new StaffService();
    private ActionLogService actionLogService = new ActionLogService();

    // Current staff for context
    private Staff currentStaff;
    private Staff originalStaff; // Store original data for cancel functionality

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Setting up system settings component...");
        setupEventHandlers();
        System.out.println("System settings component setup complete");
    }

    /**
     * Set the current logged-in staff member for context
     * Public method for use across all dashboards
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        if (staff != null) {
            currentUserLabel.setText(staff.getFullName() + " (" + staff.getRole() + ")");
            loadProfileData();
        }
    }

    private void setupEventHandlers() {
        // Add input validation (username is read-only, so no validation needed)
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                firstNameField.setText(oldVal);
            }
        });

        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                lastNameField.setText(oldVal);
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                emailField.setText(oldVal);
            }
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 20) {
                phoneField.setText(oldVal);
            }
        });
    }

    private void loadProfileData() {
        if (currentStaff == null) {
            showError("No user logged in");
            return;
        }

        try {
            // Load current staff data
            java.util.Optional<Staff> staffOpt = staffService.findById(currentStaff.getStaffId());
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                
                // Store original data for cancel functionality
                this.originalStaff = new Staff();
                this.originalStaff.setStaffId(staff.getStaffId());
                this.originalStaff.setUsername(staff.getUsername());
                this.originalStaff.setFirstName(staff.getFirstName());
                this.originalStaff.setLastName(staff.getLastName());
                this.originalStaff.setEmail(staff.getEmail());
                this.originalStaff.setPhone(staff.getPhone());
                this.originalStaff.setRole(staff.getRole());
                
                // Populate form fields
                usernameField.setText(staff.getUsername());
                firstNameField.setText(staff.getFirstName());
                lastNameField.setText(staff.getLastName());
                emailField.setText(staff.getEmail());
                phoneField.setText(staff.getPhone());
                
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                
                System.out.println("Profile data loaded successfully");
            } else {
                showError("Failed to load profile data");
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            showError("Failed to load profile: " + e.getMessage());
        }
    }

    @FXML
    private void updateProfile() {
        if (currentStaff == null) {
            showError("No user logged in");
            return;
        }

        // Validate input (excluding username)
        if (!validateProfileInput()) {
            return;
        }

        try {
            // Get current staff data
            java.util.Optional<Staff> staffOpt = staffService.findById(currentStaff.getStaffId());
            if (!staffOpt.isPresent()) {
                showError("Staff not found");
                return;
            }

            Staff staff = staffOpt.get();
            // Update staff data (excluding username)
            staff.setFirstName(firstNameField.getText().trim());
            staff.setLastName(lastNameField.getText().trim());
            staff.setEmail(emailField.getText().trim());
            staff.setPhone(phoneField.getText().trim());

            // Save changes
            Staff updatedStaff = staffService.update(staff);
            if (updatedStaff != null) {
                // Update current staff reference
                currentStaff = updatedStaff;
                currentUserLabel.setText(updatedStaff.getFullName() + " (" + updatedStaff.getRole() + ")");
                
                // Update original staff data for future cancel operations
                this.originalStaff = new Staff();
                this.originalStaff.setStaffId(updatedStaff.getStaffId());
                this.originalStaff.setUsername(updatedStaff.getUsername());
                this.originalStaff.setFirstName(updatedStaff.getFirstName());
                this.originalStaff.setLastName(updatedStaff.getLastName());
                this.originalStaff.setEmail(updatedStaff.getEmail());
                this.originalStaff.setPhone(updatedStaff.getPhone());
                this.originalStaff.setRole(updatedStaff.getRole());
                
                // Log the action
                ActionLog actionLog = new ActionLog(
                    currentStaff.getStaffId(),
                    ActionLog.ActionType.Update,
                    "Updated profile information",
                    "Profile details modified"
                );
                actionLogService.save(actionLog);
                
                showSuccess("Profile updated successfully!");
            } else {
                showError("Failed to update profile");
            }
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            showError("Failed to update profile: " + e.getMessage());
        }
    }

    @FXML
    private void cancelProfile() {
        if (currentStaff == null || originalStaff == null) {
            showError("No user logged in");
            return;
        }

        // Reset to original values
        usernameField.setText(originalStaff.getUsername());
        firstNameField.setText(originalStaff.getFirstName());
        lastNameField.setText(originalStaff.getLastName());
        emailField.setText(originalStaff.getEmail());
        phoneField.setText(originalStaff.getPhone());
        
        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        
        System.out.println("Profile changes cancelled - reverted to original values");
    }

    @FXML
    private void changePassword() {
        if (currentStaff == null) {
            showError("No user logged in");
            return;
        }

        // Validate password input
        if (!validatePasswordInput()) {
            return;
        }

        try {
            // Get current staff data
            java.util.Optional<Staff> staffOpt = staffService.findById(currentStaff.getStaffId());
            if (!staffOpt.isPresent()) {
                showError("Staff not found");
                return;
            }

            Staff staff = staffOpt.get();
            // Verify current password
            if (!staff.getPassword().equals(currentPasswordField.getText())) {
                showError("Current password is incorrect");
                return;
            }

            // Update password
            staff.setPassword(newPasswordField.getText());
            Staff updatedStaff = staffService.update(staff);
            if (updatedStaff != null) {
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                
                // Log the action
                ActionLog actionLog = new ActionLog(
                    currentStaff.getStaffId(),
                    ActionLog.ActionType.Update,
                    "Changed password",
                    "Password updated successfully"
                );
                actionLogService.save(actionLog);
                
                showSuccess("Password changed successfully!");
            } else {
                showError("Failed to change password");
            }
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            showError("Failed to change password: " + e.getMessage());
        }
    }

    private boolean validateProfileInput() {
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required");
            firstNameField.requestFocus();
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required");
            lastNameField.requestFocus();
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required");
            emailField.requestFocus();
            return false;
        }

        // Basic email validation
        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validatePasswordInput() {
        if (currentPasswordField.getText().isEmpty()) {
            showError("Current password is required");
            currentPasswordField.requestFocus();
            return false;
        }

        if (newPasswordField.getText().isEmpty()) {
            showError("New password is required");
            newPasswordField.requestFocus();
            return false;
        }

        if (newPasswordField.getText().length() < 6) {
            showError("New password must be at least 6 characters long");
            newPasswordField.requestFocus();
            return false;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showError("New password and confirm password do not match");
            confirmPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
