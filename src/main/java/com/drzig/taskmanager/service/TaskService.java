package com.drzig.taskmanager.service;

import com.drzig.taskmanager.model.Note;
import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.TaskStatus;
import com.drzig.taskmanager.repository.NoteRepository;
import com.drzig.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public Task save(Task task, List<String> noteContents) {
        // Clear and re-add notes so orphanRemoval handles deleted ones
        task.getNotes().clear();
        if (noteContents != null) {
            for (String content : noteContents) {
                if (content != null && !content.isBlank()) {
                    Note note = new Note(content.trim(), task);
                    task.getNotes().add(note);
                }
            }
        }
        return taskRepository.save(task);
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
