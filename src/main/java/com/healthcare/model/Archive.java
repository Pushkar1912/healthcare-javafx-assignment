package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Archive Entity - Stores discharged resident records for auditing
 * Maps to the Archive table in the database
 */
@Entity
@Table(name = "Archive")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Archive {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id")
    private Long archiveId;
    
    @Column(name = "resident_id")
    private Long residentId;
    
    @Column(name = "discharge_date")
    private LocalDate dischargeDate;
    
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
    
    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;
    
    // Constructors
    public Archive(Long residentId, LocalDate dischargeDate) {
        this.residentId = residentId;
        this.dischargeDate = dischargeDate;
        this.archivedAt = LocalDateTime.now();
    }
    
    public Archive(Resident resident) {
        this.resident = resident;
        this.residentId = null; // Will be set by service layer
        this.dischargeDate = null; // Will be set by service layer
        this.archivedAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getFullDescription() {
        return "Archived: Resident " + residentId + " - Discharged: " + dischargeDate + " - Archived: " + archivedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (archivedAt == null) {
            archivedAt = LocalDateTime.now();
        }
    }
}
