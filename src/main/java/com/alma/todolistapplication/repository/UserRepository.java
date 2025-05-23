package com.alma.todolistapplication.repository;

import com.alma.todolistapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA will automatically generate a query for this method
    // based on the method name. It will look for a user by their username.
    Optional<User> findByUsername(String username);

    // Optional: if you want to check for email existence or find by email
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // Find by username or email (useful for login where user can input either)
    // This would require a custom query if the field names are different or logic is complex.
    // For simple OR, Spring Data JPA might not directly support "findByUsernameOrEmail".
    // Option 1: Separate calls in service layer.
    // Option 2: Custom @Query
    @Query("SELECT u FROM User u WHERE u.username = :loginIdentifier OR u.email = :loginIdentifier")
    Optional<User> findByUsernameOrEmail(String loginIdentifier);

    // --- List Finders (less common for User, but possible) ---
    List<User> findByEnabled(boolean enabled); // Find all enabled/disabled users

    // --- Counting (if you add roles or other criteria later) ---
    // Example: if you add a Roles enum to your User entity
    // long countByRole(Roles role);

    // Note: JpaRepository already provides:
    // - save(User entity)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // - delete(User entity)
    // - count()
    // - existsById(Long id)
    // ...and more
}