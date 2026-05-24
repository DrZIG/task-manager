package com.drzig.taskmanager.repository;

import com.drzig.taskmanager.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
