package com.secure.notes.exceptions;

/**
 * Exception thrown when an admin attempts to perform operations on another admin
 */
public class UnauthorizedAdminOperationException extends RuntimeException {
    
    public UnauthorizedAdminOperationException(String message) {
        super(message);
    }
    
    public UnauthorizedAdminOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}