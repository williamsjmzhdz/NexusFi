package com.nexusfi.security;

import com.nexusfi.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 * Acts as an adapter between our User entity and Spring Security's authentication system.
 * 
 * This class wraps our User entity and provides the necessary methods for Spring Security
 * to perform authentication and authorization.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * Constructs a CustomUserDetails object wrapping a User entity.
     *
     * @param user the User entity to wrap
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the authorities (roles) granted to the user.
     * Currently returns an empty list as role-based authorization is not implemented yet.
     *
     * @return empty collection (no roles assigned)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the user's hashed password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    /**
     * Returns the username used to authenticate the user.
     * In this application, the email serves as the username.
     *
     * @return the user's email address
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return true (accounts never expire in this implementation)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true (accounts are never locked in this implementation)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     *
     * @return true (credentials never expire in this implementation)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true (all users are enabled in this implementation)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Returns the wrapped User entity.
     * Useful for accessing additional user information beyond what UserDetails provides.
     *
     * @return the User entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the user's ID.
     * Convenience method for accessing the user's ID directly.
     *
     * @return the User's ID
     */
    public Long getId() {
        return user.getId();
    }
}
