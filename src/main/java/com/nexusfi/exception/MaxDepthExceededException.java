package com.nexusfi.exception;

/**
 * Exception thrown when attempting to create a category beyond the maximum allowed depth.
 * NexusFi supports a maximum of 2 levels: categories (level 1) and subcategories (level 2).
 */
public class MaxDepthExceededException extends NexusFiException {
    
    public MaxDepthExceededException(String message) {
        super(message);
    }
    
    public MaxDepthExceededException() {
        super("Cannot create sub-subcategory. Maximum 2 levels allowed (categories and subcategories).");
    }
}
