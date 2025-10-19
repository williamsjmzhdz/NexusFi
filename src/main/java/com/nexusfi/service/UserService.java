package com.nexusfi.service;

import com.nexusfi.model.User;
import com.nexusfi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for user-related operations.
 * Handles user registration, retrieval, and validation.
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Constructor injection - Spring automatically provides the repository.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Register a new user.
     * Validates that email doesn't already exist.
     *
     * @param user the user to register
     * @return the saved user with generated ID
     * @throws IllegalArgumentException if email already exists
     */
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        
        // TODO: Add password encryption here when we implement security
        return userRepository.save(user);
    }
    
    /**
     * Find a user by email.
     * Used during login.
     *
     * @param email the user's email
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find a user by ID.
     *
     * @param id the user's ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Check if an email is already registered.
     *
     * @param email the email to check
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}