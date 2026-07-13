package com.drzig.taskmanager.repository;

import com.drzig.taskmanager.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    // ─── Scoped to a single user ───────────────────────────────────────────

    @Query("SELECT w FROM Work w JOIN FETCH w.task JOIN FETCH w.user " +
            "WHERE w.task.id = :taskId AND w.user.id = :userId " +
            "ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByTaskIdAndUserOrderByDateAndTime(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Query("SELECT w FROM Work w JOIN FETCH w.task JOIN FETCH w.user " +
            "WHERE w.user.id = :userId " +
            "ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByUserOrderByDateAndTime(@Param("userId") Long userId);

    @Query("SELECT w FROM Work w JOIN FETCH w.task JOIN FETCH w.user " +
            "WHERE w.workDate BETWEEN :from AND :to AND w.user.id = :userId " +
            "ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByDateRangeAndUser(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("userId") Long userId);

    @Query(value = "SELECT work_date, SUM(EXTRACT(EPOCH FROM (finish_time - start_time)) / 60) " +
            "FROM works WHERE work_date IN :dates AND user_id = :userId GROUP BY work_date",
            nativeQuery = true)
    List<Object[]> sumMinutesByDatesAndUser(@Param("dates") List<LocalDate> dates, @Param("userId") Long userId);

    // ─── Admin: all users ───────────────────────────────────────────────────

    @Query("SELECT w FROM Work w JOIN FETCH w.task JOIN FETCH w.user " +
            "WHERE w.task.id = :taskId " +
            "ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByTaskIdOrderByDateAndTime(@Param("taskId") Long taskId);

    @Query("SELECT w FROM Work w JOIN FETCH w.task JOIN FETCH w.user " +
            "WHERE w.workDate BETWEEN :from AND :to " +
            "ORDER BY w.workDate DESC, w.startTime ASC")
    List<Work> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT w FROM Work w JOIN FETCH w.task JOIN FETCH w.user " +
            "ORDER BY w.workDate DESC, w.startTime ASC")
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