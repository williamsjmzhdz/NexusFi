package com.nexusfi.controller;

import com.nexusfi.dto.ExpenseRequest;
import com.nexusfi.dto.ExpenseResponse;
import com.nexusfi.model.Category;
import com.nexusfi.model.ExpenseRecord;
import com.nexusfi.model.User;
import com.nexusfi.service.CategoryService;
import com.nexusfi.service.ExpenseService;
import com.nexusfi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing expense records.
 * Handles expense recording and queries.
 */
@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, CategoryService categoryService, 
                           UserService userService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Record new expense for the authenticated user.
     * Validates sufficient balance in the specified category.
     * 
     * POST /api/v1/expenses
     * 
     * @param request the expense data
     * @return 201 Created with the recorded expense
     */
    @PostMapping
    public ResponseEntity<ExpenseResponse> recordExpense(@Valid @RequestBody ExpenseRequest request) {
        // TODO: Get authenticated user from SecurityContext
        // For now, we'll use a hardcoded user ID (will be replaced with Spring Security)
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the category
        Category category = categoryService.getCategoryById(request.getCategoryId());
        
        // TODO: Verify category belongs to authenticated user
        
        // Build expense record entity
        ExpenseRecord expenseRecord = ExpenseRecord.builder()
            .amount(request.getAmount())
            .merchant(request.getMerchant())
            .description(request.getDescription())
            .expenseDate(request.getExpenseDate())
            .category(category)
            .user(user)
            .build();
        
        ExpenseRecord savedExpense = expenseService.recordExpense(expenseRecord);
        ExpenseResponse response = ExpenseResponse.fromEntity(savedExpense);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all expense records for the authenticated user.
     * 
     * GET /api/v1/expenses
     * 
     * @return 200 OK with list of expense records
     */
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ExpenseRecord> expenses = expenseService.getUserExpenseRecords(user.getId());
        List<ExpenseResponse> response = expenses.stream()
            .map(ExpenseResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single expense record by ID.
     * 
     * GET /api/v1/expenses/{id}
     * 
     * @param id the expense record ID
     * @return 200 OK with the expense record, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        // TODO: Verify expense belongs to authenticated user
        ExpenseRecord expense = expenseService.getExpenseRecordById(id);
        ExpenseResponse response = ExpenseResponse.fromEntity(expense);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all expenses for a specific category.
     * 
     * GET /api/v1/expenses/category/{categoryId}
     * 
     * @param categoryId the category ID
     * @return 200 OK with list of expense records for the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByCategory(@PathVariable Long categoryId) {
        // TODO: Verify category belongs to authenticated user
        List<ExpenseRecord> expenses = expenseService.getCategoryExpenses(categoryId);
        List<ExpenseResponse> response = expenses.stream()
            .map(ExpenseResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
