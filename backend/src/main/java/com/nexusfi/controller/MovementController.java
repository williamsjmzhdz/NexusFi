package com.nexusfi.controller;

import com.nexusfi.dto.MovementResponse;
import com.nexusfi.model.Movement;
import com.nexusfi.model.User;
import com.nexusfi.model.enums.MovementType;
import com.nexusfi.service.MovementService;
import com.nexusfi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for querying movement records.
 * Movements are READ-ONLY - created automatically by the system.
 * This controller provides the unified transaction history view.
 */
@RestController
@RequestMapping("/api/v1/movements")
public class MovementController {

    private final MovementService movementService;
    private final UserService userService;

    public MovementController(MovementService movementService, UserService userService) {
        this.movementService = movementService;
        this.userService = userService;
    }

    /**
     * Get all movements for the authenticated user.
     * Returns complete transaction history.
     * 
     * GET /api/v1/movements
     * 
     * @return 200 OK with list of all movements
     */
    @GetMapping
    public ResponseEntity<List<MovementResponse>> getAllMovements() {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Movement> movements = movementService.getUserMovements(user.getId());
        List<MovementResponse> response = movements.stream()
            .map(MovementResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single movement by ID.
     * 
     * GET /api/v1/movements/{id}
     * 
     * @param id the movement ID
     * @return 200 OK with the movement, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovementResponse> getMovementById(@PathVariable Long id) {
        // TODO: Verify movement belongs to authenticated user
        Movement movement = movementService.getMovementById(id);
        MovementResponse response = MovementResponse.fromEntity(movement);
        return ResponseEntity.ok(response);
    }

    /**
     * Get movements filtered by type.
     * 
     * GET /api/v1/movements/type/{type}
     * 
     * @param type the movement type (ASSIGNMENT, EXPENSE, TRANSFER, REBALANCE)
     * @return 200 OK with list of movements of the specified type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<MovementResponse>> getMovementsByType(@PathVariable MovementType type) {
        // TODO: Get authenticated user from SecurityContext
        User user = userService.findById(1L)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Movement> movements = movementService.getMovementsByType(user.getId(), type);
        List<MovementResponse> response = movements.stream()
            .map(MovementResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all movements for a specific category.
     * 
     * GET /api/v1/movements/category/{categoryId}
     * 
     * @param categoryId the category ID
     * @return 200 OK with list of movements for the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MovementResponse>> getMovementsByCategory(@PathVariable Long categoryId) {
        // TODO: Verify category belongs to authenticated user
        List<Movement> movements = movementService.getCategoryMovements(categoryId);
        List<MovementResponse> response = movements.stream()
            .map(MovementResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
