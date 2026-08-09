package com.drzig.taskmanager.controller;

import com.drzig.taskmanager.config.CustomUserDetails;
import com.drzig.taskmanager.model.Report;
import com.drzig.taskmanager.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("reports", reportService.findForUser(currentUser.getId(), currentUser.isAdmin()));
        model.addAttribute("isAdmin", currentUser.isAdmin());
        return "reports";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("report", new Report());
        model.addAttribute("pageTitle", "New Report");
        return "report-form";
    }

    @PostMapping
    public String create(
            @RequestParam(required = false) String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") boolean includeAllUsers,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        try {
            Report report = reportService.create(title, startDate, endDate, includeAllUsers, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Report created.");
            return "redirect:/reports/" + report.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports/new";
        }
    }

    @GetMapping("/{id}")
    public String view(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {
        Report report = reportService.findByIdForUser(id, currentUser.getId(), currentUser.isAdmin());
        model.addAttribute("report", report);
        model.addAttribute("groups", reportService.generateGroups(report, currentUser.getId(), currentUser.isAdmin()));
        model.addAttribute("effectiveEndDate", report.getEndDate() != null ? report.getEndDate() : LocalDate.now());
        model.addAttribute("isAdmin", currentUser.isAdmin());
        return "report-view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {
        model.addAttribute("report", reportService.findByIdForUser(id, currentUser.getId(), currentUser.isAdmin()));
        model.addAttribute("pageTitle", "Edit Report");
        return "report-form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") boolean includeAllUsers,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        try {
            reportService.update(id, title, startDate, endDate, includeAllUsers, currentUser.getId(), currentUser.isAdmin());
            redirectAttributes.addFlashAttribute("success", "Report updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports/" + id + "/edit";
        }
        return "redirect:/reports/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        reportService.delete(id, currentUser.getId(), currentUser.isAdmin());
        redirectAttributes.addFlashAttribute("success", "Report deleted.");
        return "redirect:/reports";
    }
}
