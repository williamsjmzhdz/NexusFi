package com.nexusfi.service;

import com.nexusfi.exception.InsufficientBalanceException;
import com.nexusfi.exception.ResourceNotFoundException;
import com.nexusfi.model.Category;
import com.nexusfi.model.ExpenseRecord;
import com.nexusfi.repository.ExpenseRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for expense-related operations.
 * Validates sufficient balance before recording expenses.
 */
@Service
@Transactional
public class ExpenseService {
    
    private final ExpenseRecordRepository expenseRecordRepository;
    private final CategoryService categoryService;
    
    public ExpenseService(
        ExpenseRecordRepository expenseRecordRepository,
        CategoryService categoryService
    ) {
        this.expenseRecordRepository = expenseRecordRepository;
        this.categoryService = categoryService;
    }
    
    /**
     * Record an expense from a category.
     * Validates that category has sufficient balance.
     *
     * Algorithm:
     * 1. Get the category
     * 2. Validate sufficient balance
     * 3. Deduct amount from category balance
     * 4. Save expense record
     * 5. Movement record created automatically by DB trigger
     *
     * @param expenseRecord the expense to record
     * @return the saved expense record
     * @throws InsufficientBalanceException if insufficient balance
     */
    public ExpenseRecord recordExpense(ExpenseRecord expenseRecord) {
        Category category = expenseRecord.getCategory();
        BigDecimal expenseAmount = expenseRecord.getAmount();
        BigDecimal currentBalance = category.getCurrentBalance();
        
        // Validate sufficient balance
        if (currentBalance.compareTo(expenseAmount) < 0) {
            throw new InsufficientBalanceException(
                category.getName(),
                currentBalance,
                expenseAmount
            );
        }
        
        // Deduct from category balance
        category.setCurrentBalance(currentBalance.subtract(expenseAmount));
        
        // Save expense record (DB trigger creates movement)
        return expenseRecordRepository.save(expenseRecord);
    }
    
    /**
     * Get all expense records for a user.
     *
     * @param userId the user's ID
     * @return list of expense records
     */
    public List<ExpenseRecord> getUserExpenseRecords(Long userId) {
        return expenseRecordRepository.findByUserIdOrderByRecordedAtDesc(userId);
    }
    
    /**
     * Get expense records within a date range.
     *
     * @param userId the user's ID
     * @param startDate start of range
     * @param endDate end of range
     * @return list of expense records
     */
    public List<ExpenseRecord> getExpenseRecordsByDateRange(
        Long userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return expenseRecordRepository.findByUserIdAndRecordedAtBetween(userId, startDate, endDate);
    }
    
    /**
     * Get all expenses for a specific category.
     *
     * @param categoryId the category ID
     * @return list of expense records
     */
    public List<ExpenseRecord> getCategoryExpenses(Long categoryId) {
        return expenseRecordRepository.findByCategoryIdOrderByRecordedAtDesc(categoryId);
    }
    
    /**
     * Get a single expense record by ID.
     *
     * @param expenseId the expense record ID
     * @return the expense record
     * @throws ResourceNotFoundException if not found
     */
    public ExpenseRecord getExpenseRecordById(Long expenseId) {
        return expenseRecordRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense record", expenseId));
    }
}
