package com.nexusfi.repository;

import com.nexusfi.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity.
 * Provides methods to query categories by user and calculate totals.
 * Supports hierarchical category structures.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find all categories belonging to a specific user.
     *
     * @param userId the user's ID
     * @return list of categories owned by the user
     */
    List<Category> findByUserId(Long userId);

    /**
     * Find all active categories belonging to a specific user.
     *
     * @param userId the user's ID
     * @return list of active categories owned by the user
     */
    List<Category> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all root categories (no parent) for a user.
     * These are the top-level categories.
     *
     * @param userId the user's ID
     * @return list of root categories
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.parent IS NULL AND c.isActive = true")
    List<Category> findRootCategoriesByUserId(@Param("userId") Long userId);

    /**
     * Find all children of a specific category.
     *
     * @param parentId the parent category's ID
     * @return list of child categories
     */
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);

    /**
     * Find a category by ID with eager loading of children.
     *
     * @param categoryId the category ID
     * @return the category with children loaded
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :categoryId AND c.isActive = true")
    Optional<Category> findByIdWithChildren(@Param("categoryId") Long categoryId);

    /**
     * Find a category by ID and user ID for ownership verification.
     * Only returns active categories.
     *
     * @param categoryId the category ID
     * @param userId the user's ID
     * @return the category if found, active, and owned by user
     */
    Optional<Category> findByIdAndUserIdAndIsActiveTrue(Long categoryId, Long userId);

    /**
     * Find a category by ID and user ID (legacy, includes inactive).
     *
     * @param categoryId the category ID
     * @param userId the user's ID
     * @return the category if found and owned by user
     */
    Optional<Category> findByIdAndUserId(Long categoryId, Long userId);

    /**
     * Find an active category by ID.
     *
     * @param categoryId the category ID
     * @return the active category if found
     */
    Optional<Category> findByIdAndIsActiveTrue(Long categoryId);

    /**
     * Calculate the sum of assigned percentages for a user's ROOT categories only.
     * Used to validate that root percentages sum to 100%.
     *
     * @param userId the user's ID
     * @return the sum of root category percentages
     */
    @Query("SELECT COALESCE(SUM(c.assignedPercentage), 0) FROM Category c WHERE c.user.id = :userId AND c.parent IS NULL AND c.isActive = true")
    BigDecimal sumRootCategoryPercentagesByUserId(@Param("userId") Long userId);

    /**
     * Calculate the sum of assigned percentages for all of a user's categories.
     * @deprecated Use sumRootCategoryPercentagesByUserId for hierarchical validation
     *
     * @param userId the user's ID
     * @return the sum of percentages, or 0 if user has no categories
     */
    @Query("SELECT COALESCE(SUM(c.assignedPercentage), 0) FROM Category c WHERE c.user.id = :userId")
    BigDecimal sumAssignedPercentagesByUserId(@Param("userId") Long userId);

    /**
     * Calculate the sum of assigned percentages for sibling categories (same parent).
     * Used to validate that sibling percentages sum to 100%.
     *
     * @param parentId the parent category's ID
     * @return the sum of sibling percentages
     */
    @Query("SELECT COALESCE(SUM(c.assignedPercentage), 0) FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true")
    BigDecimal sumChildCategoryPercentages(@Param("parentId") Long parentId);

}
