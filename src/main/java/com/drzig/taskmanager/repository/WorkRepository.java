package com.drzig.taskmanager.repository;

import com.drzig.taskmanager.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    @Query("SELECT w FROM Work w JOIN FETCH w.task WHERE w.task.id = :taskId ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByTaskIdOrderByDateAndTime(@Param("taskId") Long taskId);

    @Query("SELECT w FROM Work w JOIN FETCH w.task WHERE w.workDate BETWEEN :from AND :to ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT w FROM Work w JOIN FETCH w.task ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findAllWithTask();

    @Query(value = """
            SELECT SUM(
                CASE WHEN finish_time > start_time
                    THEN EXTRACT(EPOCH FROM (finish_time - start_time)) / 60
                    ELSE EXTRACT(EPOCH FROM (finish_time - start_time)) / 60 + 1440
                END
            ) FROM works WHERE work_date = :date
            """,
            nativeQuery = true)
    Long sumMinutesByDate(@Param("date") LocalDate date);

    @Query(value = """
            SELECT work_date, SUM(
                CASE WHEN finish_time > start_time
                    THEN EXTRACT(EPOCH FROM (finish_time - start_time)) / 60
                    ELSE EXTRACT(EPOCH FROM (finish_time - start_time)) / 60 + 1440
                END
            ) FROM works WHERE work_date IN :dates GROUP BY work_date
            """,
            nativeQuery = true)
    List<Object[]> sumMinutesByDates(@Param("dates") List<LocalDate> dates);
}