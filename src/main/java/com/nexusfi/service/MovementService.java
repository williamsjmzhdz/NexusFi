package com.nexusfi.service;

import com.nexusfi.model.Movement;
import com.nexusfi.model.enums.MovementType;
import com.nexusfi.repository.MovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for querying movement records.
 * Movements are READ-ONLY - they are created automatically by database triggers.
 * This service provides various ways to query the unified transaction history.
 */
@Service
@Transactional(readOnly = true)  // All operations are read-only
public class MovementService {
    
    private final MovementRepository movementRepository;
    
    public MovementService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }
    
    /**
     * Get all movements for a user.
     * Returns complete transaction history in chronological order.
     *
     * @param userId the user's ID
     * @return list of all movements (income, expenses, transfers)
     */
    public List<Movement> getUserMovements(Long userId) {
        return movementRepository.findByUserIdOrderByRecordedAtDesc(userId);
    }
    
    /**
     * Get movements within a date range.
     * Useful for monthly/yearly reports.
     *
     * @param userId the user's ID
     * @param startDate start of range
     * @param endDate end of range
     * @return list of movements in date range
     */
    public List<Movement> getMovementsByDateRange(
        Long userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return movementRepository.findByUserIdAndRecordedAtBetween(userId, startDate, endDate);
    }
    
    /**
     * Get movements filtered by type.
     * Filter to show only income, expenses, or transfers.
     *
     * @param userId the user's ID
     * @param type the movement type (INCOME, EXPENSE, TRANSFER)
     * @return list of movements of the specified type
     */
    public List<Movement> getMovementsByType(Long userId, MovementType type) {
        return movementRepository.findByUserIdAndMovementTypeOrderByRecordedAtDesc(userId, type);
    }
    
    /**
     * Get all movements for a specific category.
     * Shows income distributions, expenses, and transfers for a category.
     *
     * @param categoryId the category ID
     * @return list of movements involving the category
     */
    public List<Movement> getCategoryMovements(Long categoryId) {
        return movementRepository.findByCategoryIdOrderByRecordedAtDesc(categoryId);
    }
    
    /**
     * Get a single movement by ID.
     *
     * @param movementId the movement ID
     * @return the movement record
     * @throws IllegalArgumentException if not found
     */
    public Movement getMovementById(Long movementId) {
        return movementRepository.findById(movementId)
            .orElseThrow(() -> new IllegalArgumentException("Movement not found: " + movementId));
    }
}
