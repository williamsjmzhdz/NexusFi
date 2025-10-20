package com.nexusfi.exception;

/**
 * Base exception for all NexusFi business logic exceptions.
 * All custom exceptions should extend this class.
 */
public class NexusFiException extends RuntimeException {
    
    public NexusFiException(String message) {
        super(message);
    }
    
    public NexusFiException(String message, Throwable cause) {
        super(message, cause);
    }
}
