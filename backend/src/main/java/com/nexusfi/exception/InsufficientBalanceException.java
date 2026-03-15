package com.nexusfi.exception;

import java.math.BigDecimal;

/**
 * Thrown when a transaction cannot be completed due to insufficient balance.
 * Maps to HTTP 400 Bad Request.
 */
public class InsufficientBalanceException extends NexusFiException {
    
    public InsufficientBalanceException(String categoryName, BigDecimal available, BigDecimal required) {
        super(String.format(
            "Insufficient balance in category '%s'. Available: %s, Required: %s",
            categoryName,
            available.toString(),
            required.toString()
        ));
    }
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
