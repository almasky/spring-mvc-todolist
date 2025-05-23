package com.alma.todolistapplication.service.impl;

import com.alma.todolistapplication.model.TodoItem;
import com.alma.todolistapplication.model.User;
import com.alma.todolistapplication.repository.TodoItemRepository;
import com.alma.todolistapplication.repository.UserRepository; // For fetching User by ID
import com.alma.todolistapplication.service.TodoItemService;
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

    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository; // To fetch User when only userId is given

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
        // Ensure the item is associated with the correct user
        // If it's a new item or user is being reassigned (though reassignment is not typical here)
        if(todoItem.getUser() == null || !todoItem.getUser().getId().equals(user.getId())) {
            User persistentUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
            todoItem.setUser(persistentUser);
        }


        if (todoItem.getId() == null) { // New item
            todoItem.setCreatedAt(LocalDateTime.now());
        }
        return todoItemRepository.save(todoItem);
    }

    @Override
    public TodoItem createTodoItem(String description, User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null for creating a TodoItem");
        }
        User persistentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
        TodoItem newItem = new TodoItem(description, persistentUser);
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
            // Optionally throw an AccessDeniedException or just log and do nothing
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
            item.setCompletedAt(null); // Clear completion timestamp if marked incomplete
        }
        return todoItemRepository.save(item);
    }
}