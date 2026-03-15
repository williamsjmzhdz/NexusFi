package com.nexusfi.service;

import com.nexusfi.exception.InvalidPercentageException;
import com.nexusfi.exception.MaxDepthExceededException;
import com.nexusfi.exception.ResourceNotFoundException;
import com.nexusfi.model.Category;
import com.nexusfi.model.User;
import com.nexusfi.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for category-related operations.
 * Supports hierarchical categories with percentage validation per level.
 * 
 * Business rules:
 * - Root categories' percentages must sum to 100% (of total income)
 * - Subcategories' percentages must sum to 100% (of parent's allocation)
 * - Maximum 2 levels allowed (categories and subcategories, no sub-sub-categories)
 * - A category can only be deleted if it has no children and zero balance
 */
@Service
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Create a new category for a user.
     * Can be a root category (parentId = null) or subcategory (parentId != null).
     * Validates that sibling percentages don't exceed 100%.
     * Enforces maximum 2-level hierarchy (no sub-sub-categories).
     *
     * @param category the category to create
     * @param parentId optional parent category ID for subcategories
     * @return the saved category
     * @throws InvalidPercentageException if percentages would exceed 100%
     * @throws ResourceNotFoundException if parent category not found
     * @throws IllegalStateException if trying to create level 3+ category
     */
    public Category createCategory(Category category, Long parentId) {
        // If this is a subcategory, set the parent
        if (parentId != null) {
            Category parent = categoryRepository.findByIdAndUserId(parentId, category.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category", parentId));
            
            // Enforce 2-level maximum: parent must be a root category (no grandparent)
            if (parent.getParent() != null) {
                throw new MaxDepthExceededException();
            }
            
            category.setParent(parent);
        }
        
        // Validate percentages for this level
        validatePercentageForLevel(
            category.getUser().getId(),
            parentId,
            category.getAssignedPercentage(),
            null
        );
        
        return categoryRepository.save(category);
    }
    
    /**
     * Create a new category (backward compatible - assumes root category).
     */
    public Category createCategory(Category category) {
        return createCategory(category, null);
    }
    
    /**
     * Update an existing category.
     * Validates that sibling percentages don't exceed 100%.
     *
     * @param category the category to update
     * @return the updated category
     * @throws InvalidPercentageException if percentages would exceed 100%
     */
    public Category updateCategory(Category category) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        
        validatePercentageForLevel(
            category.getUser().getId(), 
            parentId,
            category.getAssignedPercentage(), 
            category.getId()
        );
        return categoryRepository.save(category);
    }
    
    /**
     * Soft delete a category (set isActive = false).
     * Validates the category can be deleted.
     *
     * @param categoryId the category ID to delete
     * @param userId the user's ID for ownership verification
     * @throws ResourceNotFoundException if category not found
     * @throws IllegalStateException if category has active children or non-zero balance
     */
    public void deleteCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        
        // Check for active children
        List<Category> activeChildren = categoryRepository.findByParentIdAndIsActiveTrue(categoryId);
        if (!activeChildren.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with active subcategories. Delete subcategories first.");
        }
        
        // Check for non-zero balance
        if (category.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot delete category with non-zero balance. Transfer funds first.");
        }
        
        // Soft delete
        category.setIsActive(false);
        category.setAssignedPercentage(BigDecimal.ZERO);
        categoryRepository.save(category);
    }
    
    /**
     * Delete a category (backward compatible - no user verification).
     * @deprecated Use deleteCategory(Long categoryId, Long userId) instead
     */
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        deleteCategory(categoryId, category.getUser().getId());
    }
    
    /**
     * Get all active categories for a user (flat list).
     *
     * @param userId the user's ID
     * @return list of all active categories
     */
    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    /**
     * Get root categories for a user (top level only).
     *
     * @param userId the user's ID
     * @return list of root categories
     */
    public List<Category> getRootCategories(Long userId) {
        return categoryRepository.findRootCategoriesByUserId(userId);
    }
    
    /**
     * Get subcategories of a parent category.
     *
     * @param parentId the parent category's ID
     * @return list of child categories
     */
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId);
    }
    
    /**
     * Get a category by ID with children eagerly loaded.
     * Only returns active categories.
     *
     * @param categoryId the category ID
     * @return the category with children
     * @throws ResourceNotFoundException if category not found or inactive
     */
    public Category getCategoryWithChildren(Long categoryId) {
        return categoryRepository.findByIdWithChildren(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
    
    /**
     * Get a category by ID.
     * Only returns active categories.
     *
     * @param categoryId the category ID
     * @return the category
     * @throws ResourceNotFoundException if category not found or inactive
     */
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findByIdAndIsActiveTrue(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
    
    /**
     * Get a category by ID with ownership verification.
     * Only returns active categories.
     *
     * @param categoryId the category ID
     * @param userId the user's ID
     * @return the category
     * @throws ResourceNotFoundException if category not found or not owned by user
     */
    public Category getCategoryByIdAndUser(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
    
    /**
     * Validates that adding/updating a category doesn't exceed 100% for that level.
     * Root categories are validated against other root categories.
     * Subcategories are validated against siblings (same parent).
     *
     * @param userId the user's ID
     * @param parentId the parent category ID (null for root categories)
     * @param newPercentage the percentage being added/changed
     * @param excludeCategoryId category ID to exclude (when updating)
     * @throws InvalidPercentageException if total would exceed 100%
     */
    private void validatePercentageForLevel(Long userId, Long parentId, BigDecimal newPercentage, Long excludeCategoryId) {
        BigDecimal currentSum;
        
        if (parentId == null) {
            // Root level - sum of all root categories
            currentSum = categoryRepository.sumRootCategoryPercentagesByUserId(userId);
        } else {
            // Subcategory level - sum of siblings
            currentSum = categoryRepository.sumChildCategoryPercentages(parentId);
        }
        
        // If updating, subtract the old percentage first
        if (excludeCategoryId != null) {
            Category existingCategory = categoryRepository.findById(excludeCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", excludeCategoryId));
            currentSum = currentSum.subtract(existingCategory.getAssignedPercentage());
        }
        
        // Calculate new total
        BigDecimal newTotal = currentSum.add(newPercentage);
        
        // Validate - allow exactly 100% or less
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidPercentageException(newTotal);
        }
    }
    
    /**
     * Calculate remaining unassigned percentage at a specific level.
     *
     * @param userId the user's ID
     * @param parentId the parent category ID (null for root level)
     * @return percentage remaining (0-100)
     */
    public BigDecimal getRemainingPercentage(Long userId, Long parentId) {
        BigDecimal currentSum;
        
        if (parentId == null) {
            currentSum = categoryRepository.sumRootCategoryPercentagesByUserId(userId);
        } else {
            currentSum = categoryRepository.sumChildCategoryPercentages(parentId);
        }
        
        return new BigDecimal("100").subtract(currentSum);
    }
    
    /**
     * Calculate remaining unassigned percentage for root categories.
     */
    public BigDecimal getRemainingPercentage(Long userId) {
        return getRemainingPercentage(userId, null);
    }
}