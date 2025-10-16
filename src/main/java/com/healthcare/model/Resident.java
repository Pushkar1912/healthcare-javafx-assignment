package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resident Entity - Represents a high care patient in the care home
 * Maps to the Residents table in the database
 */
@Entity
@Table(name = "Residents")
@Data
@NoArgsConstructor
public class Resident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resident_id")
    private Long residentId;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;
    
    @Column(name = "discharge_date")
    private LocalDate dischargeDate;
    
    @Column(name = "current_bed_id")
    private Long currentBedId;
    
    @Column(name = "assigned_doctor_id")
    private Long assignedDoctorId;
    
    @Column(name = "medical_condition")
    private String medicalCondition;
    
    @Column(name = "requires_isolation")
    private boolean requiresIsolation;
    
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_bed_id", insertable = false, updatable = false)
    private Bed currentBed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id", insertable = false, updatable = false)
    private Staff assignedDoctor;
    
    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions = new ArrayList<>();
    
    // Custom constructors for business logic
    
    public Resident(String firstName, String lastName, Gender gender, LocalDate admissionDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.admissionDate = admissionDate;
        this.requiresIsolation = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Resident(String firstName, String lastName, Gender gender, LocalDate admissionDate, 
                   String medicalCondition, boolean requiresIsolation, String emergencyContact) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.admissionDate = admissionDate;
        this.medicalCondition = medicalCondition;
        this.requiresIsolation = requiresIsolation;
        this.emergencyContact = emergencyContact;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
    
    public boolean isDischarged() {
        return dischargeDate != null;
    }
    
    public void discharge() {
        this.dischargeDate = LocalDate.now();
        this.currentBedId = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateBed(Long newBedId) {
        this.currentBedId = newBedId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getStayDuration() {
        if (dischargeDate != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(admissionDate, dischargeDate);
        } else {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(admissionDate, LocalDate.now());
        }
    }
    
    public boolean isCurrentlyAdmitted() {
        return dischargeDate == null;
    }
    
    // Gender Enum
    public enum Gender {
        M, F
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    
    public LocalDate getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDate dischargeDate) { this.dischargeDate = dischargeDate; }
    
    public Long getCurrentBedId() { return currentBedId; }
    public void setCurrentBedId(Long currentBedId) { this.currentBedId = currentBedId; }
    
    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }
    
    public boolean isRequiresIsolation() { return requiresIsolation; }
    public void setRequiresIsolation(boolean requiresIsolation) { this.requiresIsolation = requiresIsolation; }
    
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getAssignedDoctorId() { return assignedDoctorId; }
    public void setAssignedDoctorId(Long assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }
    
    public Bed getCurrentBed() { return currentBed; }
    public void setCurrentBed(Bed currentBed) { this.currentBed = currentBed; }
    
    public Staff getAssignedDoctor() { return assignedDoctor; }
    public void setAssignedDoctor(Staff assignedDoctor) { this.assignedDoctor = assignedDoctor; }
}
