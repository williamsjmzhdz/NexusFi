package com.nexusfi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for executing transfers between categories.
 * Contains validation rules for input data.
 */
public class TransferRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @NotNull(message = "Source category ID is required")
    private Long sourceCategoryId;

    @NotNull(message = "Destination category ID is required")
    private Long destinationCategoryId;

    // Constructors
    public TransferRequest() {
    }

    public TransferRequest(BigDecimal amount, String description, LocalDate transferDate,
                          Long sourceCategoryId, Long destinationCategoryId) {
        this.amount = amount;
        this.description = description;
        this.transferDate = transferDate;
        this.sourceCategoryId = sourceCategoryId;
        this.destinationCategoryId = destinationCategoryId;
    }

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public Long getSourceCategoryId() {
        return sourceCategoryId;
    }

    public void setSourceCategoryId(Long sourceCategoryId) {
        this.sourceCategoryId = sourceCategoryId;
    }

    public Long getDestinationCategoryId() {
        return destinationCategoryId;
    }

    public void setDestinationCategoryId(Long destinationCategoryId) {
        this.destinationCategoryId = destinationCategoryId;
    }
}
