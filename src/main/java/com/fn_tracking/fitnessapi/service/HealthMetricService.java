package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;

    // Truyền trực tiếp User vào hàm này
    public HealthMetric calculateAndSaveMetrics(HealthMetricRequest request, User user) {
        
        // 2. Tính BMI = Cân nặng (kg) / (Chiều cao (m) ^ 2)
        double heightInMeters = request.getHeight() / 100;
        double bmi = request.getWeight() / (heightInMeters * heightInMeters);

        // 3. Tính BMR (Công thức Harris-Benedict)
        double bmr;
        if ("MALE".equalsIgnoreCase(request.getGender())) {
            bmr = 88.362 + (13.397 * request.getWeight()) + (4.799 * request.getHeight()) - (5.677 * request.getAge());
        } else {
            bmr = 447.593 + (9.247 * request.getWeight()) + (3.098 * request.getHeight()) - (4.330 * request.getAge());
        }

        // 4. Tính TDEE
        double tdeeMultiplier = switch (request.getActivityLevel().toUpperCase()) {
            case "SEDENTARY" -> 1.2;          
            case "LIGHTLY_ACTIVE" -> 1.375;   
            case "MODERATELY_ACTIVE" -> 1.55; 
            case "VERY_ACTIVE" -> 1.725;      
            default -> 1.2;
        };
        double tdee = bmr * tdeeMultiplier;

        // 5. Cập nhật hoặc tạo mới
        HealthMetric metric = healthMetricRepository.findByUserId(user.getId())
                .orElse(new HealthMetric());

        metric.setUser(user);
        metric.setHeight(request.getHeight());
        metric.setWeight(request.getWeight());
        metric.setBmi(Math.round(bmi * 10.0) / 10.0); 
        metric.setBmr(Math.round(bmr * 10.0) / 10.0);
        metric.setTdee(Math.round(tdee * 10.0) / 10.0);

        return healthMetricRepository.save(metric);
    }
}