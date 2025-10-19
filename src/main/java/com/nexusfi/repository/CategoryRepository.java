package com.nexusfi.repository;

import com.nexusfi.model.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**¨
 * Repository interface for Category entity.
 * Provides method to query categories by user and calculate totals.
 */
public interface CategoryRepository {
    
    /**
     * Find all categories belonging to a specific user.
     *
     * @param userId the user's ID
     * @return list of categories owned by the user
     */
    List<Category> findByUserId(Long userId);

    /**
     * Calculate the sum of assigned percentages for a user's categories.
     * Used to validate that percentages sum to 100%.
     * 
     * @parm userId the user's ID
     * @return the sum of percentages, or 0 if user has no categories
     */
    @Query("SELECT COALESCE(SUM(c.assignedPercentage), 0) FROM Category c WHERE c.user.id = :userId")
    Integer sumAssignedPercentagesByUserId(@Param("userId") Long userId);

}
