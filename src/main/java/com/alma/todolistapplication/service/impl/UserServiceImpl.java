package com.alma.todolistapplication.service.impl;

import com.alma.todolistapplication.model.User;
import com.alma.todolistapplication.repository.UserRepository;
import com.alma.todolistapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // Good practice to make service methods transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // To hash passwords

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerNewUser(User user) {
        // Check if username or email already exists (optional, but good practice)
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true); // Ensure user is enabled by default
        // Set default role if you had a role field (e.g., user.setRole(Roles.USER);)

        return userRepository.save(user);
    }

    // Option A: Implement these if added to the interface
    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}