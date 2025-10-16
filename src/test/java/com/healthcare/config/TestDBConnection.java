package com.healthcare.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test Database Connection using H2 in-memory database
 * Provides a clean database for each test
 */
public class TestDBConnection {
    
    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";
    
    private static Connection connection;
    
    /**
     * Get test database connection
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(H2_URL, USERNAME, PASSWORD);
            initializeTestDatabase();
        }
        return connection;
    }
    
    /**
     * Initialize test database with schema
     */
    private static void initializeTestDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create tables for testing
            createTables(stmt);
            // Don't insert test data automatically to avoid conflicts
            // insertTestData(stmt);
        }
    }
    
    /**
     * Create all necessary tables
     */
    private static void createTables(Statement stmt) throws SQLException {
        // Staff table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Staff (
                staff_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                email VARCHAR(100),
                phone VARCHAR(20),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Residents table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Residents (
                resident_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                gender VARCHAR(10) NOT NULL,
                birth_date DATE,
                admission_date DATE NOT NULL,
                discharge_date DATE,
                current_bed_id BIGINT,
                assigned_doctor_id BIGINT,
                medical_condition TEXT,
                requires_isolation BOOLEAN DEFAULT FALSE,
                emergency_contact VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Beds table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Beds (
                bed_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                bed_number VARCHAR(20) NOT NULL,
                room_id BIGINT NOT NULL,
                bed_type VARCHAR(20) NOT NULL,
                gender_restriction VARCHAR(20) NOT NULL,
                is_occupied BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Rooms table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Rooms (
                room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                room_number VARCHAR(20) NOT NULL,
                ward_id BIGINT NOT NULL,
                room_type VARCHAR(20) NOT NULL,
                gender_preference VARCHAR(20) NOT NULL,
                capacity INT DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Wards table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Wards (
                ward_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                ward_name VARCHAR(50) NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Shift_Schedule table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Shift_Schedule (
                shift_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                staff_id BIGINT NOT NULL,
                shift_date DATE NOT NULL,
                shift_type VARCHAR(20) NOT NULL,
                start_time VARCHAR(10) NOT NULL,
                end_time VARCHAR(10) NOT NULL,
                ward_id BIGINT,
                status VARCHAR(20) DEFAULT 'Scheduled',
                assigned_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Actions_Log table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Actions_Log (
                action_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                staff_id BIGINT,
                action_type VARCHAR(50) NOT NULL,
                action_description VARCHAR(200) NOT NULL,
                action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                details TEXT
            )
        """);
        
        // Prescriptions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Prescriptions (
                prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                resident_id BIGINT NOT NULL,
                doctor_id BIGINT NOT NULL,
                prescription_date DATE NOT NULL,
                notes TEXT,
                status VARCHAR(20) DEFAULT 'Active',
                review_status VARCHAR(20) DEFAULT 'Pending',
                review_notes TEXT,
                reviewed_by BIGINT,
                reviewed_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Medicines table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Medicines (
                medicine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                description TEXT,
                dosage_unit VARCHAR(20) DEFAULT 'mg',
                category VARCHAR(50),
                classification VARCHAR(50),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Prescription_Medicines table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Prescription_Medicines (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                prescription_id BIGINT NOT NULL,
                medicine_id BIGINT NOT NULL,
                dosage VARCHAR(50) NOT NULL,
                frequency VARCHAR(100) NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE,
                instructions TEXT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        // Administered_Medication table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS Administered_Medication (
                admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                prescription_medicine_id BIGINT NOT NULL,
                nurse_id BIGINT NOT NULL,
                administered_time TIMESTAMP NOT NULL,
                dosage_given VARCHAR(50),
                notes TEXT,
                status VARCHAR(20) DEFAULT 'Given',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }
    
    /**
     * Insert test data
     */
    private static void insertTestData(Statement stmt) throws SQLException {
        // Insert test staff
        stmt.execute("""
            INSERT INTO Staff (username, password, role, first_name, last_name, is_active) VALUES
            ('manager', 'password', 'Manager', 'Test', 'Manager', TRUE),
            ('doctor1', 'password', 'Doctor', 'Dr', 'Smith', TRUE),
            ('nurse1', 'password', 'Nurse', 'Nurse', 'Johnson', TRUE)
        """);
        
        // Insert test wards
        stmt.execute("""
            INSERT INTO Wards (ward_name, description) VALUES
            ('Ward 1', 'General Ward'),
            ('Ward 2', 'Special Care Ward')
        """);
        
        // Insert test rooms
        stmt.execute("""
            INSERT INTO Rooms (room_number, ward_id, room_type, gender_preference, capacity) VALUES
            ('A1', 1, 'Standard', 'Mixed', 1),
            ('A2', 1, 'Standard', 'Mixed', 1),
            ('B1', 2, 'Special', 'Mixed', 1),
            ('B2', 2, 'Special', 'Mixed', 1)
        """);
        
        // Insert test beds
        stmt.execute("""
            INSERT INTO Beds (bed_number, room_id, bed_type, gender_restriction, is_occupied) VALUES
            ('A1-1', 1, 'Standard', 'None', FALSE),
            ('A2-1', 2, 'Standard', 'None', FALSE),
            ('B1-1', 3, 'Electric', 'None', FALSE),
            ('B2-1', 4, 'Special', 'None', FALSE)
        """);
        
        // Insert test medicines
        stmt.execute("""
            INSERT INTO Medicines (name, description, dosage_unit, category, is_active) VALUES
            ('Aspirin', 'Pain relief medication', 'mg', 'Analgesic', TRUE),
            ('Insulin', 'Diabetes medication', 'units', 'Hormone', TRUE),
            ('Antibiotics', 'Infection treatment', 'mg', 'Antibiotic', TRUE)
        """);
    }
    
    /**
     * Close test database connection
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    /**
     * Reset database for clean test state
     */
    public static void resetDatabase() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                // Clear all data but keep schema
                stmt.execute("DELETE FROM Administered_Medication");
                stmt.execute("DELETE FROM Prescription_Medicines");
                stmt.execute("DELETE FROM Prescriptions");
                stmt.execute("DELETE FROM Actions_Log");
                stmt.execute("DELETE FROM Shift_Schedule");
                stmt.execute("DELETE FROM Residents");
                stmt.execute("DELETE FROM Beds");
                stmt.execute("DELETE FROM Rooms");
                stmt.execute("DELETE FROM Wards");
                stmt.execute("DELETE FROM Staff");
                stmt.execute("DELETE FROM Medicines");
                
                // Don't re-insert test data to avoid conflicts
                // Test data will be inserted by individual tests as needed
            }
        }
    }
}
