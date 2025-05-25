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
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // For flash messages

@Controller
public class TodoController {
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoItemService todoItemService;

    @Autowired
    public TodoController(TodoItemService todoItemService) {
        this.todoItemService = todoItemService;
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        logger.info("User {} accessing to-do list", currentUser.getUsername());

        // Add task counts to the model
        model.addAttribute("totalTasks", todoItemService.countTotalTasksForUser(currentUser));
        model.addAttribute("activeTasks", todoItemService.countActiveTasksForUser(currentUser));
        model.addAttribute("completedTasks", todoItemService.countCompletedTasksForUser(currentUser));

        // Ensure "newTodo" is always in the model for the form
        if (!model.containsAttribute("newTodo")) {
            model.addAttribute("newTodo", new TodoItem());
        }
        model.addAttribute("todos", todoItemService.getTodoItemsForUser(currentUser));
        model.addAttribute("username", currentUser.getUsername());
        return "index";
    }

    @PostMapping("/add")
    public String addTodoItem(@Valid @ModelAttribute("newTodo") TodoItem newTodo,
                              BindingResult result,
                              @AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) return "redirect:/login";

        if (result.hasErrors()) { // Catches @NotBlank etc. from TodoItem validation
            logger.warn("Add to-do item form has errors for user {}: {}", currentUser.getUsername(), result.getAllErrors());
            // Re-populate necessary model attributes for returning to the index page with errors
            model.addAttribute("totalTasks", todoItemService.countTotalTasksForUser(currentUser));
            model.addAttribute("activeTasks", todoItemService.countActiveTasksForUser(currentUser));
            model.addAttribute("completedTasks", todoItemService.countCompletedTasksForUser(currentUser));
            model.addAttribute("todos", todoItemService.getTodoItemsForUser(currentUser));
            model.addAttribute("username", currentUser.getUsername());
            // 'newTodo' with its errors is already in the model due to @ModelAttribute
            return "index";
        }

        logger.info("User {} adding new to-do item: '{}', Due: {}",
                currentUser.getUsername(), newTodo.getDescription(), newTodo.getDueDate());

        todoItemService.createTodoItem(newTodo.getDescription(), newTodo.getDueDate(), currentUser);
        return "redirect:/";
    }

    @PostMapping("/toggle/{id}")
    public String toggleTodoItemComplete(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal User currentUser,
                                         RedirectAttributes redirectAttributes) { // Added RedirectAttributes
        if (currentUser == null) return "redirect:/login";
        logger.info("User {} toggling completion for to-do item ID: {}", currentUser.getUsername(), id);
        try {
            todoItemService.toggleComplete(id, currentUser);
        } catch (SecurityException e) {
            logger.warn("SecurityException for user {} trying to toggle item {}: {}", currentUser.getUsername(), id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to perform that action."); // Flash attribute
            return "redirect:/"; // Redirect to index, error will be displayed
        } catch (IllegalArgumentException e) {
            logger.warn("IllegalArgumentException for user {} trying to toggle item {}: {}", currentUser.getUsername(), id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found or invalid argument."); // Flash attribute
            return "redirect:/";
        }
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteTodoItem(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal User currentUser,
                                 RedirectAttributes redirectAttributes) { // Added RedirectAttributes
        if (currentUser == null) return "redirect:/login";
        logger.info("User {} deleting to-do item ID: {}", currentUser.getUsername(), id);
        try {
            todoItemService.deleteTodoItem(id, currentUser);
        } catch (SecurityException e) {
            logger.warn("SecurityException for user {} trying to delete item {}: {}", currentUser.getUsername(), id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to perform that action.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            logger.warn("IllegalArgumentException for user {} trying to delete item {}: {}", currentUser.getUsername(), id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found or invalid argument.");
            return "redirect:/";
        }
        return "redirect:/";
    }

    // --- NEW CONTROLLER METHOD FOR CLEARING COMPLETED TASKS ---
    @PostMapping("/clear-completed")
    public String clearCompletedTasks(@AuthenticationPrincipal User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) return "redirect:/login";
        try {
            todoItemService.deleteAllCompletedTasksForUser(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "All completed tasks cleared!");
        } catch (Exception e) {
            logger.error("Error clearing completed tasks for user {}: {}", currentUser.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not clear completed tasks.");
        }
        return "redirect:/";
    }
}