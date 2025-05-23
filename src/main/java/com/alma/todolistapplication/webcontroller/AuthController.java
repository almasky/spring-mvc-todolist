package com.alma.todolistapplication.webcontroller;

import com.alma.todolistapplication.model.User;
import com.alma.todolistapplication.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Helper to check if user is already authenticated
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered, // Changed from registrationSuccess for consistency
                            Model model) {
        if (isAuthenticated()) {
            return "redirect:/"; // Redirect to home if already logged in
        }
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        // Check for the 'registered' query parameter set by the redirect from registration
        if (registered != null && "true".equals(registered)) {
            // Check for flash attribute set by RedirectAttributes
            if (model.containsAttribute("registrationSuccess")) {
                // The message is already in the model from flash attributes, no need to add again
            } else {
                // Fallback message if flash attribute somehow missed (should not happen with RedirectAttributes)
                model.addAttribute("registrationSuccess", "Registration successful! Please log in.");
            }
        }
        return "login"; // Renders login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (isAuthenticated()) {
            return "redirect:/"; // Redirect to home if already logged in
        }
        // Pass an empty User object to bind the form data if not already present
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register"; // Renders register.html
    }

    @PostMapping("/perform-register")
    public String performRegistration(@Valid @ModelAttribute("user") User user,
                                      BindingResult result,
                                      Model model, // For adding errors back to the registration page
                                      RedirectAttributes redirectAttributes) { // For success messages on redirect
        if (isAuthenticated()) {
            return "redirect:/";
        }

        // If @Valid finds validation errors (e.g., @NotBlank, @Size on User entity)
        if (result.hasErrors()) {
            logger.warn("Registration form has validation errors: {}", result.getAllErrors());
            // The 'user' object with errors is already in the model thanks to @ModelAttribute
            // So, just return to the register view.
            return "register";
        }

        try {
            // The userService.registerNewUser() method will now handle
            // checks for existing username/email and throw IllegalArgumentException if found.
            userService.registerNewUser(user);

            // Add a flash attribute for the success message.
            // Flash attributes survive one redirect.
            redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful! Please log in.");
            return "redirect:/login?registered=true"; // Redirect to login page with a success indicator

        } catch (IllegalArgumentException e) {
            // This catches exceptions from userService.registerNewUser() like "Username already exists"
            logger.warn("Registration attempt failed: {}", e.getMessage());
            model.addAttribute("registrationError", e.getMessage());
            // The 'user' object (with the data they submitted) is already in the model
            // due to @ModelAttribute, so they can correct their input.
            return "register"; // Return to the registration page, displaying the error
        } catch (Exception e) {
            // Catch any other unexpected errors during registration
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            model.addAttribute("registrationError", "An unexpected error occurred. Please try again.");
            // The 'user' object is already in the model.
            return "register";
        }
    }
}