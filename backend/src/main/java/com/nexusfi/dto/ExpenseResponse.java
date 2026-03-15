package com.nexusfi.dto;

import com.nexusfi.model.ExpenseRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for expense responses.
 * Contains only the data that should be exposed to clients.
 */
public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private String merchant;
    private String description;
    private LocalDate expenseDate;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;

    // Constructors
    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, BigDecimal amount, String merchant, String description,
                          LocalDate expenseDate, Long categoryId, String categoryName, 
                          LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.description = description;
        this.expenseDate = expenseDate;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
    }

    /**
     * Converts an ExpenseRecord entity to an ExpenseResponse DTO.
     * 
     * @param expenseRecord the ExpenseRecord entity
     * @return ExpenseResponse DTO
     */
    public static ExpenseResponse fromEntity(ExpenseRecord expenseRecord) {
        return new ExpenseResponse(
            expenseRecord.getId(),
            expenseRecord.getAmount(),
            expenseRecord.getMerchant(),
            expenseRecord.getDescription(),
            expenseRecord.getExpenseDate(),
            expenseRecord.getCategory().getId(),
            expenseRecord.getCategory().getName(),
            expenseRecord.getCreatedAt()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
