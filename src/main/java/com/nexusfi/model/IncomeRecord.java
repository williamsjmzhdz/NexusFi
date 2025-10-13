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
 * Income Record entity - Records income entries into the system
 * Triggers automatic distribution to categories based on their percentages
 */
@Entity
@Table(
    name = "income_records",
    indexes = {
        @Index(name = "idx_income_user", columnList = "user_id"),
        @Index(name = "idx_income_date", columnList = "income_date"),
        @Index(name = "idx_income_user_date", columnList = "user_id, income_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String source;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate incomeDate;

    /**
     * Owner of this income record
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_income_user"))
    private User user;

    /**
     * Movements generated from this income (ASSIGNMENT type)
     */
    @OneToMany(mappedBy = "incomeRecord", cascade = CascadeType.ALL, orphanRemoval = true)
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
        movement.setIncomeRecord(this);
    }
}
