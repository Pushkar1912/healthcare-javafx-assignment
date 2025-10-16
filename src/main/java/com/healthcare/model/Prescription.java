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
 * Prescription Entity - Represents a prescription written by a doctor for a resident
 * Maps to the Prescriptions table in the database
 */
@Entity
@Table(name = "Prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;
    
    @Column(name = "resident_id", nullable = false)
    private Long residentId;
    
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;
    
    @Column(name = "prescription_date", nullable = false)
    private LocalDate prescriptionDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PrescriptionStatus status = PrescriptionStatus.Active;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus = ReviewStatus.Pending;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Staff doctor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", insertable = false, updatable = false)
    private Staff reviewer;
    
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrescriptionMedicine> prescriptionMedicines = new ArrayList<>();
    
    // Enums
    public enum PrescriptionStatus {
        Active, Completed, Cancelled
    }
    
    public enum ReviewStatus {
        Pending, Reviewed, Approved, Rejected
    }
    
    // Custom constructors for business logic
    
    public Prescription(Long residentId, Long doctorId, LocalDate prescriptionDate, String notes) {
        this.residentId = residentId;
        this.doctorId = doctorId;
        this.prescriptionDate = prescriptionDate;
        this.notes = notes;
        this.status = PrescriptionStatus.Active;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public void addMedicine(PrescriptionMedicine prescriptionMedicine) {
        prescriptionMedicines.add(prescriptionMedicine);
    }
    
    public void removeMedicine(PrescriptionMedicine prescriptionMedicine) {
        prescriptionMedicines.remove(prescriptionMedicine);
    }
    
    public boolean hasMedicines() {
        return !prescriptionMedicines.isEmpty();
    }
    
    public int getMedicineCount() {
        return prescriptionMedicines.size();
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    
    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    
    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }
    
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Get patient name from resident relationship
    public String getPatientName() {
        if (resident != null) {
            return resident.getFirstName() + " " + resident.getLastName();
        }
        return "Unknown Patient";
    }
}
