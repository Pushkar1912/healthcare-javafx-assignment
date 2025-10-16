-- =====================================================
-- IMPROVED HEALTHCARE DATABASE SCHEMA
-- Based on Instruction (1).txt requirements
-- =====================================================

-- Wards Table (Ward 1 & Ward 2 as specified)
CREATE TABLE Wards (
    ward_id INT PRIMARY KEY AUTO_INCREMENT,
    ward_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rooms Table (6 rooms per ward as specified)
CREATE TABLE Rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    ward_id INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type ENUM('Standard', 'Isolation', 'Special') DEFAULT 'Standard',
    max_capacity INT DEFAULT 4, -- 1-4 beds per room as specified
    gender_preference ENUM('Male', 'Female', 'Mixed') DEFAULT 'Mixed',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ward_id) REFERENCES Wards(ward_id),
    UNIQUE KEY unique_room_per_ward (ward_id, room_number)
);

-- Beds Table (Improved with better structure)
CREATE TABLE Beds (
    bed_id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT NOT NULL,
    bed_number VARCHAR(20) NOT NULL,
    bed_code VARCHAR(20) NOT NULL UNIQUE, -- Format: W{ward_number}R{room_number}B{bed_number}
    bed_type ENUM('Standard', 'Electric', 'Special') DEFAULT 'Standard',
    is_occupied BOOLEAN DEFAULT FALSE,
    occupied_by INT NULL,
    gender_restriction ENUM('Male', 'Female', 'None') DEFAULT 'None',
    isolation_required BOOLEAN DEFAULT FALSE,
    last_cleaned TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id),
    UNIQUE KEY unique_bed_per_room (room_id, bed_number)
);

-- Residents Table (Enhanced) - No foreign key to Beds initially
CREATE TABLE Residents (
    resident_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender ENUM('M', 'F') NOT NULL,
    birth_date DATE,
    admission_date DATE NOT NULL,
    discharge_date DATE NULL,
    current_bed_id INT NULL,
    assigned_doctor_id INT NULL, -- Doctor assigned to this resident
    medical_condition TEXT,
    requires_isolation BOOLEAN DEFAULT FALSE,
    emergency_contact VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Staff Table (Enhanced with shift management)
CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('Manager', 'Doctor', 'Nurse') NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shifts Table (Shift templates)
CREATE TABLE Shifts (
    shift_id INT PRIMARY KEY AUTO_INCREMENT,
    shift_name VARCHAR(100) NOT NULL,
    shift_type ENUM('Morning', 'Afternoon', 'Doctor') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    ward_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ward_id) REFERENCES Wards(ward_id)
);

-- Shift Schedule Table (For compliance requirements)
CREATE TABLE Shift_Schedule (
    shift_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT NOT NULL,
    shift_date DATE NOT NULL,
    shift_type ENUM('Morning', 'Afternoon', 'Doctor') NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    ward_id INT,
    status ENUM('Scheduled', 'Completed', 'Cancelled') DEFAULT 'Scheduled',
    assigned_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id),
    FOREIGN KEY (ward_id) REFERENCES Wards(ward_id),
    FOREIGN KEY (assigned_by) REFERENCES Staff(staff_id)
);

-- Prescriptions Table (Enhanced)
CREATE TABLE Prescriptions (
    prescription_id INT PRIMARY KEY AUTO_INCREMENT,
    resident_id INT NOT NULL,
    doctor_id INT NOT NULL,
    prescription_date DATE NOT NULL,
    notes TEXT,
    status ENUM('Active', 'Completed', 'Cancelled') DEFAULT 'Active',
    review_status ENUM('Pending', 'Reviewed', 'Approved', 'Rejected') DEFAULT 'Pending',
    review_notes TEXT,
    reviewed_by INT NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES Residents(resident_id),
    FOREIGN KEY (doctor_id) REFERENCES Staff(staff_id),
    FOREIGN KEY (reviewed_by) REFERENCES Staff(staff_id)
);

-- Medicines Table (Enhanced)
CREATE TABLE Medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    dosage_unit VARCHAR(20) DEFAULT 'mg',
    category VARCHAR(50), -- e.g., 'Pain Relief', 'Antibiotic', 'Cardiovascular'
    classification VARCHAR(50), -- e.g., 'Prescription', 'Over-the-counter', 'Controlled'
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Prescription Medicines Table (Enhanced)
CREATE TABLE Prescription_Medicines (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(100) NOT NULL, -- e.g., "Every 8 hours", "Twice daily"
    start_date DATE NOT NULL,
    end_date DATE,
    instructions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES Prescriptions(prescription_id),
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

-- Administered Medication Table (Enhanced)
CREATE TABLE Administered_Medication (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    prescription_medicine_id INT NOT NULL,
    nurse_id INT NOT NULL,
    administered_time DATETIME NOT NULL,
    dosage_given VARCHAR(50),
    notes TEXT,
    status ENUM('Given', 'Missed', 'Refused') DEFAULT 'Given',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_medicine_id) REFERENCES Prescription_Medicines(id),
    FOREIGN KEY (nurse_id) REFERENCES Staff(staff_id)
);

