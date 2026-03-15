package com.nexusfi.dto;

/**
 * DTO for authentication responses.
 * Returns JWT token and user email after successful login/registration.
 */
public class AuthResponse {
    
    private String token;
    private String email;
    
    // Constructors
    public AuthResponse() {
    }
    
    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}