package com.nexusfi.dto;

import com.nexusfi.model.IncomeRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for income responses.
 * Contains only the data that should be exposed to clients.
 */
public class IncomeResponse {

    private Long id;
    private BigDecimal amount;
    private String source;
    private String description;
    private LocalDate incomeDate;
    private LocalDateTime createdAt;

    // Constructors
    public IncomeResponse() {
    }

    public IncomeResponse(Long id, BigDecimal amount, String source, String description, 
                         LocalDate incomeDate, LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.source = source;
        this.description = description;
        this.incomeDate = incomeDate;
        this.createdAt = createdAt;
    }

    /**
     * Converts an IncomeRecord entity to an IncomeResponse DTO.
     * 
     * @param incomeRecord the IncomeRecord entity
     * @return IncomeResponse DTO
     */
    public static IncomeResponse fromEntity(IncomeRecord incomeRecord) {
        return new IncomeResponse(
            incomeRecord.getId(),
            incomeRecord.getAmount(),
            incomeRecord.getSource(),
            incomeRecord.getDescription(),
            incomeRecord.getIncomeDate(),
            incomeRecord.getCreatedAt()
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