-- Actions Log Table (Simplified for essential auditing)
CREATE TABLE Actions_Log (
    action_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT,
    action_type ENUM('Admit', 'Discharge', 'Transfer', 'Prescribe', 'Administer', 'Update', 'Add_Staff', 'Delete_Staff', 'Assign_Shift', 'Delete_Shift', 'Login', 'Logout', 'Archive') NOT NULL,
    action_description VARCHAR(200) NOT NULL,
    action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);

-- Archive Table (Enhanced)
CREATE TABLE Archive (
    archive_id INT PRIMARY KEY AUTO_INCREMENT,
    resident_id INT NOT NULL,
    discharge_date DATE NOT NULL,
    total_stay_days INT,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    archive_reason VARCHAR(100),
    FOREIGN KEY (resident_id) REFERENCES Residents(resident_id)
);

-- Bed Transfers Table (For nurse bed transfer functionality)
CREATE TABLE Bed_Transfers (
    transfer_id INT PRIMARY KEY AUTO_INCREMENT,
    resident_id INT NOT NULL,
    from_bed_id INT NULL,
    to_bed_id INT NOT NULL,
    nurse_id INT NOT NULL,
    transfer_time DATETIME NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resident_id) REFERENCES Residents(resident_id),
    FOREIGN KEY (from_bed_id) REFERENCES Beds(bed_id),
    FOREIGN KEY (to_bed_id) REFERENCES Beds(bed_id),
    FOREIGN KEY (nurse_id) REFERENCES Staff(staff_id)
);


-- =====================================================
-- ADDITIONAL FOREIGN KEY CONSTRAINTS
-- =====================================================

-- Add foreign key constraint for Beds.occupied_by after all tables are created
ALTER TABLE Beds ADD CONSTRAINT fk_bed_occupied_by 
    FOREIGN KEY (occupied_by) REFERENCES Residents(resident_id);

-- Add foreign key constraint for Residents.current_bed_id after all tables are created
ALTER TABLE Residents ADD CONSTRAINT fk_resident_current_bed 
    FOREIGN KEY (current_bed_id) REFERENCES Beds(bed_id);

-- Add foreign key constraint for Residents.assigned_doctor_id after all tables are created
ALTER TABLE Residents ADD CONSTRAINT fk_resident_assigned_doctor 
    FOREIGN KEY (assigned_doctor_id) REFERENCES Staff(staff_id);

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Insert Wards
INSERT INTO Wards (ward_name, description) VALUES 
('Ward 1', 'General care ward'),
('Ward 2', 'Specialized care ward');

-- Insert Rooms (6 rooms per ward as specified)
INSERT INTO Rooms (ward_id, room_number, room_type, max_capacity, gender_preference) VALUES 
-- Ward 1 Rooms
(1, '101', 'Standard', 4, 'Mixed'),
(1, '102', 'Standard', 3, 'Mixed'),
(1, '103', 'Isolation', 1, 'Mixed'),
(1, '104', 'Standard', 4, 'Mixed'),
(1, '105', 'Standard', 2, 'Mixed'),
(1, '106', 'Special', 2, 'Mixed'),

-- Ward 2 Rooms
(2, '201', 'Standard', 4, 'Mixed'),
(2, '202', 'Standard', 3, 'Mixed'),
(2, '203', 'Isolation', 1, 'Mixed'),
(2, '204', 'Standard', 4, 'Mixed'),
(2, '205', 'Standard', 2, 'Mixed'),
(2, '206', 'Special', 2, 'Mixed');

-- Insert Beds (1-4 beds per room as specified)
INSERT INTO Beds (room_id, bed_number, bed_code, bed_type, gender_restriction) VALUES 
-- Room 101 (4 beds) - Ward 1
(1, 'A', 'W1R101B1', 'Standard', 'None'),
(1, 'B', 'W1R101B2', 'Standard', 'None'),
(1, 'C', 'W1R101B3', 'Standard', 'None'),
(1, 'D', 'W1R101B4', 'Standard', 'None'),

-- Room 102 (3 beds) - Ward 1
(2, 'A', 'W1R102B1', 'Standard', 'None'),
(2, 'B', 'W1R102B2', 'Standard', 'None'),
(2, 'C', 'W1R102B3', 'Standard', 'None'),

-- Room 103 (1 bed - Isolation) - Ward 1
(3, 'A', 'W1R103B1', 'Special', 'None'),

-- Room 104 (4 beds) - Ward 1
(4, 'A', 'W1R104B1', 'Standard', 'None'),
(4, 'B', 'W1R104B2', 'Standard', 'None'),
(4, 'C', 'W1R104B3', 'Standard', 'None'),
(4, 'D', 'W1R104B4', 'Standard', 'None'),

