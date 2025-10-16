package com.nexusfi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
 * Transfer entity - Records transfers between categories
 * Zero-sum operation: amount debited from source = amount credited to destination
 * Ensures accounting integrity
 */
@Entity
@Table(
    name = "transfers",
    indexes = {
        @Index(name = "idx_transfer_source", columnList = "source_category_id"),
        @Index(name = "idx_transfer_destination", columnList = "destination_category_id"),
        @Index(name = "idx_transfer_user", columnList = "user_id"),
        @Index(name = "idx_transfer_date", columnList = "transfer_date"),
        @Index(name = "idx_transfer_user_date", columnList = "user_id, transfer_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate transferDate;

    /**
     * Category from which money is transferred (debit)
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transfer_source"))
    private Category sourceCategory;

    /**
     * Category to which money is transferred (credit)
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transfer_destination"))
    private Category destinationCategory;

    /**
     * Owner of this transfer
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transfer_user"))
    private User user;

    /**
     * Movements generated from this transfer (2 movements: debit and credit)
     */
    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Movement> movements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to add a movement
     */
    public void addMovement(Movement movement) {
        movements.add(movement);
        movement.setTransfer(this);
    }

    /**
     * Lifecycle callback - runs before insert
     * Validates transfer and sets timestamps
     */
    @PrePersist
    private void onPrePersist() {
        validateTransfer();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback - runs before update
     * Validates transfer and updates timestamp
     */
    @PreUpdate
    private void onPreUpdate() {
        validateTransfer();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validation to ensure source and destination are different
     */
    private void validateTransfer() {
        if (sourceCategory != null && destinationCategory != null 
            && sourceCategory.getId().equals(destinationCategory.getId())) {
            throw new IllegalStateException("Source and destination categories must be different");
        }
    }
}
