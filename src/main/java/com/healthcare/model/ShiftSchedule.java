package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ShiftSchedule Entity - Represents actual staff shift assignments
 * Simple MVP implementation for healthcare shift scheduling
 */
@Entity
@Table(name = "Shift_Schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Long scheduleId;
    
    @Column(name = "staff_id", nullable = false)
    private Long staffId;
    
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private Shift.ShiftType shiftType;
    
    @Column(name = "start_time", nullable = false)
    private String startTime;
    
    @Column(name = "end_time", nullable = false)
    private String endTime;
    
    @Column(name = "ward_id")
    private Long wardId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScheduleStatus status = ScheduleStatus.Scheduled;
    
    @Column(name = "assigned_by")
    private Long assignedBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", insertable = false, updatable = false)
    private Staff staff;
    
    // Enums
    public enum ScheduleStatus {
        Scheduled, Completed, Cancelled
    }
    
    // Custom constructors for business logic
    
    public ShiftSchedule(Long staffId, LocalDate shiftDate, Shift.ShiftType shiftType, String startTime, String endTime) {
        this.staffId = staffId;
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ScheduleStatus.Scheduled;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getDisplayName() {
        return shiftType + " Shift (" + startTime + " - " + endTime + ")";
    }
    
    public boolean isCompleted() {
        return status == ScheduleStatus.Completed;
    }
    
    public boolean isCancelled() {
        return status == ScheduleStatus.Cancelled;
    }
    
    public boolean isActive() {
        return status == ScheduleStatus.Scheduled;
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
    
    public Shift.ShiftType getShiftType() { return shiftType; }
    public void setShiftType(Shift.ShiftType shiftType) { this.shiftType = shiftType; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
    
    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
    
    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    
    // Override equals and hashCode for proper comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ShiftSchedule that = (ShiftSchedule) obj;
        return scheduleId != null ? scheduleId.equals(that.scheduleId) : that.scheduleId == null;
    }
    
    @Override
    public int hashCode() {
        return scheduleId != null ? scheduleId.hashCode() : 0;
    }
}
