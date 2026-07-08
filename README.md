# Healthcare Management System

A JavaFX-based healthcare management system for managing residents, staff, and bed assignments in a care home facility. Built as an individual assignment for RMIT's COSC1295 Advanced Programming course - the brief called for a maintainable, extensible OO system demonstrating encapsulation, inheritance, polymorphism, design patterns, generics, JavaFX GUIs, unit testing, and object-relational persistence.

Full docs (setup, database design, testing, and a reflective report on design decisions) are in [`docs/`](./docs).

## Overview

This system manages high-care patients in a care home with two wards, each containing multiple rooms with varying bed capacities. It provides a visual interface for mapping patients to beds, managing staff, and tracking medication administration, along with the audit/compliance rules the assignment spec required (shift-hour limits, role-based authorization, action logging).

## System Architecture

### Ward Structure
- **Ward 1**: 6 rooms with 1-4 beds per room
- **Ward 2**: 6 rooms with 1-4 beds per room
- **Total Capacity**: Variable based on room configuration

### Key Features
- **Resident Management**: Admit, discharge, and transfer residents
- **Staff Management**: Manage doctors, nurses, and managers
- **Bed Assignment**: Intelligent bed allocation based on gender and medical conditions
- **Medication Tracking**: Prescription management and administration logging
- **Role-Based Access**: Different permissions for managers, doctors, and nurses

## Design Patterns & OOP Concepts

- **MVC**: JavaFX controllers separate from model entities and service-layer business logic
- **Interface-driven services**: `IBedManagementService`, `IResidentService`, `IStaffService` decouple business logic from implementation
- **Custom exceptions**: `BedOccupiedException`, `ShiftComplianceException`, `StaffNotRosteredException`, `UnauthorizedActionException` enforce the assignment's business rules (no double-booking, shift-hour limits, role-based authorization) instead of failing silently
- **Object-relational persistence**: JPA/Hibernate-backed persistence (`persistence.xml`) alongside a serialization path for saving/restoring in-memory state between runs

## Technology Stack

- **Frontend**: JavaFX 21
- **Backend**: Java 17
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Architecture**: MVC (Model-View-Controller)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/Pushkar1912/healthcare-javafx-assignment.git
cd healthcare-javafx-assignment
```

### 2. Start the Database
```bash
docker-compose up -d
```
This will start:
- MySQL database on port 3306
- phpMyAdmin on port 8080
- Wait for approx 45 sec to getting containers up and running.

### 3.. Verify the docker containers status
```bash
docker ps
```

### 4.. Build and Run the Application
```bash
mvn clean compile
mvn javafx:run
```

## Database Schema

The system uses an improved database schema with the following key tables:

### Core Tables
- **Staff**: Managers, doctors, and nurses with role-based access
- **Residents**: Patient information with medical conditions
- **Beds**: Individual bed assignments with type and restrictions
- **Rooms**: Room management within wards
- **Wards**: Two fixed wards (Ward 1 & Ward 2)

### Enhanced Features
- **Bed Types**: Standard, Electric, Special
- **Gender Restrictions**: Male, Female, Mixed rooms
- **Isolation Support**: Special beds for isolation requirements
- **Medical Conditions**: Track resident medical needs

## User Roles & Permissions

### Manager
- Add/modify staff members
- Admit new residents
- View all system data
- Manage bed assignments

### Doctor
- Add prescriptions for residents
- View resident medical information
- Check medication schedules

### Nurse
- Administer medications
- Move residents between beds
- Update medication administration records

## Key Functionality

### Resident Management
- **Admission**: Add new residents with medical conditions
- **Bed Assignment**: Intelligent allocation based on gender and isolation needs
- **Discharge**: Archive resident data and free up beds
- **Transfer**: Move residents between beds

### Staff Management
- **Role-Based Access**: Different permissions for each role
- **Authentication**: Secure login system
- **Profile Management**: Update staff information

### Bed Management
- **Visual Mapping**: See bed occupancy at a glance
- **Smart Allocation**: Automatic bed assignment based on requirements
- **Type Management**: Different bed types for different needs

## Testing

The system includes comprehensive testing:
- **Unit Tests**: JUnit tests for business logic
- **Integration Tests**: Database interaction testing
- **Exception Handling**: Robust error management

Run tests with:
```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/com/healthcare/
│   │   ├── controller/          # JavaFX Controllers
│   │   ├── model/              # Entity Models
│   │   ├── services/           # Business Logic
│   │   └── config/             # Database Configuration
│   └── resources/
│       └── fxml/               # JavaFX UI Files
├── test/                       # Test Files
└── database/
    └── init/                   # Database Initialization Scripts
```

## Configuration

### Database Configuration
- **Host**: localhost:3306
- **Database**: healthcare_db
- **Username**: `<your-db-username>`
- **Password**: `<your-db-password>`

(Set via the `docker-compose.yml` environment variables - do not commit real credentials.)

### Application Settings
- **Default Manager**: `<manager-username>` / `<manager-password>` (seeded in the database init script - change before any real deployment)
- **Ward Structure**: 2 wards, 6 rooms each
- **Bed Types**: Standard, Electric, Special

## Rules

### Bed Assignment Rules
- Gender-specific rooms when required
- Isolation beds for contagious patients
- No double-booking of beds
- Automatic bed type selection based on medical needs

### Staff Management Rules
- Only managers can add/modify staff
- Role-based access control
- Secure authentication required

### Medication Rules
- Only doctors can prescribe medications
- Only nurses can administer medications
- All actions are logged with timestamps

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure Docker is running
   - Check if MySQL container is up: `docker ps`
   - Restart containers: `docker-compose restart`

2. **JavaFX Runtime Error**
   - Ensure Java 17+ is installed
   - Check Maven JavaFX plugin configuration

3. **Port Already in Use**
   - Stop other services using ports 3306 or 8080
   - Modify docker-compose.yml if needed

## Future Enhancements

- **Reporting System**: Generate reports for management
- **Mobile Interface**: Mobile app for nurses
- **Integration**: Connect with external medical systems
- **Analytics**: Patient care analytics and insights