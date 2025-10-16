package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.Bed;
import com.healthcare.model.Resident;
import com.healthcare.services.impl.IBedManagementService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Bed Management Service Implementation
 * Handles complex bed assignment logic based on healthcare requirements
 */
public class BedManagementService implements IBedManagementService {

    @Override
    public Bed save(Bed bed) {
        String sql = "INSERT INTO Beds (room_id, bed_number, bed_code, bed_type, is_occupied, occupied_by, gender_restriction, isolation_required) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, bed.getRoomId());
            stmt.setString(2, bed.getBedNumber());
            stmt.setString(3, bed.getBedCode());
            stmt.setString(4, bed.getBedType().name());
            stmt.setBoolean(5, bed.isOccupied());
            stmt.setLong(6, bed.getOccupiedBy() != null ? bed.getOccupiedBy() : 0);
            stmt.setString(7, bed.getGenderRestriction().name());
            stmt.setBoolean(8, bed.isIsolationRequired());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bed.setBedId(generatedKeys.getLong(1));
                }
            }
            return bed;
            
        } catch (SQLException e) {
            System.err.println("Error saving bed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<Bed> findById(Long id) {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE b.bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding bed by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<Bed> findAll() {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "ORDER BY r.ward_id, r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all beds: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Beds WHERE bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting bed: " + e.getMessage());
        }
    }

    @Override
    public Bed update(Bed bed) {
        String sql = "UPDATE Beds SET room_id = ?, bed_number = ?, bed_type = ?, is_occupied = ?, " +
                    "occupied_by = ?, gender_restriction = ?, isolation_required = ? WHERE bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bed.getRoomId());
            stmt.setString(2, bed.getBedNumber());
            stmt.setString(3, bed.getBedType().name());
            stmt.setBoolean(4, bed.isOccupied());
            stmt.setLong(5, bed.getOccupiedBy() != null ? bed.getOccupiedBy() : 0);
            stmt.setString(6, bed.getGenderRestriction().name());
            stmt.setBoolean(7, bed.isIsolationRequired());
            stmt.setLong(8, bed.getBedId());
            
            stmt.executeUpdate();
            return bed;
            
        } catch (SQLException e) {
            System.err.println("Error updating bed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Bed> findByWard(String wardName) {
        // Since we only have 2 fixed wards, we'll use ward_id instead
        int wardId = wardName.equals("Ward 1") ? 1 : 2;
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE r.ward_id = ? ORDER BY r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, wardId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding beds by ward: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findByRoom(Long roomId) {
        String sql = "SELECT b.*, r.room_number, r.ward_id, w.ward_name FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "JOIN Wards w ON r.ward_id = w.ward_id " +
                    "WHERE b.room_id = ? ORDER BY b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding beds by room: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findAvailableBeds() {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE b.is_occupied = FALSE ORDER BY r.ward_id, r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding available beds: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findAvailableBedsByWard(String wardName) {
        int wardId = wardName.equals("Ward 1") ? 1 : 2;
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE r.ward_id = ? AND b.is_occupied = FALSE " +
                    "ORDER BY r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, wardId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding available beds by ward: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findSuitableBeds(Resident resident) {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE b.is_occupied = FALSE " +
                    "AND (b.gender_restriction = 'None' OR b.gender_restriction = ?) " +
                    "AND (b.isolation_required = ? OR ? = FALSE) " +
                    "ORDER BY r.ward_id, r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, resident.getGender().name());
            stmt.setBoolean(2, resident.isRequiresIsolation());
            stmt.setBoolean(3, resident.isRequiresIsolation());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding suitable beds: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findBedsForGender(Resident.Gender gender) {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE b.is_occupied = FALSE " +
                    "AND (b.gender_restriction = 'None' OR b.gender_restriction = ?) " +
                    "ORDER BY r.ward_id, r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, gender.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding beds for gender: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findIsolationBeds() {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE b.isolation_required = TRUE AND b.is_occupied = FALSE " +
                    "ORDER BY r.ward_id, r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding isolation beds: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public List<Bed> findStandardBeds() {
        String sql = "SELECT b.*, r.room_number, r.ward_id FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE b.bed_type = 'Standard' AND b.is_occupied = FALSE " +
                    "ORDER BY r.ward_id, r.room_number, b.bed_number";
        List<Bed> bedList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                bedList.add(mapResultSetToBed(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding standard beds: " + e.getMessage());
        }
        
        return bedList;
    }

    @Override
    public boolean assignResidentToBed(Long bedId, Long residentId) {
        String sql = "UPDATE Beds SET is_occupied = TRUE, occupied_by = ? WHERE bed_id = ? AND is_occupied = FALSE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            stmt.setLong(2, bedId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error assigning resident to bed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean assignResidentToSuitableBed(Resident resident) {
        List<Bed> suitableBeds = findSuitableBeds(resident);
        
        if (suitableBeds.isEmpty()) {
            return false;
        }
        
        // Assign to first suitable bed
        Bed selectedBed = suitableBeds.get(0);
        return assignResidentToBed(selectedBed.getBedId(), resident.getResidentId());
    }

    @Override
    public void unassignBed(Long bedId) {
        String sql = "UPDATE Beds SET is_occupied = FALSE, occupied_by = NULL WHERE bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bedId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error unassigning bed: " + e.getMessage());
        }
    }

    @Override
    public boolean isBedSuitableForResident(Long bedId, Resident resident) {
        Optional<Bed> bedOpt = findById(bedId);
        if (!bedOpt.isPresent() || bedOpt.get().isOccupied()) {
            return false;
        }
        
        Bed bed = bedOpt.get();
        
        // Check gender restriction
        if (bed.getGenderRestriction() != Bed.GenderRestriction.None && 
            bed.getGenderRestriction().name().equals(resident.getGender().name())) {
            return false;
        }
        
        // Check isolation requirement
        if (resident.isRequiresIsolation() && !bed.isIsolationRequired()) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean validateBedAssignment(Long bedId, Long residentId) {
        String sql = "SELECT COUNT(*) FROM Beds WHERE bed_id = ? AND is_occupied = FALSE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bedId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error validating bed assignment: " + e.getMessage());
        }
        
        return false;
    }

    @Override
    public int getTotalBeds() {
        String sql = "SELECT COUNT(*) FROM Beds";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total beds: " + e.getMessage());
        }
        
        return 0;
    }

    @Override
    public int getAvailableBeds() {
        String sql = "SELECT COUNT(*) FROM Beds WHERE is_occupied = FALSE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available beds: " + e.getMessage());
        }
        
        return 0;
    }

    @Override
    public int getOccupiedBeds() {
        String sql = "SELECT COUNT(*) FROM Beds WHERE is_occupied = TRUE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting occupied beds: " + e.getMessage());
        }
        
        return 0;
    }

    @Override
    public int getBedsByWard(String wardName) {
        int wardId = wardName.equals("Ward 1") ? 1 : 2;
        String sql = "SELECT COUNT(*) FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE r.ward_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, wardId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting beds by ward: " + e.getMessage());
        }
        
        return 0;
    }

    @Override
    public int getAvailableBedsByWard(String wardName) {
        int wardId = wardName.equals("Ward 1") ? 1 : 2;
        String sql = "SELECT COUNT(*) FROM Beds b " +
                    "JOIN Rooms r ON b.room_id = r.room_id " +
                    "WHERE r.ward_id = ? AND b.is_occupied = FALSE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, wardId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available beds by ward: " + e.getMessage());
        }
        
        return 0;
    }

    private Bed mapResultSetToBed(ResultSet rs) throws SQLException {
        Bed bed = new Bed();
        bed.setBedId(rs.getLong("bed_id"));
        bed.setRoomId(rs.getLong("room_id"));
        bed.setBedNumber(rs.getString("bed_number"));
        bed.setBedCode(rs.getString("bed_code"));
        bed.setBedType(Bed.BedType.valueOf(rs.getString("bed_type")));
        bed.setOccupied(rs.getBoolean("is_occupied"));
        
        long occupiedBy = rs.getLong("occupied_by");
        bed.setOccupiedBy(occupiedBy > 0 ? occupiedBy : null);
        
        bed.setGenderRestriction(Bed.GenderRestriction.valueOf(rs.getString("gender_restriction")));
        bed.setIsolationRequired(rs.getBoolean("isolation_required"));
        
        // Set additional info from joins
        bed.setRoomNumber(rs.getString("room_number"));
        
        return bed;
    }
}
