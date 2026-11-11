package com.fn_tracking.fitnessapi.repository;

import com.fn_tracking.fitnessapi.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<DailyLog> findByLogDate(LocalDate logDate);
    List<DailyLog> findByUserIdAndLogDateGreaterThanEqualOrderByLogDateDesc(Long userId, LocalDate fromDate);

    long countByLogDateGreaterThanEqual(LocalDate fromDate);

    @Query("select count(distinct d.user.id) from DailyLog d where d.logDate >= :fromDate")
    long countDistinctUsersSince(@Param("fromDate") LocalDate fromDate);

    @Query("select count(distinct d.user.id) from DailyLog d where d.logDate = :logDate")
    long countDistinctUsersByLogDate(@Param("logDate") LocalDate logDate);
}
