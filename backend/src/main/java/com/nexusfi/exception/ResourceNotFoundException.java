package com.nexusfi.exception;

/**
 * Thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends NexusFiException {
    
    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
