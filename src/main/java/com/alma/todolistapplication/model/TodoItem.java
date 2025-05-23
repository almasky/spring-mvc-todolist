package com.alma.todolistapplication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "todo_items")
@Data
@NoArgsConstructor
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Description cannot be empty")
    @Column(nullable = false)
    private String description;

    private boolean completed = false;

    @Column(nullable = false, updatable = false) // createdAt should not be updated
    private LocalDateTime createdAt;

    private LocalDateTime completedAt; // Optional: timestamp when it was completed

    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY) // Many TodoItems can belong to one User
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in todo_items table
    private User user;


    // Constructor for creating a new item
    public TodoItem(String description, User user) {
        this.description = description;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Lifecycle callback to set createdAt before persisting if not already set
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}