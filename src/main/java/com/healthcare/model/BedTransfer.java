package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * BedTransfer Entity - Records bed transfers performed by nurses
 * Maps to the Bed_Transfers table in the database
 */
@Entity
@Table(name = "Bed_Transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BedTransfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;
    
    @Column(name = "resident_id", nullable = false)
    private Long residentId;
    
    @Column(name = "from_bed_id")
    private Long fromBedId;
    
    @Column(name = "to_bed_id", nullable = false)
    private Long toBedId;
    
    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;
    
    @Column(name = "transfer_time", nullable = false)
    private LocalDateTime transferTime;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_bed_id", insertable = false, updatable = false)
    private Bed fromBed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_bed_id", insertable = false, updatable = false)
    private Bed toBed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", insertable = false, updatable = false)
    private Staff nurse;
    
    // Custom constructors for business logic
    public BedTransfer(Long residentId, Long fromBedId, Long toBedId, Long nurseId, String reason) {
        this.residentId = residentId;
        this.fromBedId = fromBedId;
        this.toBedId = toBedId;
        this.nurseId = nurseId;
        this.transferTime = LocalDateTime.now();
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
    
    public BedTransfer(Long residentId, Long toBedId, Long nurseId, String reason) {
        this.residentId = residentId;
        this.fromBedId = null; // New admission, no previous bed
        this.toBedId = toBedId;
        this.nurseId = nurseId;
        this.transferTime = LocalDateTime.now();
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getTransferDescription() {
        if (fromBedId == null) {
            return "Admitted to bed " + toBedId;
        }
        return "Transferred from bed " + fromBedId + " to bed " + toBedId;
    }
    
    public boolean isNewAdmission() {
        return fromBedId == null;
    }
    
    public boolean isTransfer() {
        return fromBedId != null;
    }
}
