package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.entity.User;

import java.time.LocalDate;

public interface ReminderNotificationService {
    void sendMissingCheckInReminder(User user, LocalDate date);
}