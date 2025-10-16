package com.healthcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Room Entity - Represents a room within a ward
 * Maps to the Rooms table in the database
 */
@Entity
@Table(name = "Rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;
    
    @Column(name = "ward_id", nullable = false)
    private Long wardId;
    
    @Column(name = "room_number", nullable = false)
    private String roomNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private RoomType roomType;
    
    @Column(name = "max_capacity")
    private int maxCapacity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender_preference")
    private GenderPreference genderPreference;
    
    @Column(name = "is_active")
    private boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bed> beds = new ArrayList<>();
    
    // Enums
    public enum RoomType {
        Standard, Isolation, Special
    }
    
    public enum GenderPreference {
        Male, Female, Mixed
    }
    
    // Custom constructors for business logic
    
    public Room(Long wardId, String roomNumber, RoomType roomType, int maxCapacity) {
        this.wardId = wardId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.maxCapacity = maxCapacity;
        this.genderPreference = GenderPreference.Mixed;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility methods
    public int getTotalBeds() {
        return beds != null ? beds.size() : 0;
    }
    
    public int getAvailableBeds() {
        return beds != null ? (int) beds.stream()
            .filter(bed -> !bed.isOccupied())
            .count() : 0;
    }
    
    public int getOccupiedBeds() {
        return getTotalBeds() - getAvailableBeds();
    }
    
    public boolean isAtCapacity() {
        return getTotalBeds() >= maxCapacity;
    }
    
    public String getFullRoomName() {
        return "Room " + roomNumber;
    }
    
    // Manual getters and setters (since Lombok might not be working consistently)
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    
    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
    
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    
    public GenderPreference getGenderPreference() { return genderPreference; }
    public void setGenderPreference(GenderPreference genderPreference) { this.genderPreference = genderPreference; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Bed> getBeds() { return beds; }
    public void setBeds(List<Bed> beds) { this.beds = beds; }
}
