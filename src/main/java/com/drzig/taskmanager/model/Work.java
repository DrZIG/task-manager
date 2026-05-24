package com.drzig.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

@Entity
@Table(name = "works")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Work date is required")
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "Finish time is required")
    @Column(name = "finish_time", nullable = false)
    private LocalTime finishTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalTime finishTime) {
        this.finishTime = finishTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Returns the duration of this work entry in minutes.
     */
    @Transient
    public long getDurationMinutes() {
        if (startTime != null && finishTime != null) {
            return Duration.between(startTime, finishTime).toMinutes();
        }
        return 0;
    }

    /**
     * Returns a formatted duration string like "2h 30m".
     */
    @Transient
    public String getFormattedDuration() {
        long minutes = getDurationMinutes();
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0 && mins > 0) return hours + "h " + mins + "m";
        if (hours > 0) return hours + "h";
        return mins + "m";
    }
}
