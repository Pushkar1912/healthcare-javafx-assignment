package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Staff Entity - Represents staff members (Manager, Doctor, Nurse)
 * Maps to the Staff table in the database
 */
@Entity
@Table(name = "Staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "is_active")
    private boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions = new ArrayList<>();
    
    @OneToMany(mappedBy = "nurse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdministeredMedication> administeredMedications = new ArrayList<>();
    
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ActionLog> actionLogs = new ArrayList<>();
    
    // Custom constructors for business logic
    public Staff(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public Staff(String username, String password, Role role, String firstName, String lastName, String email, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getter methods (manually added since Lombok might not be working)
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public Role getRole() {
        return role;
    }
    
    public Long getStaffId() {
        return staffId;
    }
    
    // Setter methods
    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    // Utility methods
    public boolean isManager() {
        return role == Role.Manager;
    }
    
    public boolean isDoctor() {
        return role == Role.Doctor;
    }
    
    public boolean isNurse() {
        return role == Role.Nurse;
    }
    
    public boolean canAddPrescriptions() {
        return isDoctor();
    }
    
    public boolean canAdministerMedication() {
        return isNurse();
    }
    
    public boolean canManageStaff() {
        return isManager();
    }
    
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
    
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.equals(username) ? username : fullName + " (" + username + ")";
    }
    
    // Additional getters and setters for new fields
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Role Enum
    public enum Role {
        Manager, Doctor, Nurse
    }
}
