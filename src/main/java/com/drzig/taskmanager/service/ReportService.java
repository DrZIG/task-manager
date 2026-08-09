package com.drzig.taskmanager.service;

import com.drzig.taskmanager.dto.TaskReportGroupDto;
import com.drzig.taskmanager.model.Report;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.Work;
import com.drzig.taskmanager.repository.ReportRepository;
import com.drzig.taskmanager.repository.UserRepository;
import com.drzig.taskmanager.repository.WorkRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final WorkRepository workRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, WorkRepository workRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.workRepository = workRepository;
        this.userRepository = userRepository;
    }

    public List<Report> findForUser(Long currentUserId, boolean isAdmin) {
        return isAdmin
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByOwnerIdOrderByCreatedAtDesc(currentUserId);
    }

    public Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
    }

    public Report findByIdForUser(Long id, Long currentUserId, boolean isAdmin) {
        Report report = findById(id);
        if (!isAdmin && !report.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have access to this report");
        }
        return report;
    }

    @Transactional
    public Report create(String title, LocalDate startDate, LocalDate endDate, boolean includeAllUsers, Long ownerId) {
        validateDates(startDate, endDate);
        Report report = new Report();
        report.setTitle(resolveTitle(title, startDate, endDate));
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setIncludeAllUsers(includeAllUsers);
        report.setOwner(userRepository.getReferenceById(ownerId));
        return reportRepository.save(report);
    }

    @Transactional
    public Report update(Long id, String title, LocalDate startDate, LocalDate endDate, boolean includeAllUsers,
                         Long currentUserId, boolean isAdmin) {
        validateDates(startDate, endDate);
        Report report = findByIdForUser(id, currentUserId, isAdmin);
        report.setTitle(resolveTitle(title, startDate, endDate));
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setIncludeAllUsers(includeAllUsers);
        return reportRepository.save(report);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Report report = findByIdForUser(id, currentUserId, isAdmin);
        reportRepository.delete(report);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Finish date cannot be before start date.");
        }
    }

    private String resolveTitle(String title, LocalDate startDate, LocalDate endDate) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        String endLabel = endDate != null ? endDate.toString() : "today";
        return "Report " + startDate + " – " + endLabel;
    }

    /**
     * Builds the grouped, sorted report content. Not persisted — recomputed
     * live from current work data every time the report is viewed, so it
     * always reflects the latest entries for the saved period.
     */
    public List<TaskReportGroupDto> generateGroups(Report report, Long currentUserId, boolean isAdmin) {
        LocalDate start = report.getStartDate();
        LocalDate end = report.getEndDate() != null ? report.getEndDate() : LocalDate.now();
        boolean scopeAllUsers = isAdmin && report.isIncludeAllUsers();

        List<Work> works = scopeAllUsers
                ? workRepository.findByDateRange(start, end)
                : workRepository.findByDateRangeAndUser(start, end, currentUserId);

        Map<Long, List<Work>> byTask = works.stream()
                .collect(Collectors.groupingBy(w -> w.getTask().getId(), LinkedHashMap::new, Collectors.toList()));

        List<TaskReportGroupDto> groups = new ArrayList<>();
        for (List<Work> taskWorks : byTask.values()) {
            Task task = taskWorks.get(0).getTask();

            LocalDateTime firstDateTime = taskWorks.stream()
                    .map(w -> LocalDateTime.of(w.getWorkDate(), w.getStartTime()))
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDateTime.MAX);

            List<Work> described = taskWorks.stream()
                    .filter(w -> w.getDescription() != null && !w.getDescription().isBlank())
                    .sorted(Comparator.comparing(Work::getWorkDate).thenComparing(Work::getStartTime))
                    .collect(Collectors.toList());

            long totalMinutes = taskWorks.stream().mapToLong(Work::getDurationMinutes).sum();

            groups.add(new TaskReportGroupDto(task, described, firstDateTime, totalMinutes));
        }

        groups.sort(Comparator.comparing(TaskReportGroupDto::getFirstWorkDateTime));
        return groups;
    }
}
