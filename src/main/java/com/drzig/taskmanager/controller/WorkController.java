package com.drzig.taskmanager.controller;

import com.drzig.taskmanager.config.CustomUserDetails;
import com.drzig.taskmanager.model.Work;
import com.drzig.taskmanager.service.TaskService;
import com.drzig.taskmanager.service.WorkService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class WorkController {

    private final WorkService workService;
    private final TaskService taskService;

    public WorkController(WorkService workService, TaskService taskService) {
        this.workService = workService;
        this.taskService = taskService;
    }

    // ─── All works page ───────────────────────────────────────────────────────

    @GetMapping("/works")
    public String allWorks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {

        boolean isAdmin = currentUser.isAdmin();

        if (from != null && to != null) {
            model.addAttribute("works", workService.findByDateRange(from, to, currentUser.getId(), isAdmin));
        } else {
            model.addAttribute("works", workService.findAll(currentUser.getId(), isAdmin));
        }
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("isAdmin", isAdmin);
        return "works";
    }

    // ─── Create work ──────────────────────────────────────────────────────────

    @GetMapping("/works/new")
    public String newWorkForm(
            @RequestParam(required = false) Long taskId,
            Model model) {
        Work work = new Work();
        work.setWorkDate(LocalDate.now());
        model.addAttribute("work", work);
        model.addAttribute("tasks", taskService.findAll());
        model.addAttribute("selectedTaskId", taskId);
        model.addAttribute("pageTitle", "Log Work");
        return "work-form";
    }

    @PostMapping("/works")
    public String createWork(
            @ModelAttribute Work work,
            @RequestParam Long taskId,
            @RequestParam(required = false) String returnTo,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        workService.createWork(work, taskId, currentUser.getId());
        redirectAttributes.addFlashAttribute("success", "Work logged successfully.");
        if ("works".equals(returnTo)) return "redirect:/works";
        return "redirect:/?taskId=" + taskId;
    }

    // ─── Edit work ────────────────────────────────────────────────────────────

    @GetMapping("/works/{id}/edit")
    public String editWorkForm(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {
        Work work = workService.findByIdForUser(id, currentUser.getId(), currentUser.isAdmin());
        model.addAttribute("work", work);
        model.addAttribute("tasks", taskService.findAll());
        model.addAttribute("selectedTaskId", work.getTask().getId());
        model.addAttribute("pageTitle", "Edit Work");
        return "work-form";
    }

    @PostMapping("/works/{id}")
    public String updateWork(
            @PathVariable Long id,
            @ModelAttribute Work work,
            @RequestParam Long taskId,
            @RequestParam(required = false) String returnTo,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        workService.updateWork(id, work, taskId, currentUser.getId(), currentUser.isAdmin());
        redirectAttributes.addFlashAttribute("success", "Work updated.");
        if ("works".equals(returnTo)) return "redirect:/works";
        return "redirect:/?taskId=" + taskId;
    }

    // ─── Delete work ──────────────────────────────────────────────────────────

    @PostMapping("/works/{id}/delete")
    public String deleteWork(
            @PathVariable Long id,
            @RequestParam(required = false) String returnTo,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        Work work = workService.findById(id);
        Long taskId = work.getTask().getId();
        workService.delete(id, currentUser.getId(), currentUser.isAdmin());
        redirectAttributes.addFlashAttribute("success", "Work deleted.");
        if ("works".equals(returnTo)) return "redirect:/works";
        return "redirect:/?taskId=" + taskId;
    }
}
