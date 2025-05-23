package com.alma.todolistapplication.service;

import com.alma.todolistapplication.model.User; // Or a DTO if you prefer for registration input

import java.util.Optional;

public interface UserService {
    User registerNewUser(User user); // Takes a User object, returns the saved User
    // Later you might add:
    // Optional<User> findByUsername(String username);
    // void changePassword(User user, String newPassword);

    // Option A: Add these methods if you want the controller to check first
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