-- Room 105 (2 beds) - Ward 1
(5, 'A', 'W1R105B1', 'Standard', 'None'),
(5, 'B', 'W1R105B2', 'Standard', 'None'),

-- Room 106 (2 beds - Special) - Ward 1
(6, 'A', 'W1R106B1', 'Electric', 'None'),
(6, 'B', 'W1R106B2', 'Electric', 'None'),

-- Ward 2 beds (similar pattern)
-- Room 201 (4 beds) - Ward 2
(7, 'A', 'W2R201B1', 'Standard', 'None'),
(7, 'B', 'W2R201B2', 'Standard', 'None'),
(7, 'C', 'W2R201B3', 'Standard', 'None'),
(7, 'D', 'W2R201B4', 'Standard', 'None'),

-- Room 202 (3 beds) - Ward 2
(8, 'A', 'W2R202B1', 'Standard', 'None'),
(8, 'B', 'W2R202B2', 'Standard', 'None'),
(8, 'C', 'W2R202B3', 'Standard', 'None'),

-- Room 203 (1 bed - Isolation) - Ward 2
(9, 'A', 'W2R203B1', 'Special', 'None'),

-- Room 204 (4 beds) - Ward 2
(10, 'A', 'W2R204B1', 'Standard', 'None'),
(10, 'B', 'W2R204B2', 'Standard', 'None'),
(10, 'C', 'W2R204B3', 'Standard', 'None'),
(10, 'D', 'W2R204B4', 'Standard', 'None'),

-- Room 205 (2 beds) - Ward 2
(11, 'A', 'W2R205B1', 'Standard', 'None'),
(11, 'B', 'W2R205B2', 'Standard', 'None'),

-- Room 206 (2 beds - Special) - Ward 2
(12, 'A', 'W2R206B1', 'Electric', 'None'),
(12, 'B', 'W2R206B2', 'Electric', 'None');

-- Insert Sample Staff
INSERT INTO Staff (username, password, role, first_name, last_name) VALUES 
('manager', 'password', 'Manager', 'John', 'Manager'),
('doctor1', 'password', 'Doctor', 'Dr. Sarah', 'Smith'),
('doctor2', 'password', 'Doctor', 'Dr. Michael', 'Wilson'),
('nurse1', 'password', 'Nurse', 'Mary', 'Johnson'),
('nurse2', 'password', 'Nurse', 'Lisa', 'Brown'),
('nurse3', 'password', 'Nurse', 'Emma', 'Davis');

-- Insert Sample Shifts
INSERT INTO Shifts (shift_name, shift_type, start_time, end_time, ward_id) VALUES 
('Morning Shift', 'Morning', '08:00:00', '16:00:00', 1),
('Afternoon Shift', 'Afternoon', '14:00:00', '22:00:00', 1),
('Doctor Round', 'Doctor', '09:00:00', '10:00:00', 1),
('Morning Shift', 'Morning', '08:00:00', '16:00:00', 2),
('Afternoon Shift', 'Afternoon', '14:00:00', '22:00:00', 2),
('Doctor Round', 'Doctor', '09:00:00', '10:00:00', 2);

-- Insert Sample Medicines
INSERT INTO Medicines (name, description, dosage_unit, category, classification) VALUES 
('Paracetamol', 'Pain relief and fever reducer', 'mg', 'Pain Relief', 'Over-the-counter'),
('Ibuprofen', 'Anti-inflammatory pain relief', 'mg', 'Pain Relief', 'Over-the-counter'),
('Insulin', 'Blood sugar management', 'units', 'Endocrine', 'Prescription'),
('Morphine', 'Strong pain relief', 'mg', 'Pain Relief', 'Controlled'),
('Amoxicillin', 'Antibiotic for bacterial infections', 'mg', 'Antibiotic', 'Prescription'),
('Metformin', 'Diabetes medication', 'mg', 'Endocrine', 'Prescription'),
('Lisinopril', 'ACE inhibitor for hypertension', 'mg', 'Cardiovascular', 'Prescription'),
('Aspirin', 'Blood thinner and pain relief', 'mg', 'Cardiovascular', 'Over-the-counter');

-- Insert Sample Residents with Doctor Assignments
INSERT INTO Residents (first_name, last_name, gender, birth_date, admission_date, assigned_doctor_id, medical_condition, emergency_contact) VALUES 
('John', 'Doe', 'M', '1980-05-15', '2024-01-15', 2, 'Diabetes Type 2', 'Jane Doe - 555-0101'),
('Mary', 'Smith', 'F', '1975-08-22', '2024-01-20', 2, 'Hypertension', 'Bob Smith - 555-0102'),
('Robert', 'Johnson', 'M', '1965-12-10', '2024-01-25', 3, 'Post-surgery recovery', 'Linda Johnson - 555-0103'),
('Sarah', 'Wilson', 'F', '1990-03-08', '2024-02-01', 3, 'Pneumonia', 'Mike Wilson - 555-0104');

