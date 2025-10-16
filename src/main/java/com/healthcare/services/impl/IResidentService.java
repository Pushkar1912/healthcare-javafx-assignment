package com.healthcare.services.impl;

import com.healthcare.model.Resident;

import java.util.List;
import java.util.Optional;

/**
 * Simple CRUD interface for Resident operations
 */
public interface IResidentService {

    // Basic CRUD operations
    Resident save(Resident resident);
    Optional<Resident> findById(Long id);
    List<Resident> findAll();
    void deleteById(Long id);
    Resident update(Resident resident);
    
    // Business operations
    List<Resident> findActiveResidents();
    List<Resident> findDischargedResidents();
    Resident admitResident(Resident resident, Long bedId);
    void dischargeResident(Long residentId);
    void assignBed(Long residentId, Long bedId);
    void unassignBed(Long residentId);
}
