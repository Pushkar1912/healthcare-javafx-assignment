# Testing Guide - What Tests We Have

## Overview
We have a comprehensive testing setup that makes sure our healthcare system works correctly. Think of tests as quality checks that run automatically to catch problems before they reach real users.

## Test Files and What They Do

### 🧪 **SimpleBusinessLogicTest.java**
**What it tests**: Core business rules without touching the database
**Why it's important**: Tests the logic that makes our system work correctly

**Tests included**:
- ✅ Staff scheduling rules (nurses can't work more than 8 hours)
- ✅ Bed allocation logic (can't put two patients in the same bed)
- ✅ Staff permissions (managers can add staff, doctors can prescribe)
- ✅ Medication timing (medicines given at the right times)
- ✅ Audit logging (everything gets recorded properly)
- ✅ Complete workflow (admission → prescription → medication)

### 🔗 **HealthcareSystemIntegrationTest.java**
**What it tests**: Full system integration with real database operations
**Why it's important**: Tests how all parts work together in real scenarios

**Tests included**:
- ✅ Staff scheduling compliance (real shift assignments)
- ✅ Bed allocation rules (actual bed assignments)
- ✅ Staff permissions (real user role checks)
- ✅ Medication administration (full prescription workflow)
- ✅ Audit logging (database logging verification)
- ✅ Complete workflow integration (end-to-end patient care)

### 🏗️ **Test Services** (TestStaffService, TestResidentService, TestBedManagementService)
**What they do**: Special versions of our services that use a test database
**Why we need them**: Keeps our tests separate from real data

## How to Run Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Files
```bash
# Test just the business logic
mvn test -Dtest=SimpleBusinessLogicTest

# Test just the integration
mvn test -Dtest=HealthcareSystemIntegrationTest

# Test both working tests
mvn test -Dtest=SimpleBusinessLogicTest,HealthcareSystemIntegrationTest
```

## What Each Test Checks

### Staff Scheduling Tests
- Nurses can't work more than 8 hours per day
- We always have a doctor assigned
- Shift schedules follow the rules

### Bed Management Tests
- Patients can only be assigned to empty beds
- Bed transfers work correctly
- No double-booking of beds

### Permission Tests
- Managers can add new staff
- Doctors can create prescriptions
- Nurses can administer medications
- Each role can only do what they're supposed to

### Medication Tests
- Medicines are given at the correct times
- Prescriptions are created properly
- Administration is logged correctly

### Audit Tests
- Every action gets recorded
- Logs include who did what and when
- No important actions are missed

## Test Results
When tests pass, you'll see:
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

This means everything is working correctly! 🎉


