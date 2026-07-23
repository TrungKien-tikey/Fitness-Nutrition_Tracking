package com.fn_tracking.fitnessapi.repository;
import com.fn_tracking.fitnessapi.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<DailyLog> findByUserIdAndLogDateGreaterThanEqualOrderByLogDateDesc(Long userId, LocalDate fromDate);
}