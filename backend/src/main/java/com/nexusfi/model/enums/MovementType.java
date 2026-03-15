package com.nexusfi.model.enums;

/**
 * Movement types in the NexusFi system
 * - ASSIGNMENT: Automatic distribution of income to categories
 * - EXPENSE: Money leaving the system (reduces category balance)
 * - TRANSFER: Money moving between categories (zero-sum operation)
 * - REBALANCE: Adjustment due to percentage changes (zero-sum operation)
 */
public enum MovementType {
    /**
     * Automatic assignment of income to categories
     * Created when an income is registered
     */
    ASSIGNMENT,

    /**
     * Expense from a category
     * Money leaves the system, reduces total balance
     */
    EXPENSE,

    /**
     * Transfer between categories
     * Zero-sum operation, no change to total balance
     */
    TRANSFER,

    /**
     * Rebalancing due to percentage changes
     * Zero-sum operation between sibling categories
     */
    REBALANCE
}
