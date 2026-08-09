package com.drzig.taskmanager.dto;

import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.Work;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One task's worth of work entries within a report period, pre-sorted and
 * pre-filtered for display: only entries with a description end up in
 * getDescribedWorks(), but the task itself is always shown as long as it
 * has at least one work entry in the period.
 */
public class TaskReportGroupDto {

    private final Task task;
    private final List<Work> describedWorks;
    private final LocalDateTime firstWorkDateTime;
    private final long totalMinutes;

    public TaskReportGroupDto(Task task, List<Work> describedWorks, LocalDateTime firstWorkDateTime, long totalMinutes) {
        this.task = task;
        this.describedWorks = describedWorks;
        this.firstWorkDateTime = firstWorkDateTime;
        this.totalMinutes = totalMinutes;
    }

    public Task getTask() { return task; }
    public List<Work> getDescribedWorks() { return describedWorks; }
    public LocalDateTime getFirstWorkDateTime() { return firstWorkDateTime; }
    public long getTotalMinutes() { return totalMinutes; }

    public String getFormattedTotal() {
        long hours = totalMinutes / 60;
        long mins = totalMinutes % 60;
        if (hours > 0 && mins > 0) return hours + "h " + mins + "m";
        if (hours > 0) return hours + "h";
        return mins + "m";
    }
}
