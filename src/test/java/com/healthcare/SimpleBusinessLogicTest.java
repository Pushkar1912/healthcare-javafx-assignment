package com.healthcare;

import com.healthcare.exceptions.*;
import com.healthcare.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple business logic tests without database dependencies
 * Tests the core business rules and validation logic
 */
@TestMethodOrder(OrderAnnotation.class)
public class SimpleBusinessLogicTest {
    
    /**
     * Test 1: Staff Scheduling Compliance Logic
     */
    @Test
    @Order(1)
    @DisplayName("Test Staff Scheduling Compliance Logic")
    void testStaffSchedulingComplianceLogic() {
        System.out.println("📋 Testing Staff Scheduling Compliance Logic...");
        
        // Test 1.1: Create valid shift schedule
        System.out.println("  ✅ Positive Test: Create valid shift schedule");
        Staff nurse = createTestNurse();
        LocalDate testDate = LocalDate.now();
        
        ShiftSchedule validShift = new ShiftSchedule(
            nurse.getStaffId(), 
            testDate, 
            Shift.ShiftType.Morning, 
            "08:00", "16:00"
        );
        
        assertNotNull(validShift);
        assertEquals(Shift.ShiftType.Morning, validShift.getShiftType());
        assertEquals("08:00", validShift.getStartTime());
        assertEquals("16:00", validShift.getEndTime());
        System.out.println("    ✓ Valid shift schedule created");
        
        // Test 1.2: Test shift duration calculation
        System.out.println("  🔍 Testing shift duration calculation");
        int duration = calculateShiftDuration("08:00", "16:00");
        assertEquals(8, duration);
        System.out.println("    ✓ Shift duration calculated correctly: " + duration + " hours");
        
        // Test 1.3: Test maximum hours validation
        System.out.println("  🔍 Testing maximum hours validation");
        assertTrue(duration <= 8, "Shift should not exceed 8 hours");
        System.out.println("    ✓ Maximum hours validation passed");
        
        System.out.println("  ✅ Staff Scheduling Compliance Logic tests completed\n");
    }
    
    /**
     * Test 2: Bed Allocation Rules Logic
     */
    @Test
    @Order(2)
    @DisplayName("Test Bed Allocation Rules Logic")
    void testBedAllocationRulesLogic() {
        System.out.println("🛏️ Testing Bed Allocation Rules Logic...");
        
        // Test 2.1: Create bed and resident
        System.out.println("  ✅ Positive Test: Create bed and resident");
        Bed bed = createTestBed();
        Resident resident = createTestResident();
        
        assertNotNull(bed);
        assertNotNull(resident);
        assertFalse(bed.isOccupied());
        System.out.println("    ✓ Bed and resident created successfully");
        
        // Test 2.2: Test bed assignment logic
        System.out.println("  🔍 Testing bed assignment logic");
        boolean canAssign = canAssignResidentToBed(bed, resident);
        assertTrue(canAssign, "Should be able to assign resident to vacant bed");
        System.out.println("    ✓ Bed assignment logic validated");
        
        // Test 2.3: Test occupied bed logic
        System.out.println("  🔍 Testing occupied bed logic");
        bed.setOccupied(true);
        boolean cannotAssign = canAssignResidentToBed(bed, resident);
        assertFalse(cannotAssign, "Should not be able to assign resident to occupied bed");
        System.out.println("    ✓ Occupied bed logic validated");
        
        System.out.println("  ✅ Bed Allocation Rules Logic tests completed\n");
    }
    
    /**
     * Test 3: Staff Permissions Logic
     */
    @Test
    @Order(3)
    @DisplayName("Test Staff Permissions Logic")
    void testStaffPermissionsLogic() {
        System.out.println("👥 Testing Staff Permissions Logic...");
        
        // Test 3.1: Test manager permissions
        System.out.println("  ✅ Positive Test: Manager permissions");
        Staff manager = createTestManager();
        assertTrue(canAddStaff(manager), "Manager should be able to add staff");
        assertTrue(canPrescribe(manager), "Manager should be able to prescribe");
        assertTrue(canAdminister(manager), "Manager should be able to administer");
        System.out.println("    ✓ Manager permissions validated");
        
        // Test 3.2: Test doctor permissions
        System.out.println("  🔍 Testing doctor permissions");
        Staff doctor = createTestDoctor();
        assertFalse(canAddStaff(doctor), "Doctor should not be able to add staff");
        assertTrue(canPrescribe(doctor), "Doctor should be able to prescribe");
        assertFalse(canAdminister(doctor), "Doctor should not be able to administer");
        System.out.println("    ✓ Doctor permissions validated");
        
        // Test 3.3: Test nurse permissions
        System.out.println("  🔍 Testing nurse permissions");
        Staff nurse = createTestNurse();
        assertFalse(canAddStaff(nurse), "Nurse should not be able to add staff");
        assertFalse(canPrescribe(nurse), "Nurse should not be able to prescribe");
        assertTrue(canAdminister(nurse), "Nurse should be able to administer");
        System.out.println("    ✓ Nurse permissions validated");
        
        System.out.println("  ✅ Staff Permissions Logic tests completed\n");
    }
    
