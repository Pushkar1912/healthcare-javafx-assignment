package com.healthcare.exceptions;

/**
 * Exception thrown when trying to assign a resident to an already occupied bed
 */
public class BedOccupiedException extends Exception {
    
    public BedOccupiedException(String message) {
        super(message);
    }
    
    public BedOccupiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
