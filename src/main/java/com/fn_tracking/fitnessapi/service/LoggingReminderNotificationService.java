package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class LoggingReminderNotificationService implements ReminderNotificationService {

    @Override
    public void sendMissingCheckInReminder(User user, LocalDate date) {
        log.info("Reminder sent to user={} for missing daily check-in on {}", user.getUsername(), date);
    }
}