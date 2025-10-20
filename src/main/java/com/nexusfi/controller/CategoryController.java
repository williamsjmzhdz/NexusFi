package com.nexusfi.controller;

import com.nexusfi.dto.CategoryRequest;
import com.nexusfi.dto.CategoryResponse;
import com.nexusfi.model.Category;
import com.nexusfi.model.User;
import com.nexusfi.service.CategoryService;
import com.nexusfi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing budget categories.
 * Handles CRUD operations and percentage allocation queries.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Create a new category for the authenticated user.
     * 
     * POST /api/categories
     * 
     * @param request the category data
     * @return 201 Created with the created category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        // TODO: Get authenticated user from SecurityContext
        // For now, we'll use a hardcoded user ID (will be replaced with Spring Security)
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Build category entity
        Category category = Category.builder()
            .name(request.getName())
            .assignedPercentage(request.getPercentage())
            .user(user)
            .build();
        
        Category savedCategory = categoryService.createCategory(category);
        CategoryResponse response = CategoryResponse.fromEntity(savedCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all categories for the authenticated user.
     * 
     * GET /api/categories
     * 
     * @return 200 OK with list of categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Category> categories = categoryService.getUserCategories(user.getId());
        List<CategoryResponse> response = categories.stream()
            .map(CategoryResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single category by ID.
     * 
     * GET /api/categories/{id}
     * 
     * @param id the category ID
     * @return 200 OK with the category, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        // TODO: Verify category belongs to authenticated user
        Category category = categoryService.getCategoryById(id);
        CategoryResponse response = CategoryResponse.fromEntity(category);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing category.
     * 
     * PUT /api/categories/{id}
     * 
     * @param id the category ID
     * @param request the updated category data
     * @return 200 OK with the updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        
        // TODO: Verify category belongs to authenticated user
        // Get existing category and update it
        Category existingCategory = categoryService.getCategoryById(id);
        existingCategory.setName(request.getName());
        existingCategory.setAssignedPercentage(request.getPercentage());
        
        Category updatedCategory = categoryService.updateCategory(existingCategory);
        CategoryResponse response = CategoryResponse.fromEntity(updatedCategory);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a category.
     * 
     * DELETE /api/categories/{id}
     * 
     * @param id the category ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        // TODO: Verify category belongs to authenticated user
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the remaining unassigned percentage for the authenticated user.
     * 
     * GET /api/categories/remaining
     * 
     * @return 200 OK with the remaining percentage
     */
    @GetMapping("/remaining")
    public ResponseEntity<BigDecimal> getRemainingPercentage() {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal remaining = categoryService.getRemainingPercentage(user.getId());
        return ResponseEntity.ok(remaining);
    }
}
