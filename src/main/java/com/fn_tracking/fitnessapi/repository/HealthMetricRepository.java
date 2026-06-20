package com.fn_tracking.fitnessapi.repository;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    Optional<HealthMetric> findByUserId(Long userId);
}