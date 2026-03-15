package com.nexusfi.repository;

import com.nexusfi.model.ExpenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ExpenseRecord entity.
 * Provides methods to query expense records by user, category, and date range.
 */
@Repository
public interface ExpenseRecordRepository extends JpaRepository<ExpenseRecord, Long> {
    
    /**
     * Find all expense records for a specific user.
     *
     * @param userId the user's ID
     * @return list of expense records, ordered by date descending (newest first)
     */
    List<ExpenseRecord> findByUserIdOrderByExpenseDateDesc(Long userId);
    
    /**
     * Find expense records for a user within a date range.
     * Useful for monthly/yearly reports.
     *
     * @param userId the user's ID
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return list of expense records in the date range
     */
    List<ExpenseRecord> findByUserIdAndExpenseDateBetween(
        Long userId, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    /**
     * Find all expense records for a specific category.
     * Useful to see spending history per category.
     *
     * @param categoryId the category's ID
     * @return list of expense records for the category
     */
    List<ExpenseRecord> findByCategoryIdOrderByExpenseDateDesc(Long categoryId);
}
