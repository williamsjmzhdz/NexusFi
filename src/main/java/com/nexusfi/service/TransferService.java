package com.nexusfi.service;

import com.nexusfi.exception.InsufficientBalanceException;
import com.nexusfi.exception.ResourceNotFoundException;
import com.nexusfi.model.Category;
import com.nexusfi.model.Movement;
import com.nexusfi.model.Transfer;
import com.nexusfi.model.enums.MovementType;
import com.nexusfi.repository.MovementRepository;
import com.nexusfi.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for transfer-related operations.
 * Handles zero-sum transfers between categories.
 */
@Service
@Transactional
public class TransferService {
    
    private final TransferRepository transferRepository;
    private final MovementRepository movementRepository;
    private final CategoryService categoryService;
    
    public TransferService(
        TransferRepository transferRepository,
        MovementRepository movementRepository,
        CategoryService categoryService
    ) {
        this.transferRepository = transferRepository;
        this.movementRepository = movementRepository;
        this.categoryService = categoryService;
    }
    
    /**
     * Execute a transfer between two categories.
     * This is a zero-sum operation: source decreases, destination increases.
     *
     * Algorithm:
     * 1. Validate source has sufficient balance
     * 2. Validate source and destination are different
     * 3. Deduct from source category
     * 4. Add to destination category
     * 5. Save transfer record
     * 6. Movement records created automatically by DB trigger
     *
     * @param transfer the transfer to execute
     * @return the saved transfer record
     * @throws IllegalArgumentException if attempting transfer to same category
     * @throws InsufficientBalanceException if source doesn't have sufficient balance
     */
    public Transfer executeTransfer(Transfer transfer) {
        Category sourceCategory = transfer.getSourceCategory();
        Category destinationCategory = transfer.getDestinationCategory();
        BigDecimal amount = transfer.getAmount();
        
        // Validate different categories
        if (sourceCategory.getId().equals(destinationCategory.getId())) {
            throw new IllegalArgumentException(
                "Cannot transfer to the same category"
            );
        }
        
        // Validate sufficient balance in source
        BigDecimal sourceBalance = sourceCategory.getCurrentBalance();
        if (sourceBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                sourceCategory.getName(),
                sourceBalance,
                amount
            );
        }
        
        // Execute zero-sum transfer
        sourceCategory.setCurrentBalance(sourceBalance.subtract(amount));
        destinationCategory.setCurrentBalance(destinationCategory.getCurrentBalance().add(amount));
        
        // Save transfer record
        Transfer savedTransfer = transferRepository.save(transfer);
        
        // Create TRANSFER movement (debit from source)
        Movement outMovement = Movement.builder()
            .amount(amount.negate()) // Negative for outgoing
            .type(MovementType.TRANSFER)
            .description("Transfer to: " + destinationCategory.getName())
            .movementDate(transfer.getTransferDate())
            .category(sourceCategory)
            .user(transfer.getUser())
            .transfer(savedTransfer)
            .build();
        movementRepository.save(outMovement);
        
        // Create TRANSFER movement (credit to destination)
        Movement inMovement = Movement.builder()
            .amount(amount) // Positive for incoming
            .type(MovementType.TRANSFER)
            .description("Transfer from: " + sourceCategory.getName())
            .movementDate(transfer.getTransferDate())
            .category(destinationCategory)
            .user(transfer.getUser())
            .transfer(savedTransfer)
            .build();
        movementRepository.save(inMovement);
        
        return savedTransfer;
    }
    
    /**
     * Get all transfers for a user.
     *
     * @param userId the user's ID
     * @return list of transfers
     */
    public List<Transfer> getUserTransfers(Long userId) {
        return transferRepository.findByUserIdOrderByTransferDateDesc(userId);
    }
    
    /**
     * Get transfers within a date range.
     *
     * @param userId the user's ID
     * @param startDate start of range
     * @param endDate end of range
     * @return list of transfers
     */
    public List<Transfer> getTransfersByDateRange(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return transferRepository.findByUserIdAndTransferDateBetween(userId, startDate, endDate);
    }
    
    /**
     * Get all transfers involving a specific category.
     *
     * @param categoryId the category ID
     * @return list of transfers (as source or destination)
     */
    public List<Transfer> getCategoryTransfers(Long categoryId) {
        return transferRepository.findBySourceCategoryIdOrDestinationCategoryIdOrderByTransferDateDesc(
            categoryId, 
            categoryId
        );
    }
    
    /**
     * Get a single transfer by ID.
     *
     * @param transferId the transfer ID
     * @return the transfer record
     * @throws ResourceNotFoundException if not found
     */
    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId)
            .orElseThrow(() -> new ResourceNotFoundException("Transfer", transferId));
    }
}
