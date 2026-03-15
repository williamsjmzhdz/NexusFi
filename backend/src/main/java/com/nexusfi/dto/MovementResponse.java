package com.nexusfi.dto;

import com.nexusfi.model.Movement;
import com.nexusfi.model.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for movement responses.
 * Movements are read-only records of all transactions.
 * Contains only the data that should be exposed to clients.
 */
public class MovementResponse {

    private Long id;
    private BigDecimal amount;
    private MovementType type;
    private String description;
    private LocalDate movementDate;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;

    // Optional references (depending on type)
    private Long incomeRecordId;
    private Long expenseRecordId;
    private Long transferId;

    // Constructors
    public MovementResponse() {
    }

    public MovementResponse(Long id, BigDecimal amount, MovementType type, String description,
                           LocalDate movementDate, Long categoryId, String categoryName,
                           LocalDateTime createdAt, Long incomeRecordId, Long expenseRecordId,
                           Long transferId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.movementDate = movementDate;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.incomeRecordId = incomeRecordId;
        this.expenseRecordId = expenseRecordId;
        this.transferId = transferId;
    }

    /**
     * Converts a Movement entity to a MovementResponse DTO.
     * 
     * @param movement the Movement entity
     * @return MovementResponse DTO
     */
    public static MovementResponse fromEntity(Movement movement) {
        return new MovementResponse(
            movement.getId(),
            movement.getAmount(),
            movement.getType(),
            movement.getDescription(),
            movement.getMovementDate(),
            movement.getCategory().getId(),
            movement.getCategory().getName(),
            movement.getCreatedAt(),
            movement.getIncomeRecord() != null ? movement.getIncomeRecord().getId() : null,
            movement.getExpenseRecord() != null ? movement.getExpenseRecord().getId() : null,
            movement.getTransfer() != null ? movement.getTransfer().getId() : null
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

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
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

    public Long getIncomeRecordId() {
        return incomeRecordId;
    }

    public void setIncomeRecordId(Long incomeRecordId) {
        this.incomeRecordId = incomeRecordId;
    }

    public Long getExpenseRecordId() {
        return expenseRecordId;
    }

    public void setExpenseRecordId(Long expenseRecordId) {
        this.expenseRecordId = expenseRecordId;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }
}
