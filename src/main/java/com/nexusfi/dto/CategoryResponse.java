package com.nexusfi.dto;

import com.nexusfi.model.Category;

import java.math.BigDecimal;

/**
 * DTO for category responses.
 * Contains only the data that should be exposed to clients.
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private BigDecimal percentage;
    private BigDecimal balance;

    // Constructors
    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, BigDecimal percentage, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.percentage = percentage;
        this.balance = balance;
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO.
     * Balance must be calculated separately and set afterwards.
     * 
     * @param category the Category entity
     * @return CategoryResponse DTO
     */
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getAssignedPercentage(),
            BigDecimal.ZERO  // Balance will be calculated by service layer
        );
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO with balance.
     * 
     * @param category the Category entity
     * @param balance the calculated balance for this category
     * @return CategoryResponse DTO
     */
    public static CategoryResponse fromEntity(Category category, BigDecimal balance) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getAssignedPercentage(),
            balance
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
