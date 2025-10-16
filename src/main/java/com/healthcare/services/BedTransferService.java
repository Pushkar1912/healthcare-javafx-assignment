package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.Bed;
import com.healthcare.model.Resident;
import com.healthcare.model.BedTransfer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing bed transfers by nurses
 */
public class BedTransferService {
    
    private BedManagementService bedManagementService = new BedManagementService();
    private ResidentService residentService = new ResidentService();
    
    /**
     * Get all available beds for transfer
     */
    public List<Bed> getAvailableBeds() {
        return bedManagementService.findAvailableBeds();
    }
    
    /**
     * Get available beds by ward
     */
    public List<Bed> getAvailableBedsByWard(String wardName) {
        return bedManagementService.findAvailableBedsByWard(wardName);
    }
    
    /**
     * Get bed by ID
     */
    public Bed getBedById(Long bedId) {
        return bedManagementService.findById(bedId).orElse(null);
    }
    
    /**
     * Get suitable beds for a specific resident
     */
    public List<Bed> getSuitableBedsForResident(Long residentId) {
        Optional<Resident> residentOpt = residentService.findById(residentId);
        if (residentOpt.isPresent()) {
            return bedManagementService.findSuitableBeds(residentOpt.get());
        }
        return new ArrayList<>();
    }
    
    /**
     * Transfer resident to a new bed
     */
    public boolean transferResident(Long residentId, Long newBedId, Long nurseId, String reason) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get current resident info
                Optional<Resident> residentOpt = residentService.findById(residentId);
                if (!residentOpt.isPresent()) {
                    throw new RuntimeException("Resident not found");
                }
                
                Resident resident = residentOpt.get();
                Long currentBedId = resident.getCurrentBedId();
                
                // Validate new bed is available
                Optional<Bed> newBedOpt = bedManagementService.findById(newBedId);
                if (!newBedOpt.isPresent()) {
                    throw new RuntimeException("New bed not found");
                }
                
                Bed newBed = newBedOpt.get();
                if (newBed.isOccupied()) {
                    throw new RuntimeException("New bed is already occupied");
                }
                
                // Check if bed is suitable for resident
                if (!bedManagementService.isBedSuitableForResident(newBedId, resident)) {
                    throw new RuntimeException("New bed is not suitable for this resident");
                }
                
                // Free current bed
                if (currentBedId != null && currentBedId > 0) {
                    bedManagementService.unassignBed(currentBedId);
                }
                
                // Assign new bed
                boolean assigned = bedManagementService.assignResidentToBed(newBedId, residentId);
                if (!assigned) {
                    throw new RuntimeException("Failed to assign resident to new bed");
                }
                
                // Update resident's current bed
                resident.setCurrentBedId(newBedId);
                residentService.update(resident);
                
                // Log the transfer
                logBedTransfer(residentId, currentBedId, newBedId, nurseId, reason);
                
