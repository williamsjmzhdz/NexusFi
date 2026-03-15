package com.nexusfi.model;

import com.nexusfi.model.enums.MovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Movement entity - Records all money movements in the system
 * Ensures accounting integrity by tracking all balance changes
 * 
 * Types:
 * - ASSIGNMENT: Income distribution to categories
 * - EXPENSE: Money leaving the system
 * - TRANSFER: Money moving between categories
 * - REBALANCE: Adjustments from percentage changes
 */
@Entity
@Table(
    name = "movements",
    indexes = {
        @Index(name = "idx_movement_category", columnList = "category_id"),
        @Index(name = "idx_movement_user", columnList = "user_id"),
        @Index(name = "idx_movement_type", columnList = "type"),
        @Index(name = "idx_movement_date", columnList = "movement_date"),
        @Index(name = "idx_movement_income", columnList = "income_record_id"),
        @Index(name = "idx_movement_expense", columnList = "expense_record_id"),
        @Index(name = "idx_movement_transfer", columnList = "transfer_id"),
        @Index(name = "idx_movement_user_date", columnList = "user_id, movement_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Amount of the movement
     * Positive for credits (ASSIGNMENT, incoming TRANSFER)
     * Negative for debits (EXPENSE, outgoing TRANSFER, REBALANCE reduction)
     */
    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate movementDate;

    /**
     * Category affected by this movement
     */
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movement_category"))
    private Category category;

    /**
     * Owner of this movement
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movement_user"))
    private User user;

    /**
     * Reference to income record if type is ASSIGNMENT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_record_id", foreignKey = @ForeignKey(name = "fk_movement_income"))
    private IncomeRecord incomeRecord;

    /**
     * Reference to expense record if type is EXPENSE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_record_id", foreignKey = @ForeignKey(name = "fk_movement_expense"))
    private ExpenseRecord expenseRecord;

    /**
     * Reference to transfer if type is TRANSFER
     * Links the two movements (debit and credit) of the same transfer
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", foreignKey = @ForeignKey(name = "fk_movement_transfer"))
    private Transfer transfer;

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
     * Helper method to check if this is a debit movement
     */
    public boolean isDebit() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Helper method to check if this is a credit movement
     */
    public boolean isCredit() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
