package com.drzig.taskmanager.repository;

import com.drzig.taskmanager.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<Report> findAllByOrderByCreatedAtDesc();
}
