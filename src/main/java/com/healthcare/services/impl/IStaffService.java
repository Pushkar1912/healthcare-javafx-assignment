package com.healthcare.services.impl;

import com.healthcare.model.Staff;

import java.util.List;
import java.util.Optional;

/**
 * Simple CRUD interface for Staff operations
 */
public interface IStaffService {

    // Basic CRUD operations
    Staff save(Staff staff);

    Optional<Staff> findById(Long id);

    List<Staff> findAll();

    void deleteById(Long id);
    
    //update staff 
    Staff update(Staff staff);
    // Authentication
    Optional<Staff> authenticate(String username, String password);
}
