package com.healthcare.exceptions;

/**
 * Exception thrown when shift assignments violate business rules
 * (e.g., more than 8 hours assigned to a nurse in a single day)
 */
public class ShiftComplianceException extends Exception {
    
    public ShiftComplianceException(String message) {
        super(message);
    }
    
    public ShiftComplianceException(String message, Throwable cause) {
        super(message, cause);
    }
}
