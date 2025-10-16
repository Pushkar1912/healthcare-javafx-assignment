package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * ActionLog Model - Simplified for essential auditing
 * Stores only the most important action tracking information:
 * - staff_id: Who performed the action
 * - action_type: What type of action was performed
 * - action_description: Description of what happened
 * - action_time: When it happened
 * - details: Additional details
 */
@Entity
@Table(name = "Actions_Log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;
    
    @Column(name = "staff_id")
    private Long staffId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    @Column(name = "action_description", nullable = false, length = 200)
    private String actionDescription;
    
    @Column(name = "action_time")
    private LocalDateTime actionTime;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    // Action Type Enum
    public enum ActionType {
        Admit, Discharge, Transfer, Prescribe, Administer, Update, 
        Add_Staff, Delete_Staff, Assign_Shift, Delete_Shift,
        Login, Logout, Archive
    }
    
    // Constructors
    public ActionLog(Long staffId, ActionType actionType, String actionDescription, String details) {
        this.staffId = staffId;
        this.actionType = actionType;
        this.actionDescription = actionDescription;
        this.details = details;
        this.actionTime = LocalDateTime.now();
    }
    
    public ActionLog(Staff staff, ActionType actionType, String actionDescription, String details) {
        this.staffId = staff != null ? staff.getStaffId() : null;
        this.actionType = actionType;
        this.actionDescription = actionDescription;
        this.details = details;
        this.actionTime = LocalDateTime.now();
    }
    
    // Utility methods
    public String getFullDescription() {
        return "[" + actionTime + "] " + actionType + ": " + actionDescription + 
               (details != null && !details.isEmpty() ? " - " + details : "");
    }
    
    @PrePersist
    protected void onCreate() {
        if (actionTime == null) {
            actionTime = LocalDateTime.now();
        }
    }
    
    // Manual getters and setters (in case Lombok doesn't work properly)
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    
    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }
    
    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}