package com.alma.todolistapplication.service;

import com.alma.todolistapplication.model.TodoItem;
import com.alma.todolistapplication.model.User;

import java.util.List;
import java.util.Optional;

public interface TodoItemService {
    List<TodoItem> getTodoItemsForUser(User user); // Get all items for a specific user
    List<TodoItem> getTodoItemsForUser(Long userId); // Overload for convenience

    Optional<TodoItem> getTodoItemByIdForUser(Long itemId, User user);
    Optional<TodoItem> getTodoItemByIdForUser(Long itemId, Long userId);

    TodoItem saveTodoItem(TodoItem todoItem, User user); // Ensure item is linked to the user
    TodoItem createTodoItem(String description, User user);

    void deleteTodoItem(Long itemId, User user); // Ensure user can only delete their own items

    TodoItem toggleComplete(Long itemId, User user); // Toggle completion status

    // Optional: if you implement admin features later to see all tasks
    // List<TodoItem> getAllTodoItemsAdmin();
}