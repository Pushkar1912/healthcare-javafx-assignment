package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ward Entity - Represents a ward in the healthcare facility
 * Maps to the Wards table in the database
 * Note: Only 2 fixed wards (Ward 1 & Ward 2) as per requirements
 */
@Entity
@Table(name = "Wards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Long wardId;
    
    @Column(name = "ward_name", nullable = false, unique = true)
    private String wardName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Custom constructors for business logic
    
    public Ward(String wardName, String description) {
        this.wardName = wardName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getDisplayName() {
        return wardName + (description != null ? " - " + description : "");
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
    
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
