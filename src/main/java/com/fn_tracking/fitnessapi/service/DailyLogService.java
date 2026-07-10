package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.DailyLogCheckInRequest;
import com.fn_tracking.fitnessapi.entity.DailyLog;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyLogService {

    private final DailyLogRepository dailyLogRepository;

    public DailyLog checkIn(DailyLogCheckInRequest request, User user) {
        LocalDate targetDate = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(user.getId(), targetDate)
                .orElse(DailyLog.builder()
                        .user(user)
                        .logDate(targetDate)
                        .build());

        dailyLog.setCaloriesIn(request.getCaloriesIn());
        dailyLog.setCurrentWeight(request.getCurrentWeight());

        return dailyLogRepository.save(dailyLog);
    }

    public List<DailyLog> getRecentLogs(Long userId, int days) {
        if (days <= 0) {
            throw new RuntimeException("Days must be greater than 0");
        }

        LocalDate fromDate = LocalDate.now().minusDays(days - 1L);
        return dailyLogRepository.findByUserIdAndLogDateGreaterThanEqualOrderByLogDateDesc(userId, fromDate);
    }
}