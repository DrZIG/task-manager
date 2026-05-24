package com.drzig.taskmanager.dto;

import com.drzig.taskmanager.model.Work;

/**
 * Wraps a Work with its pre-calculated daily total time (all works on same date).
 */
public class WorkSummaryDto {

    private final Work work;
    private final long dailyTotalMinutes;

    public WorkSummaryDto(Work work, long dailyTotalMinutes) {
        this.work = work;
        this.dailyTotalMinutes = dailyTotalMinutes;
    }

    public Work getWork() {
        return work;
    }

    public String getFormattedDailyTotal() {
        long hours = dailyTotalMinutes / 60;
        long mins = dailyTotalMinutes % 60;
        if (hours > 0 && mins > 0) return hours + "h " + mins + "m";
        if (hours > 0) return hours + "h";
        return mins + "m";
    }
}
