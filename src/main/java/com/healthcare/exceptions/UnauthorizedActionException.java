package com.healthcare.exceptions;

/**
 * Exception thrown when a staff member is not authorized to perform a specific action
 */
public class UnauthorizedActionException extends Exception {
    
    public UnauthorizedActionException(String message) {
        super(message);
    }
    
    public UnauthorizedActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
