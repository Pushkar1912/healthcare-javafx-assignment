package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.Shift;
import com.healthcare.model.ShiftSchedule;
import com.healthcare.model.Staff;
import com.healthcare.exceptions.ShiftComplianceException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shift Management Service - Simple MVP implementation
 * Handles shift scheduling and compliance validation
 */
public class ShiftManagementService {
    
    /**
     * Save a new shift schedule
     */
    public ShiftSchedule save(ShiftSchedule schedule) {
        String sql = "INSERT INTO Shift_Schedule (staff_id, shift_date, shift_type, start_time, end_time, ward_id, status, assigned_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, schedule.getStaffId());
            stmt.setDate(2, Date.valueOf(schedule.getShiftDate()));
            stmt.setString(3, schedule.getShiftType().name());
            stmt.setString(4, schedule.getStartTime());
            stmt.setString(5, schedule.getEndTime());
            if (schedule.getWardId() != null) {
                stmt.setLong(6, schedule.getWardId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setString(7, schedule.getStatus().name());
            if (schedule.getAssignedBy() != null) {
                stmt.setLong(8, schedule.getAssignedBy());
            } else {
                stmt.setNull(8, java.sql.Types.INTEGER);
            }
            stmt.setTimestamp(9, Timestamp.valueOf(schedule.getCreatedAt()));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    schedule.setScheduleId(generatedKeys.getLong(1));
                }
            }
            return schedule;
            
        } catch (SQLException e) {
            System.err.println("Error saving shift schedule: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find shift schedule by ID
     */
    public Optional<ShiftSchedule> findById(Long id) {
        String sql = "SELECT ss.*, s.first_name, s.last_name, s.role FROM Shift_Schedule ss " +
                    "JOIN Staff s ON ss.staff_id = s.staff_id " +
                    "WHERE ss.shift_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToShiftSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding shift schedule by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find all shift schedules
     */
    public List<ShiftSchedule> findAll() {
        String sql = "SELECT ss.*, s.first_name, s.last_name, s.role FROM Shift_Schedule ss " +
                    "JOIN Staff s ON ss.staff_id = s.staff_id " +
                    "ORDER BY ss.shift_date DESC, ss.start_time";
        List<ShiftSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                schedules.add(mapResultSetToShiftSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all shift schedules: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Find shifts by date range
     */
    public List<ShiftSchedule> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT ss.*, s.first_name, s.last_name, s.role FROM Shift_Schedule ss " +
                    "JOIN Staff s ON ss.staff_id = s.staff_id " +
                    "WHERE ss.shift_date BETWEEN ? AND ? " +
                    "ORDER BY ss.shift_date, ss.start_time";
        List<ShiftSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapResultSetToShiftSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding shifts by date range: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Find shifts for a specific staff member on a specific date
     */
    public List<ShiftSchedule> findByStaffAndDate(Long staffId, LocalDate shiftDate) {
        String sql = "SELECT ss.*, s.first_name, s.last_name, s.role FROM Shift_Schedule ss " +
                    "JOIN Staff s ON ss.staff_id = s.staff_id " +
                    "WHERE ss.staff_id = ? AND ss.shift_date = ? " +
                    "ORDER BY ss.start_time";
        List<ShiftSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, staffId);
            stmt.setDate(2, Date.valueOf(shiftDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapResultSetToShiftSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding shifts by staff and date: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Find shifts for a specific staff member
     */
    public List<ShiftSchedule> findByStaffId(Long staffId) {
        String sql = "SELECT ss.*, s.first_name, s.last_name, s.role FROM Shift_Schedule ss " +
                    "JOIN Staff s ON ss.staff_id = s.staff_id " +
                    "WHERE ss.staff_id = ? " +
                    "ORDER BY ss.shift_date DESC";
        List<ShiftSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, staffId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapResultSetToShiftSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding shifts by staff ID: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Find shifts for a specific date
     */
    public List<ShiftSchedule> findByDate(LocalDate date) {
        String sql = "SELECT ss.*, s.first_name, s.last_name, s.role FROM Shift_Schedule ss " +
                    "JOIN Staff s ON ss.staff_id = s.staff_id " +
                    "WHERE ss.shift_date = ? " +
                    "ORDER BY ss.start_time";
        List<ShiftSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapResultSetToShiftSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding shifts by date: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Delete shift schedule
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM Shift_Schedule WHERE shift_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting shift schedule: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if staff is available for a specific shift
     */
    public boolean isStaffAvailable(Long staffId, LocalDate date, Shift.ShiftType shiftType) {
        String sql = "SELECT COUNT(*) FROM Shift_Schedule WHERE staff_id = ? AND shift_date = ? AND shift_type = ? AND status = 'Scheduled'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, staffId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, shiftType.name());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking staff availability: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Simple compliance check - throws exception if rules are violated
     */
    public void checkCompliance(LocalDate weekStart) throws ShiftComplianceException {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<ShiftSchedule> weekShifts = findByDateRange(weekStart, weekEnd);
        
        // Check nurse shifts (should be 14 per week - 2 per day)
        long nurseShifts = weekShifts.stream()
            .filter(s -> s.getShiftType() == Shift.ShiftType.Morning || s.getShiftType() == Shift.ShiftType.Afternoon)
            .count();
        
        if (nurseShifts != 14) {
            throw new ShiftComplianceException("Nurse shifts must be exactly 14 per week (2 per day). Found: " + nurseShifts);
        }
        
        // Check doctor shifts (should be 7 per week - 1 per day)
        long doctorShifts = weekShifts.stream()
            .filter(s -> s.getShiftType() == Shift.ShiftType.Doctor)
            .count();
        
        if (doctorShifts != 7) {
            throw new ShiftComplianceException("Doctor shifts must be exactly 7 per week (1 per day). Found: " + doctorShifts);
        }
        
        // Check daily hours for each staff member
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            checkDailyHours(date, weekShifts);
        }
    }
    
    /**
     * Check daily hours for a specific date
     */
    private void checkDailyHours(LocalDate date, List<ShiftSchedule> allShifts) {
        List<ShiftSchedule> dayShifts = allShifts.stream()
            .filter(s -> s.getShiftDate().equals(date))
            .toList();
        
        // Group by staff member
        dayShifts.stream()
            .collect(java.util.stream.Collectors.groupingBy(ShiftSchedule::getStaffId))
            .forEach((staffId, shifts) -> {
                int totalHours = shifts.stream()
                    .mapToInt(s -> calculateShiftHours(s.getStartTime(), s.getEndTime()))
                    .sum();
                
                if (totalHours > 8) {
                    try {
                        throw new ShiftComplianceException("Staff member " + staffId + " has more than 8 hours on " + date);
                    } catch (ShiftComplianceException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }
    
    /**
     * Calculate shift hours from time strings
     */
    private int calculateShiftHours(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int endHour = Integer.parseInt(endParts[0]);
            
            return endHour - startHour;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Map ResultSet to ShiftSchedule object
     */
    private ShiftSchedule mapResultSetToShiftSchedule(ResultSet rs) throws SQLException {
        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setScheduleId(rs.getLong("shift_id"));
        schedule.setStaffId(rs.getLong("staff_id"));
        schedule.setShiftDate(rs.getDate("shift_date").toLocalDate());
        schedule.setShiftType(Shift.ShiftType.valueOf(rs.getString("shift_type")));
        schedule.setStartTime(rs.getString("start_time"));
        schedule.setEndTime(rs.getString("end_time"));
        schedule.setWardId(rs.getLong("ward_id"));
        schedule.setStatus(ShiftSchedule.ScheduleStatus.valueOf(rs.getString("status")));
        schedule.setAssignedBy(rs.getLong("assigned_by"));
        schedule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // Set staff info if available
        if (rs.getString("first_name") != null) {
            Staff staff = new Staff();
            staff.setStaffId(rs.getLong("staff_id"));
            staff.setFirstName(rs.getString("first_name"));
            staff.setLastName(rs.getString("last_name"));
            staff.setRole(Staff.Role.valueOf(rs.getString("role")));
            schedule.setStaff(staff);
        }
        
        return schedule;
    }
    
    /**
     * Custom exception for compliance violations
     */
    public static class ComplianceException extends Exception {
        public ComplianceException(String message) {
            super(message);
        }
    }
}
