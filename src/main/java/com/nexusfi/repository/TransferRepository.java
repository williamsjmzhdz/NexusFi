package com.nexusfi.repository;

import com.nexusfi.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Transfer entity.
 * Provides methods to query transfers by user and date range.
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    
    /**
     * Find all transfers for a specific user.
     *
     * @param userId the user's ID
     * @return list of transfers, ordered by date descending (newest first)
     */
    List<Transfer> findByUserIdOrderByRecordedAtDesc(Long userId);
    
    /**
     * Find transfers for a user within a date range.
     *
     * @param userId the user's ID
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return list of transfers in the date range
     */
    List<Transfer> findByUserIdAndRecordedAtBetween(
        Long userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    /**
     * Find all transfers involving a specific category (as source or destination).
     * Useful to see transfer history for a category.
     *
     * @param sourceCategoryId the category's ID (as source)
     * @param destinationCategoryId the category's ID (as destination)
     * @return list of transfers involving the category
     */
    List<Transfer> findBySourceCategoryIdOrDestinationCategoryIdOrderByRecordedAtDesc(
        Long sourceCategoryId, 
        Long destinationCategoryId
    );
}
