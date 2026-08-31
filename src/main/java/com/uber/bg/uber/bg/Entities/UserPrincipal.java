package com.uber.bg.uber.bg.Entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

// 1. MUST be a public class (NOT an interface, NOT an abstract class)
public class UserPrincipal implements UserDetails {

    private final User user;
    // Holds your custom database User entity

    // 2. Add the concrete constructor that your service is calling
    public UserPrincipal(User user) {
        this.user = user;
    }

    // 3. Map your database user's role to Spring Security authorities
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming your User entity has a getRole() method (e.g., "DRIVER", "PASSENGER")
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Return hashed password from DB
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // Return username/email from DB
    }

    // 4. Boilerplate overrides (Must return true so the user can log in)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Optional: Add a helper to get the raw user ID later in your controllers
    public UUID getId() {
        return user.getId();
    }

}
