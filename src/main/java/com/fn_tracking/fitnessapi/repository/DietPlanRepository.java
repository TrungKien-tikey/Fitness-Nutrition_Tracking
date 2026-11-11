package com.fn_tracking.fitnessapi.repository;

import com.fn_tracking.fitnessapi.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByUserId(Long userId);
    Optional<DietPlan> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    long countByCreatedAtAfter(LocalDateTime time);
}
