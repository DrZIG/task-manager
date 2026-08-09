package com.drzig.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Null means "up to today" — re-evaluated as the current date every time the report is rendered. */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "include_all_users", nullable = false)
    private boolean includeAllUsers = false;

    /**
     * Tasks manually removed from this specific report (e.g. noise/irrelevant for this
     * period's self-review). Scoped to this report only — the task still appears normally
     * in any other report, and reappears here automatically if unexcluded.
     */
    @ElementCollection
    @CollectionTable(name = "report_excluded_tasks", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "task_id")
    private Set<Long> excludedTaskIds = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isIncludeAllUsers() { return includeAllUsers; }
    public void setIncludeAllUsers(boolean includeAllUsers) { this.includeAllUsers = includeAllUsers; }

    public Set<Long> getExcludedTaskIds() { return excludedTaskIds; }
    public void setExcludedTaskIds(Set<Long> excludedTaskIds) { this.excludedTaskIds = excludedTaskIds; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
