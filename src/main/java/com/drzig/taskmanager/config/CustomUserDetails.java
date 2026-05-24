package com.drzig.taskmanager.config;

import com.drzig.taskmanager.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final String role;
    private final boolean mustChangePassword;
    private final Collection<SimpleGrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.mustChangePassword = user.isMustChangePassword();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    public String getRole() {
        return role;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    @Override public Collection<SimpleGrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
