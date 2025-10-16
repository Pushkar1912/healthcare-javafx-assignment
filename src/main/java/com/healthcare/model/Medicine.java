package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Medicine Entity - Represents available medicines in the system
 * Maps to the Medicines table in the database
 */
@Entity
@Table(name = "Medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "dosage_unit")
    private String dosageUnit = "mg";
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "classification")
    private String classification;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrescriptionMedicine> prescriptionMedicines = new ArrayList<>();
    
    // Custom constructors for business logic
    
    public Medicine(String name) {
        this.name = name;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public Medicine(String name, String description, String dosageUnit) {
        this.name = name;
        this.description = description;
        this.dosageUnit = dosageUnit;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public Medicine(String name, String description, String dosageUnit, String category, String classification) {
        this.name = name;
        this.description = description;
        this.dosageUnit = dosageUnit;
        this.category = category;
        this.classification = classification;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public void addToPrescription(PrescriptionMedicine prescriptionMedicine) {
        prescriptionMedicines.add(prescriptionMedicine);
    }
    
    public void removeFromPrescription(PrescriptionMedicine prescriptionMedicine) {
        prescriptionMedicines.remove(prescriptionMedicine);
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDosageUnit() { return dosageUnit; }
    public void setDosageUnit(String dosageUnit) { this.dosageUnit = dosageUnit; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
