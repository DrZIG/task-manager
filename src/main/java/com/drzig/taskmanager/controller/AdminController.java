package com.drzig.taskmanager.controller;

import com.drzig.taskmanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    private static final String DEFAULT_PASSWORD = "Welcome123!";

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("defaultPassword", DEFAULT_PASSWORD);
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(
            @RequestParam String username,
            @RequestParam(defaultValue = "ROLE_USER") String role,
            RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, DEFAULT_PASSWORD, role);
            redirectAttributes.addFlashAttribute("success",
                    "User '" + username + "' created. Temporary password: " + DEFAULT_PASSWORD);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        userService.resetPassword(id, DEFAULT_PASSWORD);
        redirectAttributes.addFlashAttribute("success",
                "Password reset to: " + DEFAULT_PASSWORD + " — user must change it on next login.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }
}
