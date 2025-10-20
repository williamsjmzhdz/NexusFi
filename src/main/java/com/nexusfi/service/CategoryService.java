package com.nexusfi.service;

import com.nexusfi.exception.InvalidPercentageException;
import com.nexusfi.exception.ResourceNotFoundException;
import com.nexusfi.model.Category;
import com.nexusfi.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for category-related operations.
 * Enforces the core business rule: user's category percentages must sum to 100%.
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
     * Validates that total percentages don't exceed 100%.
     *
     * @param category the category to create
     * @return the saved category
     * @throws InvalidPercentageException if percentages would exceed 100%
     */
    public Category createCategory(Category category) {
        validatePercentageLimit(category.getUser().getId(), category.getAssignedPercentage(), null);
        return categoryRepository.save(category);
    }
    
    /**
     * Update an existing category.
     * Validates that total percentages don't exceed 100%.
     *
     * @param category the category to update
     * @return the updated category
     * @throws InvalidPercentageException if percentages would exceed 100%
     */
    public Category updateCategory(Category category) {
        validatePercentageLimit(
            category.getUser().getId(), 
            category.getAssignedPercentage(), 
            category.getId()
        );
        return categoryRepository.save(category);
    }
    
    /**
     * Delete a category.
     *
     * @param categoryId the category ID to delete
     */
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
    
    /**
     * Get all categories for a user.
     *
     * @param userId the user's ID
     * @return list of categories
     */
    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findByUserId(userId);
    }
    
    /**
     * Get a category by ID.
     *
     * @param categoryId the category ID
     * @return the category
     * @throws ResourceNotFoundException if category not found
     */
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
    
    /**
     * Validates that adding/updating a category doesn't exceed 100% total.
     *
     * @param userId the user's ID
     * @param newPercentage the percentage being added/changed
     * @param excludeCategoryId category ID to exclude (when updating, exclude current value)
     * @throws InvalidPercentageException if total would exceed 100%
     * @throws ResourceNotFoundException if category to update is not found
     */
    private void validatePercentageLimit(Long userId, BigDecimal newPercentage, Long excludeCategoryId) {
        // Get current sum of all percentages
        BigDecimal currentSum = categoryRepository.sumAssignedPercentagesByUserId(userId);
        
        // If updating, subtract the old percentage first
        if (excludeCategoryId != null) {
            Category existingCategory = categoryRepository.findById(excludeCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", excludeCategoryId));
            currentSum = currentSum.subtract(existingCategory.getAssignedPercentage());
        }
        
        // Calculate new total
        BigDecimal newTotal = currentSum.add(newPercentage);
        
        // Validate
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidPercentageException(newTotal);
        }
    }
    
    /**
     * Calculate remaining unassigned percentage for a user.
     * Useful for UI to show available percentage.
     *
     * @param userId the user's ID
     * @return percentage remaining (0-100)
     */
    public BigDecimal getRemainingPercentage(Long userId) {
        BigDecimal currentSum = categoryRepository.sumAssignedPercentagesByUserId(userId);
        return new BigDecimal("100").subtract(currentSum);
    }
}