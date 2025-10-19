package com.nexusfi.repository;

import com.nexusfi.model.IncomeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for IncomeRecord entity.
 * Provides methods to query income records by user and date range.
 */
@Repository
public interface IncomeRecordRepository extends JpaRepository<IncomeRecord, Long> {
    
    /**
     * Find all income records for a specific user.
     *
     * @param userId the user's ID
     * @return list of income records, ordered by date descending (newest first)
     */
    List<IncomeRecord> findByUserIdOrderByRecordedAtDesc(Long userId);
    
    /**
     * Find income records for a user within a date range.
     * Useful for monthly/yearly reports.
     *
     * @param userId the user's ID
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return list of income records in the date range
     */
    List<IncomeRecord> findByUserIdAndRecordedAtBetween(
        Long userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
