package com.healthcare.services.impl;

import com.healthcare.model.Bed;
import com.healthcare.model.Resident;

import java.util.List;
import java.util.Optional;

/**
 * Enhanced Bed Management Service Interface
 * Handles complex bed assignment logic based on requirements
 */
public interface IBedManagementService {

    // Basic CRUD operations
    Bed save(Bed bed);
    Optional<Bed> findById(Long id);
    List<Bed> findAll();
    void deleteById(Long id);
    Bed update(Bed bed);
    
    // Ward and Room management
    List<Bed> findByWard(String wardName);
    List<Bed> findByRoom(Long roomId);
    List<Bed> findAvailableBeds();
    List<Bed> findAvailableBedsByWard(String wardName);
    
    // Advanced bed assignment logic
    List<Bed> findSuitableBeds(Resident resident);
    List<Bed> findBedsForGender(Resident.Gender gender);
    List<Bed> findIsolationBeds();
    List<Bed> findStandardBeds();
    
    // Bed assignment with business rules
    boolean assignResidentToBed(Long bedId, Long residentId);
    boolean assignResidentToSuitableBed(Resident resident);
    void unassignBed(Long bedId);
    
    // Compliance and validation
    boolean isBedSuitableForResident(Long bedId, Resident resident);
    boolean validateBedAssignment(Long bedId, Long residentId);
    
    // Statistics and reporting
    int getTotalBeds();
    int getAvailableBeds();
    int getOccupiedBeds();
    int getBedsByWard(String wardName);
    int getAvailableBedsByWard(String wardName);
}
