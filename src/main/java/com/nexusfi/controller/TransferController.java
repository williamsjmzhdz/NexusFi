package com.nexusfi.controller;

import com.nexusfi.dto.TransferRequest;
import com.nexusfi.dto.TransferResponse;
import com.nexusfi.model.Category;
import com.nexusfi.model.Transfer;
import com.nexusfi.model.User;
import com.nexusfi.service.CategoryService;
import com.nexusfi.service.TransferService;
import com.nexusfi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing transfers between categories.
 * Handles zero-sum transfers and queries.
 */
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final CategoryService categoryService;
    private final UserService userService;

    public TransferController(TransferService transferService, CategoryService categoryService,
                            UserService userService) {
        this.transferService = transferService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Execute a transfer between two categories.
     * This is a zero-sum operation: source decreases, destination increases.
     * 
     * POST /api/transfers
     * 
     * @param request the transfer data
     * @return 201 Created with the executed transfer
     */
    @PostMapping
    public ResponseEntity<TransferResponse> executeTransfer(@Valid @RequestBody TransferRequest request) {
        // TODO: Get authenticated user from SecurityContext
        // For now, we'll use a hardcoded user ID (will be replaced with Spring Security)
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get source and destination categories
        Category sourceCategory = categoryService.getCategoryById(request.getSourceCategoryId());
        Category destinationCategory = categoryService.getCategoryById(request.getDestinationCategoryId());
        
        // TODO: Verify both categories belong to authenticated user
        
        // Build transfer entity
        Transfer transfer = Transfer.builder()
            .amount(request.getAmount())
            .description(request.getDescription())
            .transferDate(request.getTransferDate())
            .sourceCategory(sourceCategory)
            .destinationCategory(destinationCategory)
            .user(user)
            .build();
        
        Transfer executedTransfer = transferService.executeTransfer(transfer);
        TransferResponse response = TransferResponse.fromEntity(executedTransfer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all transfers for the authenticated user.
     * 
     * GET /api/transfers
     * 
     * @return 200 OK with list of transfers
     */
    @GetMapping
    public ResponseEntity<List<TransferResponse>> getAllTransfers() {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transfer> transfers = transferService.getUserTransfers(user.getId());
        List<TransferResponse> response = transfers.stream()
            .map(TransferResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single transfer by ID.
     * 
     * GET /api/transfers/{id}
     * 
     * @param id the transfer ID
     * @return 200 OK with the transfer, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransferById(@PathVariable Long id) {
        // TODO: Verify transfer belongs to authenticated user
        Transfer transfer = transferService.getTransferById(id);
        TransferResponse response = TransferResponse.fromEntity(transfer);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transfers involving a specific category.
     * 
     * GET /api/transfers/category/{categoryId}
     * 
     * @param categoryId the category ID
     * @return 200 OK with list of transfers (as source or destination)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransferResponse>> getTransfersByCategory(@PathVariable Long categoryId) {
        // TODO: Verify category belongs to authenticated user
        List<Transfer> transfers = transferService.getCategoryTransfers(categoryId);
        List<TransferResponse> response = transfers.stream()
            .map(TransferResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
