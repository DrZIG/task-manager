package com.drzig.taskmanager.controller;

import com.drzig.taskmanager.config.CustomUserDetails;
import com.drzig.taskmanager.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // /register GET and POST are removed entirely

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "change-password";
        }

        userService.changePassword(currentUser.getUsername(), newPassword);
        redirectAttributes.addFlashAttribute("success", "Password updated. Please log in again.");
        return "redirect:/login";  // force re-login so security context refreshes
    }
}
