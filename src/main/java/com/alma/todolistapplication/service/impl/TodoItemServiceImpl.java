package com.alma.todolistapplication.service.impl;

import com.alma.todolistapplication.model.TodoItem;
import com.alma.todolistapplication.model.User;
import com.alma.todolistapplication.repository.TodoItemRepository;
import com.alma.todolistapplication.repository.UserRepository;
import com.alma.todolistapplication.service.TodoItemService;
import org.slf4j.Logger; // Added Logger
import org.slf4j.LoggerFactory; // Added Logger
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoItemServiceImpl implements TodoItemService {

    private static final Logger logger = LoggerFactory.getLogger(TodoItemServiceImpl.class); // Added Logger instance

    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository;

    @Autowired
    public TodoItemServiceImpl(TodoItemRepository todoItemRepository, UserRepository userRepository) {
        this.todoItemRepository = todoItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TodoItem> getTodoItemsForUser(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptyList();
        }
        return todoItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    public List<TodoItem> getTodoItemsForUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return todoItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }


    @Override
    public Optional<TodoItem> getTodoItemByIdForUser(Long itemId, User user) {
        if (itemId == null || user == null || user.getId() == null) {
            return Optional.empty();
        }
        return todoItemRepository.findById(itemId)
                .filter(item -> item.getUser().getId().equals(user.getId()));
    }

    @Override
    public Optional<TodoItem> getTodoItemByIdForUser(Long itemId, Long userId) {
        if (itemId == null || userId == null) {
            return Optional.empty();
        }
        return todoItemRepository.findById(itemId)
                .filter(item -> item.getUser().getId().equals(userId));
    }


    @Override
    public TodoItem saveTodoItem(TodoItem todoItem, User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null when saving a TodoItem");
        }
        if(todoItem.getUser() == null || !todoItem.getUser().getId().equals(user.getId())) {
            User persistentUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
            todoItem.setUser(persistentUser);
        }

        if (todoItem.getId() == null && todoItem.getCreatedAt() == null) {
            // @PrePersist in TodoItem entity should handle this, but defensive check is okay
            todoItem.setCreatedAt(LocalDateTime.now());
        }
        return todoItemRepository.save(todoItem);
    }

    @Override
    public TodoItem createTodoItem(String description, LocalDateTime dueDate, User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null for creating a TodoItem");
        }
        User persistentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));

        TodoItem newItem = new TodoItem(description, persistentUser); // Constructor sets description, user, createdAt
        newItem.setDueDate(dueDate);
        return todoItemRepository.save(newItem);
    }


    @Override
    public void deleteTodoItem(Long itemId, User user) {
        if (itemId == null || user == null || user.getId() == null) {
            throw new IllegalArgumentException("Item ID and User cannot be null for deletion");
        }
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("TodoItem not found with id: " + itemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to delete this item");
        }
        todoItemRepository.deleteById(itemId);
    }

    @Override
    public TodoItem toggleComplete(Long itemId, User user) {
        if (itemId == null || user == null || user.getId() == null) {
            throw new IllegalArgumentException("Item ID and User cannot be null for toggling completion");
        }
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("TodoItem not found with id: " + itemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to modify this item");
        }

        item.setCompleted(!item.isCompleted());
        if (item.isCompleted()) {
            item.setCompletedAt(LocalDateTime.now());
        } else {
            item.setCompletedAt(null);
        }
        return todoItemRepository.save(item);
    }

    // --- IMPLEMENTATION OF NEW METHODS ---
    @Override
    public long countTotalTasksForUser(User user) {
        if (user == null || user.getId() == null) return 0;
        return todoItemRepository.countByUserId(user.getId());
    }

    @Override
    public long countActiveTasksForUser(User user) {
        if (user == null || user.getId() == null) return 0;
        // Active tasks are those not completed
        return todoItemRepository.countByUserIdAndCompleted(user.getId(), false);
    }

    @Override
    public long countCompletedTasksForUser(User user) {
        if (user == null || user.getId() == null) return 0;
        return todoItemRepository.countByUserIdAndCompleted(user.getId(), true);
    }

    @Override
    public void deleteAllCompletedTasksForUser(User user) {
        if (user != null && user.getId() != null) {
            int deletedCount = todoItemRepository.deleteAllCompletedForUser(user.getId());
            logger.info("Deleted {} completed tasks for user {}", deletedCount, user.getUsername());
        } else {
            logger.warn("Attempted to delete completed tasks for a null user or user with null ID.");
        }
    }
}