package com.fn_tracking.fitnessapi.repository;
import com.fn_tracking.fitnessapi.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByUserId(Long userId);
    Optional<DietPlan> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}