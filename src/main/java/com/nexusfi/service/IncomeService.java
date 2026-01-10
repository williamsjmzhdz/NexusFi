package com.nexusfi.service;

import com.nexusfi.exception.InvalidPercentageException;
import com.nexusfi.exception.ResourceNotFoundException;
import com.nexusfi.model.Category;
import com.nexusfi.model.IncomeRecord;
import com.nexusfi.model.Movement;
import com.nexusfi.model.enums.MovementType;
import com.nexusfi.repository.IncomeRecordRepository;
import com.nexusfi.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for income-related operations.
 * Handles income recording and automatic HIERARCHICAL distribution to categories.
 * 
 * Distribution Algorithm:
 * 1. Income is first distributed to ROOT categories based on their percentages
 * 2. Each category's share is then recursively distributed to its children (if any)
 * 3. Only LEAF categories (no children) accumulate balance
 * 4. Parent categories with children act as "pass-through" - they distribute to children
 */
@Service
@Transactional
public class IncomeService {
    
    private final IncomeRecordRepository incomeRecordRepository;
    private final MovementRepository movementRepository;
    private final CategoryService categoryService;
    
    public IncomeService(
        IncomeRecordRepository incomeRecordRepository,
        MovementRepository movementRepository,
        CategoryService categoryService
    ) {
        this.incomeRecordRepository = incomeRecordRepository;
        this.movementRepository = movementRepository;
        this.categoryService = categoryService;
    }
    
    /**
     * Record income and automatically distribute to categories hierarchically.
     * This is the CORE algorithm of NexusFi.
     *
     * Algorithm:
     * 1. Validate that user has ROOT categories with 100% allocated
     * 2. Save the income record
     * 3. For each root category, calculate its share (amount × percentage)
     * 4. Recursively distribute to subcategories
     * 5. Create movement records for leaf category assignments
     *
     * @param incomeRecord the income to record
     * @return the saved income record
     * @throws InvalidPercentageException if user doesn't have 100% allocated
     */
    public IncomeRecord recordIncome(IncomeRecord incomeRecord) {
        Long userId = incomeRecord.getUser().getId();
        BigDecimal totalAmount = incomeRecord.getAmount();
        
        // Step 1: Get ROOT categories only (top level)
        List<Category> rootCategories = categoryService.getRootCategories(userId);
        
        if (rootCategories.isEmpty()) {
            throw new InvalidPercentageException("Cannot record income: No categories defined. Create categories first.");
        }
        
        // Validate root categories sum to 100%
        BigDecimal totalPercentage = rootCategories.stream()
            .map(Category::getAssignedPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidPercentageException(
                String.format("Cannot record income: Root category percentages is %s%%. Must be exactly 100%%.", 
                    totalPercentage.toString())
            );
        }
        
        // Step 2: Save income record
        IncomeRecord savedIncome = incomeRecordRepository.save(incomeRecord);
        
        // Step 3: Distribute to root categories, which will recursively distribute to children
        distributeIncomeToCategories(savedIncome, rootCategories, totalAmount);
        
        return savedIncome;
    }
    
    /**
     * Distribute income across categories based on their percentages.
     * Recursively distributes to subcategories.
     *
     * @param incomeRecord the saved income record
     * @param categories the categories at this level
     * @param amountToDistribute the amount to distribute at this level
     */
    private void distributeIncomeToCategories(IncomeRecord incomeRecord, List<Category> categories, BigDecimal amountToDistribute) {
        BigDecimal distributed = BigDecimal.ZERO;
        
        // Distribute to all categories except the last one
        for (int i = 0; i < categories.size() - 1; i++) {
            Category category = categories.get(i);
            
            // Calculate share: amount × (percentage / 100)
            BigDecimal percentage = category.getAssignedPercentage();
            BigDecimal share = amountToDistribute
                .multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            // Distribute this share (either to children or to this category as leaf)
            distributeToCategory(incomeRecord, category, share);
            
            distributed = distributed.add(share);
        }
        
        // Last category gets the remainder (handles rounding errors)
        if (!categories.isEmpty()) {
            Category lastCategory = categories.get(categories.size() - 1);
            BigDecimal remainder = amountToDistribute.subtract(distributed);
            distributeToCategory(incomeRecord, lastCategory, remainder);
        }
    }
    
    /**
     * Distribute amount to a single category.
     * If category has children, recursively distribute to them.
     * If category is a leaf (no children), add to its balance.
     *
     * @param incomeRecord the income record
     * @param category the category receiving the amount
     * @param amount the amount to distribute
     */
    private void distributeToCategory(IncomeRecord incomeRecord, Category category, BigDecimal amount) {
        // Get active children for this category
        List<Category> children = categoryService.getSubcategories(category.getId());
        
        if (children.isEmpty()) {
            // LEAF category - accumulate balance and create movement
            category.setCurrentBalance(category.getCurrentBalance().add(amount));
            createAssignmentMovement(incomeRecord, category, amount);
        } else {
            // PARENT category - distribute to children
            // First validate children sum to 100%
            BigDecimal childrenPercentage = children.stream()
                .map(Category::getAssignedPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (childrenPercentage.compareTo(new BigDecimal("100")) != 0) {
                // If children don't sum to 100%, put remainder in parent category
                // This allows partial distribution during setup
                BigDecimal toChildren = amount
                    .multiply(childrenPercentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal toParent = amount.subtract(toChildren);
                
                // Distribute to children
                distributeIncomeToCategories(incomeRecord, children, toChildren);
                
                // Keep remainder in parent
                if (toParent.compareTo(BigDecimal.ZERO) > 0) {
                    category.setCurrentBalance(category.getCurrentBalance().add(toParent));
                    createAssignmentMovement(incomeRecord, category, toParent);
                }
            } else {
                // Children sum to 100% - distribute all to children
                distributeIncomeToCategories(incomeRecord, children, amount);
            }
        }
    }
    
    /**
     * Create an ASSIGNMENT movement for income distribution.
     */
    private void createAssignmentMovement(IncomeRecord incomeRecord, Category category, BigDecimal amount) {
        Movement movement = Movement.builder()
            .amount(amount)
            .type(MovementType.ASSIGNMENT)
            .description("Income distribution: " + incomeRecord.getSource())
            .movementDate(incomeRecord.getIncomeDate())
            .category(category)
            .user(incomeRecord.getUser())
            .incomeRecord(incomeRecord)
            .build();
        movementRepository.save(movement);
    }
    
    /**
     * Get all income records for a user.
     *
     * @param userId the user's ID
     * @return list of income records
     */
    public List<IncomeRecord> getUserIncomeRecords(Long userId) {
        return incomeRecordRepository.findByUserIdOrderByIncomeDateDesc(userId);
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
        LocalDate startDate, 
        LocalDate endDate
    ) {
        return incomeRecordRepository.findByUserIdAndIncomeDateBetween(userId, startDate, endDate);
    }
    
    /**
     * Get a single income record by ID.
     *
     * @param incomeId the income record ID
     * @return the income record
     * @throws ResourceNotFoundException if not found
     */
    public IncomeRecord getIncomeRecordById(Long incomeId) {
        return incomeRecordRepository.findById(incomeId)
            .orElseThrow(() -> new ResourceNotFoundException("Income record", incomeId));
    }
}