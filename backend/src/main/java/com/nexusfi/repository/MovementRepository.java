package com.nexusfi.repository;

import com.nexusfi.model.Movement;
import com.nexusfi.model.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Movement entity.
 * This is a READ-ONLY view that combines income, expense, and transfer records.
 * No insert/update/delete methods should be used - movements are created
 * automatically by the database view.
 */
@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    
    /**
     * Find all movements for a specific user.
     * Shows complete transaction history in chronological order.
     *
     * @param userId the user's ID
     * @return list of all movements, ordered by date descending (newest first)
     */
    List<Movement> findByUserIdOrderByMovementDateDesc(Long userId);
    
    /**
     * Find movements for a user within a date range.
     * Useful for monthly/yearly reports.
     *
     * @param userId the user's ID
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return list of movements in the date range
     */
    List<Movement> findByUserIdAndMovementDateBetween(
        Long userId, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    /**
     * Find movements by type for a specific user.
     * Filter to show only income, expenses, or transfers.
     *
     * @param userId the user's ID
     * @param type the movement type (INCOME, EXPENSE, TRANSFER)
     * @return list of movements of the specified type
     */
    List<Movement> findByUserIdAndTypeOrderByMovementDateDesc(
        Long userId, 
        MovementType type
    );
    
    /**
     * Find all movements for a specific category.
     * Shows all income distributions, expenses, and transfers for a category.
     *
     * @param categoryId the category's ID
     * @return list of movements involving the category
     */
    List<Movement> findByCategoryIdOrderByMovementDateDesc(Long categoryId);
}
