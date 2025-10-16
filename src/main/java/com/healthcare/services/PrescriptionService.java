package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.Prescription;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing prescriptions
 */
public class PrescriptionService {
    
    /**
     * Save a new prescription
     */
    public Prescription save(Prescription prescription) {
        String sql = "INSERT INTO Prescriptions (resident_id, doctor_id, prescription_date, notes, status, review_status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, prescription.getResidentId());
            stmt.setLong(2, prescription.getDoctorId());
            stmt.setDate(3, Date.valueOf(prescription.getPrescriptionDate()));
            stmt.setString(4, prescription.getNotes());
            stmt.setString(5, prescription.getStatus().name());
            stmt.setString(6, prescription.getReviewStatus().name());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prescription.setPrescriptionId(generatedKeys.getLong(1));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            throw new RuntimeException("Failed to save prescription", e);
        }
        
        return prescription;
    }
    
    /**
     * Find prescription by ID
     */
    public Optional<Prescription> findById(Long prescriptionId) {
        String sql = "SELECT * FROM Prescriptions WHERE prescription_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrescription(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding prescription: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find all prescriptions for a specific doctor
     */
    public List<Prescription> findByDoctorId(Long doctorId) {
        String sql = "SELECT * FROM Prescriptions WHERE doctor_id = ? ORDER BY prescription_date DESC";
        
        List<Prescription> prescriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, doctorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by doctor: " + e.getMessage());
        }
        
        return prescriptions;
    }
    
    /**
     * Find all prescriptions for a specific resident
     */
    public List<Prescription> findByResidentId(Long residentId) {
        String sql = "SELECT * FROM Prescriptions WHERE resident_id = ? ORDER BY prescription_date DESC";
        
        List<Prescription> prescriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by resident: " + e.getMessage());
        }
        
        return prescriptions;
    }
    
    /**
     * Find prescriptions by review status
     */
    public List<Prescription> findByReviewStatus(Prescription.ReviewStatus reviewStatus) {
        String sql = "SELECT * FROM Prescriptions WHERE review_status = ? ORDER BY prescription_date DESC";
        
        List<Prescription> prescriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, reviewStatus.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by review status: " + e.getMessage());
        }
        
        return prescriptions;
    }
    
    /**
     * Find pending prescriptions for a specific doctor
     */
    public List<Prescription> findPendingByDoctorId(Long doctorId) {
        String sql = "SELECT * FROM Prescriptions WHERE doctor_id = ? AND review_status = 'Pending' ORDER BY prescription_date DESC";
        
        List<Prescription> prescriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, doctorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding pending prescriptions by doctor: " + e.getMessage());
        }
        
        return prescriptions;
    }
    
    /**
     * Find prescriptions for today for a specific doctor
     */
    public List<Prescription> findTodaysByDoctorId(Long doctorId) {
        String sql = "SELECT * FROM Prescriptions WHERE doctor_id = ? AND prescription_date = CURDATE() ORDER BY created_at DESC";
        
        List<Prescription> prescriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, doctorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding today's prescriptions by doctor: " + e.getMessage());
        }
        
        return prescriptions;
    }
    
    /**
     * Find all prescriptions
     */
    public List<Prescription> findAll() {
        String sql = "SELECT * FROM Prescriptions ORDER BY prescription_date DESC";
        
        List<Prescription> prescriptions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prescriptions.add(mapResultSetToPrescription(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all prescriptions: " + e.getMessage());
        }
        
        return prescriptions;
    }
    
    /**
     * Update prescription
     */
    public Prescription update(Prescription prescription) {
        String sql = "UPDATE Prescriptions SET resident_id = ?, doctor_id = ?, prescription_date = ?, notes = ?, status = ?, review_status = ?, review_notes = ?, reviewed_by = ?, reviewed_at = ? WHERE prescription_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescription.getResidentId());
            stmt.setLong(2, prescription.getDoctorId());
            stmt.setDate(3, Date.valueOf(prescription.getPrescriptionDate()));
            stmt.setString(4, prescription.getNotes());
            stmt.setString(5, prescription.getStatus().name());
            stmt.setString(6, prescription.getReviewStatus().name());
            stmt.setString(7, prescription.getReviewNotes());
            stmt.setObject(8, prescription.getReviewedBy());
            stmt.setTimestamp(9, prescription.getReviewedAt() != null ? Timestamp.valueOf(prescription.getReviewedAt()) : null);
            stmt.setLong(10, prescription.getPrescriptionId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                return prescription;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating prescription: " + e.getMessage());
            throw new RuntimeException("Failed to update prescription", e);
        }
        
        return null;
    }
    
    /**
     * Delete prescription by ID
     */
    public boolean deleteById(Long prescriptionId) {
        String sql = "DELETE FROM Prescriptions WHERE prescription_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting prescription: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to Prescription object
     */
    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        Prescription prescription = new Prescription();
        
        prescription.setPrescriptionId(rs.getLong("prescription_id"));
        prescription.setResidentId(rs.getLong("resident_id"));
        prescription.setDoctorId(rs.getLong("doctor_id"));
        prescription.setPrescriptionDate(rs.getDate("prescription_date").toLocalDate());
        prescription.setNotes(rs.getString("notes"));
        prescription.setStatus(Prescription.PrescriptionStatus.valueOf(rs.getString("status")));
        prescription.setReviewStatus(Prescription.ReviewStatus.valueOf(rs.getString("review_status")));
        prescription.setReviewNotes(rs.getString("review_notes"));
        
        Long reviewedBy = rs.getObject("reviewed_by", Long.class);
        prescription.setReviewedBy(reviewedBy);
        
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        if (reviewedAt != null) {
            prescription.setReviewedAt(reviewedAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            prescription.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return prescription;
    }
    
    /**
     * Get patient name for a prescription by querying the database
     */
    public String getPatientNameForPrescription(Long prescriptionId) {
        String sql = "SELECT r.first_name, r.last_name FROM Prescriptions p " +
                    "JOIN Residents r ON p.resident_id = r.resident_id " +
                    "WHERE p.prescription_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting patient name: " + e.getMessage());
        }
        
        return "Unknown Patient";
    }
    
    /**
     * Get patient name for a prescription by resident ID
     */
    public String getPatientNameByResidentId(Long residentId) {
        String sql = "SELECT first_name, last_name FROM Residents WHERE resident_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting patient name by resident ID: " + e.getMessage());
        }
        
        return "Unknown Patient";
    }
    
    /**
     * Delete prescription by ID (alias for deleteById)
     */
    public boolean delete(Long prescriptionId) {
        return deleteById(prescriptionId);
    }
    
    /**
     * Save a prescription medicine to the Prescription_Medicines table
     */
    public void savePrescriptionMedicine(Long prescriptionId, Long medicineId, String dosage, 
                                       String frequency, java.time.LocalDate startDate, 
                                       java.time.LocalDate endDate, String instructions) {
        String sql = "INSERT INTO Prescription_Medicines (prescription_id, medicine_id, dosage, frequency, start_date, end_date, instructions, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionId);
            stmt.setLong(2, medicineId);
            stmt.setString(3, dosage);
            stmt.setString(4, frequency);
            stmt.setDate(5, startDate != null ? Date.valueOf(startDate) : null);
            stmt.setDate(6, endDate != null ? Date.valueOf(endDate) : null);
            stmt.setString(7, instructions);
            stmt.setBoolean(8, true);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving prescription medicine: " + e.getMessage());
            throw new RuntimeException("Failed to save prescription medicine", e);
        }
    }
}