                conn.commit();
                return true;
                
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error transferring resident: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get transfer history for a resident
     */
    public List<BedTransfer> getTransferHistory(Long residentId) {
        String sql = "SELECT * FROM Bed_Transfers WHERE resident_id = ? ORDER BY transfer_time DESC";
        
        List<BedTransfer> transfers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transfers.add(mapResultSetToBedTransfer(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting transfer history: " + e.getMessage());
        }
        
        return transfers;
    }
    
    /**
     * Get recent transfers by a nurse
     */
    public List<BedTransfer> getRecentTransfersByNurse(Long nurseId, int limit) {
        String sql = "SELECT * FROM Bed_Transfers WHERE nurse_id = ? ORDER BY transfer_time DESC LIMIT ?";
        
        List<BedTransfer> transfers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, nurseId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transfers.add(mapResultSetToBedTransfer(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting recent transfers by nurse: " + e.getMessage());
        }
        
        return transfers;
    }
    
    /**
     * Validate if a transfer is allowed
     */
    public TransferValidation validateTransfer(Long residentId, Long newBedId) {
        TransferValidation validation = new TransferValidation();
        validation.setValid(true);
        
        try {
            // Check if resident exists and is active
            Optional<Resident> residentOpt = residentService.findById(residentId);
            if (!residentOpt.isPresent()) {
                validation.setValid(false);
                validation.addError("Resident not found");
                return validation;
            }
            
            Resident resident = residentOpt.get();
            if (resident.getDischargeDate() != null) {
                validation.setValid(false);
                validation.addError("Cannot transfer discharged resident");
                return validation;
            }
            
            // Check if new bed exists and is available
            Optional<Bed> newBedOpt = bedManagementService.findById(newBedId);
            if (!newBedOpt.isPresent()) {
                validation.setValid(false);
                validation.addError("New bed not found");
                return validation;
            }
            
            Bed newBed = newBedOpt.get();
            if (newBed.isOccupied()) {
                validation.setValid(false);
                validation.addError("New bed is already occupied");
                return validation;
            }
            
            // Check if bed is suitable for resident
            if (!bedManagementService.isBedSuitableForResident(newBedId, resident)) {
                validation.setValid(false);
                validation.addError("New bed is not suitable for this resident");
                return validation;
            }
            
            // Check if resident is already in this bed
            if (resident.getCurrentBedId() != null && resident.getCurrentBedId().equals(newBedId)) {
                validation.setValid(false);
                validation.addError("Resident is already in this bed");
                return validation;
            }
            
        } catch (Exception e) {
            validation.setValid(false);
            validation.addError("Error validating transfer: " + e.getMessage());
        }
        
        return validation;
    }
    
    /**
     * Get transfer statistics
     */
    public TransferStats getTransferStats() {
        TransferStats stats = new TransferStats();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total transfers today
            String todaySql = "SELECT COUNT(*) FROM Bed_Transfers WHERE DATE(transfer_time) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(todaySql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.transfersToday = rs.getInt(1);
                }
            }
            
            // Total transfers this week
            String weekSql = "SELECT COUNT(*) FROM Bed_Transfers WHERE transfer_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            try (PreparedStatement stmt = conn.prepareStatement(weekSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.transfersThisWeek = rs.getInt(1);
                }
            }
            
            // Pending transfers (if any)
            stats.pendingTransfers = 0; // No pending transfers in current implementation
            
        } catch (SQLException e) {
            System.err.println("Error getting transfer stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    // Helper methods
    private void logBedTransfer(Long residentId, Long fromBedId, Long toBedId, Long nurseId, String reason) {
        String sql = "INSERT INTO Bed_Transfers (resident_id, from_bed_id, to_bed_id, nurse_id, transfer_time, reason, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            stmt.setObject(2, fromBedId);
            stmt.setLong(3, toBedId);
            stmt.setLong(4, nurseId);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(6, reason);
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging bed transfer: " + e.getMessage());
        }
    }
    
    private BedTransfer mapResultSetToBedTransfer(ResultSet rs) throws SQLException {
        BedTransfer transfer = new BedTransfer();
        
        transfer.setTransferId(rs.getLong("transfer_id"));
        transfer.setResidentId(rs.getLong("resident_id"));
        
        Long fromBedId = rs.getLong("from_bed_id");
        transfer.setFromBedId(rs.wasNull() ? null : fromBedId);
        
        transfer.setToBedId(rs.getLong("to_bed_id"));
        transfer.setNurseId(rs.getLong("nurse_id"));
        transfer.setTransferTime(rs.getTimestamp("transfer_time").toLocalDateTime());
        transfer.setReason(rs.getString("reason"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        transfer.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        
        return transfer;
    }
    
    
    public static class TransferValidation {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void addError(String error) { 
            this.errors.add(error);
            this.valid = false;
        }
        
        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
    
    public static class TransferStats {
        private int transfersToday = 0;
        private int transfersThisWeek = 0;
        private int pendingTransfers = 0;
        
        public int getTransfersToday() { return transfersToday; }
        public void setTransfersToday(int transfersToday) { this.transfersToday = transfersToday; }
        
        public int getTransfersThisWeek() { return transfersThisWeek; }
        public void setTransfersThisWeek(int transfersThisWeek) { this.transfersThisWeek = transfersThisWeek; }
        
        public int getPendingTransfers() { return pendingTransfers; }
        public void setPendingTransfers(int pendingTransfers) { this.pendingTransfers = pendingTransfers; }
    }
}
