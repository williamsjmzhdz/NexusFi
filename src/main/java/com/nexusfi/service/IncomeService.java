package com.nexusfi.service;

import com.nexusfi.model.Category;
import com.nexusfi.model.IncomeRecord;
import com.nexusfi.model.Movement;
import com.nexusfi.repository.IncomeRecordRepository;
import com.nexusfi.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for income-related operations.
 * Handles income recording and automatic distribution to categories.
 */
@Service
@Transactional
public class IncomeService {
    
    private final IncomeRecordRepository incomeRecordRepository;
    private final CategoryService categoryService;
    
    public IncomeService(
        IncomeRecordRepository incomeRecordRepository,
        CategoryService categoryService
    ) {
        this.incomeRecordRepository = incomeRecordRepository;
        this.categoryService = categoryService;
    }
    
    /**
     * Record income and automatically distribute to categories.
     * This is the CORE algorithm of NexusFi.
     *
     * Algorithm:
     * 1. Validate that user has categories with 100% allocated
     * 2. Save the income record
     * 3. For each category, calculate its share (amount × percentage)
     * 4. Update category balances
     * 5. Create movement records (done automatically by DB trigger)
     *
     * @param incomeRecord the income to record
     * @return the saved income record
     * @throws IllegalArgumentException if user doesn't have 100% allocated
     */
    public IncomeRecord recordIncome(IncomeRecord incomeRecord) {
        Long userId = incomeRecord.getUser().getId();
        BigDecimal totalAmount = incomeRecord.getAmount();
        
        // Step 1: Validate user has exactly 100% allocated
        List<Category> categories = categoryService.getUserCategories(userId);
        
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Cannot record income: No categories defined. Create categories first.");
        }
        
        BigDecimal totalPercentage = categories.stream()
            .map(Category::getAssignedPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException(
                String.format("Cannot record income: Total category percentages is %s%%. Must be exactly 100%%.", 
                    totalPercentage.toString())
            );
        }
        
        // Step 2: Save income record
        IncomeRecord savedIncome = incomeRecordRepository.save(incomeRecord);
        
        // Step 3 & 4: Distribute to categories
        distributeIncomeToCategories(savedIncome, categories);
        
        return savedIncome;
    }
    
    /**
     * Distribute income across categories based on their percentages.
     * Updates category balances.
     *
     * @param incomeRecord the saved income record
     * @param categories the user's categories
     */
    private void distributeIncomeToCategories(IncomeRecord incomeRecord, List<Category> categories) {
        BigDecimal totalAmount = incomeRecord.getAmount();
        BigDecimal distributed = BigDecimal.ZERO;
        
        // Distribute to all categories except the last one
        for (int i = 0; i < categories.size() - 1; i++) {
            Category category = categories.get(i);
            
            // Calculate share: amount × (percentage / 100)
            BigDecimal percentage = category.getAssignedPercentage();
            BigDecimal share = totalAmount
                .multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            // Update category balance
            category.setCurrentBalance(category.getCurrentBalance().add(share));
            
            distributed = distributed.add(share);
        }
        
        // Last category gets the remainder (handles rounding errors)
        Category lastCategory = categories.get(categories.size() - 1);
        BigDecimal remainder = totalAmount.subtract(distributed);
        lastCategory.setCurrentBalance(lastCategory.getCurrentBalance().add(remainder));
    }
    
    /**
     * Get all income records for a user.
     *
     * @param userId the user's ID
     * @return list of income records
     */
    public List<IncomeRecord> getUserIncomeRecords(Long userId) {
        return incomeRecordRepository.findByUserIdOrderByRecordedAtDesc(userId);
    }
    
    /**
     * Get income records within a date range.
     *
     * @param userId the user's ID
     * @param startDate start of range
     * @param endDate end of range
     * @return list of income records
     */
    public List<IncomeRecord> getIncomeRecordsByDateRange(
        Long userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    ) {
        return incomeRecordRepository.findByUserIdAndRecordedAtBetween(userId, startDate, endDate);
    }
    
    /**
     * Get a single income record by ID.
     *
     * @param incomeId the income record ID
     * @return the income record
     * @throws IllegalArgumentException if not found
     */
    public IncomeRecord getIncomeRecordById(Long incomeId) {
        return incomeRecordRepository.findById(incomeId)
            .orElseThrow(() -> new IllegalArgumentException("Income record not found: " + incomeId));
    }
}