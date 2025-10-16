package com.healthcare.services;

import com.healthcare.config.DBConnection;
import com.healthcare.model.AdministeredMedication;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing medication administration by nurses
 */
public class MedicationAdministrationService {
    
    /**
     * Get all scheduled medications for today that need to be administered
     */
    public List<MedicationSchedule> getTodaysMedicationSchedule() {
        String sql = """
            SELECT pm.id, pm.prescription_id, pm.medicine_id, pm.dosage, pm.frequency, 
                   pm.start_date, pm.end_date, pm.instructions, pm.is_active,
                   p.resident_id, p.doctor_id, p.prescription_date,
                   r.first_name, r.last_name, r.current_bed_id,
                   m.name as medicine_name, m.dosage_unit,
                   s.first_name as doctor_first_name, s.last_name as doctor_last_name
            FROM Prescription_Medicines pm
            JOIN Prescriptions p ON pm.prescription_id = p.prescription_id
            JOIN Residents r ON p.resident_id = r.resident_id
            JOIN Medicines m ON pm.medicine_id = m.medicine_id
            JOIN Staff s ON p.doctor_id = s.staff_id
            WHERE pm.is_active = TRUE 
            AND pm.start_date <= CURDATE() 
            AND (pm.end_date IS NULL OR pm.end_date >= CURDATE())
            AND p.status = 'Active'
            AND r.discharge_date IS NULL
            ORDER BY r.first_name, r.last_name, m.name
            """;
        
        List<MedicationSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                schedules.add(mapResultSetToMedicationSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting today's medication schedule: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Get medications scheduled for a specific nurse's shift
     */
    public List<MedicationSchedule> getMedicationScheduleForNurse(Long nurseId) {
        // For now, return all scheduled medications
        // In a real system, this would filter by nurse's assigned patients/ward
        return getTodaysMedicationSchedule();
    }
    
    /**
     * Get pending medications (not yet administered today)
     */
    public List<MedicationSchedule> getPendingMedications() {
        String sql = """
            SELECT pm.id, pm.prescription_id, pm.medicine_id, pm.dosage, pm.frequency, 
                   pm.start_date, pm.end_date, pm.instructions, pm.is_active,
                   p.resident_id, p.doctor_id, p.prescription_date,
                   r.first_name, r.last_name, r.current_bed_id,
                   m.name as medicine_name, m.dosage_unit,
                   s.first_name as doctor_first_name, s.last_name as doctor_last_name
            FROM Prescription_Medicines pm
            JOIN Prescriptions p ON pm.prescription_id = p.prescription_id
            JOIN Residents r ON p.resident_id = r.resident_id
            JOIN Medicines m ON pm.medicine_id = m.medicine_id
            JOIN Staff s ON p.doctor_id = s.staff_id
            WHERE pm.is_active = TRUE 
            AND pm.start_date <= CURDATE() 
            AND (pm.end_date IS NULL OR pm.end_date >= CURDATE())
            AND p.status = 'Active'
            AND r.discharge_date IS NULL
            AND NOT EXISTS (
                SELECT 1 FROM Administered_Medication am 
                WHERE am.prescription_medicine_id = pm.id 
                AND DATE(am.administered_time) = CURDATE()
                AND am.status = 'Given'
            )
            ORDER BY r.first_name, r.last_name, m.name
            """;
        
        List<MedicationSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                schedules.add(mapResultSetToMedicationSchedule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending medications: " + e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Mark medication as administered
     */
    public boolean markMedicationAsAdministered(Long prescriptionMedicineId, Long nurseId, 
                                               String dosageGiven, String notes) {
        String sql = "INSERT INTO Administered_Medication (prescription_medicine_id, nurse_id, administered_time, dosage_given, notes, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionMedicineId);
            stmt.setLong(2, nurseId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, dosageGiven);
            stmt.setString(5, notes);
            stmt.setString(6, "Given");
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking medication as administered: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark medication as missed
     */
    public boolean markMedicationAsMissed(Long prescriptionMedicineId, Long nurseId, String notes) {
        String sql = "INSERT INTO Administered_Medication (prescription_medicine_id, nurse_id, administered_time, dosage_given, notes, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionMedicineId);
            stmt.setLong(2, nurseId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, "0"); // No dosage given
            stmt.setString(5, notes);
            stmt.setString(6, "Missed");
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking medication as missed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark medication as refused by patient
     */
    public boolean markMedicationAsRefused(Long prescriptionMedicineId, Long nurseId, String notes) {
        String sql = "INSERT INTO Administered_Medication (prescription_medicine_id, nurse_id, administered_time, dosage_given, notes, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionMedicineId);
            stmt.setLong(2, nurseId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, "0"); // No dosage given
            stmt.setString(5, notes);
            stmt.setString(6, "Refused");
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking medication as refused: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get administration history for a specific medication
     */
    public List<AdministeredMedication> getAdministrationHistory(Long prescriptionMedicineId) {
        String sql = "SELECT * FROM Administered_Medication WHERE prescription_medicine_id = ? ORDER BY administered_time DESC";
        
        List<AdministeredMedication> history = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, prescriptionMedicineId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                history.add(mapResultSetToAdministeredMedication(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting administration history: " + e.getMessage());
        }
        
        return history;
    }
    
    /**
     * Get today's administration records for a nurse
     */
    public List<AdministeredMedication> getTodaysAdministrations(Long nurseId) {
        String sql = "SELECT * FROM Administered_Medication WHERE nurse_id = ? AND DATE(administered_time) = CURDATE() ORDER BY administered_time DESC";
        
        List<AdministeredMedication> administrations = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, nurseId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                administrations.add(mapResultSetToAdministeredMedication(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting today's administrations: " + e.getMessage());
        }
        
        return administrations;
    }
    
    /**
     * Get overdue medications (scheduled but not administered)
     */
    public List<MedicationSchedule> getOverdueMedications() {
        // This would typically check against scheduled times
        // For now, return pending medications as overdue
        return getPendingMedications();
    }
    
    /**
     * Get medication statistics for dashboard
     */
    public MedicationStats getMedicationStats() {
        MedicationStats stats = new MedicationStats();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total scheduled medications for today
            String totalSql = """
                SELECT COUNT(*) FROM Prescription_Medicines pm
                JOIN Prescriptions p ON pm.prescription_id = p.prescription_id
                JOIN Residents r ON p.resident_id = r.resident_id
                WHERE pm.is_active = TRUE 
                AND pm.start_date <= CURDATE() 
                AND (pm.end_date IS NULL OR pm.end_date >= CURDATE())
                AND p.status = 'Active'
                AND r.discharge_date IS NULL
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(totalSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.totalScheduled = rs.getInt(1);
                }
            }
            
            // Administered today
            String administeredSql = """
                SELECT COUNT(*) FROM Administered_Medication 
                WHERE DATE(administered_time) = CURDATE() AND status = 'Given'
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(administeredSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.administeredToday = rs.getInt(1);
                }
            }
            
            // Pending
            stats.pending = stats.totalScheduled - stats.administeredToday;
            
            // Overdue (simplified - just pending for now)
            stats.overdue = stats.pending;
            
        } catch (SQLException e) {
            System.err.println("Error getting medication stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    // Helper methods
    private MedicationSchedule mapResultSetToMedicationSchedule(ResultSet rs) throws SQLException {
        MedicationSchedule schedule = new MedicationSchedule();
        
        schedule.setPrescriptionMedicineId(rs.getLong("id"));
        schedule.setPrescriptionId(rs.getLong("prescription_id"));
        schedule.setMedicineId(rs.getLong("medicine_id"));
        schedule.setDosage(rs.getString("dosage"));
        schedule.setFrequency(rs.getString("frequency"));
        schedule.setStartDate(rs.getDate("start_date").toLocalDate());
        
        Date endDate = rs.getDate("end_date");
        schedule.setEndDate(endDate != null ? endDate.toLocalDate() : null);
        
        schedule.setInstructions(rs.getString("instructions"));
        schedule.setIsActive(rs.getBoolean("is_active"));
        schedule.setResidentId(rs.getLong("resident_id"));
        schedule.setDoctorId(rs.getLong("doctor_id"));
        schedule.setPrescriptionDate(rs.getDate("prescription_date").toLocalDate());
        schedule.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        schedule.setBedId(rs.getLong("current_bed_id"));
        schedule.setMedicineName(rs.getString("medicine_name"));
        schedule.setDosageUnit(rs.getString("dosage_unit"));
        schedule.setDoctorName(rs.getString("doctor_first_name") + " " + rs.getString("doctor_last_name"));
        
        return schedule;
    }
    
    private AdministeredMedication mapResultSetToAdministeredMedication(ResultSet rs) throws SQLException {
        AdministeredMedication admin = new AdministeredMedication();
        
        admin.setAdminId(rs.getLong("admin_id"));
        admin.setPrescriptionMedicineId(rs.getLong("prescription_medicine_id"));
        admin.setNurseId(rs.getLong("nurse_id"));
        admin.setAdministeredTime(rs.getTimestamp("administered_time").toLocalDateTime());
        admin.setDosageGiven(rs.getString("dosage_given"));
        admin.setNotes(rs.getString("notes"));
        admin.setStatus(AdministeredMedication.AdministrationStatus.valueOf(rs.getString("status")));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        admin.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        
        return admin;
    }
    
    // Inner classes for data transfer
    public static class MedicationSchedule {
        private Long prescriptionMedicineId;
        private Long prescriptionId;
        private Long medicineId;
        private String dosage;
        private String frequency;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private String instructions;
        private boolean isActive;
        private Long residentId;
        private Long doctorId;
        private java.time.LocalDate prescriptionDate;
        private String patientName;
        private Long bedId;
        private String medicineName;
        private String dosageUnit;
        private String doctorName;
        
        // Getters and setters
        public Long getPrescriptionMedicineId() { return prescriptionMedicineId; }
        public void setPrescriptionMedicineId(Long prescriptionMedicineId) { this.prescriptionMedicineId = prescriptionMedicineId; }
        
        public Long getPrescriptionId() { return prescriptionId; }
        public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
        
        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        
        public java.time.LocalDate getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }
        
        public java.time.LocalDate getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }
        
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        
        public boolean isActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        
        public Long getResidentId() { return residentId; }
        public void setResidentId(Long residentId) { this.residentId = residentId; }
        
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        
        public java.time.LocalDate getPrescriptionDate() { return prescriptionDate; }
        public void setPrescriptionDate(java.time.LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }
        
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        
        public Long getBedId() { return bedId; }
        public void setBedId(Long bedId) { this.bedId = bedId; }
        
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        
        public String getDosageUnit() { return dosageUnit; }
        public void setDosageUnit(String dosageUnit) { this.dosageUnit = dosageUnit; }
        
        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
        
        public String getFullDescription() {
            return medicineName + " " + dosage + " " + dosageUnit + " - " + frequency;
        }
    }
    
    public static class MedicationStats {
        private int totalScheduled = 0;
        private int administeredToday = 0;
        private int pending = 0;
        private int overdue = 0;
        
        // Getters and setters
        public int getTotalScheduled() { return totalScheduled; }
        public void setTotalScheduled(int totalScheduled) { this.totalScheduled = totalScheduled; }
        
        public int getAdministeredToday() { return administeredToday; }
        public void setAdministeredToday(int administeredToday) { this.administeredToday = administeredToday; }
        
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        
        public int getOverdue() { return overdue; }
        public void setOverdue(int overdue) { this.overdue = overdue; }
    }
    
    /**
     * Find prescription medicine ID for a specific patient and medicine
     */
    public Long findPrescriptionMedicineId(Long residentId, Long medicineId) {
        String sql = """
            SELECT pm.id 
            FROM Prescription_Medicines pm 
            JOIN Prescriptions p ON pm.prescription_id = p.prescription_id 
            WHERE p.resident_id = ? AND pm.medicine_id = ? AND pm.is_active = TRUE 
            LIMIT 1
            """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            stmt.setLong(2, medicineId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding prescription medicine ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all medicines prescribed to a specific patient
     */
    public List<com.healthcare.model.Medicine> getMedicinesForPatient(Long residentId) {
        String sql = """
            SELECT DISTINCT m.medicine_id, m.name, m.description, m.dosage_unit, m.category, m.classification, m.is_active, m.created_at
            FROM Medicines m
            JOIN Prescription_Medicines pm ON m.medicine_id = pm.medicine_id
            JOIN Prescriptions p ON pm.prescription_id = p.prescription_id
            WHERE p.resident_id = ? 
            AND pm.is_active = TRUE 
            AND p.status = 'Active'
            AND m.is_active = TRUE
            ORDER BY m.name
            """;
        
        List<com.healthcare.model.Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, residentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    com.healthcare.model.Medicine medicine = new com.healthcare.model.Medicine();
                    medicine.setMedicineId(rs.getLong("medicine_id"));
                    medicine.setName(rs.getString("name"));
                    medicine.setDescription(rs.getString("description"));
                    medicine.setDosageUnit(rs.getString("dosage_unit"));
                    medicine.setCategory(rs.getString("category"));
                    medicine.setClassification(rs.getString("classification"));
                    medicine.setActive(rs.getBoolean("is_active"));
                    medicine.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    medicines.add(medicine);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting medicines for patient: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicines;
    }
}
