package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.ShiftSchedule;
import com.healthcare.model.Staff;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing shift schedule data
 */
public class ShiftService {

    /**
     * Find all shift schedules
     */
    public List<ShiftSchedule> findAll() {
        String sql = "SELECT s.*, st.first_name, st.last_name, st.role FROM Shift_Schedule s " +
                    "LEFT JOIN Staff st ON s.staff_id = st.staff_id " +
                    "ORDER BY s.shift_date DESC, s.start_time ASC";
        
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
     * Map ResultSet to ShiftSchedule object
     */
    private ShiftSchedule mapResultSetToShiftSchedule(ResultSet rs) throws SQLException {
        ShiftSchedule schedule = new ShiftSchedule();
        
        schedule.setScheduleId(rs.getLong("shift_id"));
        schedule.setStaffId(rs.getLong("staff_id"));
        schedule.setShiftDate(rs.getDate("shift_date").toLocalDate());
        schedule.setShiftType(com.healthcare.model.Shift.ShiftType.valueOf(rs.getString("shift_type")));
        schedule.setStartTime(rs.getString("start_time"));
        schedule.setEndTime(rs.getString("end_time"));
        schedule.setWardId(rs.getLong("ward_id"));
        schedule.setStatus(ShiftSchedule.ScheduleStatus.valueOf(rs.getString("status")));
        
        return schedule;
    }
}
