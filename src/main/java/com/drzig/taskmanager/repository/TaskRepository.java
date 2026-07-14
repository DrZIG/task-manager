package com.drzig.taskmanager.repository;

import com.drzig.taskmanager.model.Task;
import com.drzig.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.notes ORDER BY t.createdAt DESC")
    List<Task> findAllWithNotes();

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.notes WHERE t.status IN :statuses ORDER BY t.createdAt DESC")
    List<Task> findAllWithNotesByStatusIn(@Param("statuses") List<TaskStatus> statuses);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.notes WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(Long id);
}
