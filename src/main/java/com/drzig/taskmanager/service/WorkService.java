package com.drzig.taskmanager.service;

import com.drzig.taskmanager.dto.WorkSummaryDto;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.User;
import com.drzig.taskmanager.model.Work;
import com.drzig.taskmanager.repository.TaskRepository;
import com.drzig.taskmanager.repository.UserRepository;
import com.drzig.taskmanager.repository.WorkRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkService {

    private final WorkRepository workRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public WorkService(WorkRepository workRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.workRepository = workRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<WorkSummaryDto> findByTaskId(Long taskId, Long currentUserId, boolean isAdmin) {
        List<Work> works = isAdmin
                ? workRepository.findByTaskIdOrderByDateAndTime(taskId)
                : workRepository.findByTaskIdAndUserOrderByDateAndTime(taskId, currentUserId);
        return enrichWithDailyTotals(works, currentUserId, isAdmin);
    }

    public List<WorkSummaryDto> findAll(Long currentUserId, boolean isAdmin) {
        List<Work> works = isAdmin
                ? workRepository.findAllWithTask()
                : workRepository.findByUserOrderByDateAndTime(currentUserId);
        return enrichWithDailyTotals(works, currentUserId, isAdmin);
    }

    public List<WorkSummaryDto> findByDateRange(LocalDate from, LocalDate to, Long currentUserId, boolean isAdmin) {
        List<Work> works = isAdmin
                ? workRepository.findByDateRange(from, to)
                : workRepository.findByDateRangeAndUser(from, to, currentUserId);
        return enrichWithDailyTotals(works, currentUserId, isAdmin);
    }

    /**
     * For each work, calculates the total minutes logged on its workDate.
     * For a regular user this is scoped to their own works only.
     * For an admin it's the total across all users.
     */
    private List<WorkSummaryDto> enrichWithDailyTotals(List<Work> works, Long currentUserId, boolean isAdmin) {
        Set<LocalDate> dates = works.stream()
                .map(Work::getWorkDate)
                .collect(Collectors.toSet());

        Map<LocalDate, Long> dailyTotals = new HashMap<>();
        if (!dates.isEmpty()) {
            List<Object[]> rows = isAdmin
                    ? workRepository.sumMinutesByDates(new ArrayList<>(dates))
                    : workRepository.sumMinutesByDatesAndUser(new ArrayList<>(dates), currentUserId);
            for (Object[] row : rows) {
                // Native query returns java.sql.Date, not LocalDate — convert explicitly
                LocalDate date = ((Date) row[0]).toLocalDate();
                Long minutes = row[1] == null ? 0L : ((Number) row[1]).longValue();
                dailyTotals.put(date, minutes);
            }
        }

        return works.stream()
                .map(w -> new WorkSummaryDto(w, dailyTotals.getOrDefault(w.getWorkDate(), 0L)))
                .collect(Collectors.toList());
    }

    /** Fetches a work entry, enforcing that only the owner or an admin can access it. */
    public Work findByIdForUser(Long id, Long currentUserId, boolean isAdmin) {
        Work work = findById(id);
        if (!isAdmin && !work.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have access to this work entry");
        }
        return work;
    }

    public Work findById(Long id) {
        return workRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work not found: " + id));
    }

    // ─── Writes ─────────────────────────────────────────────────────────────

    @Transactional
    public Work createWork(Work work, Long taskId, Long ownerUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        User owner = userRepository.getReferenceById(ownerUserId);
        work.setTask(task);
        work.setUser(owner);
        return workRepository.save(work);
    }

    @Transactional
    public Work updateWork(Long id, Work updatedFields, Long taskId, Long currentUserId, boolean isAdmin) {
        Work existing = findById(id);
        if (!isAdmin && !existing.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have access to this work entry");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        existing.setTask(task);
        existing.setWorkDate(updatedFields.getWorkDate());
        existing.setStartTime(updatedFields.getStartTime());
        existing.setFinishTime(updatedFields.getFinishTime());
        existing.setDescription(updatedFields.getDescription());
        // Owner is intentionally left unchanged.

        return workRepository.save(existing);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Work work = findById(id);
        if (!isAdmin && !work.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have access to this work entry");
        }
        workRepository.deleteById(id);
    }
}