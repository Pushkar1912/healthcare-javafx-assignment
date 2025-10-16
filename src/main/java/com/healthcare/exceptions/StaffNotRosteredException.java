package com.healthcare.exceptions;

/**
 * Exception thrown when a staff member is not rostered for the current day/time
 */
public class StaffNotRosteredException extends Exception {
    
    public StaffNotRosteredException(String message) {
        super(message);
    }
    
    public StaffNotRosteredException(String message, Throwable cause) {
        super(message, cause);
    }
}
