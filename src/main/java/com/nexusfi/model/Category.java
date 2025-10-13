package com.nexusfi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity - Budget categories with hierarchical structure
 * Supports parent-child relationships for subcategories
 * Enforces allocation integrity: sibling categories must sum to 100%
 */
@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_category_user", columnList = "user_id"),
        @Index(name = "idx_category_parent", columnList = "parent_id"),
        @Index(name = "idx_category_active", columnList = "is_active"),
        @Index(name = "idx_category_user_active", columnList = "user_id, is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_category_name_parent_user",
            columnNames = {"name", "parent_id", "user_id"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Assigned percentage of parent's amount (or total income if root category)
     * Must sum to 100% with sibling categories
     */
    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @DecimalMax(value = "100.00", inclusive = true)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal assignedPercentage;

    /**
     * Current balance in this category
     * Updated by income assignments, expenses, and transfers
     */
    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    /**
     * Soft delete flag
     * Can only be archived when balance = 0 and assignedPercentage = 0
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Hierarchical relationship - parent category
     * Null for root categories
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_category_parent"))
    private Category parent;

    /**
     * Hierarchical relationship - child categories
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    /**
     * Owner of this category
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_category_user"))
    private User user;

    /**
     * Movements associated with this category
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Movement> movements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to check if this is a root category
     */
    public boolean isRootCategory() {
        return parent == null;
    }

    /**
     * Helper method to check if this category has children
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Helper method to add a child category
     */
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Helper method to remove a child category
     */
    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }
}
