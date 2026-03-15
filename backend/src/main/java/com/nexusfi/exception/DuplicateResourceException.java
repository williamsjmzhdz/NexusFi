package com.nexusfi.exception;

/**
 * Thrown when attempting to create a resource that already exists.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends NexusFiException {
    
    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s already exists: %s", resourceType, identifier));
    }
    
    public DuplicateResourceException(String message) {
        super(message);
    }
}
