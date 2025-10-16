package com.healthcare.services;

import com.healthcare.config.TestDBConnection;
import com.healthcare.model.Bed;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test BedManagementService implementation using H2 test database
 */
public class TestBedManagementService {

    public List<Bed> findAvailableBeds() {
        String sql = "SELECT * FROM Beds WHERE is_occupied = FALSE ORDER BY bed_id";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding available beds: " + e.getMessage());
        }
        
        return bedList;
    }

    public boolean assignResidentToBed(Long bedId, Long residentId) {
        // First check if bed is available
        String checkSql = "SELECT is_occupied FROM Beds WHERE bed_id = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setLong(1, bedId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getBoolean("is_occupied")) {
                return false; // Bed is already occupied
            }
            
            // Update bed to occupied
            String updateBedSql = "UPDATE Beds SET is_occupied = TRUE WHERE bed_id = ?";
            try (PreparedStatement updateBedStmt = conn.prepareStatement(updateBedSql)) {
                updateBedStmt.setLong(1, bedId);
                int bedRows = updateBedStmt.executeUpdate();
                
                if (bedRows > 0) {
                    // Update resident's current bed
                    String updateResidentSql = "UPDATE Residents SET current_bed_id = ? WHERE resident_id = ?";
                    try (PreparedStatement updateResidentStmt = conn.prepareStatement(updateResidentSql)) {
                        updateResidentStmt.setLong(1, bedId);
                        updateResidentStmt.setLong(2, residentId);
                        int residentRows = updateResidentStmt.executeUpdate();
                        return residentRows > 0;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error assigning resident to bed: " + e.getMessage());
        }
        
        return false;
    }

    public boolean releaseBed(Long bedId) {
        String sql = "UPDATE Beds SET is_occupied = FALSE WHERE bed_id = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bedId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error releasing bed: " + e.getMessage());
            return false;
        }
    }

    public List<Bed> findAllBeds() {
        String sql = "SELECT * FROM Beds ORDER BY bed_id";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all beds: " + e.getMessage());
        }
        
        return bedList;
    }

    public Bed findBedById(Long bedId) {
        String sql = "SELECT * FROM Beds WHERE bed_id = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bedId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBed(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding bed by ID: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Map ResultSet to Bed object
     */
    private Bed mapResultSetToBed(ResultSet rs) throws SQLException {
        Bed bed = new Bed();
        bed.setBedId(rs.getLong("bed_id"));
        bed.setBedNumber(rs.getString("bed_number"));
        bed.setRoomId(rs.getLong("room_id"));
        bed.setBedType(Bed.BedType.valueOf(rs.getString("bed_type")));
        bed.setGenderRestriction(Bed.GenderRestriction.valueOf(rs.getString("gender_restriction")));
        bed.setOccupied(rs.getBoolean("is_occupied"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            bed.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return bed;
    }
}
