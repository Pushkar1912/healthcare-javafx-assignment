# Healthcare Database Overview

## What This Database Does

This database is the backbone of the healthcare management system. Think of it as a digital filing cabinet that keeps track of everything happening in a care home facility - from patients and staff to medications and schedules.

## Core Tables and Their Purpose

### Wards & Rooms
- **Wards**: Two main areas (Ward 1 & Ward 2) where residents stay
- **Rooms**: Each ward has 6 rooms that can hold 1-4 beds depending on the room type
- **Beds**: Individual sleeping spaces for residents, with codes like "W1R101B1" (Ward 1, Room 101, Bed 1)

### People Management
- **Staff**: Managers, doctors, and nurses with their login details
- **Residents**: The patients staying in the facility
- **Shifts**: Work schedules for staff members to ensure proper coverage

### Medical Care
- **Medicines**: Medicine inventory with details like dosage and category
- **Prescriptions**: Doctor's orders for what medicines patients need
- **Administered Medication**: Records of when nurses actually give medicines to patients

### Operations & Tracking
- **Shift Schedule**: Who's working when and where
- **Actions Log**: Every important action gets recorded here for accountability
- **Bed Transfers**: When patients move between beds
- **Archive**: Historical records of discharged patients

## How It All Works Together

1. **Patient Admission**: When someone comes in, they get assigned to a bed and a doctor
2. **Daily Operations**: Nurses work shifts, doctors make rounds, medications get administered
3. **Record Keeping**: Everything gets logged so I can track what happened when
4. **Compliance**: The system enforces the staffing rules from the assignment spec (e.g. shift limits)

## Key Features

- **Flexible Bed Management**: Beds can be standard, electric, or special care
- **Role-Based Access**: Different staff types have different permissions
- **Audit Trail**: Every action is tracked with timestamps and staff IDs
- **Compliance Tracking**: Ensures proper nurse-to-patient ratios and shift coverage

This database design keeps the healthcare facility's records consistent while tracking every patient care activity.
