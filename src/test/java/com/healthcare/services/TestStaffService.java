package com.healthcare.services;

import com.healthcare.config.TestDBConnection;
import com.healthcare.model.Staff;
import com.healthcare.services.impl.IStaffService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test StaffService implementation using H2 test database
 */
public class TestStaffService implements IStaffService {

    @Override
    public Staff save(Staff staff) {
        String sql = "INSERT INTO Staff (username, password, role, first_name, last_name, email, phone, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, staff.getUsername());
            stmt.setString(2, staff.getPassword());
            stmt.setString(3, staff.getRole().toString());
            stmt.setString(4, staff.getFirstName());
            stmt.setString(5, staff.getLastName());
            stmt.setString(6, staff.getEmail());
            stmt.setString(7, staff.getPhone());
            stmt.setBoolean(8, staff.isActive());
            // Handle null createdAt by using current timestamp
            if (staff.getCreatedAt() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(staff.getCreatedAt()));
            } else {
                stmt.setTimestamp(9, Timestamp.valueOf(java.time.LocalDateTime.now()));
            }
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    staff.setStaffId(generatedKeys.getLong(1));
                }
            }
            return staff;
            
        } catch (SQLException e) {
            System.err.println("Error saving staff: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<Staff> findById(Long id) {
        String sql = "SELECT * FROM Staff WHERE staff_id = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToStaff(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding staff by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<Staff> findAll() {
        String sql = "SELECT * FROM Staff ORDER BY staff_id";
        List<Staff> staffList = new ArrayList<>();
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                staffList.add(mapResultSetToStaff(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all staff: " + e.getMessage());
        }
        
        return staffList;
    }

    @Override
    public Staff update(Staff staff) {
        String sql = "UPDATE Staff SET username = ?, password = ?, role = ?, first_name = ?, last_name = ?, email = ?, phone = ?, is_active = ?, updated_at = ? WHERE staff_id = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, staff.getUsername());
            stmt.setString(2, staff.getPassword());
            stmt.setString(3, staff.getRole().toString());
            stmt.setString(4, staff.getFirstName());
            stmt.setString(5, staff.getLastName());
            stmt.setString(6, staff.getEmail());
            stmt.setString(7, staff.getPhone());
            stmt.setBoolean(8, staff.isActive());
            stmt.setTimestamp(9, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setLong(10, staff.getStaffId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return staff;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating staff: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Staff WHERE staff_id = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting staff: " + e.getMessage());
        }
    }

    @Override
    public Optional<Staff> authenticate(String username, String password) {
        String sql = "SELECT * FROM Staff WHERE username = ? AND password = ?";
        
        try (Connection conn = TestDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToStaff(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error authenticating staff: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Map ResultSet to Staff object
     */
    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setStaffId(rs.getLong("staff_id"));
        staff.setUsername(rs.getString("username"));
        staff.setPassword(rs.getString("password"));
        staff.setRole(Staff.Role.valueOf(rs.getString("role")));
        staff.setFirstName(rs.getString("first_name"));
        staff.setLastName(rs.getString("last_name"));
        staff.setEmail(rs.getString("email"));
        staff.setPhone(rs.getString("phone"));
        staff.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            staff.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return staff;
    }
}
