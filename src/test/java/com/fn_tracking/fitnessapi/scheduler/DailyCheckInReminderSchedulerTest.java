package com.fn_tracking.fitnessapi.scheduler;

import com.fn_tracking.fitnessapi.entity.DailyLog;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.ReminderNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyCheckInReminderSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DailyLogRepository dailyLogRepository;

    @Mock
    private ReminderNotificationService reminderNotificationService;

    private DailyCheckInReminderScheduler scheduler;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-07-24T12:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        scheduler = new DailyCheckInReminderScheduler(userRepository, dailyLogRepository, reminderNotificationService, fixedClock);
    }

    @Test
    void sendMissingCheckInRemindersShouldDoNothingWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        scheduler.sendMissingCheckInReminders();

        verify(userRepository).findAll();
        verifyNoInteractions(dailyLogRepository);
        verifyNoInteractions(reminderNotificationService);
    }

    @Test
    void sendMissingCheckInRemindersShouldSkipUsersWhoAlreadyCheckedIn() {
        User user = User.builder().id(1L).username("john").email("john@example.com").build();
        DailyLog log = DailyLog.builder().id(10L).user(user).logDate(LocalDate.of(2026, 7, 24)).build();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(dailyLogRepository.findByLogDate(LocalDate.of(2026, 7, 24))).thenReturn(List.of(log));

        scheduler.sendMissingCheckInReminders();

        verify(reminderNotificationService, never()).sendMissingCheckInReminder(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendMissingCheckInRemindersShouldNotifyOnlyUsersMissingTodayLog() {
        User checkedInUser = User.builder().id(1L).username("john").email("john@example.com").build();
        User missingUser = User.builder().id(2L).username("anna").email("anna@example.com").build();
        DailyLog log = DailyLog.builder().id(10L).user(checkedInUser).logDate(LocalDate.of(2026, 7, 24)).build();

        when(userRepository.findAll()).thenReturn(List.of(checkedInUser, missingUser));
        when(dailyLogRepository.findByLogDate(LocalDate.of(2026, 7, 24))).thenReturn(List.of(log));

        scheduler.sendMissingCheckInReminders();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reminderNotificationService).sendMissingCheckInReminder(userCaptor.capture(), dateCaptor.capture());
        assertEquals(2L, userCaptor.getValue().getId());
        assertEquals(LocalDate.of(2026, 7, 24), dateCaptor.getValue());
    }

    @Test
    void sendMissingCheckInRemindersShouldContinueWhenNotificationFailsForOneUser() {
        User firstMissingUser = User.builder().id(1L).username("john").email("john@example.com").build();
        User secondMissingUser = User.builder().id(2L).username("anna").email("anna@example.com").build();

        when(userRepository.findAll()).thenReturn(List.of(firstMissingUser, secondMissingUser));
        when(dailyLogRepository.findByLogDate(LocalDate.of(2026, 7, 24))).thenReturn(List.of());
        doThrow(new RuntimeException("send failed"))
                .when(reminderNotificationService)
                .sendMissingCheckInReminder(firstMissingUser, LocalDate.of(2026, 7, 24));

        scheduler.sendMissingCheckInReminders();

        verify(reminderNotificationService).sendMissingCheckInReminder(firstMissingUser, LocalDate.of(2026, 7, 24));
        verify(reminderNotificationService).sendMissingCheckInReminder(secondMissingUser, LocalDate.of(2026, 7, 24));
    }
}