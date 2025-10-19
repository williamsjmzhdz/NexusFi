package com.nexusfi.repository;

import com.nexusfi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by their email address.
     * Used during login to retrieve user credentials
     * 
     * @param email the user's email
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * Used during registration to prevent duplicate emails.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
