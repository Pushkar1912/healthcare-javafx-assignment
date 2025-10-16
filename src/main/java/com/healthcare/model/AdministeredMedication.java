package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * AdministeredMedication Entity - Records when nurses administer medications
 * Maps to the Administered_Medication table in the database
 */
@Entity
@Table(name = "Administered_Medication")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministeredMedication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;
    
    @Column(name = "prescription_medicine_id", nullable = false)
    private Long prescriptionMedicineId;
    
    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;
    
    @Column(name = "administered_time", nullable = false)
    private LocalDateTime administeredTime;
    
    @Column(name = "dosage_given")
    private String dosageGiven;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AdministrationStatus status = AdministrationStatus.Given;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_medicine_id", insertable = false, updatable = false)
    private PrescriptionMedicine prescriptionMedicine;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", insertable = false, updatable = false)
    private Staff nurse;
    
    // Enums
    public enum AdministrationStatus {
        Given, Missed, Refused
    }
    
    // Custom constructors for business logic
    public AdministeredMedication(Long prescriptionMedicineId, Long nurseId, LocalDateTime administeredTime) {
        this.prescriptionMedicineId = prescriptionMedicineId;
        this.nurseId = nurseId;
        this.administeredTime = administeredTime;
        this.status = AdministrationStatus.Given;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getFullDescription() {
        return "Medication administered at " + administeredTime;
    }
}
