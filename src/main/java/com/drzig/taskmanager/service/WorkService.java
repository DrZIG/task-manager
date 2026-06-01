package com.drzig.taskmanager.service;

import com.drzig.taskmanager.dto.WorkSummaryDto;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.Work;
import com.drzig.taskmanager.repository.TaskRepository;
import com.drzig.taskmanager.repository.WorkRepository;
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

    public WorkService(WorkRepository workRepository, TaskRepository taskRepository) {
        this.workRepository = workRepository;
        this.taskRepository = taskRepository;
    }

    public List<WorkSummaryDto> findByTaskId(Long taskId) {
        List<Work> works = workRepository.findByTaskIdOrderByDateAndTime(taskId);
        return enrichWithDailyTotals(works);
    }

    public List<WorkSummaryDto> findAll() {
        List<Work> works = workRepository.findAllWithTask();
        return enrichWithDailyTotals(works);
    }

    public List<WorkSummaryDto> findByDateRange(LocalDate from, LocalDate to) {
        List<Work> works = workRepository.findByDateRange(from, to);
        return enrichWithDailyTotals(works);
    }

    private List<WorkSummaryDto> enrichWithDailyTotals(List<Work> works) {
        Set<LocalDate> dates = works.stream()
                .map(Work::getWorkDate)
                .collect(Collectors.toSet());

        Map<LocalDate, Long> dailyTotals = new HashMap<>();
        if (!dates.isEmpty()) {
            List<Object[]> rows = workRepository.sumMinutesByDates(new ArrayList<>(dates));
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

    @Transactional
    public Work save(Work work, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        work.setTask(task);
        return workRepository.save(work);
    }

    public Work findById(Long id) {
        return workRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        workRepository.deleteById(id);
    }
}