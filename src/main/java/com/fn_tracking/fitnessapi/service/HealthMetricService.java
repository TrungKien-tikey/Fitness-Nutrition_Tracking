package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;
    private final UserRepository userRepository;

    public HealthMetric calculateAndSaveMetrics(HealthMetricRequest request) {
        // 1. Kiểm tra User có tồn tại không
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // 2. Tính chỉ số BMI = Cân nặng (kg) / (Chiều cao (m) ^ 2)
        double heightInMeters = request.getHeight() / 100;
        double bmi = request.getWeight() / (heightInMeters * heightInMeters);

        // 3. Tính chỉ số BMR (Công thức Harris-Benedict cải tiến)
        double bmr;
        if ("MALE".equalsIgnoreCase(request.getGender())) {
            bmr = 88.362 + (13.397 * request.getWeight()) + (4.799 * request.getHeight()) - (5.677 * request.getAge());
        } else {
            bmr = 447.593 + (9.247 * request.getWeight()) + (3.098 * request.getHeight()) - (4.330 * request.getAge());
        }

        // 4. Tính TDEE dựa trên mức độ vận động
        double tdeeMultiplier = switch (request.getActivityLevel().toUpperCase()) {
            case "SEDENTARY" -> 1.2;          // Ít vận động
            case "LIGHTLY_ACTIVE" -> 1.375;   // Vận động nhẹ
            case "MODERATELY_ACTIVE" -> 1.55; // Vận động vừa
            case "VERY_ACTIVE" -> 1.725;      // Vận động nhiều
            default -> 1.2;
        };
        double tdee = bmr * tdeeMultiplier;

        // 5. Kiểm tra xem user này đã có bản ghi chỉ số nào chưa, nếu có thì update, chưa có thì tạo mới
        HealthMetric metric = healthMetricRepository.findByUserId(user.getId())
                .orElse(new HealthMetric());

        metric.setUser(user);
        metric.setHeight(request.getHeight());
        metric.setWeight(request.getWeight());
        metric.setBmi(Math.round(bmi * 10.0) / 10.0); // Làm tròn 1 chữ số thập phân
        metric.setBmr(Math.round(bmr * 10.0) / 10.0);
        metric.setTdee(Math.round(tdee * 10.0) / 10.0);

        return healthMetricRepository.save(metric);
    }
}