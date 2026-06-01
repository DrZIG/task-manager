package com.drzig.taskmanager.repository;

import com.drzig.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.notes ORDER BY t.createdAt DESC")
    List<Task> findAllWithNotes();

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.notes WHERE t.id = :id")
    Optional<Task> findByIdWithNotes(Long id);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.works WHERE t.id = :id")
    Optional<Task> findByIdWithWorks(Long id);
}
