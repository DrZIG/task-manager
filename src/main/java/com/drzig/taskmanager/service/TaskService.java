package com.drzig.taskmanager.service;

import com.drzig.taskmanager.model.Note;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.TaskStatus;
import com.drzig.taskmanager.repository.NoteRepository;
import com.drzig.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;

    public TaskService(TaskRepository taskRepository, NoteRepository noteRepository) {
        this.taskRepository = taskRepository;
        this.noteRepository = noteRepository;
    }

    /** Unfiltered — used where every task must be selectable regardless of status (e.g. work form dropdown). */
    public List<Task> findAll() {
        return taskRepository.findAllWithNotes();
    }

    /** Filtered — used by the main task list page, respecting the show-done / show-inactive toggles. */
    public List<Task> findByStatuses(List<TaskStatus> statuses) {
        return taskRepository.findAllWithNotesByStatusIn(statuses);
    }

    public Task findById(Long id) {
        return taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    /**
     * Map of taskId -> githubIssueLink for every task that has a link set.
     * Used by the task form to flag duplicate GitHub issue links client-side.
     */
    public Map<Long, String> findAllGithubIssueLinks() {
        Map<Long, String> map = new HashMap<>();
        for (Object[] row : taskRepository.findAllGithubIssueLinks()) {
            map.put((Long) row[0], (String) row[1]);
        }
        return map;
    }

    @Transactional
    public Task save(Task task, List<String> noteContents) {
        Task target;
        if (task.getId() != null) {
            target = taskRepository.findById(task.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Task not found: " + task.getId()));
            target.setTitle(task.getTitle());
            target.setDescription(task.getDescription());
            target.setGithubIssueLink(task.getGithubIssueLink());
            target.setStatus(task.getStatus());
        } else {
            target = task;
        }

        // Clear and re-add notes so orphanRemoval handles deleted ones
        target.getNotes().clear();
        if (noteContents != null) {
            for (String content : noteContents) {
                if (content != null && !content.isBlank()) {
                    Note note = new Note(content.trim(), target);
                    target.getNotes().add(note);
                }
            }
        }
        return taskRepository.save(target);
    }

    @Transactional
    public void updateStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        task.setStatus(status);
        taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}