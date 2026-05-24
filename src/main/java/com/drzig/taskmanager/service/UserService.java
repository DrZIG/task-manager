package com.drzig.taskmanager.service;

import com.drzig.taskmanager.config.CustomUserDetails;
import com.drzig.taskmanager.model.User;
import com.drzig.taskmanager.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new CustomUserDetails(user);
    }

    /** Admin creates a user with a temporary password — must change on first login. */
    @Transactional
    public User createUser(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setMustChangePassword(true);
        return userRepository.save(user);
    }

    /** Called after a user sets their own new password. Clears the force-change flag. */
    @Transactional
    public void changePassword(String username, String newRawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setPassword(passwordEncoder.encode(newRawPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setMustChangePassword(true);   // force change again after admin reset
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
