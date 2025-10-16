package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Shift Entity - Represents shift templates
 * Simple MVP implementation for healthcare shift management
 */
@Entity
@Table(name = "Shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Column(name = "shift_name", nullable = false)
    private String shiftName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "ward_id")
    private Long wardId;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Enums
    public enum ShiftType {
        Morning, Afternoon, Doctor
    }
    
    // Custom constructors for business logic
    
    public Shift(String shiftName, ShiftType shiftType, LocalTime startTime, LocalTime endTime) {
        this.shiftName = shiftName;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public int getDurationHours() {
        return endTime.getHour() - startTime.getHour();
    }
    
    public String getDisplayName() {
        return shiftName + " (" + startTime + " - " + endTime + ")";
    }
    
    public boolean isNurseShift() {
        return shiftType == ShiftType.Morning || shiftType == ShiftType.Afternoon;
    }
    
    public boolean isDoctorShift() {
        return shiftType == ShiftType.Doctor;
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    
    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
