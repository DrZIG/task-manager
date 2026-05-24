package com.drzig.taskmanager.controller;

import com.drzig.taskmanager.dto.WorkSummaryDto;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.service.TaskService;
import com.drzig.taskmanager.service.WorkService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final WorkService workService;

    public TaskController(TaskService taskService, WorkService workService) {
        this.taskService = taskService;
        this.workService = workService;
    }

    // ─── Main page ────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", taskService.findAll());
        model.addAttribute("newTask", new Task());
        return "index";
    }

    // ─── AJAX: works for a task ───────────────────────────────────────────────

    @GetMapping("/api/tasks/{id}/works")
    @ResponseBody
    public ResponseEntity<?> getWorksForTask(@PathVariable Long id) {
        try {
            Task task = taskService.findById(id);
            List<WorkSummaryDto> works = workService.findByTaskId(id);
            return ResponseEntity.ok(new TaskWorksResponse(task, works));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── Create task ──────────────────────────────────────────────────────────

    @GetMapping("/tasks/new")
    public String newTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("pageTitle", "New Task");
        return "task-form";
    }

    @PostMapping("/tasks")
    public String createTask(
            @ModelAttribute Task task,
            @RequestParam(required = false) List<String> notes,
            RedirectAttributes redirectAttributes) {
        taskService.save(task, notes);
        redirectAttributes.addFlashAttribute("success", "Task created successfully.");
        return "redirect:/";
    }

    // ─── Edit task ────────────────────────────────────────────────────────────

    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("pageTitle", "Edit Task");
        return "task-form";
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(
            @PathVariable Long id,
            @ModelAttribute Task task,
            @RequestParam(required = false) List<String> notes,
            RedirectAttributes redirectAttributes) {
        task.setId(id);
        taskService.save(task, notes);
        redirectAttributes.addFlashAttribute("success", "Task updated successfully.");
        return "redirect:/";
    }

    // ─── Delete task ──────────────────────────────────────────────────────────

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Task deleted.");
        return "redirect:/";
    }

    // ─── Inner response class ─────────────────────────────────────────────────

    record TaskWorksResponse(Task task, List<WorkSummaryDto> works) {}
}
