package com.nexusfi.exception;

import java.math.BigDecimal;

/**
 * Thrown when category percentages don't sum to 100% or would exceed 100%.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidPercentageException extends NexusFiException {
    
    public InvalidPercentageException(BigDecimal total) {
        super(String.format("Total percentage would be %s%%. Maximum is 100%%.", total.toString()));
    }
    
    public InvalidPercentageException(String message) {
        super(message);
    }
}
