package com.alma.todolistapplication.webcontroller;

import com.alma.todolistapplication.model.TodoItem;
import com.alma.todolistapplication.model.User;
import com.alma.todolistapplication.service.TodoItemService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TodoController {
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoItemService todoItemService;

    @Autowired
    public TodoController(TodoItemService todoItemService) {
        this.todoItemService = todoItemService;
    }

    @GetMapping("/") // Root path, will display the user's to-do list
    public String index(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) {
            // Should not happen if security is configured correctly for "/" to be authenticated
            return "redirect:/login";
        }
        logger.info("User {} accessing to-do list", currentUser.getUsername());
        model.addAttribute("todos", todoItemService.getTodoItemsForUser(currentUser));
        model.addAttribute("newTodo", new TodoItem()); // For the "add new item" form
        model.addAttribute("username", currentUser.getUsername()); // For display
        return "index"; // Renders index.html
    }

    @PostMapping("/add")
    public String addTodoItem(@Valid @ModelAttribute("newTodo") TodoItem todoItem, // Bind to 'description' field of newTodo
                              BindingResult result,
                              @AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) return "redirect:/login";

        if (result.hasFieldErrors("description")) { // Check only description error for this simple form
            logger.warn("Add to-do item form has errors for user {}: {}", currentUser.getUsername(), result.getAllErrors());
            // Reload page with errors and existing items
            model.addAttribute("todos", todoItemService.getTodoItemsForUser(currentUser));
            model.addAttribute("username", currentUser.getUsername());
            // model.addAttribute("newTodo", todoItem); // Keep the erroneous item in the form
            return "index";
        }
        logger.info("User {} adding new to-do item: {}", currentUser.getUsername(), todoItem.getDescription());
        todoItemService.createTodoItem(todoItem.getDescription(), currentUser);
        return "redirect:/";
    }

    // Using POST for state changes is generally better than GET
    @PostMapping("/toggle/{id}")
    public String toggleTodoItemComplete(@PathVariable("id") Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return "redirect:/login";
        logger.info("User {} toggling completion for to-do item ID: {}", currentUser.getUsername(), id);
        try {
            todoItemService.toggleComplete(id, currentUser);
        } catch (SecurityException e) {
            logger.warn("SecurityException for user {} trying to toggle item {}: {}", currentUser.getUsername(), id, e.getMessage());
            // Optionally add a flash attribute to show an error message on redirect
            return "redirect:/?error=authError";
        } catch (IllegalArgumentException e) {
            logger.warn("IllegalArgumentException for user {} trying to toggle item {}: {}", currentUser.getUsername(), id, e.getMessage());
            return "redirect:/?error=notFound";
        }
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteTodoItem(@PathVariable("id") Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return "redirect:/login";
        logger.info("User {} deleting to-do item ID: {}", currentUser.getUsername(), id);
        try {
            todoItemService.deleteTodoItem(id, currentUser);
        } catch (SecurityException e) {
            logger.warn("SecurityException for user {} trying to delete item {}: {}", currentUser.getUsername(), id, e.getMessage());
            return "redirect:/?error=authError";
        } catch (IllegalArgumentException e) {
            logger.warn("IllegalArgumentException for user {} trying to delete item {}: {}", currentUser.getUsername(), id, e.getMessage());
            return "redirect:/?error=notFound";
        }
        return "redirect:/";
    }
}