package com.nexusfi.controller;

import com.nexusfi.dto.CategoryRequest;
import com.nexusfi.dto.CategoryResponse;
import com.nexusfi.model.Category;
import com.nexusfi.model.User;
import com.nexusfi.security.CustomUserDetails;
import com.nexusfi.service.CategoryService;
import com.nexusfi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing budget categories.
 * Handles CRUD operations for hierarchical categories and subcategories.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Create a new category or subcategory for the authenticated user.
     * If parentId is provided, creates a subcategory under that parent.
     * 
     * POST /api/v1/categories
     * 
     * @param request the category data (includes optional parentId)
     * @param userDetails the authenticated user
     * @return 201 Created with the created category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        User user = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Build category entity
        Category category = Category.builder()
            .name(request.getName())
            .assignedPercentage(request.getPercentage())
            .user(user)
            .build();
        
        // Create category (service handles parent relationship)
        Category savedCategory = categoryService.createCategory(category, request.getParentId());
        CategoryResponse response = CategoryResponse.fromEntity(savedCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all categories for the authenticated user (flat list).
     * 
     * GET /api/v1/categories
     * 
     * @param userDetails the authenticated user
     * @return 200 OK with list of all categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<Category> categories = categoryService.getUserCategories(userDetails.getId());
        List<CategoryResponse> response = categories.stream()
            .map(CategoryResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get root categories for the authenticated user (top level only).
     * 
     * GET /api/v1/categories/root
     * 
     * @param userDetails the authenticated user
     * @return 200 OK with list of root categories
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<Category> categories = categoryService.getRootCategories(userDetails.getId());
        List<CategoryResponse> response = categories.stream()
            .map(CategoryResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get category tree for the authenticated user (hierarchical structure).
     * Returns root categories with nested children.
     * 
     * GET /api/v1/categories/tree
     * 
     * @param userDetails the authenticated user
     * @return 200 OK with hierarchical category tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<Category> rootCategories = categoryService.getRootCategories(userDetails.getId());
        
        // Convert to tree structure with children
        List<CategoryResponse> response = rootCategories.stream()
            .map(root -> {
                Category withChildren = categoryService.getCategoryWithChildren(root.getId());
                return CategoryResponse.fromEntityWithChildren(withChildren);
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get subcategories of a specific category.
     * 
     * GET /api/v1/categories/{id}/subcategories
     * 
     * @param id the parent category ID
     * @param userDetails the authenticated user
     * @return 200 OK with list of subcategories
     */
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Verify ownership
        categoryService.getCategoryByIdAndUser(id, userDetails.getId());
        
        List<Category> subcategories = categoryService.getSubcategories(id);
        List<CategoryResponse> response = subcategories.stream()
            .map(CategoryResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single category by ID with its subcategories.
     * 
     * GET /api/v1/categories/{id}
     * 
     * @param id the category ID
     * @param userDetails the authenticated user
     * @return 200 OK with the category and its children, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Verify ownership and get with children
        categoryService.getCategoryByIdAndUser(id, userDetails.getId());
        Category category = categoryService.getCategoryWithChildren(id);
        CategoryResponse response = CategoryResponse.fromEntityWithChildren(category);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing category.
     * 
     * PUT /api/v1/categories/{id}
     * 
     * @param id the category ID
     * @param request the updated category data
     * @param userDetails the authenticated user
     * @return 200 OK with the updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Verify ownership and get existing category
        Category existingCategory = categoryService.getCategoryByIdAndUser(id, userDetails.getId());
        existingCategory.setName(request.getName());
        existingCategory.setAssignedPercentage(request.getPercentage());
        
        // Note: We don't allow changing parent via update - that would require special handling
        
        Category updatedCategory = categoryService.updateCategory(existingCategory);
        CategoryResponse response = CategoryResponse.fromEntity(updatedCategory);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a category (soft delete).
     * Cannot delete categories with active subcategories or non-zero balance.
     * 
     * DELETE /api/v1/categories/{id}
     * 
     * @param id the category ID
     * @param userDetails the authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        categoryService.deleteCategory(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the remaining unassigned percentage at a specific level.
     * 
     * GET /api/v1/categories/remaining?parentId={parentId}
     * 
     * @param parentId optional parent ID (null for root level)
     * @param userDetails the authenticated user
     * @return 200 OK with the remaining percentage
     */
    @GetMapping("/remaining")
    public ResponseEntity<BigDecimal> getRemainingPercentage(
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // If parentId provided, verify ownership
        if (parentId != null) {
            categoryService.getCategoryByIdAndUser(parentId, userDetails.getId());
        }
        
        BigDecimal remaining = categoryService.getRemainingPercentage(userDetails.getId(), parentId);
        return ResponseEntity.ok(remaining);
    }
}
