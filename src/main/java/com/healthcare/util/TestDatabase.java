package com.healthcare.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.healthcare.config.DBConnection;
import com.healthcare.model.Staff;
import com.healthcare.services.StaffService;

/**
 * Simple test to verify database connection and staff data
 */
public class TestDatabase {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");
        
        // Test 1: Database Connection
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Database connection successful!");
            
            // Test 2: Check if Staff table exists and has data
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Staff");
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✅ Staff table exists with " + count + " records");
            }
            
            // Test 3: List all staff
            rs = stmt.executeQuery("SELECT * FROM Staff");
            System.out.println("\n=== Staff Records ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("staff_id") + 
                                 ", Username: " + rs.getString("username") + 
                                 ", Role: " + rs.getString("role"));
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 4: StaffService
        System.out.println("\n=== Testing StaffService ===");
        try {
            StaffService staffService = new StaffService();
            List<Staff> allStaff = staffService.findAll();
            System.out.println("✅ StaffService found " + allStaff.size() + " staff members");
            
            for (Staff staff : allStaff) {
                System.out.println("Staff: " + staff.getUsername() + " (" + staff.getRole() + ")");
            }
            
        } catch (Exception e) {
            System.err.println("❌ StaffService failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
