package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.ActionLog;
import com.healthcare.model.Staff;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing ActionLog entries
 * Simplified to handle only essential action logging
 */
public class ActionLogService {
    
    /**
     * Save action log
     */
    public ActionLog save(ActionLog actionLog) {
        String sql = "INSERT INTO Actions_Log (staff_id, action_type, action_description, action_time, details) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setObject(1, actionLog.getStaffId());
            stmt.setString(2, actionLog.getActionType().name());
            stmt.setString(3, actionLog.getActionDescription());
            stmt.setTimestamp(4, Timestamp.valueOf(actionLog.getActionTime()));
            stmt.setString(5, actionLog.getDetails());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        actionLog.setActionId(generatedKeys.getLong(1));
                    }
                }
                return actionLog;
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving action log: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find action log by ID
     */
    public ActionLog findById(Long id) {
        String sql = "SELECT * FROM Actions_Log WHERE action_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToActionLog(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding action log by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find all action logs (simple version without joins)
     */
    public List<ActionLog> findAll() {
        String sql = "SELECT * FROM Actions_Log ORDER BY action_time DESC";
        
        List<ActionLog> actionLogs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                actionLogs.add(mapResultSetToActionLogSimple(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all action logs: " + e.getMessage());
        }
        
        return actionLogs;
    }
    
    /**
     * Find action logs by date range
     */
    public List<ActionLog> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT al.*, s.first_name, s.last_name, s.role FROM Actions_Log al " +
                    "LEFT JOIN Staff s ON al.staff_id = s.staff_id " +
                    "WHERE DATE(al.action_time) BETWEEN ? AND ? " +
                    "ORDER BY al.action_time DESC";
        
        List<ActionLog> actionLogs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                actionLogs.add(mapResultSetToActionLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding action logs by date range: " + e.getMessage());
        }
        
        return actionLogs;
    }
    
    /**
     * Find action logs by staff ID
     */
    public List<ActionLog> findByStaffId(Long staffId) {
        String sql = "SELECT al.*, s.first_name, s.last_name, s.role FROM Actions_Log al " +
                    "LEFT JOIN Staff s ON al.staff_id = s.staff_id " +
                    "WHERE al.staff_id = ? " +
                    "ORDER BY al.action_time DESC";
        
        List<ActionLog> actionLogs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, staffId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                actionLogs.add(mapResultSetToActionLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding action logs by staff ID: " + e.getMessage());
        }
        
        return actionLogs;
    }
    
    /**
     * Find action logs by action type
     */
    public List<ActionLog> findByActionType(ActionLog.ActionType actionType) {
        String sql = "SELECT al.*, s.first_name, s.last_name, s.role FROM Actions_Log al " +
                    "LEFT JOIN Staff s ON al.staff_id = s.staff_id " +
                    "WHERE al.action_type = ? " +
                    "ORDER BY al.action_time DESC";
        
        List<ActionLog> actionLogs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, actionType.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                actionLogs.add(mapResultSetToActionLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding action logs by action type: " + e.getMessage());
        }
        
        return actionLogs;
    }
    
    /**
     * Delete action log by ID
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM Actions_Log WHERE action_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting action log: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to ActionLog object (simple version without joins)
     */
    private ActionLog mapResultSetToActionLogSimple(ResultSet rs) throws SQLException {
        ActionLog actionLog = new ActionLog(
                rs.getObject("staff_id", Long.class),
                ActionLog.ActionType.valueOf(rs.getString("action_type")),
                rs.getString("action_description"),
                rs.getString("details")
        );

        actionLog.setActionId(rs.getLong("action_id"));
        java.sql.Timestamp timestamp = rs.getTimestamp("action_time");
        if (timestamp != null) {
            actionLog.setActionTime(timestamp.toLocalDateTime());
        }

        return actionLog;
    }

    /**
     * Find recent action logs (for testing and dashboard)
     */
    public List<ActionLog> findRecentLogs(int limit) {
        String sql = "SELECT * FROM Actions_Log ORDER BY action_time DESC LIMIT ?";
        
        List<ActionLog> actionLogs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                actionLogs.add(mapResultSetToActionLogSimple(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding recent action logs: " + e.getMessage());
        }
        
        return actionLogs;
    }
    
    /**
     * Map ResultSet to ActionLog object
     */
    private ActionLog mapResultSetToActionLog(ResultSet rs) throws SQLException {
        // Create ActionLog with required parameters
        ActionLog actionLog = new ActionLog(
            rs.getObject("staff_id", Long.class),
            ActionLog.ActionType.valueOf(rs.getString("action_type")),
            rs.getString("action_description"),
            rs.getString("details")
        );
        
        // Set additional fields
        actionLog.setActionId(rs.getLong("action_id"));
        
        // Handle action_time - it might be null
        java.sql.Timestamp timestamp = rs.getTimestamp("action_time");
        if (timestamp != null) {
            actionLog.setActionTime(timestamp.toLocalDateTime());
        }
        
        return actionLog;
    }
}