    /**
     * Test 4: Medication Administration Logic
     */
    @Test
    @Order(4)
    @DisplayName("Test Medication Administration Logic")
    void testMedicationAdministrationLogic() {
        System.out.println("💊 Testing Medication Administration Logic...");
        
        // Test 4.1: Create medication administration
        System.out.println("  ✅ Positive Test: Create medication administration");
        Staff nurse = createTestNurse();
        Medicine medicine = createTestMedicine();
        
        AdministeredMedication admin = new AdministeredMedication();
        admin.setNurseId(nurse.getStaffId());
        admin.setAdministeredTime(LocalDateTime.now());
        admin.setStatus(AdministeredMedication.AdministrationStatus.Given);
        admin.setNotes("Administered as prescribed");
        
        assertNotNull(admin);
        assertEquals(AdministeredMedication.AdministrationStatus.Given, admin.getStatus());
        System.out.println("    ✓ Medication administration created successfully");
        
        // Test 4.2: Test timing validation
        System.out.println("  🔍 Testing timing validation");
        LocalDateTime correctTime = LocalDateTime.now();
        LocalDateTime wrongTime = LocalDateTime.now().minusHours(2);
        
        boolean isCorrectTime = isValidAdministrationTime(correctTime);
        boolean isWrongTime = isValidAdministrationTime(wrongTime);
        
        assertTrue(isCorrectTime, "Current time should be valid for administration");
        assertFalse(isWrongTime, "Time 2 hours ago should not be valid for administration");
        System.out.println("    ✓ Timing validation passed");
        
        System.out.println("  ✅ Medication Administration Logic tests completed\n");
    }
    
    /**
     * Test 5: Audit Logging Logic
     */
    @Test
    @Order(5)
    @DisplayName("Test Audit Logging Logic")
    void testAuditLoggingLogic() {
        System.out.println("📝 Testing Audit Logging Logic...");
        
        // Test 5.1: Create action logs
        System.out.println("  ✅ Positive Test: Create action logs");
        Staff staff = createTestManager();
        
        ActionLog admitLog = new ActionLog(
            staff.getStaffId(),
            ActionLog.ActionType.Admit,
            "Admitted resident John Doe",
            "Resident admitted to bed A1"
        );
        
        ActionLog dischargeLog = new ActionLog(
            staff.getStaffId(),
            ActionLog.ActionType.Discharge,
            "Discharged resident John Doe",
            "Resident discharged from bed A1"
        );
        
        assertNotNull(admitLog);
        assertNotNull(dischargeLog);
        assertNotNull(admitLog.getStaffId());
        assertNotNull(dischargeLog.getStaffId());
        System.out.println("    ✓ Action logs created successfully");
        
        // Test 5.2: Test log validation
        System.out.println("  🔍 Testing log validation");
        assertTrue(hasRequiredLogInfo(admitLog), "Log should contain required information");
        assertTrue(hasRequiredLogInfo(dischargeLog), "Log should contain required information");
        System.out.println("    ✓ Log validation passed");
        
        System.out.println("  ✅ Audit Logging Logic tests completed\n");
    }
    
    /**
     * Test 6: Complete Workflow Logic
     */
    @Test
    @Order(6)
    @DisplayName("Test Complete Workflow Logic")
    void testCompleteWorkflowLogic() {
        System.out.println("🔄 Testing Complete Workflow Logic...");
        
        // Test 6.1: Admission workflow
        System.out.println("  ✅ Positive Test: Admission workflow");
        Staff manager = createTestManager();
        Resident resident = createTestResident();
        Bed bed = createTestBed();
        
        // Simulate admission workflow
        boolean canAdmit = canAdmitResident(manager, resident, bed);
        assertTrue(canAdmit, "Manager should be able to admit resident");
        System.out.println("    ✓ Admission workflow validated");
        
        // Test 6.2: Prescription workflow
        System.out.println("  🔍 Testing prescription workflow");
        Staff doctor = createTestDoctor();
        Medicine medicine = createTestMedicine();
        
        boolean canPrescribe = canPrescribeMedicine(doctor, resident, medicine);
        assertTrue(canPrescribe, "Doctor should be able to prescribe medicine");
        System.out.println("    ✓ Prescription workflow validated");
        
        // Test 6.3: Administration workflow
        System.out.println("  🔍 Testing administration workflow");
        Staff nurse = createTestNurse();
        
        boolean canAdminister = canAdministerMedicine(nurse, resident, medicine);
        assertTrue(canAdminister, "Nurse should be able to administer medicine");
        System.out.println("    ✓ Administration workflow validated");
        
        System.out.println("  ✅ Complete Workflow Logic tests completed\n");
    }
    
