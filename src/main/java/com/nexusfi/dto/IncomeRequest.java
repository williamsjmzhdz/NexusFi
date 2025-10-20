package com.nexusfi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for recording income.
 * Contains validation rules for input data.
 */
public class IncomeRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Source is required")
    private String source;

    private String description;

    @NotNull(message = "Income date is required")
    private LocalDate incomeDate;

    // Constructors
    public IncomeRequest() {
    }

    public IncomeRequest(BigDecimal amount, String source, String description, LocalDate incomeDate) {
        this.amount = amount;
        this.source = source;
        this.description = description;
        this.incomeDate = incomeDate;
    }

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getIncomeDate() {
        return incomeDate;
    }

    public void setIncomeDate(LocalDate incomeDate) {
        this.incomeDate = incomeDate;
    }
}
