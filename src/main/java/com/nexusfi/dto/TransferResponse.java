package com.nexusfi.dto;

import com.nexusfi.model.Transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for transfer responses.
 * Contains only the data that should be exposed to clients.
 */
public class TransferResponse {

    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate transferDate;
    private Long sourceCategoryId;
    private String sourceCategoryName;
    private Long destinationCategoryId;
    private String destinationCategoryName;
    private LocalDateTime createdAt;

    // Constructors
    public TransferResponse() {
    }

    public TransferResponse(Long id, BigDecimal amount, String description, LocalDate transferDate,
                           Long sourceCategoryId, String sourceCategoryName,
                           Long destinationCategoryId, String destinationCategoryName,
                           LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.transferDate = transferDate;
        this.sourceCategoryId = sourceCategoryId;
        this.sourceCategoryName = sourceCategoryName;
        this.destinationCategoryId = destinationCategoryId;
        this.destinationCategoryName = destinationCategoryName;
        this.createdAt = createdAt;
    }

    /**
     * Converts a Transfer entity to a TransferResponse DTO.
     * 
     * @param transfer the Transfer entity
     * @return TransferResponse DTO
     */
    public static TransferResponse fromEntity(Transfer transfer) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getAmount(),
            transfer.getDescription(),
            transfer.getTransferDate(),
            transfer.getSourceCategory().getId(),
            transfer.getSourceCategory().getName(),
            transfer.getDestinationCategory().getId(),
            transfer.getDestinationCategory().getName(),
            transfer.getCreatedAt()
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

    public String getSourceCategoryName() {
        return sourceCategoryName;
    }

    public void setSourceCategoryName(String sourceCategoryName) {
        this.sourceCategoryName = sourceCategoryName;
    }

    public Long getDestinationCategoryId() {
        return destinationCategoryId;
    }

    public void setDestinationCategoryId(Long destinationCategoryId) {
        this.destinationCategoryId = destinationCategoryId;
    }

    public String getDestinationCategoryName() {
        return destinationCategoryName;
    }

    public void setDestinationCategoryName(String destinationCategoryName) {
        this.destinationCategoryName = destinationCategoryName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