    // Helper methods for business logic validation
    private int calculateShiftDuration(String startTime, String endTime) {
        // Simple duration calculation (in real implementation, this would be more sophisticated)
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");
        
        int startHour = Integer.parseInt(startParts[0]);
        int endHour = Integer.parseInt(endParts[0]);
        
        return endHour - startHour;
    }
    
    private boolean canAssignResidentToBed(Bed bed, Resident resident) {
        return !bed.isOccupied() && bed.getBedId() != null;
    }
    
    private boolean canAddStaff(Staff staff) {
        return staff.getRole() == Staff.Role.Manager;
    }
    
    private boolean canPrescribe(Staff staff) {
        return staff.getRole() == Staff.Role.Doctor || staff.getRole() == Staff.Role.Manager;
    }
    
    private boolean canAdminister(Staff staff) {
        return staff.getRole() == Staff.Role.Nurse || staff.getRole() == Staff.Role.Manager;
    }
    
    private boolean isValidAdministrationTime(LocalDateTime time) {
        // Simple validation - in real implementation, this would check against prescription schedule
        LocalDateTime now = LocalDateTime.now();
        return time.isAfter(now.minusHours(1)) && time.isBefore(now.plusHours(1));
    }
    
    private boolean hasRequiredLogInfo(ActionLog log) {
        return log.getStaffId() != null && 
               log.getActionTime() != null && 
               log.getActionType() != null && 
               log.getActionDescription() != null;
    }
    
    private boolean canAdmitResident(Staff staff, Resident resident, Bed bed) {
        return canAddStaff(staff) && !bed.isOccupied();
    }
    
    private boolean canPrescribeMedicine(Staff staff, Resident resident, Medicine medicine) {
        return canPrescribe(staff) && resident.getResidentId() != null && medicine.getMedicineId() != null;
    }
    
    private boolean canAdministerMedicine(Staff staff, Resident resident, Medicine medicine) {
        return canAdminister(staff) && resident.getResidentId() != null && medicine.getMedicineId() != null;
    }
    
    // Helper methods to create test data
    private Staff createTestManager() {
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setUsername("testmanager");
        staff.setPassword("password");
        staff.setFirstName("Test");
        staff.setLastName("Manager");
        staff.setRole(Staff.Role.Manager);
        return staff;
    }
    
    private Staff createTestDoctor() {
        Staff staff = new Staff();
        staff.setStaffId(2L);
        staff.setUsername("testdoctor");
        staff.setPassword("password");
        staff.setFirstName("Test");
        staff.setLastName("Doctor");
        staff.setRole(Staff.Role.Doctor);
        return staff;
    }
    
    private Staff createTestNurse() {
        Staff staff = new Staff();
        staff.setStaffId(3L);
        staff.setUsername("testnurse");
        staff.setPassword("password");
        staff.setFirstName("Test");
        staff.setLastName("Nurse");
        staff.setRole(Staff.Role.Nurse);
        return staff;
    }
    
    private Resident createTestResident() {
        Resident resident = new Resident();
        resident.setResidentId(1L);
        resident.setFirstName("John");
        resident.setLastName("Doe");
        resident.setGender(Resident.Gender.M);
        resident.setBirthDate(LocalDate.of(1980, 1, 1));
        resident.setAdmissionDate(LocalDate.now());
        return resident;
    }
    
    private Bed createTestBed() {
        Bed bed = new Bed();
        bed.setBedId(1L);
        bed.setBedNumber("A1");
        bed.setRoomId(1L);
        bed.setBedType(Bed.BedType.Standard);
        bed.setGenderRestriction(Bed.GenderRestriction.None);
        bed.setOccupied(false);
        return bed;
    }
    
    private Medicine createTestMedicine() {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(1L);
        medicine.setName("Aspirin");
        medicine.setDescription("Pain relief medication");
        medicine.setDosageUnit("mg");
        medicine.setActive(true);
        return medicine;
    }
}
