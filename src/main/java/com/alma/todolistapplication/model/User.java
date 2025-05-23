package com.alma.todolistapplication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users") // Using "users" as "user" can be a reserved keyword in some SQL dialects
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    private boolean enabled = true; // For Spring Security UserDetails

    // For simplicity, we'll assume a single role "USER" for now.
    // If you need multiple roles (like ADMIN), you can add an Enum field here.
    // For this simple app, all registered users are just "USER".

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TodoItem> todoItems;

    // --- UserDetails methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For this simple app, all users have a "ROLE_USER" authority.
        // If you add a role field to User, derive it from that field.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or add logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or add logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or add logic
    }

    @Override
    public boolean isEnabled() {
        return enabled; // Use the 'enabled' field
    }
}