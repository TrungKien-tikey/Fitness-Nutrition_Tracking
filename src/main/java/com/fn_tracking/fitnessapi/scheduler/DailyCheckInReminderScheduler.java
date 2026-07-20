package com.fn_tracking.fitnessapi.scheduler;

import com.fn_tracking.fitnessapi.entity.DailyLog;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.ReminderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyCheckInReminderScheduler {

    private final UserRepository userRepository;
    private final DailyLogRepository dailyLogRepository;
    private final ReminderNotificationService reminderNotificationService;
    private final Clock clock;

    @Scheduled(cron = "${app.reminder-cron:0 0 20 * * *}", zone = "${app.reminder-zone:Asia/Ho_Chi_Minh}")
    public void sendMissingCheckInReminders() {
        LocalDate today = LocalDate.now(clock);
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            log.info("No users found for reminder scan on {}", today);
            return;
        }

        Set<Long> checkedInUserIds = new HashSet<>();
        for (DailyLog logEntry : dailyLogRepository.findByLogDate(today)) {
            if (logEntry.getUser() != null && logEntry.getUser().getId() != null) {
                checkedInUserIds.add(logEntry.getUser().getId());
            }
        }

        for (User user : users) {
            if (user.getId() == null) {
                continue;
            }

            if (checkedInUserIds.contains(user.getId())) {
                continue;
            }

            try {
                reminderNotificationService.sendMissingCheckInReminder(user, today);
            } catch (Exception ex) {
                log.error("Failed to send reminder to userId={} for date={}", user.getId(), today, ex);
            }
        }
    }
}