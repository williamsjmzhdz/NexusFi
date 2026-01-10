package com.nexusfi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Expense Record entity - Records expenses from categories
 * Represents money leaving the system
 */
@Entity
@Table(
    name = "expense_records",
    indexes = {
        @Index(name = "idx_expense_category", columnList = "category_id"),
        @Index(name = "idx_expense_user", columnList = "user_id"),
        @Index(name = "idx_expense_date", columnList = "expense_date"),
        @Index(name = "idx_expense_user_date", columnList = "user_id, expense_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String merchant;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate expenseDate;

    /**
     * Category from which the expense is made
     */
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_category"))
    private Category category;

    /**
     * Owner of this expense record
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_user"))
    private User user;

    /**
     * Movements generated from this expense (EXPENSE type)
     */
    @OneToMany(mappedBy = "expenseRecord", cascade = CascadeType.ALL, orphanRemoval = true)
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
     * Helper method to add a movement
     */
    public void addMovement(Movement movement) {
        movements.add(movement);
        movement.setExpenseRecord(this);
    }
}
