package com.alma.todolistapplication.service;

import com.alma.todolistapplication.model.TodoItem;
import com.alma.todolistapplication.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoItemService {

    List<TodoItem> getTodoItemsForUser(User user); // Get all items for a specific user
    List<TodoItem> getTodoItemsForUser(Long userId); // Overload for convenience

    Optional<TodoItem> getTodoItemByIdForUser(Long itemId, User user);
    Optional<TodoItem> getTodoItemByIdForUser(Long itemId, Long userId);

    TodoItem saveTodoItem(TodoItem todoItem, User user);
    TodoItem createTodoItem(String description, LocalDateTime dueDate, User user); // Assumes this signature

    void deleteTodoItem(Long itemId, User user);

    TodoItem toggleComplete(Long itemId, User user);

    // --- NEW METHODS FOR COUNTS AND CLEARING ---
    long countTotalTasksForUser(User user);
    long countActiveTasksForUser(User user);
    long countCompletedTasksForUser(User user);
    void deleteAllCompletedTasksForUser(User user);
}