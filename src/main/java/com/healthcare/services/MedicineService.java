package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.Medicine;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing medicines
 */
public class MedicineService {
    
    /**
     * Save a new medicine
     */
    public Medicine save(Medicine medicine) {
        String sql = "INSERT INTO Medicines (name, description, dosage_unit, category, classification, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getDescription());
            stmt.setString(3, medicine.getDosageUnit());
            stmt.setString(4, medicine.getCategory());
            stmt.setString(5, medicine.getClassification());
            stmt.setBoolean(6, medicine.isActive());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        medicine.setMedicineId(generatedKeys.getLong(1));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving medicine: " + e.getMessage());
            throw new RuntimeException("Failed to save medicine", e);
        }
        
        return medicine;
    }
    
    /**
     * Find medicine by ID
     */
    public Optional<Medicine> findById(Long medicineId) {
        String sql = "SELECT * FROM Medicines WHERE medicine_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, medicineId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedicine(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding medicine: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find medicine by name
     */
    public Optional<Medicine> findByName(String name) {
        String sql = "SELECT * FROM Medicines WHERE name = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedicine(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding medicine by name: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find medicines by category
     */
    public List<Medicine> findByCategory(String category) {
        String sql = "SELECT * FROM Medicines WHERE category = ? AND is_active = true ORDER BY name";
        
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding medicines by category: " + e.getMessage());
        }
        
        return medicines;
    }
    
    /**
     * Find medicines by classification
     */
    public List<Medicine> findByClassification(String classification) {
        String sql = "SELECT * FROM Medicines WHERE classification = ? AND is_active = true ORDER BY name";
        
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, classification);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding medicines by classification: " + e.getMessage());
        }
        
        return medicines;
    }
    
    /**
     * Find all active medicines
     */
    public List<Medicine> findAllActive() {
        String sql = "SELECT * FROM Medicines WHERE is_active = true ORDER BY category, name";
        
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all active medicines: " + e.getMessage());
        }
        
        return medicines;
    }
    
    /**
     * Find all medicines
     */
    public List<Medicine> findAll() {
        String sql = "SELECT * FROM Medicines ORDER BY category, name";
        
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all medicines: " + e.getMessage());
        }
        
        return medicines;
    }
    
    /**
     * Search medicines by name (partial match)
     */
    public List<Medicine> searchByName(String searchTerm) {
        String sql = "SELECT * FROM Medicines WHERE name LIKE ? AND is_active = true ORDER BY name";
        
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching medicines by name: " + e.getMessage());
        }
        
        return medicines;
    }
    
    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        String sql = "SELECT DISTINCT category FROM Medicines WHERE category IS NOT NULL AND is_active = true ORDER BY category";
        
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        
        return categories;
    }
    
    /**
     * Get all unique classifications
     */
    public List<String> getAllClassifications() {
        String sql = "SELECT DISTINCT classification FROM Medicines WHERE classification IS NOT NULL AND is_active = true ORDER BY classification";
        
        List<String> classifications = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                classifications.add(rs.getString("classification"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting classifications: " + e.getMessage());
        }
        
        return classifications;
    }
    
    /**
     * Update medicine
     */
    public Medicine update(Medicine medicine) {
        String sql = "UPDATE Medicines SET name = ?, description = ?, dosage_unit = ?, category = ?, classification = ?, is_active = ? WHERE medicine_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getDescription());
            stmt.setString(3, medicine.getDosageUnit());
            stmt.setString(4, medicine.getCategory());
            stmt.setString(5, medicine.getClassification());
            stmt.setBoolean(6, medicine.isActive());
            stmt.setLong(7, medicine.getMedicineId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                return medicine;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating medicine: " + e.getMessage());
            throw new RuntimeException("Failed to update medicine", e);
        }
        
        return null;
    }
    
    /**
     * Delete medicine by ID
     */
    public boolean deleteById(Long medicineId) {
        String sql = "DELETE FROM Medicines WHERE medicine_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, medicineId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting medicine: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to Medicine object
     */
    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        
        medicine.setMedicineId(rs.getLong("medicine_id"));
        medicine.setName(rs.getString("name"));
        medicine.setDescription(rs.getString("description"));
        medicine.setDosageUnit(rs.getString("dosage_unit"));
        medicine.setCategory(rs.getString("category"));
        medicine.setClassification(rs.getString("classification"));
        medicine.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            medicine.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return medicine;
    }
}
