package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.dto.ActivityLevel;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;

    public HealthMetric calculateAndSaveMetrics(HealthMetricRequest request, User user) {
        
        // 1. Kiểm tra (Validate) dữ liệu đầu vào trước khi tính toán
        if (request.getHeight() <= 0 || request.getWeight() <= 0) {
            throw new IllegalArgumentException("Chiều cao và cân nặng phải lớn hơn 0");
        }

        if (request.getGender() == null || 
           (!request.getGender().equalsIgnoreCase("MALE") && !request.getGender().equalsIgnoreCase("FEMALE"))) {
            throw new IllegalArgumentException("Giới tính không hợp lệ. Chỉ chấp nhận MALE hoặc FEMALE");
        }

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
        ActivityLevel activityLevel = request.getActivityLevel();
        double tdeeMultiplier = activityLevel.getMultiplier();
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