package com.nexusfi.dto;

import com.nexusfi.model.Category;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for category responses.
 * Supports hierarchical structure with parent/children relationships.
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private BigDecimal percentage;
    private BigDecimal currentBalance;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;

    // Constructors
    public CategoryResponse() {
        this.children = new ArrayList<>();
    }

    public CategoryResponse(Long id, String name, BigDecimal percentage, BigDecimal currentBalance,
                           Long parentId, String parentName, List<CategoryResponse> children) {
        this.id = id;
        this.name = name;
        this.percentage = percentage;
        this.currentBalance = currentBalance;
        this.parentId = parentId;
        this.parentName = parentName;
        this.children = children != null ? children : new ArrayList<>();
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO (flat, no children).
     * 
     * @param category the Category entity
     * @return CategoryResponse DTO
     */
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getAssignedPercentage(),
            category.getCurrentBalance(),
            category.getParent() != null ? category.getParent().getId() : null,
            category.getParent() != null ? category.getParent().getName() : null,
            new ArrayList<>()
        );
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO with children (tree structure).
     * Use this for hierarchical views.
     * 
     * @param category the Category entity
     * @return CategoryResponse DTO with nested children
     */
    public static CategoryResponse fromEntityWithChildren(Category category) {
        List<CategoryResponse> childResponses = new ArrayList<>();
        
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            childResponses = category.getChildren().stream()
                .filter(Category::getIsActive)
                .map(CategoryResponse::fromEntityWithChildren)
                .collect(Collectors.toList());
        }
        
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getAssignedPercentage(),
            category.getCurrentBalance(),
            category.getParent() != null ? category.getParent().getId() : null,
            category.getParent() != null ? category.getParent().getName() : null,
            childResponses
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

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public List<CategoryResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryResponse> children) {
        this.children = children;
    }
}
