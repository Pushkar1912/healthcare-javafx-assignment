package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PrescriptionMedicine Entity - Links prescriptions to medicines with dosage and schedule
 * Maps to the Prescription_Medicines table in the database
 */
@Entity
@Table(name = "Prescription_Medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;
    
    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;
    
    @Column(name = "dosage", nullable = false)
    private String dosage;
    
    @Column(name = "frequency", nullable = false)
    private String frequency; // e.g., "Every 8 hours", "Twice daily"
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", insertable = false, updatable = false)
    private Prescription prescription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
    private Medicine medicine;
    
    @OneToMany(mappedBy = "prescriptionMedicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdministeredMedication> administeredMedications = new ArrayList<>();
    
    // Constructors
    public PrescriptionMedicine(Long prescriptionId, Long medicineId, String dosage, String frequency, LocalDate startDate) {
        this.prescriptionId = prescriptionId;
        this.medicineId = medicineId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public void addAdministration(AdministeredMedication administeredMedication) {
        administeredMedications.add(administeredMedication);
    }
    
    public void removeAdministration(AdministeredMedication administeredMedication) {
        administeredMedications.remove(administeredMedication);
    }
    
    public boolean hasBeenAdministered() {
        return !administeredMedications.isEmpty();
    }
    
    public int getAdministrationCount() {
        return administeredMedications.size();
    }
    
    public String getMedicineName() {
        return "Medicine";
    }
    
    public String getFullDescription() {
        return getMedicineName() + " - " + dosage + " - " + frequency;
    }
}
