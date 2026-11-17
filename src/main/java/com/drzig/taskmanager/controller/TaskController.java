package com.drzig.taskmanager.controller;

import com.drzig.taskmanager.config.CustomUserDetails;
import com.drzig.taskmanager.dto.WorkSummaryDto;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.TaskStatus;
import com.drzig.taskmanager.service.TaskService;
import com.drzig.taskmanager.service.WorkService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TaskController {

    // Session keys for the "Show done" / "Show not active" filter checkboxes.
    private static final String SESSION_SHOW_DONE = "taskFilter.showDone";
    private static final String SESSION_SHOW_INACTIVE = "taskFilter.showInactive";

    private final TaskService taskService;
    private final WorkService workService;

    public TaskController(TaskService taskService, WorkService workService) {
        this.taskService = taskService;
        this.workService = workService;
    }

    // ─── Main page ────────────────────────────────────────────────────────────

    /**
     * The filter flags are remembered in the HTTP session. When the request carries
     * them explicitly (checkbox form submit, or a redirect from an edit/status/work
     * action) they win and are saved; when it carries none (navbar brand, "Back to
     * Tasks" links from Works/Reports pages, etc.) the last saved state is restored
     * instead of silently resetting to "off".
     */
    @GetMapping("/")
    public String index(
            @RequestParam(required = false) Boolean showDone,
            @RequestParam(required = false) Boolean showInactive,
            HttpSession session,
            Model model) {

        boolean done = resolveFilter(showDone, SESSION_SHOW_DONE, session);
        boolean inactive = resolveFilter(showInactive, SESSION_SHOW_INACTIVE, session);

        List<TaskStatus> statuses = new ArrayList<>();
        statuses.add(TaskStatus.ACTIVE);
        if (done) statuses.add(TaskStatus.DONE);
        if (inactive) statuses.add(TaskStatus.INACTIVE);

        model.addAttribute("tasks", taskService.findByStatuses(statuses));
        model.addAttribute("showDone", done);
        model.addAttribute("showInactive", inactive);
        model.addAttribute("newTask", new Task());
        return "index";
    }

    /** Explicit request value wins and is saved; otherwise fall back to the saved session value (default false). */
    private boolean resolveFilter(Boolean requested, String sessionKey, HttpSession session) {
        if (requested != null) {
            session.setAttribute(sessionKey, requested);
            return requested;
        }
        return Boolean.TRUE.equals(session.getAttribute(sessionKey));
    }

    // ─── AJAX: works for a task ───────────────────────────────────────────────

    @GetMapping("/api/tasks/{id}/works")
    @ResponseBody
    public ResponseEntity<?> getWorksForTask(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Task task = taskService.findById(id);
            List<WorkSummaryDto> works = workService.findByTaskId(id, currentUser.getId(), currentUser.isAdmin());
            return ResponseEntity.ok(new TaskWorksResponse(task, works));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── Create task ──────────────────────────────────────────────────────────

    @GetMapping("/tasks/new")
    public String newTaskForm(
            @RequestParam(required = false, defaultValue = "false") boolean showDone,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("pageTitle", "New Task");
        model.addAttribute("showDone", showDone);
        model.addAttribute("showInactive", showInactive);
        model.addAttribute("existingLinks", taskService.findAllGithubIssueLinks());
        return "task-form";
    }

    @PostMapping("/tasks")
    public String createTask(
            @ModelAttribute Task task,
            @RequestParam(required = false) List<String> noteContents,
            @RequestParam(required = false, defaultValue = "false") boolean showDone,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            RedirectAttributes redirectAttributes) {
        Task saved = taskService.save(task, noteContents);
        redirectAttributes.addFlashAttribute("success", "Task created successfully.");
        return "redirect:/?showDone=" + showDone + "&showInactive=" + showInactive + "&taskId=" + saved.getId();
    }

    // ─── Edit task ────────────────────────────────────────────────────────────

    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean showDone,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            Model model) {
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("pageTitle", "Edit Task");
        model.addAttribute("showDone", showDone);
        model.addAttribute("showInactive", showInactive);
        model.addAttribute("existingLinks", taskService.findAllGithubIssueLinks());
        return "task-form";
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(
            @PathVariable Long id,
            @ModelAttribute Task task,
            @RequestParam(required = false) List<String> noteContents,
            @RequestParam(required = false, defaultValue = "false") boolean showDone,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            RedirectAttributes redirectAttributes) {
        task.setId(id);
        taskService.save(task, noteContents);
        redirectAttributes.addFlashAttribute("success", "Task updated successfully.");
        return "redirect:/?showDone=" + showDone + "&showInactive=" + showInactive + "&taskId=" + id;
    }

    // ─── Change task status (quick action from card) ───────────────────────────

    @PostMapping("/tasks/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean showDone,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            RedirectAttributes redirectAttributes) {
        taskService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Task marked as " + status.name().toLowerCase() + ".");
        return "redirect:/?showDone=" + showDone + "&showInactive=" + showInactive + "&taskId=" + id;
    }

    // ─── Delete task ──────────────────────────────────────────────────────────

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean showDone,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            RedirectAttributes redirectAttributes) {
        taskService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Task deleted.");
        return "redirect:/?showDone=" + showDone + "&showInactive=" + showInactive;
    }

    // ─── Inner response class ─────────────────────────────────────────────────

    record TaskWorksResponse(Task task, List<WorkSummaryDto> works) {}
}
