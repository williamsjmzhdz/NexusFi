package com.nexusfi.controller;

import com.nexusfi.dto.IncomeRequest;
import com.nexusfi.dto.IncomeResponse;
import com.nexusfi.model.IncomeRecord;
import com.nexusfi.model.User;
import com.nexusfi.service.IncomeService;
import com.nexusfi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing income records.
 * Handles income recording and queries.
 */
@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService;

    public IncomeController(IncomeService incomeService, UserService userService) {
        this.incomeService = incomeService;
        this.userService = userService;
    }

    /**
     * Record new income for the authenticated user.
     * This triggers automatic distribution to categories.
     * 
     * POST /api/incomes
     * 
     * @param request the income data
     * @return 201 Created with the recorded income
     */
    @PostMapping
    public ResponseEntity<IncomeResponse> recordIncome(@Valid @RequestBody IncomeRequest request) {
        // TODO: Get authenticated user from SecurityContext
        // For now, we'll use a hardcoded user ID (will be replaced with Spring Security)
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Build income record entity
        IncomeRecord incomeRecord = IncomeRecord.builder()
            .amount(request.getAmount())
            .source(request.getSource())
            .description(request.getDescription())
            .incomeDate(request.getIncomeDate())
            .user(user)
            .build();
        
        IncomeRecord savedIncome = incomeService.recordIncome(incomeRecord);
        IncomeResponse response = IncomeResponse.fromEntity(savedIncome);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all income records for the authenticated user.
     * 
     * GET /api/incomes
     * 
     * @return 200 OK with list of income records
     */
    @GetMapping
    public ResponseEntity<List<IncomeResponse>> getAllIncomes() {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<IncomeRecord> incomes = incomeService.getUserIncomeRecords(user.getId());
        List<IncomeResponse> response = incomes.stream()
            .map(IncomeResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single income record by ID.
     * 
     * GET /api/incomes/{id}
     * 
     * @param id the income record ID
     * @return 200 OK with the income record, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(@PathVariable Long id) {
        // TODO: Verify income belongs to authenticated user
        IncomeRecord income = incomeService.getIncomeRecordById(id);
        IncomeResponse response = IncomeResponse.fromEntity(income);
        return ResponseEntity.ok(response);
    }
}
