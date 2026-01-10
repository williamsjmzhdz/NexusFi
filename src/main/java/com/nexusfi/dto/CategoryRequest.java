package com.nexusfi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for creating and updating categories.
 * Contains validation rules for input data.
 * Supports hierarchical categories via parentId.
 */
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Percentage must be at least 0")
    @DecimalMax(value = "100.00", inclusive = true, message = "Percentage cannot exceed 100")
    private BigDecimal percentage;
    
    /**
     * Parent category ID for subcategories.
     * If null, this is a root category (percentage of total income).
     * If set, percentage is relative to parent's allocation.
     */
    private Long parentId;

    // Constructors
    public CategoryRequest() {
    }

    public CategoryRequest(String name, BigDecimal percentage) {
        this.name = name;
        this.percentage = percentage;
    }
    
    public CategoryRequest(String name, BigDecimal percentage, Long parentId) {
        this.name = name;
        this.percentage = percentage;
        this.parentId = parentId;
    }

    // Getters and Setters
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
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
