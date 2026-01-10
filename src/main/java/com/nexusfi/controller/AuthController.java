package com.nexusfi.controller;

import com.nexusfi.dto.AuthResponse;
import com.nexusfi.dto.LoginRequest;
import com.nexusfi.dto.RegisterRequest;
import com.nexusfi.model.User;
import com.nexusfi.security.JwtUtil;
import com.nexusfi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles user registration and login with JWT token generation.
 * All endpoints in this controller are public (no authentication required).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructs AuthController with required dependencies.
     *
     * @param userService handles user registration and retrieval
     * @param jwtUtil generates and validates JWT tokens
     * @param authenticationManager authenticates user credentials
     */
    public AuthController(UserService userService, 
                         JwtUtil jwtUtil, 
                         AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user account.
     * Creates user with encrypted password and returns JWT token for immediate login.
     *
     * @param request contains email and password for new account
     * @return ResponseEntity with JWT token and user email (201 Created)
     * @throws DuplicateResourceException if email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User savedUser = userService.registerUser(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        AuthResponse response = new AuthResponse(token, savedUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates user and generates JWT token.
     * Validates credentials using Spring Scurity's AuthenticationManager.
     * 
     * @param request constains email and password
     * @return ResponseEntity with JWT token and user email (200 OK)
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getEmail());
        AuthResponse response = new AuthResponse(token, request.getEmail());

        return ResponseEntity.ok(response);
    }

}
